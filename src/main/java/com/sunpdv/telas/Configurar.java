package com.sunpdv.telas;

import com.sunpdv.AutenticarUser;
import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class Configurar {
    // Alerta personalizado com CSS para confirmação
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

    // Criação de botão lateral com ícone e efeito de hover com barra amarela
    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            StackPane indicatorContainer = new StackPane(); // barra amarela
            indicatorContainer.setMinWidth(3);
            indicatorContainer.setMaxWidth(3);
            indicatorContainer.setMinHeight(30);
            indicatorContainer.setMaxHeight(30);
            indicatorContainer.setStyle("-fx-background-color: transparent;");

            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);

            HBox content = new HBox(leftContent, new Region(), indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

            Button btn = new Button();
            btn.setGraphic(content);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            // Efeito hover (barra amarela e fundo)
            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18));");
                indicatorContainer.setStyle("-fx-background-color: rgba(255, 204, 0, 0.64);");
            }); 
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });

            return btn;
        } catch (Exception e) {
            return new Button(texto);
        }
    }

    public void show(Stage stage) {
        // --- Barra lateral (Menu esquerdo) ---
        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        // Logo SUN PDV no topo do menu lateral
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        logoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        Label titulonaABA = new Label("Configurações");
        titulonaABA.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        // Botões Home e Sair com ícones
        Button btnVoltarHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        VBox buttonBox = new VBox(10, btnVoltarHome, btnSair);
        buttonBox.setAlignment(Pos.TOP_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        leftMenu.getChildren().addAll(logoBox, new Region(), buttonBox);
        VBox.setVgrow(leftMenu.getChildren().get(1), Priority.ALWAYS); // empurra os botões para baixo

        // --- Área central (Configurações da logo) ---

        Label titulo = new Label("Configuração de Logo");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Botões "Selecionar logo" e "Remover logo"
        Button btnSelecionarLogo = new Button("Selecionar");
        Button btnRemoverLogo = new Button("Tirar");
        btnSelecionarLogo.setPrefSize(100, 30);
        btnSelecionarLogo.getStyleClass().add("BotaoConfig");
        btnRemoverLogo.setPrefSize(100, 30);
        btnRemoverLogo.getStyleClass().add("BotaoConfig");

        VBox botoesLogo = new VBox();
        botoesLogo.setSpacing(7);
        botoesLogo.setPadding(new Insets(0, 0, 0, 0));
        VBox.setMargin(titulo, new Insets(0, 50, 10, 50));
        botoesLogo.getChildren().addAll(titulo, btnSelecionarLogo, btnRemoverLogo);

        // Configuração do retângulo com a imagem ou texto "Sem logo"
        ImageView imageLogo = new ImageView();
        imageLogo.setFitWidth(100);
        imageLogo.setPreserveRatio(true);

        Label semLogoLabel = new Label("Sem logo");
        semLogoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(150, 100);
        imageContainer.setStyle("-fx-background-color: #D3D3D3; -fx-border-color: #A9A9A9; -fx-border-width: 2;");
        imageContainer.setAlignment(Pos.CENTER);

        File fileLogo = new File("logo_empresa.png");
        if (fileLogo.exists()) {
            imageLogo.setImage(new Image(fileLogo.toURI().toString()));
            semLogoLabel.setVisible(false);
        } else {
            imageLogo.setImage(null);
            semLogoLabel.setVisible(true);
        }

        imageContainer.getChildren().addAll(imageLogo, semLogoLabel);

        VBox imagemBox = new VBox(imageContainer);
        imagemBox.setPadding(new Insets(50, 600, 20, 0));

        // Organiza botões à esquerda e imagem à direita
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Expande o spacer para ocupar o espaço disponível
        HBox confgLogo = new HBox(botoesLogo, spacer, imagemBox);
        confgLogo.setPadding(new Insets(20, 0, 30, 0));
        confgLogo.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(botoesLogo, new Insets(0, 0, 0, 50)); // Mantém a margem para mover os botões
        HBox.setMargin(imagemBox, new Insets(0, 0, 0, 2)); // Margem mínima para a imagem

        // Layout principal
        BorderPane root = new BorderPane();
        root.setLeft(leftMenu);
        root.setCenter(confgLogo);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Configurações");
        stage.setFullScreen(true);
        stage.show();

        // --- Ações dos botões de configuração de logo ---

        // Botão Selecionar logo
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

                    imageLogo.setImage(new Image(selectedFile.toURI().toString()));
                    semLogoLabel.setVisible(false);
                    mostrarAlerta("Logo atualizada", "Logo da empresa atualizada com sucesso!", AlertType.INFORMATION);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostrarAlerta("Erro", "Erro ao salvar imagem.", AlertType.ERROR);
                }
            }
        });

        // Botão Remover logo
        btnRemoverLogo.setOnAction(e -> {
            File logoFile = new File("logo_empresa.png");
            if (logoFile.exists() && logoFile.delete()) {
                imageLogo.setImage(null);
                semLogoLabel.setVisible(true);
                mostrarAlerta("Logo removida", "Logo da empresa foi removida com sucesso!", AlertType.INFORMATION);
            } else {
                imageLogo.setImage(null);
                semLogoLabel.setVisible(true);
                mostrarAlerta("Erro", "Erro ao remover logo ou logo não encontrada.", AlertType.ERROR);
            }
        });

        // Botão Home: retorna para a tela inicial de acordo com o cargo
        btnVoltarHome.setOnAction(e -> {
            try {
                String Cargo = AutenticarUser.getCargo();
                switch (Cargo) {
                    case "Administrador":
                        new TelaHomeADM(AutenticarUser.getNome(), Cargo).mostrar(stage);
                        break;
                    case "Moderador":
                        new TelaHomeMOD(AutenticarUser.getNome(), Cargo).mostrar(stage);
                        break;
                    case "Funcionário":
                        new TelaHomeFUN(AutenticarUser.getNome(), Cargo).mostrar(stage);
                        break;
                    default:
                        System.out.println("Cargo não reconhecido: " + Cargo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Erro", "Erro ao retornar para a tela principal.", AlertType.ERROR);
            }
        });

        // Botão Sair
        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Sair", "Deseja realmente sair do sistema?", "");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) stage.close();
            });
        });
    }

    // Função de alerta genérica
    private void mostrarAlerta(String titulo, String mensagem, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}