package com.sunpdv.telas;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TelaHomeMOD extends Application {
    
    @Override 
    public void start(Stage stage) {
        
        // Botões do menu
        Button btnVendas = new Button("Vendas");
        Button btnProdutos = new Button("Gerenciar Produtos");
        Button btnUsuarios = new Button("Gerenciar Usuários");
        Button btnSair = new Button("Sair do Sistema");
        
        // Ações dos botões (exemplo)
        btnSair.setOnAction(e -> stage.close());
        
        // Configuração do layout principal
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(btnVendas, btnProdutos, btnSair);
        
        // Configuração da cena
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Configuração da janela
        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Administrativo");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}