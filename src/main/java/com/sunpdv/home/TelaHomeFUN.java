package com.sunpdv.home;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TelaHomeFUN {

    private String nome;
    private String cargo;

    public TelaHomeFUN(String nome, String cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

    public void mostrar() {
        Stage stage = new Stage();

        Image logo = new Image(getClass().getResourceAsStream("/img/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);

        VBox topBox = new VBox(10, logoView);
        topBox.setPadding(new Insets(20));
        topBox.setAlignment(Pos.TOP_LEFT);

        // Botões da tela FUN (apenas vendas e sair)
        Button btnVendas = new Button("Vendas");
        Button btnSair = new Button("Sair do Sistema");

        double larguraPadrao = 250;
        btnVendas.setPrefWidth(larguraPadrao);
        btnSair.setPrefWidth(larguraPadrao);

        btnSair.setOnAction(e -> stage.close());

        VBox botoesBox = new VBox(15, btnVendas, btnSair);
        botoesBox.setAlignment(Pos.BOTTOM_RIGHT);

        Label mensagemFixa = new Label("Bem-vindo(a), " + nome + " você é " + cargo);
        mensagemFixa.getStyleClass().add("mensagem-bemvindo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottomBox = new HBox();
        bottomBox.setPadding(new Insets(0, 15, 10, 30));
        bottomBox.setSpacing(20);
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBox.getChildren().addAll(mensagemFixa, spacer, botoesBox);

        BorderPane layout = new BorderPane();
        layout.setTop(topBox);
        layout.setBottom(bottomBox);

        Scene scene = new Scene(layout, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Funcionário");
        stage.setResizable(true);
        stage.setFullScreen(true);
        stage.show();
    }
}
