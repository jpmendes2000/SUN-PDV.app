package com.sunpdv.telas;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Usuarios {
    public void show(Stage stage) {
        VBox root = new VBox();
        // Adicione componentes da tela de usuários aqui
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Gerenciamento de Usuários");
        stage.show();
    }
}