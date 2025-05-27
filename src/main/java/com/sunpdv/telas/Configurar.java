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

public class Configurar {

    /**
     * Classe interna para criar uma caixa de confirmação customizada
     * com estilo CSS aplicado.
     */
    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            
            // Aplica o estilo CSS à janela de diálogo
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
        }
    }

    /**
     * Método principal que exibe a tela de Configurações.
     */
    public void show(Stage stage) {
        // Configuração da logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(130); // Define a largura
        logoView.setPreserveRatio(true); // Mantém a proporção da imagem

        // Coloca a logo em um VBox para alinhar
        VBox logoBox = new VBox(logoView);
        logoBox.setPadding(new Insets(20));
        logoBox.setAlignment(Pos.TOP_LEFT);

        // Botões principais
        Button btnVoltarHome = new Button("Home");
        Button btnSair = new Button("Sair do Sistema");

        // Define largura padrão para os botões
        double larguraPadrao = 250;
        btnVoltarHome.setPrefWidth(larguraPadrao);
        btnSair.setPrefWidth(larguraPadrao);

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
                    case "Funcionário":
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

        // Ação do botão Sair do Sistema
        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(
                stage,
                "Confirmação de Saída",
                "Deseja realmente sair do sistema?",
                ""
            );

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close(); // Fecha o aplicativo
                }
            });
        });

        // Layout dos botões
        VBox botoesBox = new VBox(15, btnVoltarHome, btnSair);
        botoesBox.setPadding(new Insets(40));
        botoesBox.setAlignment(Pos.BOTTOM_LEFT);

        // Layout principal
        StackPane principal = new StackPane();
        principal.getChildren().addAll(logoBox, botoesBox);
        StackPane.setAlignment(logoBox, Pos.TOP_LEFT);
        StackPane.setAlignment(botoesBox, Pos.CENTER);

        // Configuração da cena
        Scene scene = new Scene(principal, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Configuração da janela
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Caixa");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }
}
