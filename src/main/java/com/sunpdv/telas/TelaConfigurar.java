package com.sunpdv.telas;

import com.sunpdv.AutenticarUser;
import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class TelaConfigurar {

    public void show(Stage stage) {
        // Sidebar
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: #00536d;");

        // Logo SUN PDV na sidebar
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/img/logo/logo.png")));
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        VBox logoBox = new VBox(logoView);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        // Botões com estilo igual ao da TelaHomeADM
        Button btnHome = criarBotaoLateral("Home", "/img/icon/inicio.png");
        Button btnVendas = criarBotaoLateral("Vendas", "/img/icon/carrinho-de-compras.png");
        Button btnProdutos = criarBotaoLateral("Gerenciar Produtos", "/img/icon/lista.png");
        Button btnUsuarios = criarBotaoLateral("Gerenciar Usuários", "/img/icon/grupo.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        // AÇÃO: Voltar para a tela principal
        btnHome.setOnAction(e -> {
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
        btnSair.setOnAction(e -> stage.close());

        VBox buttonBox = new VBox(10, btnHome, btnVendas, btnProdutos, btnUsuarios, btnSair);
        buttonBox.setAlignment(Pos.TOP_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));
        sidebar.getChildren().addAll(logoBox, new Region(), buttonBox);
        VBox.setVgrow(sidebar.getChildren().get(1), Priority.ALWAYS);

        // Painel de configurações principal
        StackPane centro = new StackPane();
        centro.setPadding(new Insets(30));

        Label lblCentral = new Label("Selecione uma opção de configuração à direita.");
        lblCentral.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
        centro.getChildren().add(lblCentral);

        // Lista de opções à direita
        VBox listaConfigs = new VBox(20);
        listaConfigs.setPadding(new Insets(30));
        listaConfigs.setPrefWidth(300);
        listaConfigs.setStyle("-fx-background-color: #f0f0f0;");

        Button btnAlterarLogo = new Button("Alterar logo da empresa");
        Button btnRemoverLogo = new Button("Remover logo da empresa");
        btnAlterarLogo.setMaxWidth(Double.MAX_VALUE);
        btnRemoverLogo.setMaxWidth(Double.MAX_VALUE);

        btnAlterarLogo.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Imagem comercial");
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

        btnRemoverLogo.setOnAction(e -> {
            File file = new File("logo_empresa.png");
            if (file.exists() && file.delete()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Logo removida");
                alert.setHeaderText(null);
                alert.setContentText("Logo da empresa removida com sucesso!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Atenção");
                alert.setHeaderText(null);
                alert.setContentText("Nenhuma logo encontrada para remover.");
                alert.showAndWait();
            }
        });

        listaConfigs.getChildren().addAll(new Label("Configurações"), btnAlterarLogo, btnRemoverLogo);

        BorderPane layout = new BorderPane();
        layout.setLeft(sidebar);
        layout.setCenter(centro);
        layout.setRight(listaConfigs);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Configurações");
        stage.setMaximized(true);
        stage.show();
    }

    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            StackPane indicator = new StackPane();
            indicator.setMinWidth(3);
            indicator.setMaxWidth(3);
            indicator.setStyle("-fx-background-color: transparent;");

            HBox leftContent = new HBox(10, icon, textLabel);
            HBox content = new HBox(leftContent, new Region(), indicator);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

            Button btn = new Button();
            btn.setGraphic(content);
            btn.setStyle("-fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18));");
                indicator.setStyle("-fx-background-color: rgba(255, 204, 0, 0.64);");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent;");
                indicator.setStyle("-fx-background-color: transparent;");
            });
            return btn;
        } catch (Exception e) {
            return new Button(texto);
        }
    }
}
