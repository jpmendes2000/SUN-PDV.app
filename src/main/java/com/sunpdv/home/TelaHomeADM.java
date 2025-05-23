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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
                getClass().getResource("/css/style.css").toExternalForm()
            );
        }
    }

    public void mostrar(Stage stage) {

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);

        VBox topBox = new VBox(10, logoView);
        topBox.setPadding(new Insets(20));
        topBox.setAlignment(Pos.TOP_LEFT);

        // Ícones para os botões
        ImageView iconVendas = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/carrinho-de-compras.png")));
        iconVendas.setFitWidth(30);
        iconVendas.setFitHeight(30);

        ImageView iconProdutos = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/lista.png")));
        iconProdutos.setFitWidth(30);
        iconProdutos.setFitHeight(30);

        ImageView iconUsuarios = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/grupo.png")));
        iconUsuarios.setFitWidth(30);
        iconUsuarios.setFitHeight(30);

        ImageView iconConfig = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/definicoes.png")));
        iconConfig.setFitWidth(30);
        iconConfig.setFitHeight(30);

        ImageView iconSair = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/fechar.png")));
        iconSair.setFitWidth(30);
        iconSair.setFitHeight(30);

        // Botões com ícones
        Button btnVendas = new Button("Vendas", iconVendas);
        Button btnProdutos = new Button("Gerenciar Produtos", iconProdutos);
        Button btnUsuarios = new Button("Gerenciar Usuários", iconUsuarios);
        Button btnConfigurar = new Button("Configurações", iconConfig);
        Button btnSair = new Button("Sair do Sistema", iconSair);

        // Define largura padrão para todos os botões
        double larguraPadrao = 250;
        for (Button btn : new Button[]{btnVendas, btnProdutos, btnUsuarios, btnConfigurar, btnSair}) {
            btn.setPrefWidth(larguraPadrao);
        }

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

        VBox botoesBox = new VBox(15, btnVendas, btnProdutos, btnUsuarios, btnConfigurar, btnSair);
        botoesBox.setAlignment(Pos.BOTTOM_RIGHT);
        botoesBox.setPadding(new Insets(40));

        Label mensagemFixa = new Label("Bem-vindo(a), " + nome + " você é " + cargo);
        mensagemFixa.getStyleClass().add("mensagem-bemvindo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottomBox = new HBox(20, mensagemFixa, spacer, botoesBox);
        bottomBox.setPadding(new Insets(0, 15, 10, 30));
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);

        BorderPane layout = new BorderPane();
        layout.setTop(topBox);
        layout.setBottom(bottomBox);

        Scene scene = new Scene(layout, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Administrativo");
        stage.setResizable(true);
        stage.show();
    }
}
