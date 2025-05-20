package com.sunpdv.telas;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TelaHomeFUN extends Application {

    @Override
    public void start(Stage stage) {
        // Imagem logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);

        // VBox com a logo no canto superior esquerdo
        VBox logoBox = new VBox(logoView);
        logoBox.setPadding(new Insets(20));
        logoBox.setAlignment(Pos.TOP_LEFT);

        // Botões
        Button btnVendas = new Button("Vendas");
        Button btnSair = new Button("Sair do Sistema");

        double larguraPadrao = 250;
        btnVendas.setPrefWidth(larguraPadrao);
        btnSair.setPrefWidth(larguraPadrao);

        btnSair.setOnAction(e -> stage.close());

        // VBox com os botões no canto inferior direito
        VBox botoesBox = new VBox(15, btnVendas, btnSair);
        botoesBox.setPadding(new Insets(40));
        botoesBox.setAlignment(Pos.BOTTOM_RIGHT);

        // Pane para alinhar os botões no canto inferior direito
        StackPane botoesPane = new StackPane(botoesBox);
        StackPane.setAlignment(botoesBox, Pos.BOTTOM_RIGHT);

        // Pane para alinhar a logo no canto superior esquerdo
        StackPane logoPane = new StackPane(logoBox);
        StackPane.setAlignment(logoBox, Pos.TOP_LEFT);

        // Container principal com logo e botões sobrepostos
        StackPane principal = new StackPane();
        principal.getChildren().addAll(logoPane, botoesPane);

        // Cena e estilo
        Scene scene = new Scene(principal, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Janela
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Administrativo");
        stage.setResizable(true);
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
