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
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginApp extends Application {

    private static final String AES_KEY = "MinhaChaveSuperSegura1234567890!";
    private int tentativas = 0;
    private long tempoBloqueio = 0;
    private static final int MAX_TENTATIVAS = 6;
    private static final int TEMPO_ESPERA = 120;
    private Timeline contagemRegressiva;

    public static String url = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "Senha@12345!";

    public static void main(String[] args) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {
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

        // Criar SVG para o ícone de entrar
        SVGPath entrarIcon = new SVGPath();
        entrarIcon.setContent("M217.9 105.9L340.7 228.7c7.2 7.2 11.3 17.1 11.3 27.3s-4.1 20.1-11.3 27.3L217.9 406.1c-6.4 6.4-15 9.9-24 9.9c-18.7 0-33.9-15.2-33.9-33.9l0-62.1L32 320c-17.7 0-32-14.3-32-32l0-64c0-17.7 14.3-32 32-32l128 0 0-62.1c0-18.7 15.2-33.9 33.9-33.9c9 0 17.6 3.6 24 9.9zM352 416l64 0c17.7 0 32-14.3 32-32l0-256c0-17.7-14.3-32-32-32l-64 0c-17.7 0-32-14.3-32-32s14.3-32 32-32l64 0c53 0 96 43 96 96l0 256c0 53-43 96-96 96l-64 0c-17.7 0-32-14.3-32-32s14.3-32 32-32z");
        entrarIcon.setFill(Color.WHITE);
        entrarIcon.setScaleX(0.07);
        entrarIcon.setScaleY(0.07);

        // Configurar botão de olho com SVG
        ToggleButton olhoBtn = new ToggleButton();
        olhoBtn.getStyleClass().add("olho-btn");
        olhoBtn.setStyle("-fx-background-color: transparent; -fx-padding: 5;");

        // Carregar os SVGs dos olhos
        SVGPath olhoFechadoPath = new SVGPath();
        olhoFechadoPath.setContent("M38.8 5.1C28.4-3.1 13.3-1.2 5.1 9.2S-1.2 34.7 9.2 42.9l592 464c10.4 8.2 25.5 6.3 33.7-4.1s6.3-25.5-4.1-33.7L525.6 386.7c39.6-40.6 66.4-86.1 79.9-118.4c3.3-7.9 3.3-16.7 0-24.6c-14.9-35.7-46.2-87.7-93-131.1C465.5 68.8 400.8 32 320 32c-68.2 0-125 26.3-169.3 60.8L38.8 5.1zM223.1 149.5C248.6 126.2 282.7 112 320 112c79.5 0 144 64.5 144 144c0 24.9-6.3 48.3-17.4 68.7L408 294.5c8.4-19.3 10.6-41.4 4.8-63.3c-11.1-41.5-47.8-69.4-88.6-71.1c-5.8-.2-9.2 6.1-7.4 11.7c2.1 6.4 3.3 13.2 3.3 20.3c0 10.2-2.4 19.8-6.6 28.3l-90.3-70.8zM373 389.9c-16.4 6.5-34.3 10.1-53 10.1c-79.5 0-144-64.5-144-144c0-6.9 .5-13.6 1.4-20.2L83.1 161.5C60.3 191.2 44 220.8 34.5 243.7c-3.3 7.9-3.3 16.7 0 24.6c14.9 35.7 46.2 87.7 93 131.1C174.5 443.2 239.2 480 320 480c47.8 0 89.9-12.9 126.2-32.5L373 389.9z");
        olhoFechadoPath.setFill(Color.BLACK);
        
        SVGPath olhoAbertoPath = new SVGPath();
        olhoAbertoPath.setContent("M288 32c-80.8 0-145.5 36.8-192.6 80.6C48.6 156 17.3 208 2.5 243.7c-3.3 7.9-3.3 16.7 0 24.6C17.3 304 48.6 356 95.4 399.4C142.5 443.2 207.2 480 288 480s145.5-36.8 192.6-80.6c46.8-43.5 78.1-95.4 93-131.1c3.3-7.9 3.3-16.7 0-24.6c-14.9-35.7-46.2-87.7-93-131.1C433.5 68.8 368.8 32 288 32zM144 256a144 144 0 1 1 288 0 144 144 0 1 1 -288 0zm144-64c0 35.3-28.7 64-64 64c-7.1 0-13.9-1.2-20.3-3.3c-5.5-1.8-11.9 1.6-11.7 7.4c.3 6.9 1.3 13.8 3.2 20.7c13.7 51.2 66.4 81.6 117.6 67.9s81.6-66.4 67.9-117.6c-11.1-41.5-47.8-69.4-88.6-71.1c-5.8-.2-9.2 6.1-7.4 11.7c2.1 6.4 3.3 13.2 3.3 20.3z");
        olhoAbertoPath.setFill(Color.BLACK);

        // Configurar o botão do olho
        olhoBtn.setGraphic(olhoFechadoPath);
        olhoBtn.setOnAction(e -> {
            boolean mostrar = olhoBtn.isSelected();
            senhaField.setVisible(!mostrar);
            senhaField.setManaged(!mostrar);
            senhaVisivelField.setVisible(mostrar);
            senhaVisivelField.setManaged(mostrar);
            olhoBtn.setGraphic(mostrar ? olhoAbertoPath : olhoFechadoPath);
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

        // Criar botão de login com ícone SVG
        Button loginBtn = new Button();
        loginBtn.setGraphic(entrarIcon);
        loginBtn.setText("Entrar");
        loginBtn.setContentDisplay(ContentDisplay.LEFT);
        loginBtn.setDisable(true);
        loginBtn.getStyleClass().add("login-btn");

        Label statusLabel = new Label();

        Runnable verificarCampos = () -> {
            boolean preenchido = !emailField.getText().trim().isEmpty() &&
                    !(senhaField.isVisible() ? senhaField.getText() : senhaVisivelField.getText()).trim().isEmpty();
            loginBtn.setDisable(!preenchido);
        };

        emailField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        senhaField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        senhaVisivelField.textProperty().addListener((obs, o, n) -> verificarCampos.run());

        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(100);
        logoView.setPreserveRatio(true);

        VBox root = new VBox(15, logoView, emailLinha, senhaLinha, loginBtn, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 680, 380);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

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
        stage.setResizable(true);
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
        });
        contagemRegressiva.setCycleCount(Timeline.INDEFINITE);
        contagemRegressiva.play();
    }

    private String autenticarUsuario(String email, String senha) throws Exception {
        String emailCriptografado = criptografarAES(email);
        String senhaHash = hashSHA256(senha);

        try (Connection conn = DriverManager.getConnection(url, USER, PASSWORD)) {
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