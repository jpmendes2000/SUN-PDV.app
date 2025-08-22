package com.sunpdv.telas.operacao;

import com.sunpdv.model.AutenticarUser;
import com.sunpdv.telas.home.TelaHomeADM;
import com.sunpdv.telas.home.TelaHomeFUN;
import com.sunpdv.telas.home.TelaHomeMOD;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
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
            System.out.println("Driver JDBC carregado com sucesso!");
        } catch (ClassNotFoundException e) {
            System.err.println("Erro ao carregar driver JDBC: " + e.getMessage());
            e.printStackTrace();
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Erro ao carregar driver JDBC: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // Componentes da interface
        TextField emailField = new TextField();
        emailField.setPromptText("E-mail");

        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Senha         min: 8 dígitos");

        TextField senhaVisivelField = new TextField();
        senhaVisivelField.setPromptText("Senha         min: 8 dígitos");
        senhaVisivelField.setManaged(false);
        senhaVisivelField.setVisible(false);
        senhaVisivelField.textProperty().bindBidirectional(senhaField.textProperty());

        ToggleButton olhoBtn = new ToggleButton();
        olhoBtn.getStyleClass().add("olho-btn");
        olhoBtn.setStyle("-fx-background-color: transparent; -fx-padding: 5 5 5 5;");

        // Ícones
        String caminhoVisivel = getClass().getResource("/img/icon/visibilidade.png").toExternalForm();
        String caminhoNaoVisivel = getClass().getResource("/img/icon/not-visibilidade.png").toExternalForm();
        String entrarIcon = getClass().getResource("/img/icon/entrar.png").toExternalForm();

        ImageView olhoIcon = new ImageView(new Image(caminhoNaoVisivel));
        olhoIcon.setFitWidth(27);
        olhoIcon.setFitHeight(27);
        olhoBtn.setGraphic(olhoIcon);

        ImageView entrarIconView = new ImageView(new Image(entrarIcon));
        entrarIconView.setFitHeight(24);
        entrarIconView.setFitWidth(24);

        // Ação do botão de visibilidade da senha
        olhoBtn.setOnAction(e -> {
            boolean mostrar = olhoBtn.isSelected();
            senhaField.setVisible(!mostrar);
            senhaField.setManaged(!mostrar);
            senhaVisivelField.setVisible(mostrar);
            senhaVisivelField.setManaged(mostrar);
            olhoIcon.setImage(new Image(mostrar ? caminhoVisivel : caminhoNaoVisivel));
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

        Button loginBtn = new Button("Entrar", entrarIconView);
        loginBtn.setDisable(true);

        // Botão Criar Usuário
        Button criarUsuarioBtn = new Button("Criar Usuário");
        criarUsuarioBtn.getStyleClass().add("criar-usuario-btn");

        Label statusLabel = new Label();

        // Verificação de campos preenchidos
        Runnable verificarCampos = () -> {
            boolean preenchido = !emailField.getText().trim().isEmpty() &&
                    !(senhaField.isVisible() ? senhaField.getText() : senhaVisivelField.getText()).trim().isEmpty();
            loginBtn.setDisable(!preenchido);
        };

        emailField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        senhaField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        senhaVisivelField.textProperty().addListener((obs, o, n) -> verificarCampos.run());

        // Logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(100);
        logoView.setPreserveRatio(true);

        Image logomini = new Image(getClass().getResourceAsStream("/img/logo/logominuscula.png"));
        stage.getIcons().add(logomini);

        // Layout
        VBox root = new VBox(8, logoView, emailLinha, senhaLinha, loginBtn, criarUsuarioBtn, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 680, 380);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Ação de tecla Enter
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !loginBtn.isDisabled()) {
                loginBtn.fire();
                event.consume();
            }
        });

        // Animação do botão de login
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

        // No método start da classe LoginApp, na ação do botão Criar Usuário
        criarUsuarioBtn.setOnAction(e -> {
            try {
                new Cadastro().mostrar(stage); // Corrigido de Cadastro para TelaCadastro
            } catch (Exception ex) {
                statusLabel.setText("Erro ao abrir tela de cadastro: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Ação do botão de login
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
                        boolean autenticado = AutenticarUser.autenticar(email, senha);
                        return autenticado ? "sucesso" : "E-mail ou senha inválidos.";
                    } catch (Exception ex) {
                        return "Erro: " + ex.getMessage();
                    }
                }
            };

            loginTask.setOnSucceeded(event -> {
                String resultado = loginTask.getValue();
                System.out.println("Resultado da autenticação: " + resultado);
                if ("sucesso".equalsIgnoreCase(resultado)) {
                    tentativas = 0;
                    try {
                        String cargo = AutenticarUser.getCargo();
                        System.out.println("Cargo do usuário: " + cargo);
                        switch (cargo != null ? cargo : "") {
                            case "Administrador":
                                System.out.println("Abrindo TelaHomeADM para: " + AutenticarUser.getNome());
                                new TelaHomeADM(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                                break;
                            case "Moderador":
                                System.out.println("Abrindo TelaHomeMOD para: " + AutenticarUser.getNome());
                                new TelaHomeMOD(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                                break;
                            case "Funcionario":
                                System.out.println("Abrindo TelaHomeFUN para: " + AutenticarUser.getNome());
                                new TelaHomeFUN(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                                break;
                            default:
                                statusLabel.setText("Cargo não reconhecido: " + cargo);
                                System.err.println("Cargo inválido: " + cargo);
                                loginBtn.setDisable(false);
                                return;
                        }
                    } catch (Exception ex) {
                        statusLabel.setText("Erro ao abrir a tela principal: " + ex.getMessage());
                        System.err.println("Erro ao abrir tela principal: " + ex.getMessage());
                        ex.printStackTrace();
                        loginBtn.setDisable(false);
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
                String erro = loginTask.getException() != null ? loginTask.getException().getMessage() : "Erro desconhecido";
                statusLabel.setText("Erro de login: " + erro);
                System.err.println("Erro no loginTask: " + erro);
                loginBtn.setDisable(false);
            });

            new Thread(loginTask).start();
        });

        stage.setScene(scene);
        stage.setTitle("Login - SUN PDV");
        stage.setResizable(true);
        Platform.runLater(() -> root.requestFocus());
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
}