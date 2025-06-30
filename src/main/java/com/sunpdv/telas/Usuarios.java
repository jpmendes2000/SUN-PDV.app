package com.sunpdv.telas;

import com.sunpdv.AutenticarUser;
import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Usuarios {

    private Stage stage;

    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                getClass().getResource("/img/css/style.css").toExternalForm()
            );
        }
    }

    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            StackPane indicatorContainer = new StackPane();
            indicatorContainer.setMinWidth(3);
            indicatorContainer.setMaxWidth(3);
            indicatorContainer.setMinHeight(30);
            indicatorContainer.setMaxHeight(30);
            indicatorContainer.setStyle("-fx-background-color: transparent;");

            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);

            HBox content = new HBox(leftContent, new Region(), indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

            Button btn = new Button();
            btn.setGraphic(content);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18));");
                indicatorContainer.setStyle("-fx-background-color: rgba(255, 204, 0, 0.64);");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });

            return btn;
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            return new Button(texto);
        }
    }

    public void show(Stage stage) {
        this.stage = stage;

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // Menu lateral
        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        // Logo SUN PDV
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        
        Label titulonaABA = new Label("Gerenciamento de Usuários");
        titulonaABA.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        // Botões
        Button btnHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        // Ações
        btnHome.setOnAction(e -> {
            try {
                String cargo = AutenticarUser.getCargo();
                switch (cargo) {
                    case "Administrador":
                        new TelaHomeADM(AutenticarUser.getNome(), cargo).mostrar(stage);
                        break;
                    case "Moderador":
                        new TelaHomeMOD(AutenticarUser.getNome(), cargo).mostrar(stage);
                        break;
                    case "Funcionário":
                        new TelaHomeFUN(AutenticarUser.getNome(), cargo).mostrar(stage);
                        break;
                    default:
                        System.out.println("Cargo não reconhecido: " + cargo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Erro ao retornar para a tela principal.");
                alert.showAndWait();
            }
        });

        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(
                stage,
                "Confirmação de Saída",
                "Deseja realmente sair do sistema?",
                ""
            );
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });

        VBox buttonBox = new VBox(10, btnHome, btnSair);
        buttonBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        leftMenu.getChildren().addAll(logoBox, espaco, buttonBox);

        // Conteúdo da tela
        StackPane centro = new StackPane();
        Label placeholder = new Label("Gerenciamento de Usuários (conteúdo aqui)");
        placeholder.setStyle("-fx-font-size: 22px; -fx-text-fill: #999;");
        centro.getChildren().add(placeholder);
        centro.setPadding(new Insets(20));

        // Mensagem inferior direita
        String nome = AutenticarUser.getNome() != null ? AutenticarUser.getNome() : "Usuário";
        String cargo = AutenticarUser.getCargo() != null ? AutenticarUser.getCargo() : "Cargo";

        Label mensagemFixa = new Label("Bem-vindo(a), " + nome + " você é " + cargo);
        mensagemFixa.getStyleClass().add("mensagem-bemvindo");

        StackPane posMensagem = new StackPane(mensagemFixa);
        posMensagem.setAlignment(Pos.BOTTOM_RIGHT);
        posMensagem.setPadding(new Insets(0, 20, 20, 280));

        StackPane centroComMensagem = new StackPane(centro, posMensagem);

        // Layout principal
        BorderPane layout = new BorderPane();
        layout.setLeft(leftMenu);
        layout.setCenter(centroComMensagem);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Gerenciamento de Usuários");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }
}
