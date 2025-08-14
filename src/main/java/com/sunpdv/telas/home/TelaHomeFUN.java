// IMPORTS NECESSÁRIOS

package com.sunpdv.telas.home;

import com.sunpdv.model.AutenticarUser;
import com.sunpdv.telas.operacao.Caixa;
import com.sunpdv.telas.operacao.Configurar;
import com.sunpdv.telas.operacao.Produtos;
import com.sunpdv.telas.operacao.Usuarios;

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

import java.io.File;

public class TelaHomeFUN {

    private String nome;
    private String cargo;

    public TelaHomeFUN(String nome, String cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

    // Alerta de confirmação com CSS customizado
    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);

            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                TelaHomeADM.class.getResource("/css/style.css").toExternalForm()
            );
        }
    }

    /**
     * Cria botão lateral com ícone, texto e barra lateral amarela no hover
     */
    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            if (img.isError()) {
                throw new Exception("Erro ao carregar imagem: " + caminhoIcone);
            }

            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            StackPane indicatorContainer = new StackPane(); // barra amarela lateral
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
            btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18)); -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color: rgba(255, 204, 0, 0.64); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 0);");
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

    /**
     * Exibe a tela principal
     */
    public void mostrar(Stage stage) {
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

        VBox logoBox = new VBox(logoView);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        // Botões do menu
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
                "Deseja sair?",
                "Isso fechará o sistema."
            );
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    AutenticarUser.limparDados();
                    stage.close();
                }
            });
        });

        VBox buttonBox = new VBox(10, btnVendas, btnSair);
        buttonBox.setAlignment(Pos.TOP_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        leftMenu.getChildren().addAll(logoBox, new Region(), buttonBox);
        VBox.setVgrow(leftMenu.getChildren().get(1), Priority.ALWAYS);

        // Conteúdo central - logo do mercado configurada (se existir)
        StackPane centro = new StackPane();
        centro.setPadding(new Insets(20));

        File imagemLogo = new File("logo_empresa.png");
        if (imagemLogo.exists()) {
            Image imageLogo = new Image(imagemLogo.toURI().toString());
            ImageView imageView = new ImageView(imageLogo);

            imageView.setFitWidth(1000);
            imageView.setFitHeight(800);
            imageView.setPreserveRatio(true);

            centro.getChildren().add(imageView);
        } else {
            Label placeholder = new Label("Nenhuma logo configurada");
            placeholder.setStyle("-fx-font-size: 22px; -fx-text-fill: #999;");
            centro.getChildren().add(placeholder);
        }

        // Mensagem inferior direita com nome e cargo
        Label mensagemFixa = new Label("Bem-vindo(a), " + nome + " você é " + cargo);
        mensagemFixa.getStyleClass().add("mensagem-bemvindo");

        StackPane posMensagem = new StackPane(mensagemFixa);
        posMensagem.setAlignment(Pos.BOTTOM_RIGHT);
        posMensagem.setPadding(new Insets(0, 20, 20, 0));

        // Layout principal
        BorderPane layout = new BorderPane();
        layout.setLeft(leftMenu);
        layout.setCenter(centro);
        StackPane conteudoComMensagem = new StackPane(centro, posMensagem);
        layout.setCenter(conteudoComMensagem);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Administrativo");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();;
    }
}
