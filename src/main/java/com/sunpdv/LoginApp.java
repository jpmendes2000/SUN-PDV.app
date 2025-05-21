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

import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeMOD;
import com.sunpdv.home.TelaHomeFUN;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginApp extends Application {

    private static final String AES_KEY = "MinhaChaveSuperSegura1234567890!";
    private int tentativas = 0;
    private long tempoBloqueio = 0;
    private static final int MAX_TENTATIVAS = 6;
    private static final int TEMPO_ESPERA = 120;
    private Timeline contagemRegressiva;

    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "database=SUN_PDVlocal;"
            + "user=sa;"
            + "password=Jp081007!;"
            + "encrypt=false;"
            + "trustServerCertificate=true;"
            + "loginTimeout=30;";

    public static void main(String[] args) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection conn = DriverManager.getConnection(URL)) {
                System.out.println("Conexão com banco local OK!");
            }
        } catch (Exception e) {
            System.err.println("Erro ao conectar no banco:");
            e.printStackTrace();
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        TextField emailField = new TextField();
        emailField.setPromptText("E-mail");

        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Senha");

        TextField senhaVisivelField = new TextField();
        senhaVisivelField.setPromptText("Senha");
        senhaVisivelField.setManaged(false);
        senhaVisivelField.setVisible(false);

        senhaVisivelField.textProperty().bindBidirectional(senhaField.textProperty());

        ToggleButton olhoBtn = new ToggleButton("👁");
        olhoBtn.getStyleClass().add("olho-btn");
        olhoBtn.setOnAction(e -> {
            boolean mostrar = olhoBtn.isSelected();
            senhaField.setVisible(!mostrar);
            senhaField.setManaged(!mostrar);
            senhaVisivelField.setVisible(mostrar);
            senhaVisivelField.setManaged(mostrar);
        });

        StackPane senhaStack = new StackPane();
        senhaStack.setAlignment(Pos.CENTER_RIGHT);
        senhaStack.getChildren().addAll(senhaField, senhaVisivelField, olhoBtn);

        senhaField.prefWidthProperty().bind(emailField.widthProperty());
        senhaVisivelField.prefWidthProperty().bind(emailField.widthProperty());

        VBox senhaLinha = new VBox(senhaStack);
        VBox emailLinha = new VBox(emailField);
        emailLinha.setAlignment(Pos.CENTER_LEFT);
        senhaLinha.setAlignment(Pos.CENTER_LEFT);

        Button loginBtn = new Button("Entrar");
        loginBtn.setDisable(true);

        Label statusLabel = new Label();

        Runnable verificarCampos = () -> {
            boolean preenchido = !emailField.getText().trim().isEmpty() &&
                    !(senhaField.isVisible() ? senhaField.getText() : senhaVisivelField.getText()).trim().isEmpty();
            loginBtn.setDisable(!preenchido);
        };

        emailField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        senhaField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        senhaVisivelField.textProperty().addListener((obs, o, n) -> verificarCampos.run());

        Image logo = new Image(getClass().getResourceAsStream("/img/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(100);
        logoView.setPreserveRatio(true);

        VBox root = new VBox(15, logoView, emailLinha, senhaLinha, loginBtn, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 680, 380);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

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
                iniciarContagem(statusLabel);
                return;
            }

            loginBtn.setDisable(true);
            statusLabel.setText("Verificando...");

            Task<String> loginTask = new Task<>() {
                @Override
                protected String call() {
                    try {
                        return autenticarUsuario(email, senha);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return "Erro: " + ex.getMessage();
                    }
                }
            };

            loginTask.setOnSucceeded(event -> {
                String resultado = loginTask.getValue();
                if ("sucesso".equalsIgnoreCase(resultado)) {
                    tentativas = 0;

                    try {
                        switch (AutenticarUser.getCargo()) {
                            case "Administrador":
                                new TelaHomeADM(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                                break;
                            case "Moderador":
                                new TelaHomeMOD(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                                break;
                            case "Funcionario":
                                new TelaHomeFUN(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                                break;
                            default:
                                statusLabel.setText("Cargo não reconhecido: " + AutenticarUser.getCargo());
                                loginBtn.setDisable(false);
                                return;
                        }
                        
                        // Fecha a janela de login
                        ((Stage) loginBtn.getScene().getWindow()).close();
                    } catch (Exception ex) {
                        statusLabel.setText("Erro ao abrir a tela principal");
                        loginBtn.setDisable(false);
                        ex.printStackTrace();
                    }
                } else {
                    tentativas++;
                    if (tentativas >= MAX_TENTATIVAS) {
                        tempoBloqueio = System.currentTimeMillis() + (TEMPO_ESPERA * 1000);
                        iniciarContagem(statusLabel);
                    } else {
                        statusLabel.setText(resultado);
                        loginBtn.setDisable(false);
                    }
                }
            });

            loginTask.setOnFailed(event -> {
                statusLabel.setText("Erro de login.");
                loginBtn.setDisable(false);
            });

            new Thread(loginTask).start();
        });

        stage.setScene(scene);
        stage.setTitle("Login - SUN PDV");
        stage.setResizable(false);
        stage.show();
    }

    private void iniciarContagem(Label statusLabel) {
        if (contagemRegressiva != null) contagemRegressiva.stop();

        contagemRegressiva = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            long restante = (tempoBloqueio - System.currentTimeMillis()) / 1000;
            if (restante > 0) {
                statusLabel.setText("Muitas tentativas. Aguarde " + restante + " segundos.");
            } else {
                contagemRegressiva.stop();
                statusLabel.setText("");
            }
        }));
        contagemRegressiva.setCycleCount(Timeline.INDEFINITE);
        contagemRegressiva.play();
    }

    private String autenticarUsuario(String email, String senha) throws Exception {
    String emailCriptografado = criptografarAES(email);
    String senhaHash = hashSHA256(senha);

    try (Connection conn = DriverManager.getConnection(URL)) {
       String sql = "SELECT l.Nome, c.Cargo, l.ID_Permissao " +
             "FROM login_sistema l " +
             "INNER JOIN Cargo c ON l.ID_Cargo = c.ID_Cargo " +
             "WHERE l.Email = ? AND l.Senha = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailCriptografado);
            stmt.setString(2, senhaHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("Nome");
                    String cargo = rs.getString("Cargo");
                    int idPermissao = rs.getInt("ID_Permissao");

                    AutenticarUser.setNome(nome);
                    AutenticarUser.setCargo(cargo);
                    AutenticarUser.setIdPermissao(idPermissao);

                    return "sucesso";
                } else {
                    return "E-mail ou senha inválidos.";
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "Erro ao autenticar: " + e.getMessage();
    }
}


    private String criptografarAES(String texto) throws Exception {
    SecretKeySpec chave = new SecretKeySpec(AES_KEY.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.ENCRYPT_MODE, chave);
    byte[] textoCriptografado = cipher.doFinal(texto.getBytes("UTF-8"));
    return Base64.getEncoder().encodeToString(textoCriptografado);
}

private String hashSHA256(String texto) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(texto.getBytes());
    StringBuilder sb = new StringBuilder();
    for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
    }
    return sb.toString();
}
}