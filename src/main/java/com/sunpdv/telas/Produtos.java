package com.sunpdv.telas;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Produtos {
    public void show(Stage stage) {
        VBox root = new VBox();
        // Adicione componentes da tela de produtos aqui
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Gerenciamento de Produtos");
        stage.show();
    }
}