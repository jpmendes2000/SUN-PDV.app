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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Configurar {

    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                getClass().getResource("/img/css/style.css").toExternalForm()
            );
        }
    }

    public void show(Stage stage) {
        // LOGO superior
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(130);
        logoView.setPreserveRatio(true);

        VBox logoBox = new VBox(logoView);
        logoBox.setPadding(new Insets(20));
        logoBox.setAlignment(Pos.TOP_LEFT);

        // BOTÕES principais
        Button btnVoltarHome = new Button("Home");
        Button btnSair = new Button("Sair do Sistema");
        Button btnSelecionarLogo = new Button("Selecionar logo da empresa");

        btnVoltarHome.setPrefWidth(250);
        btnSair.setPrefWidth(250);
        btnSelecionarLogo.setPrefWidth(250);

        // AÇÃO: Selecionar logo
        btnSelecionarLogo.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar Imagem de Logo");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
            );

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                try (FileInputStream fis = new FileInputStream(selectedFile);
                     FileOutputStream fos = new FileOutputStream("logo_empresa.png")) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, bytesRead);
                    }

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Logo atualizada");
                    alert.setHeaderText(null);
                    alert.setContentText("Logo da empresa atualizada com sucesso!");
                    alert.showAndWait();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("Erro ao salvar imagem.");
                    alert.showAndWait();
                }
            }
        });

        // AÇÃO: Voltar para a tela principal
        btnVoltarHome.setOnAction(e -> {
            try {
                String cargo = AutenticarUser.getCargo();
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

        // AÇÃO: Sair
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

        VBox botoesBox = new VBox(15, btnVoltarHome, btnSair, btnSelecionarLogo);
        botoesBox.setPadding(new Insets(40));
        botoesBox.setAlignment(Pos.CENTER_LEFT);

        StackPane principal = new StackPane();
        principal.getChildren().addAll(logoBox, botoesBox);
        StackPane.setAlignment(logoBox, Pos.TOP_LEFT);
        StackPane.setAlignment(botoesBox, Pos.CENTER_LEFT);

        Scene scene = new Scene(principal, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Configurações");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }
}
