package com.sunpdv.telas.operacao;

import com.sunpdv.model.AutenticarUser;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class Cadastro {

    public void mostrar(Stage stage) throws Exception {
        // Componentes da tela
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome completo");

        TextField emailField = new TextField();
        emailField.setPromptText("E-mail");

        // Campo de senha
        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Senha         min: 8 dígitos");

        TextField senhaVisivelField = new TextField();
        senhaVisivelField.setPromptText("Senha         min: 8 dígitos");
        senhaVisivelField.setManaged(false);
        senhaVisivelField.setVisible(false);
        senhaVisivelField.textProperty().bindBidirectional(senhaField.textProperty());

        // Botão de visibilidade
        ToggleButton olhoBtn = new ToggleButton();
        olhoBtn.getStyleClass().add("olho-btn");
        olhoBtn.setStyle("-fx-background-color: transparent; -fx-padding: 5 5 5 5;");

        // Ícones
        String caminhoVisivel = getClass().getResource("/img/icon/visibilidade.png").toExternalForm();
        String caminhoNaoVisivel = getClass().getResource("/img/icon/not-visibilidade.png").toExternalForm();
        ImageView olhoIcon = new ImageView(new Image(caminhoNaoVisivel));
        olhoIcon.setFitWidth(27);
        olhoIcon.setFitHeight(27);
        olhoBtn.setGraphic(olhoIcon);

        // Ação do botão de visibilidade
        olhoBtn.setOnAction(e -> {
            boolean mostrar = olhoBtn.isSelected();
            senhaField.setVisible(!mostrar);
            senhaField.setManaged(!mostrar);
            senhaVisivelField.setVisible(mostrar);
            senhaVisivelField.setManaged(mostrar);
            olhoIcon.setImage(new Image(mostrar ? caminhoVisivel : caminhoNaoVisivel));
        });

        // StackPane para alinhar o campo de senha e o botão de visibilidade
        StackPane senhaStack = new StackPane();
        senhaStack.setAlignment(Pos.CENTER_RIGHT);
        senhaStack.getChildren().addAll(senhaField, senhaVisivelField, olhoBtn);

        // Ajustar largura dos campos
        senhaField.prefWidthProperty().bind(nomeField.widthProperty());
        senhaVisivelField.prefWidthProperty().bind(nomeField.widthProperty());

        // HBox para o campo de senha
        HBox senhaLinha = new HBox(senhaStack);
        senhaLinha.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cargoComboBox = new ComboBox<>();
        cargoComboBox.getItems().addAll("Administrador", "Moderador", "Funcionario");
        cargoComboBox.setPromptText("Selecione o cargo");

        Button cadastrarBtn = new Button("Cadastrar");
        cadastrarBtn.setDisable(true); // Inicialmente desabilitado
        Label statusLabel = new Label();

        HBox cargoECadastrarBox = new HBox(20);
        cargoECadastrarBox.setAlignment(Pos.CENTER);
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        leftSpacer.setMinWidth(10);
        rightSpacer.setMinWidth(163
        );
        HBox leftContainer = new HBox(cargoComboBox);
        leftContainer.setAlignment(Pos.CENTER_LEFT);
        cargoECadastrarBox.getChildren().addAll(leftContainer, leftSpacer, cadastrarBtn, rightSpacer);

        // Verificar se preenchido
        Runnable verificarCampos = () -> {
            String senha = senhaField.isVisible() ? senhaField.getText() : senhaVisivelField.getText();
            boolean preenchido = !nomeField.getText().trim().isEmpty() &&
                    !emailField.getText().trim().isEmpty() &&
                    !senha.trim().isEmpty() &&
                    senha.trim().length() >= 8 && // Verifica se a senha tem pelo menos 8 dígitos
                    cargoComboBox.getValue() != null;
            cadastrarBtn.setDisable(!preenchido);
        };

        nomeField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        emailField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        senhaField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        senhaVisivelField.textProperty().addListener((obs, o, n) -> verificarCampos.run());
        cargoComboBox.valueProperty().addListener((obs, o, n) -> verificarCampos.run());

        // Botão Voltar
        Button voltarBtn = new Button("Voltar");
        voltarBtn.getStyleClass().add("voltar-btn");
        voltarBtn.setOnAction(e -> {
            try {
                new LoginApp().start(stage); // Volta para a tela de login
            } catch (Exception ex) {
                statusLabel.setText("Erro ao voltar: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // botão Cadastrar
        cadastrarBtn.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String email = emailField.getText().trim();
            String senha = senhaField.isVisible() ? senhaField.getText().trim() : senhaVisivelField.getText().trim();
            String cargo = cargoComboBox.getValue();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cargo == null) {
                statusLabel.setText("Preencha todos os campos!");
                return;
            }

            if (senha.length() < 8) {
                statusLabel.setText("A senha deve ter no mínimo 8 dígitos!");
                return;
            }

            try {
                boolean sucesso = AutenticarUser.cadastrar(nome, email, senha, cargo);
                if (sucesso) {
                    statusLabel.setText("Usuário cadastrado com sucesso!");
                    nomeField.clear();
                    emailField.clear();
                    senhaField.clear();
                    senhaVisivelField.clear();
                    cargoComboBox.setValue(null);
                } else {
                    statusLabel.setText("Erro ao cadastrar usuário. Verifique os dados e tente novamente.");
                }
            } catch (Exception ex) {
                statusLabel.setText("Erro ao cadastrar: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Logo
        ImageView logoView = null;
        try {
            if (getClass().getResourceAsStream("/img/logo/logo.png") != null) {
                Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
                logoView = new ImageView(logo);
                logoView.setFitWidth(100);
                logoView.setPreserveRatio(true);
            } else {
                System.err.println("Imagem /img/logo/logo.png não encontrada.");
                statusLabel.setText("Aviso: Logo não encontrado.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar logo: " + e.getMessage());
            statusLabel.setText("Aviso: Erro ao carregar logo.");
        }

        // Layout
        VBox root = new VBox(8);    
        if (logoView != null) {
            root.getChildren().add(logoView);
        }
        root.getChildren().addAll(nomeField, emailField, senhaLinha, cargoECadastrarBox, voltarBtn, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 680, 420);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSS: " + e.getMessage());
            statusLabel.setText("Aviso: Erro ao carregar estilo CSS.");
        }

        stage.setScene(scene);
        stage.setTitle("Cadastro - SUN PDV");
        stage.setResizable(true);
        Platform.runLater(() -> root.requestFocus());
        stage.show();
    }
}