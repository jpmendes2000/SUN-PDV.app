package com.sunpdv.home;

import com.sunpdv.AutenticarUser;
import com.sunpdv.telas.Caixa;
import com.sunpdv.telas.Configurar;
import com.sunpdv.telas.Produtos;
import com.sunpdv.telas.Usuarios;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class TelaHomeADM {

    private String nome;
    private String cargo;

    // Construtor recebendo nome e cargo do usuário
    public TelaHomeADM(String nome, String cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

    // Classe interna para exibir alertas de confirmação com estilo personalizado
    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);

            // Aplica a folha de estilos CSS
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
        }
    }

    // Método para exibir a tela principal do ADM
    public void mostrar(Stage stage) {

        // Define a janela para maximizar na tela do usuário
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // Carrega o logo da aplicação
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);

        VBox topBox = new VBox(10, logoView);
        topBox.setPadding(new Insets(20));
        topBox.setAlignment(Pos.TOP_LEFT);

        // Botões disponíveis para o ADM
        Button btnVendas = new Button("Vendas");
        Button btnProdutos = new Button("Gerenciar Produtos");
        Button btnUsuarios = new Button("Gerenciar Usuários");
        Button btnConfigurar = new Button("Configurações");
        Button btnSair = new Button("Sair do Sistema");

        // Define largura padrão para todos os botões
        double larguraPadrao = 250;
        for (Button btn : new Button[]{btnVendas, btnProdutos, btnUsuarios, btnConfigurar, btnSair}) {
            btn.setPrefWidth(larguraPadrao);
        }

        // Define as ações dos botões
        btnVendas.setOnAction(e -> new Caixa().show(stage));
        btnProdutos.setOnAction(e -> new Produtos().show(stage));
        btnUsuarios.setOnAction(e -> new Usuarios().show(stage));
        btnConfigurar.setOnAction(e -> new Configurar().show(stage));

        // Botão sair exibe confirmação antes de fechar
        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(
                stage,
                "Confirmação de Saída",
                "Deseja realmente sair do sistema?",
                ""
            );
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    AutenticarUser.limparDados();  // Limpa dados do usuário ao sair
                    stage.close();
                }
            });
        });

        // Caixa vertical para agrupar os botões
        VBox botoesBox = new VBox(15, btnVendas, btnProdutos, btnUsuarios, btnConfigurar, btnSair);
        botoesBox.setAlignment(Pos.BOTTOM_RIGHT);
        botoesBox.setPadding(new Insets(40));

        // Label de boas-vindas com nome e cargo do usuário
        Label mensagemFixa = new Label("Bem-vindo(a), " + nome + " você é " + cargo);
        mensagemFixa.getStyleClass().add("mensagem-bemvindo");

        // Espaçador flexível para empurrar o conteúdo para os lados corretos
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Barra inferior com mensagem e botões
        HBox bottomBox = new HBox(20, mensagemFixa, spacer, botoesBox);
        bottomBox.setPadding(new Insets(0, 15, 10, 30));
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);

        // Layout principal com bordas
        BorderPane layout = new BorderPane();
        layout.setTop(topBox);
        layout.setBottom(bottomBox);

        // Cria cena com o layout e aplica o CSS
        Scene scene = new Scene(layout, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Aplica a cena no palco e configura a janela
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Painel Administrativo");
        stage.setResizable(true);
        stage.show();
    }
}
