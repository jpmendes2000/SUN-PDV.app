package com.sunpdv.telas;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TelaHomeFUN extends Application {
    
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
        VBox root = new VBox(10);
        root.setAlignment(Pos.BOTTOM_RIGHT);
        root.setPadding(new Insets(45));
        root.getChildren().addAll(btnVendas, btnSair);
        
        // Configuração da cena
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Configuração da janela
        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Administrativo");
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}