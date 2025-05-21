package com.sunpdv.home;

import com.sunpdv.AutenticarUser;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TelaHomeADM {

    private String nome;
    private String cargo;
    

    // Construtor que recebe nome e cargo do usuário
    public TelaHomeADM(String nome, String cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

    // Método para criar e mostrar a janela
    public void mostrar() {
        Stage stage = new Stage();

        // Logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);

        VBox topBox = new VBox(10, logoView);
        topBox.setPadding(new Insets(20));
        topBox.setAlignment(Pos.TOP_LEFT);

        // Botões da tela ADM (vendas, gerenciar produtos, gerenciar usuários, sair)
        Button btnVendas = new Button("Vendas");
        Button btnProdutos = new Button("Gerenciar Produtos");
        Button btnUsuarios = new Button("Gerenciar Usuários");
        Button btnConfigurar = new Button("Configurações");
        Button btnSair = new Button("Sair do Sistema");

        double larguraPadrao = 250;
        btnVendas.setPrefWidth(larguraPadrao);
        btnProdutos.setPrefWidth(larguraPadrao);
        btnUsuarios.setPrefWidth(larguraPadrao);
        btnConfigurar.setPrefWidth(larguraPadrao);
        btnSair.setPrefWidth(larguraPadrao);

        btnSair.setOnAction(e -> {
        AutenticarUser.limparDados(); // Limpa os dados de autenticação
        stage.close(); // Fecha a janela atual
        });


        VBox botoesBox = new VBox(15, btnVendas, btnProdutos, btnUsuarios, btnConfigurar, btnSair);
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
        stage.setTitle("SUN PDV - Painel Administrativo");
        stage.setResizable(true);
        stage.setFullScreen(true);
        stage.show();
    }
}
