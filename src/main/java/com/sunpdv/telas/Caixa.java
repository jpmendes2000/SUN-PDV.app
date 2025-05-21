package com.sunpdv.telas;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
                getClass().getResource("/css/style.css").toExternalForm()
            );
        }
    }

    public void show(Stage stage) {
        // Configuração da logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(130);
        logoView.setPreserveRatio(true);

        VBox logoBox = new VBox(logoView);
        logoBox.setPadding(new Insets(20));
        logoBox.setAlignment(Pos.TOP_LEFT);

        // Configuração dos botões
        Button btnNovaVenda = new Button("Nova Venda");
        Button btnProdutos = new Button("Gerenciar Produtos");
        Button btnUsuarios = new Button("Gerenciar Usuários");
        Button btnConfigurar = new Button("Configurações");
        Button btnVoltarHome = new Button("Voltar a Home");
        Button btnSair = new Button("Sair do Sistema");

        double larguraPadrao = 250;
        btnNovaVenda.setPrefWidth(larguraPadrao);
        btnProdutos.setPrefWidth(larguraPadrao);
        btnUsuarios.setPrefWidth(larguraPadrao);
        btnConfigurar.setPrefWidth(larguraPadrao);
        btnVoltarHome.setPrefWidth(larguraPadrao);
        btnSair.setPrefWidth(larguraPadrao);

        btnNovaVenda.setOnAction(e -> {
            // Lógica para nova venda
            System.out.println("Nova venda iniciada");
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

        // Layout dos botões
        VBox botoesBox = new VBox(15, btnNovaVenda, btnProdutos, btnUsuarios, btnConfigurar, btnVoltarHome, btnSair);
        botoesBox.setPadding(new Insets(40));
        botoesBox.setAlignment(Pos.BOTTOM_RIGHT);

        // Layout principal
        StackPane principal = new StackPane();
        principal.getChildren().addAll(logoBox, botoesBox);
        StackPane.setAlignment(logoBox, Pos.TOP_LEFT);
        StackPane.setAlignment(botoesBox, Pos.CENTER);

        // Configuração da cena
        Scene scene = new Scene(principal, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Configuração da janela
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Caixa");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }
}