package com.sunpdv.home;

<<<<<<< HEAD
import com.sunpdv.AutenticarUser;

=======
import com.sunpdv.telas.Caixa;
import com.sunpdv.telas.Configurar;
import com.sunpdv.telas.Produtos;
import com.sunpdv.telas.Usuarios;

import javafx.application.Application;
>>>>>>> main
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
<<<<<<< HEAD
import javafx.scene.control.Label;
=======
import javafx.scene.control.ButtonType;
>>>>>>> main
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TelaHomeADM {

<<<<<<< HEAD
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
=======
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
>>>>>>> main
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

<<<<<<< HEAD
        btnSair.setOnAction(e -> {
        AutenticarUser.limparDados(); // Limpa os dados de autenticação
        stage.close(); // Fecha a janela atual
        });

        VBox botoesBox = new VBox(15, btnVendas, btnProdutos, btnUsuarios, btnSair);
=======
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
>>>>>>> main
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

<<<<<<< HEAD
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Administrativo");
        stage.setResizable(true);
        stage.setFullScreen(true);
        stage.show();
    }
}
=======
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
>>>>>>> main
