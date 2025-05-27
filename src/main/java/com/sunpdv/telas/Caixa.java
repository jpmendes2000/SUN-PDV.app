package com.sunpdv.telas;

import com.sunpdv.AutenticarUser;
import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;

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

    // Classe interna para criar um Alert de confirmação com estilo CSS
    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            // Adiciona o CSS ao Alert
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
        }
    }

    // Método principal que exibe a tela Caixa
    public void show(Stage stage) {

        // Carrega o logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(130);
        logoView.setPreserveRatio(true);

        // Caixa do logo alinhada ao topo
        VBox logoBox = new VBox(logoView);
        logoBox.setPadding(new Insets(20));
        logoBox.setAlignment(Pos.TOP_LEFT);

        // Botões da tela
        Button btnNovaVenda = new Button("Nova Venda");
        Button btnVoltarHome = new Button("Home");
        Button btnSair = new Button("Sair do Sistema");

        // Define largura padrão para os botões
        double larguraPadrao = 250;
        btnNovaVenda.setPrefWidth(larguraPadrao);
        btnVoltarHome.setPrefWidth(larguraPadrao);
        btnSair.setPrefWidth(larguraPadrao);

        // Ação de iniciar nova venda
        btnNovaVenda.setOnAction(e -> {
            System.out.println("Nova venda iniciada");
        });

        // Ação de voltar à tela principal (ADM, MOD ou FUN)
        btnVoltarHome.setOnAction(e -> {
            try {
                String cargo = AutenticarUser.getCargo();

                // Direciona para a tela conforme o cargo
                switch (cargo) {
                    case "Administrador":
                        new TelaHomeADM(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                        break;
                    case "Moderador":
                        new TelaHomeMOD(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                        break;
                    case "Funcionario":
                        new TelaHomeFUN(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                        break;
                    default:
                        System.out.println("Cargo não reconhecido: " + cargo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Erro ao retornar para a tela principal.");
                alert.showAndWait();
            }
        });

        // Ação de sair do sistema
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

        // Caixa com os botões alinhada no centro inferior
        VBox botoesBox = new VBox(15, btnNovaVenda, btnVoltarHome, btnSair);
        botoesBox.setPadding(new Insets(40));
        botoesBox.setAlignment(Pos.BOTTOM_LEFT);

        // Layout principal com logo e botões
        StackPane principal = new StackPane();
        principal.getChildren().addAll(logoBox, botoesBox);
        StackPane.setAlignment(logoBox, Pos.TOP_LEFT);
        StackPane.setAlignment(botoesBox, Pos.CENTER);

        // Cena com tamanho padrão e CSS
        Scene scene = new Scene(principal, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Configura o Stage principal
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Caixa");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }
}
