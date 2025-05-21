package com.sunpdv.telas;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Caixa {
    public void show(Stage stage) {
        VBox root = new VBox();
        // Adicione componentes da tela de caixa aqui
        Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Caixa");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }
}