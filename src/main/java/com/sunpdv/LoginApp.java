package com.sunpdv;

// Importações para criptografia
import java.security.MessageDigest; // Classe da biblioteca java.security para gerar hash (SHA-256)
import java.security.NoSuchAlgorithmException;
import java.sql.*; // Conjunto de classes para conexão e manipulação de bancos de dados SQL
import java.util.Base64; // Classe da biblioteca java.util para codificação Base64 (criptografia)

import javax.crypto.Cipher; // Classe da biblioteca javax.crypto usada para criptografar dados com AES
import javax.crypto.spec.SecretKeySpec; // Define a chave secreta para o algoritmo AES

// Importações do JavaFX para criação de interface gráfica
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginApp extends Application {

    // Chave usada para criptografia AES (não recomendada em código-fonte em produção)
    private static final String AES_KEY = "MinhaChaveSuperSegura1234567890!";
    
    // Variáveis para controle de tentativas de login
    private int tentativas = 0;
    private long tempoBloqueio = 0;
    private static final int MAX_TENTATIVAS = 6;
    private static final int TEMPO_ESPERA = 120; // segundos

    public static void main(String[] args) {
        launch(args); // Inicia a aplicação JavaFX
    }

    @Override
    public void start(Stage primaryStage) {

        // Label e campo de texto para o e-mail
        Label emailLabel = new Label("E-mail:");
        TextField emailField = new TextField();
        emailField.setPrefWidth(280);

        // Label e campo de senha com ocultação
        Label senhaLabel = new Label("Senha:");
        PasswordField senhaField = new PasswordField();
        senhaField.setPrefWidth(280);

        // Campo alternativo para mostrar senha visível (ligado ao mesmo texto do PasswordField)
        TextField senhaVisivelField = new TextField();
        senhaVisivelField.setPrefWidth(280);
        senhaVisivelField.setManaged(false); // Não ocupa espaço inicialmente
        senhaVisivelField.setVisible(false);
        senhaVisivelField.textProperty().bindBidirectional(senhaField.textProperty());

        // Botão para alternar visibilidade da senha
        ToggleButton mostrarSenhaBtn = new ToggleButton("👁");
        mostrarSenhaBtn.getStyleClass().add("visible");
        mostrarSenhaBtn.setOnAction(e -> {
            boolean mostrar = mostrarSenhaBtn.isSelected();
            senhaField.setVisible(!mostrar);
            senhaField.setManaged(!mostrar);
            senhaVisivelField.setVisible(mostrar);
            senhaVisivelField.setManaged(mostrar);
        });

        // Layout horizontal com campo de senha e botão de visualização
        HBox senhaBox = new HBox(10, senhaField, senhaVisivelField, mostrarSenhaBtn);
        senhaBox.setAlignment(Pos.CENTER_LEFT);

        VBox emailLinha = new VBox(5, emailLabel, emailField);
        emailLinha.setAlignment(Pos.CENTER_LEFT);

        VBox senhaLinha = new VBox(5, senhaLabel, senhaBox);
        senhaLinha.setAlignment(Pos.CENTER_LEFT);

        // Botão de login e label para mensagens de status
        Button loginBtn = new Button("Entrar");
        Label statusLabel = new Label();

        // Carrega imagem da logo
        Image logoImage = new Image(getClass().getResourceAsStream("/img/logo.png"));
        ImageView logoView = new ImageView(logoImage);
        logoView.setPreserveRatio(true);
        logoView.setFitWidth(100);
        logoView.getStyleClass().add("logo");

        // Layout principal da tela
        VBox root = new VBox(15, logoView, emailLinha, senhaLinha, loginBtn, statusLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);

        // Cena da interface e aplicação de CSS externo
        Scene scene = new Scene(root, 680, 380);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        // Animações de hover no botão de login
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

        // Ação do botão de login
        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String senha = senhaField.isVisible() ? senhaField.getText() : senhaVisivelField.getText();

            // Verifica se usuário está bloqueado por excesso de tentativas
            if (System.currentTimeMillis() < tempoBloqueio) {
                long restante = (tempoBloqueio - System.currentTimeMillis()) / 1000;
                statusLabel.setText("Muitas tentativas. Tente novamente em " + restante + " segundos.");
                return;
            }

            // Verifica se campos foram preenchidos
            if (email.isEmpty() || senha.isEmpty()) {
                statusLabel.setText("Por favor, preencha todos os campos.");
                return;
            }

            loginBtn.setDisable(true);
            statusLabel.setText("Verificando...");

            // Cria tarefa em segundo plano para autenticação
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

            // Lida com o retorno da autenticação
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

            new Thread(task).start(); // Inicia a thread da autenticação
        });

        // Configurações da janela principal
        primaryStage.setTitle("Login - SUN PDV");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // Método para autenticar o usuário no banco de dados Azure SQL
    private String autenticarUsuario(String email, String senha) throws Exception {
        String url = "jdbc:sqlserver://serverpdv.database.windows.net:1433;"
                   + "database=SUN_PDVcloud;"
                   + "user=adminuser@serverpdv;"
                   + "password=Jp081007!;"
                   + "encrypt=true;"
                   + "trustServerCertificate=false;"
                   + "hostNameInCertificate=*.database.windows.net;"
                   + "loginTimeout=30;";

        // Carrega o driver JDBC da Microsoft SQL Server
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        // Criptografa o e-mail com AES e a senha com SHA-256
        String emailCriptografado = criptografarAES(email);
        String senhaCriptografada = hashSHA256(senha);

        try (Connection conn = DriverManager.getConnection(url)) {
            // Consulta para autenticação com email e senha
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

    // Método para criptografar texto com AES
    private String criptografarAES(String texto) throws Exception {
        // AES é um algoritmo de criptografia simétrica
        SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES"); // Usa a biblioteca javax.crypto.Cipher
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] textoCriptografado = cipher.doFinal(texto.getBytes());
        return Base64.getEncoder().encodeToString(textoCriptografado); // Codifica o resultado em Base64
    }

    // Método para gerar hash SHA-256 da senha
    private String hashSHA256(String senha) throws NoSuchAlgorithmException {
        // SHA-256 é uma função de hash criptográfica unidirecional (java.security.MessageDigest)
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(senha.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash)
            hexString.append(String.format("%02x", b)); // Converte os bytes em hexadecimal
        return hexString.toString();
    }
}
