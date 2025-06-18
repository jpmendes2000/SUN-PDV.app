package com.sunpdv.telas;

import com.sunpdv.AutenticarUser;
import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Usuarios {

    private Stage stage;

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
                getClass().getResource("/img/css/style.css").toExternalForm()
            );
        }
    }

    public Usuarios() {}

    public void show(Stage stage) {
        this.stage = stage;

        // Maximiza janela na tela principal
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // Logo do sistema no topo esquerdo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        VBox topBox = new VBox(logoView);
        topBox.setPadding(new Insets(20));
        topBox.setAlignment(Pos.TOP_LEFT);

        // Título da tela
        // Pode colocar ou usar conforme seu CSS:
        // Label titulo = new Label("Gerenciamento de Usuários");
        // titulo.getStyleClass().add("titulo-tela");

        String nome = AutenticarUser.getNome() != null ? AutenticarUser.getNome() : "Usuário";
        String cargo = AutenticarUser.getCargo() != null ? AutenticarUser.getCargo() : "Cargo";

        // Botões
        Button btnHome = new Button("Home");
        Button btnSair = new Button("Sair do Sistema");
        double larguraBotao = 200;
        btnHome.setPrefWidth(larguraBotao);
        btnSair.setPrefWidth(larguraBotao);

         // AÇÃO: Voltar para a tela principal
        btnHome.setOnAction(e -> {
            try {
                String Cargo = AutenticarUser.getCargo();
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

        // Ação do botão Sair
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

        // VBox com os botões empilhados verticalmente no canto inferior direito
        VBox botoesBox = new VBox(15, btnHome, btnSair);
        botoesBox.setPadding(new Insets(35));
        botoesBox.setAlignment(Pos.BOTTOM_LEFT);

        // Layout principal usando StackPane para posicionar logo e botões
        StackPane root = new StackPane();
        root.getChildren().addAll(topBox, botoesBox);

        StackPane.setAlignment(topBox, Pos.TOP_LEFT);
        StackPane.setAlignment(botoesBox, Pos.BOTTOM_RIGHT);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Gerenciamento de Usuários");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }
}
