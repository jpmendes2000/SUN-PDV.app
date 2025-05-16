package com.sunpdv.telas;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TelaHomeADM extends Application{
    
    @Override 
    public void start(Stage stage) {
        
        VBox root = new VBox(15);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(20));
            
        Scene scene = new Scene(root, 540, 370);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Login - SUN PDV");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
