package com.sunpdv.home;

import com.sunpdv.telas.Caixa;
import com.sunpdv.telas.Configurar;
import com.sunpdv.telas.Produtos;
import com.sunpdv.telas.Usuarios;

import javafx.application.Application;
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

public class TelaHomeADM extends Application {

    // Classe interna para o Alert customizado
    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            
            // Aplica o CSS
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
        }
    }

    @Override
    public void start(Stage primaryStage) {
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

        // Ação do botão Vendas
        btnVendas.setOnAction(e -> {
            primaryStage.close();
            new Caixa().show(new Stage());
        });

        btnProdutos.setOnAction(e -> {
            primaryStage.close();
            new Produtos().show(new Stage());
        });

        btnUsuarios.setOnAction(e -> {
            primaryStage.close();
            new Usuarios().show(new Stage());
        });

        btnConfigurar.setOnAction(e -> {
            primaryStage.close();
            new Configurar().show(new Stage());
        });

        // Ação do botão Sair
        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(
                primaryStage,
                "Confirmação de Saída",
                "Deseja realmente sair do sistema?",
                ""
            );
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    primaryStage.close();
                }
            });
        });

        // VBox com os botões no canto inferior direito
        VBox botoesBox = new VBox(15, btnVendas, btnProdutos, btnUsuarios, btnConfigurar, btnSair);
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
        primaryStage.setScene(scene);
        primaryStage.setTitle("SUN PDV - Painel Administrativo");
        primaryStage.setResizable(true);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}