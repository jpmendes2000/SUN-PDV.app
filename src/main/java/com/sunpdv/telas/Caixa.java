package com.sunpdv.telas;

import com.sunpdv.AutenticarUser;
import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Caixa {

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
            indicatorContainer.setVisible(false);
            indicatorContainer.setManaged(false); // não ocupa espaço quando invisível

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
                indicatorContainer.setVisible(true);
                indicatorContainer.setManaged(true);
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
                indicatorContainer.setVisible(false);
                indicatorContainer.setManaged(false);
            });

            return btn;
        } catch (Exception e) {
            return new Button(texto);
        }
    }

    public void show(Stage stage) {

        // MENU LATERAL
        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        logoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        Label titulonaABA = new Label("Caixa");
        titulonaABA.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        Button btnVoltarHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        VBox buttonBox = new VBox(10, btnVoltarHome, btnSair);
        buttonBox.setAlignment(Pos.TOP_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        leftMenu.getChildren().addAll(logoBox, new Region(), buttonBox);
        VBox.setVgrow(leftMenu.getChildren().get(1), Priority.ALWAYS); // garante que estica até embaixo

        // ÁREA CENTRAL
        Label titulo = new Label("Módulo de Caixa");
        titulo.setStyle("-fx-text-fill: #062e3aff; -fx-font-size: 24px; -fx-font-weight: bold;");

        StackPane conteudoCentral = new StackPane(titulo);
        conteudoCentral.setAlignment(Pos.CENTER);

        // BOTÃO NOVA VENDA FIXO NO CANTO INFERIOR DIREITO
        Button btnNovaVenda = new Button("Nova Venda");
        btnNovaVenda.setStyle(
            "-fx-background-color: #e8ba23; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px; " +
            "-fx-background-radius: 6px; -fx-padding: 10 60 10 60;"
        );

        AnchorPane containerCentral = new AnchorPane(conteudoCentral, btnNovaVenda);
        AnchorPane.setTopAnchor(conteudoCentral, 0.0);
        AnchorPane.setBottomAnchor(conteudoCentral, 0.0);
        AnchorPane.setLeftAnchor(conteudoCentral, 0.0);
        AnchorPane.setRightAnchor(conteudoCentral, 0.0);

        AnchorPane.setBottomAnchor(btnNovaVenda, 20.0);
        AnchorPane.setRightAnchor(btnNovaVenda, 20.0);

        // Layout principal
        BorderPane root = new BorderPane();
        root.setLeft(leftMenu);
        root.setCenter(containerCentral);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Caixa");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();

        // AÇÕES DOS BOTÕES
        btnNovaVenda.setOnAction(e -> {
            System.out.println("Nova venda iniciada");
        });

        btnVoltarHome.setOnAction(e -> {
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
    }
}
