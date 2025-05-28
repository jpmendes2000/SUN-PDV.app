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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class TelaHomeMOD {

    private String nome;
    private String cargo;

    // Construtor com nome e cargo do usuário
    public TelaHomeMOD(String nome, String cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

    // Classe interna para alertas de confirmação com estilo
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

    // Exibe a tela principal do moderador
    public void mostrar(Stage stage) {

        // Maximiza janela na tela do usuário
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // Logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);

        VBox topBox = new VBox(10, logoView);
        topBox.setPadding(new Insets(20));
        topBox.setAlignment(Pos.TOP_LEFT);

        ImageView iconVendas = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/carrinho-de-compras.png")));
        iconVendas.setFitWidth(32);
        iconVendas.setFitHeight(32);

        ImageView iconProdutos = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/lista.png")));
        iconProdutos.setFitWidth(32);
        iconProdutos.setFitHeight(32);

        ImageView iconSair = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/fechar.png")));
        iconSair.setFitWidth(32);
        iconSair.setFitHeight(32);

        // Botões para moderador (menos opções que ADM)
        Button btnVendas = new Button("Vendas", iconVendas);
        Button btnProdutos = new Button("Gerenciar Produtos", iconProdutos);
        Button btnSair = new Button("Sair do Sistema", iconSair);

        double larguraPadrao = 250;
        for (Button btn : new Button[]{btnVendas, btnProdutos, btnSair}) {
            btn.setPrefWidth(larguraPadrao);
        }

        // Define ações dos botões
        btnVendas.setOnAction(e -> new Caixa().show(stage));
        btnProdutos.setOnAction(e -> new Produtos().show(stage));

        // Botão sair exibe alerta de confirmação
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

        // Caixa vertical com botões
        VBox botoesBox = new VBox(15, btnVendas, btnProdutos, btnSair);
        botoesBox.setAlignment(Pos.BOTTOM_LEFT);
        botoesBox.setPadding(new Insets(0, 0, 6, 6)); 

        Label mensagemFixa = new Label("Bem-vindo(a), " + nome + " você é " + cargo);
        mensagemFixa.getStyleClass().add("mensagem-bemvindo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottomBox = new HBox(20, botoesBox, spacer, mensagemFixa);
        bottomBox.setPadding(new Insets(0, 30, 10, 15));
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);

        // Layout principal
        BorderPane layout = new BorderPane();
        layout.setTop(topBox);
        layout.setBottom(bottomBox);

        // Cena com estilo CSS
        Scene scene = new Scene(layout, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        // Configura e mostra a janela
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Moderador");
        stage.setResizable(true);
        stage.show();
    }
}
