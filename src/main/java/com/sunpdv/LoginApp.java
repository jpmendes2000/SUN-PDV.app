package com.sunpdv;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginApp extends Application {

    private static final String AES_KEY = "MinhaChaveSuperSegura1234567890!";
    private int tentativas = 0;
    private long tempoBloqueio = 0;
    private static final int MAX_TENTATIVAS = 6;
    private static final int TEMPO_ESPERA = 120;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Label emailLabel = new Label("E-mail:");
        TextField emailField = new TextField();
        emailField.setPrefWidth(280);

        Label senhaLabel = new Label("Senha:");
        PasswordField senhaField = new PasswordField();
        senhaField.setPrefWidth(280);

        TextField senhaVisivelField = new TextField();
        senhaVisivelField.setPrefWidth(280);
        senhaVisivelField.setManaged(false);
        senhaVisivelField.setVisible(false);
        senhaVisivelField.textProperty().bindBidirectional(senhaField.textProperty());

        ToggleButton mostrarSenhaBtn = new ToggleButton("👁");
        mostrarSenhaBtn.getStyleClass().add("visible");
        mostrarSenhaBtn.setOnAction(e -> {
            boolean mostrar = mostrarSenhaBtn.isSelected();
            senhaField.setVisible(!mostrar);
            senhaField.setManaged(!mostrar);
            senhaVisivelField.setVisible(mostrar);
            senhaVisivelField.setManaged(mostrar);
        });

        HBox senhaBox = new HBox(10, senhaField, senhaVisivelField, mostrarSenhaBtn);
        senhaBox.setAlignment(Pos.CENTER_LEFT);

        VBox senhaLinha = new VBox(5, senhaLabel, senhaBox);
        VBox emailLinha = new VBox(5, emailLabel, emailField);

        Button loginBtn = new Button("Entrar");
        Label statusLabel = new Label();

        // Carrega a imagem da logo
        Image logoImage = new Image(getClass().getResourceAsStream("/img/logo.png"));
        ImageView logoView = new ImageView(logoImage);
        logoView.setPreserveRatio(true);
        logoView.setFitWidth(180);

        VBox root = new VBox(15, logoView, emailLinha, senhaLinha, loginBtn, statusLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 500, 320);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        loginBtn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), loginBtn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        loginBtn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), loginBtn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String senha = senhaField.isVisible() ? senhaField.getText() : senhaVisivelField.getText();

            if (System.currentTimeMillis() < tempoBloqueio) {
                long restante = (tempoBloqueio - System.currentTimeMillis()) / 1000;
                statusLabel.setText("Muitas tentativas. Tente novamente em " + restante + " segundos.");
                return;
            }

            if (email.isEmpty() || senha.isEmpty()) {
                statusLabel.setText("Por favor, preencha todos os campos.");
                return;
            }

            loginBtn.setDisable(true);
            statusLabel.setText("Verificando...");

            Task<String> task = new Task<>() {
                @Override
                protected String call() {
                    try {
                        return autenticarUsuario(email, senha);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return "Erro inesperado: " + ex.getMessage();
                    }
                }
            };

            task.setOnSucceeded(event -> {
                String resultado = task.getValue();
                if (resultado.startsWith("Bem-vindo")) {
                    tentativas = 0;
                } else {
                    tentativas++;
                    if (tentativas >= MAX_TENTATIVAS) {
                        tempoBloqueio = System.currentTimeMillis() + (TEMPO_ESPERA * 1000);
                        resultado = "Muitas tentativas. Tente novamente em 2 minutos.";
                    }
                }
                statusLabel.setText(resultado);
                loginBtn.setDisable(false);
            });

            task.setOnFailed(event -> {
                statusLabel.setText("Erro ao processar login.");
                loginBtn.setDisable(false);
            });

            new Thread(task).start();
        });

        primaryStage.setTitle("Login - SUN PDV");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private String autenticarUsuario(String email, String senha) throws Exception {
        String url = "jdbc:sqlserver://serverpdv.database.windows.net:1433;"
                   + "database=SUN_PDVcloud;"
                   + "user=adminuser@serverpdv;"
                   + "password=Jp081007!;"
                   + "encrypt=true;"
                   + "trustServerCertificate=false;"
                   + "hostNameInCertificate=*.database.windows.net;"
                   + "loginTimeout=30;";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        String emailCriptografado = criptografarAES(email);
        String senhaCriptografada = hashSHA256(senha);

        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "SELECT l.Nome, c.Cargo, l.ID_Permissao FROM login_sistema l " +
                         "LEFT JOIN cargo c ON l.ID_Cargo = c.ID_Cargo " +
                         "WHERE l.Email = ? AND l.Senha = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emailCriptografado);
            stmt.setString(2, senhaCriptografada);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int permissao = rs.getInt("ID_Permissao");
                if (permissao == 2) {
                    return "Acesso negado. Permissão bloqueada.";
                }
                String nome = rs.getString("Nome");
                String cargo = rs.getString("Cargo");
                return "Bem-vindo, " + nome + " (" + cargo + ")";
            } else {
                return "E-mail ou senha incorretos.";
            }
        }
    }

    private String criptografarAES(String texto) throws Exception {
        SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] textoCriptografado = cipher.doFinal(texto.getBytes());
        return Base64.getEncoder().encodeToString(textoCriptografado);
    }

    private String hashSHA256(String senha) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(senha.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash)
            hexString.append(String.format("%02x", b));
        return hexString.toString();
    }
}