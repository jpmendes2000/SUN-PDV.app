package com.sunpdv.telas.operacao;

import com.sunpdv.model.AutenticarUser;
import com.sunpdv.model.TaxaPagamentoService;
import com.sunpdv.telas.home.TelaHomeADM;
import com.sunpdv.telas.home.TelaHomeFUN;
import com.sunpdv.telas.home.TelaHomeMOD;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Configurar {

    // Classe interna para criar alertas personalizados com estilo CSS
    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        }
    }

    // Método para criar botões laterais com ícone e efeito de hover
    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            // Carrega a imagem do ícone
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            // Configura o texto do botão com estilo
            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            // Cria o contêiner para a barra indicadora amarela
            StackPane indicatorContainer = new StackPane();
            indicatorContainer.setMinWidth(3);
            indicatorContainer.setMaxWidth(3);
            indicatorContainer.setMinHeight(30);
            indicatorContainer.setMaxHeight(30);
            indicatorContainer.setStyle("-fx-background-color: transparent;");

            // Alinha o ícone e o texto à esquerda
            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);

            // Monta o conteúdo do botão com espaçamento
            HBox content = new HBox(leftContent, new Region(), indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

            // Configura o botão
            Button btn = new Button();
            btn.setGraphic(content);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            // Efeito de hover
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
        // Cria a barra lateral (menu esquerdo)
        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        // Configura o logo SUN PDV no topo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        logoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        Label titulonaABA = new Label("Configurações");
        titulonaABA.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(10, logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 5, 0));

        // Labels para hora e data
        Label horaLabel = new Label();
        horaLabel.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 16px; -fx-font-weight: bold;");
        horaLabel.setAlignment(Pos.CENTER);
        horaLabel.setMaxWidth(Double.MAX_VALUE);

        Label dataLabel = new Label();
        dataLabel.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 14px; -fx-font-weight: bold;");
        dataLabel.setAlignment(Pos.CENTER);
        dataLabel.setMaxWidth(Double.MAX_VALUE);

        // VBox para organizar hora acima da data
        VBox dataHoraBox = new VBox(5, horaLabel, dataLabel);
        dataHoraBox.setAlignment(Pos.CENTER);
        dataHoraBox.setPadding(new Insets(0, 0, 5, 0));

        // Formatadores para hora e data
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Definir texto inicial
        LocalDateTime now = LocalDateTime.now();
        horaLabel.setText(now.format(horaFormatter));
        dataLabel.setText(now.format(dataFormatter));

        // Atualizar hora e data a cada segundo
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            horaLabel.setText(currentTime.format(horaFormatter));
            dataLabel.setText(currentTime.format(dataFormatter));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Espaço para empurrar os botões para baixo
        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);

        // Adiciona botões ao menu
        Button btnVoltarHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        VBox buttonBox = new VBox(10, btnVoltarHome, btnSair);
        buttonBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        // Adicionar elementos ao menu lateral, com dataHoraBox abaixo do logoBox
        leftMenu.getChildren().addAll(logoBox, dataHoraBox, espaco, buttonBox);

        // Configura o contêiner da imagem (logo)
        ImageView imageLogo = new ImageView();
        imageLogo.setFitWidth(100);
        imageLogo.setPreserveRatio(true);

        Label semLogoLabel = new Label("Sem logo");
        semLogoLabel.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 14px;");

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(150, 100);
        imageContainer.setStyle("-fx-background-color: transparent; -fx-border-color: #012d5c; -fx-border-width: 2; -fx-border-radius: 4px;");
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

        // Configuração de Logo
        Label titulo = new Label("Configuração de Logo");
        titulo.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnSelecionarLogo = new Button("Selecionar");
        Button btnRemoverLogo = new Button("Tirar");
        btnSelecionarLogo.setPrefSize(100, 30);
        btnSelecionarLogo.getStyleClass().add("BotaoConfig");
        btnRemoverLogo.setPrefSize(100, 30);
        btnRemoverLogo.getStyleClass().add("BotaoConfig");

        VBox botoesStack = new VBox(10, btnSelecionarLogo, btnRemoverLogo);
        botoesStack.setAlignment(Pos.CENTER);

        HBox botoesLogo = new HBox(10, botoesStack, imageContainer);
        botoesLogo.setAlignment(Pos.CENTER_LEFT);

        // Configuração de Taxas de Pagamento
        Label tituloTaxas = new Label("Configuração de Taxas de Pagamento");
        tituloTaxas.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Taxa para Cartão de Crédito
        Label lblCredito = new Label("Taxa Crédito");
        lblCredito.setStyle("-fx-text-fill: #a9cce3;");
        TextField txtCredito = new TextField();
        txtCredito.setPrefWidth(50);
        txtCredito.setPromptText("0.0");
        
        // Taxa para Cartão de Débito
        Label lblDebito = new Label("Taxa Débito ");
        lblDebito.setStyle("-fx-text-fill: #a9cce3;");
        TextField txtDebito = new TextField();
        txtDebito.setPrefWidth(50);
        txtDebito.setPromptText("0.0");
        
        Button btnSalvarTaxas = new Button("Salvar Taxas");
        btnSalvarTaxas.getStyleClass().add("BotaoConfig");

        HBox creditoBox = new HBox(10, lblCredito, txtCredito);
        creditoBox.setAlignment(Pos.CENTER_LEFT);
        HBox debitoBox = new HBox(10, lblDebito, txtDebito);
        debitoBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox taxasBox = new VBox(10, creditoBox, debitoBox, btnSalvarTaxas);
        taxasBox.setAlignment(Pos.CENTER_LEFT);
        taxasBox.setPadding(new Insets(10, 0, 0, 0));

        // Carrega as taxas atuais do banco de dados
        try {
            TaxaPagamentoService taxaService = new TaxaPagamentoService();
            double taxaCredito = taxaService.obterTaxa("Crédito");
            double taxaDebito = taxaService.obterTaxa("Débito");
            
            txtCredito.setText(String.valueOf(taxaCredito));
            txtDebito.setText(String.valueOf(taxaDebito));
        } catch (SQLException e) {
            mostrarAlerta("Erro", "Não foi possível carregar as taxas atuais.", AlertType.ERROR);
        }

        // Layout principal das configurações
        VBox configLayout = new VBox(20, 
            new VBox(10, titulo, botoesLogo),
            new VBox(10, tituloTaxas, taxasBox)
        );
        configLayout.setAlignment(Pos.TOP_LEFT);
        configLayout.setPadding(new Insets(20, 0, 0, 30));

        // Layout raiz
        BorderPane layout = new BorderPane();
        layout.setLeft(leftMenu);
        layout.setCenter(configLayout);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                if (!btnSair.isDisabled()) {
                    btnSair.fire();
                }
                event.consume();
            }
        });

        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);   
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Configurações");
        stage.setFullScreen(true);
        stage.show();

        // Ações dos botões
        btnSelecionarLogo.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecionar Imagem de Logo");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));

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

        // Ação para salvar as taxas
        btnSalvarTaxas.setOnAction(e -> {
            try {
                double credito = Double.parseDouble(txtCredito.getText());
                double debito = Double.parseDouble(txtDebito.getText());
                
                TaxaPagamentoService service = new TaxaPagamentoService();
                service.salvarTaxa("Crédito", credito);
                service.salvarTaxa("Débito", debito);
                
                mostrarAlerta("Sucesso", "Taxas atualizadas com sucesso!", AlertType.INFORMATION);
            } catch (NumberFormatException ex) {
                mostrarAlerta("Erro", "Por favor, insira valores numéricos válidos.", AlertType.ERROR);
            } catch (Exception ex) {
                mostrarAlerta("Erro", "Não foi possível salvar as taxas.", AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        btnVoltarHome.setOnAction(e -> {
            try {
                String cargo = AutenticarUser.getCargo();
                switch (cargo) {
                    case "Administrador":
                        new TelaHomeADM(AutenticarUser.getNome(), cargo).mostrar(stage);
                        break;
                    case "Moderador":
                        new TelaHomeMOD(AutenticarUser.getNome(), cargo).mostrar(stage);
                        break;
                    case "Funcionário":
                        new TelaHomeFUN(AutenticarUser.getNome(), cargo).mostrar(stage);
                        break;
                    default:
                        System.out.println("Cargo não reconhecido: " + cargo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Erro", "Erro ao retornar para a tela principal.", AlertType.ERROR);
            }
        });

        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Confirmação", "Deseja sair?", "Isso fechará o sistema.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });
        btnSair.setDisable(false); // Garante que o botão está habilitado
    }

    // Método para exibir alertas
    private void mostrarAlerta(String titulo, String mensagem, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}