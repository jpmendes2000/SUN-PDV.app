package com.sunpdv.home;

import com.sunpdv.AutenticarUser;
import com.sunpdv.telas.Caixa;
import com.sunpdv.telas.Configurar;
import com.sunpdv.telas.Produtos;
import com.sunpdv.telas.Usuarios;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.control.Tooltip;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class TelaHomeADM {

    private String nome;
    private String cargo;

    public TelaHomeADM(String nome, String cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

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
            if (img.isError()) {
                throw new Exception("Error loading image: " + caminhoIcone);
            }
            
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            icon.setStyle("-fx-fill: white;");
            
            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            
            // Container for the yellow bar indicator (agora à direita)
            StackPane indicatorContainer = new StackPane();
            indicatorContainer.setMinWidth(3);
            indicatorContainer.setMaxWidth(3);
            indicatorContainer.setMinHeight(30);
            indicatorContainer.setMaxHeight(30);
            indicatorContainer.setStyle("-fx-background-color: transparent;");
            
            // HBox para organizar ícone e texto à esquerda
            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);
            
            // HBox principal que empurra o indicador para a direita
            HBox content = new HBox(leftContent, new Region(), indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);
            
            Button btn = new Button();
            btn.setGraphic(content);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);
            
            // Hover effect com a barra amarela à direita
            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left,rgba(192, 151, 39, 0.39),rgba(232, 186, 35, 0.18)); -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color:rgba(255, 204, 0, 0.64); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });
            
            return btn;
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            Button btn = new Button(texto);
            btn.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(40);
            return btn;
        }
    }

    public void mostrar(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // Área esquerda (menu lateral)
        VBox leftMenu = new VBox();
        leftMenu.setPadding(new Insets(0));
        leftMenu.setStyle("-fx-background-color: #00536d; -fx-border-color: #00536d; -fx-border-width: 0 1 0 0;-fx-border-radius: 0 18 18 0;-fx-background-radius: 0 18 18 0;");
        leftMenu.setPrefWidth(280);
        leftMenu.setMinWidth(280);

        // Logo no topo do menu lateral
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        logoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        VBox logoBox = new VBox(logoView);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        // Botões do menu lateral
        Button btnVendas = criarBotaoLateral("Vendas", "/img/icon/carrinho-de-compras.png");
        Button btnProdutos = criarBotaoLateral("Gerenciar Produtos", "/img/icon/lista.png");
        Button btnUsuarios = criarBotaoLateral("Gerenciar Usuários", "/img/icon/grupo.png");
        Button btnConfigurar = criarBotaoLateral("Configurações", "/img/icon/definicoes.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        // Ações dos botões
        btnVendas.setOnAction(e -> new Caixa().show(stage));
        btnProdutos.setOnAction(e -> new Produtos().show(stage));
        btnUsuarios.setOnAction(e -> new Usuarios().show(stage));
        btnConfigurar.setOnAction(e -> new Configurar().show(stage));

        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(
                stage,
                "Confirmação de Saída",
                "Deseja realmente sair do sistema?",
                ""
            );
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    AutenticarUser.limparDados();
                    stage.close();
                }
            });
        });

        VBox buttonBox = new VBox(10, btnVendas, btnProdutos, btnUsuarios, btnConfigurar, btnSair);
        buttonBox.setAlignment(Pos.TOP_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        // Organização do menu lateral
        leftMenu.getChildren().addAll(logoBox, new Region(), buttonBox);
        VBox.setVgrow(leftMenu.getChildren().get(1), Priority.ALWAYS);

        // Área direita (conteúdo principal)
        Label mensagemFixa = new Label("Bem-vindo(a), " + nome + " você é " + cargo);
        mensagemFixa.getStyleClass().add("mensagem-bemvindo");

        StackPane centerPane = new StackPane(mensagemFixa);
        centerPane.setAlignment(Pos.CENTER);

        // Layout principal
        BorderPane layout = new BorderPane();
        layout.setLeft(leftMenu);
        layout.setCenter(centerPane);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Administrativo");
        stage.setMaximized(true);
        stage.show();
    }
}