package com.sunpdv.home;

import com.sunpdv.AutenticarUser;
import com.sunpdv.telas.Caixa;
import com.sunpdv.telas.Produtos;
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
import javafx.stage.Screen;
import javafx.stage.Stage;

public class TelaHomeMOD {

    private String nome;
    private String cargo;

    public TelaHomeMOD(String nome, String cargo) {
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
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            icon.setStyle("-fx-fill: white;");
            
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
                btn.setStyle("-fx-background-color: linear-gradient(to left,rgba(192,151,39,0.39),rgba(232,186,35,0.18));");
                indicatorContainer.setStyle("-fx-background-color:rgba(255,204,0,0.64);");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });
            
            return btn;
        } catch (Exception e) {
            Button btn = new Button(texto);
            btn.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            btn.setPrefWidth(280);
            return btn;
        }
    }

    public void mostrar(Stage stage) {
        // Configuração do layout principal
        BorderPane mainPane = new BorderPane();
        
        // Área esquerda (menu lateral)
        VBox leftMenu = new VBox();
        leftMenu.setStyle("-fx-background-color: #00536d;");
        leftMenu.setPrefWidth(280);
        leftMenu.setMinWidth(280);

        // Logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        
        VBox logoBox = new VBox(logoView);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        // Botões empilhados na parte inferior
        Button btnVendas = criarBotaoLateral("Vendas", "/img/icon/carrinho-de-compras.png");
        Button btnProdutos = criarBotaoLateral("Produtos", "/img/icon/lista.png");
        Button btnSair = criarBotaoLateral("Sair", "/img/icon/fechar.png");
        
        // Ações dos botões
        btnVendas.setOnAction(e -> new Caixa().show(stage));
        btnProdutos.setOnAction(e -> new Produtos().show(stage));
        btnSair.setOnAction(e -> {
            new CustomConfirmationAlert(stage, "Confirmação", "Deseja sair?", "")
                .showAndWait().ifPresent(r -> { if (r == ButtonType.OK) stage.close(); });
        });

        // Container para os botões (alinhado na parte inferior)
        VBox buttonContainer = new VBox(10, btnVendas, btnProdutos, btnSair);
        buttonContainer.setAlignment(Pos.BOTTOM_LEFT);
        buttonContainer.setPadding(new Insets(0, 0, 20, 0));
        
        // Espaçador para empurrar os botões para baixo
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Layout completo do menu lateral
        leftMenu.getChildren().addAll(logoBox, spacer, buttonContainer);
        
               // Área direita (conteúdo principal)
        Label mensagemFixa = new Label("Bem-vindo(a), " + nome + " você é " + cargo);
        mensagemFixa.getStyleClass().add("mensagem-bemvindo");

        StackPane posMensagem = new StackPane(mensagemFixa);
        posMensagem.setAlignment(Pos.BOTTOM_RIGHT);
        posMensagem.setPadding(new Insets(0, 20, 20, 0));

        BorderPane layout = new BorderPane();
        layout.setLeft(leftMenu);
        layout.setCenter(posMensagem);
        
        // Configuração final
        mainPane.setLeft(leftMenu);
        mainPane.setCenter(posMensagem);
        
        Scene scene = new Scene(mainPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Moderador");
        stage.setMaximized(true);
        stage.show();
    }
}