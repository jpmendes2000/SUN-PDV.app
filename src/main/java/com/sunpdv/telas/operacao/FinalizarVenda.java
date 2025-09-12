package com.sunpdv.telas.operacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;

import com.sunpdv.connection.ConexaoDB;
import com.sunpdv.model.AutenticarUser;
// Importar a nova classe CupomFiscal
import com.sunpdv.model.CupomFiscal;

public class FinalizarVenda {

    private String documento;
    private String tipoDocumento;
    private List<Caixa.ItemVenda> itens;
    private double totalVenda;
    private double Subtotal;
    private Stage stage;
    private Caixa caixa;
    private List<Pagamento> pagamentos = new ArrayList<>();
    private Label totalRestanteLabel;
    private VBox pagamentosListaBox;
    private Button btnFinalizar;
    private CheckBox imprimirCupomCaixa;
    
    // Instância da classe CupomFiscal
    private CupomFiscal cupomFiscal;

    // Constantes de cores
    private static final String COR_FUNDO_PRINCIPAL = "#00435a";
    private static final String COR_FUNDO_SECUNDARIO = "#686de0";
    private static final String COR_AZUL_CLARO = "#00536d";
    private static final String COR_AMARELO = "#f39c12";
    private static final String COR_VERDE = "#27ae60";
    private static final String COR_VERMELHO = "#e74c3c";

    // representa um pagamento adicionado
    public FinalizarVenda(String documento, String tipoDocumento, List<Caixa.ItemVenda> itens, double totalVenda, Caixa caixa) {
        try {
            this.documento = documento;
            this.tipoDocumento = tipoDocumento;
            this.itens = new ArrayList<>(itens);
            this.totalVenda = totalVenda;
            this.caixa = caixa;
            this.cupomFiscal = new CupomFiscal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mostrar(Stage owner, Caixa caixa) {
        this.caixa = caixa;
        try {
            System.out.println("Iniciando mostrar FinalizarVenda..."); // Debug: início

            stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            stage.setTitle("SUN PDV - Finalizar Venda");


            BorderPane layout = new BorderPane();
            aplicarGradienteFundo(layout);

            VBox menuLateral = criarMenuLateralEstiloCaixa();

            VBox areaCentral = criarAreaCentral();

            VBox painelDireito = criarPainelDireito();

            layout.setLeft(menuLateral);
            layout.setCenter(areaCentral);
            layout.setRight(painelDireito);

            Scene scene = new Scene(layout, 1720, 780);
            aplicarAnimacaoEntrada(layout);

            try {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            } catch (NullPointerException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.showAndWait();
            }

            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace(); // Printa o stack trace no console
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro ao Abrir Tela");
            if (owner != null) {
                alert.initOwner(owner);
            }
            alert.showAndWait();
        }
    }

    private void aplicarGradienteFundo(BorderPane layout) {
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, null,
                new Stop(0, Color.web(COR_FUNDO_PRINCIPAL)),
                new Stop(1, Color.web(COR_FUNDO_SECUNDARIO))
        );
        Background background = new Background(new BackgroundFill(gradient, null, null));
        layout.setBackground(background);
    }


    private VBox criarMenuLateralEstiloCaixa() {
        VBox menuLateral = new VBox();
        menuLateral.setPrefWidth(280);
        menuLateral.setStyle("-fx-background-color: #00536d;");

        // Logo SUN PDV
        try {
            Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(120);
            logoView.setPreserveRatio(true);

            VBox logoBox = new VBox(logoView);
            logoBox.setAlignment(Pos.CENTER);
            logoBox.setPadding(new Insets(20, 0, 20, 0));
            menuLateral.getChildren().add(logoBox);
        } catch (Exception e) {
            Label logoText = new Label("SUN PDV");
            logoText.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            VBox logoBox = new VBox(logoText);
            logoBox.setAlignment(Pos.CENTER);
            logoBox.setPadding(new Insets(20, 0, 20, 0));
            menuLateral.getChildren().add(logoBox);
        }
        
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

        // Formatadores para hora e data
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Definir texto inicial
        LocalDateTime now = LocalDateTime.now();
        horaLabel.setText(now.format(horaFormatter));
        dataLabel.setText(now.format(dataFormatter));

        // Atualizar hora e data
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            horaLabel.setText(currentTime.format(horaFormatter));
            dataLabel.setText(currentTime.format(dataFormatter));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();        

        menuLateral.getChildren().add(dataHoraBox);

        // Botão Voltar
        Button btnVoltar = criarBotaoLateral("Voltar", "/img/icon/voltar.png");
        btnVoltar.setOnAction(e -> {
            stage.close();
            
            // Restaurar a stage do Caixa de forma assíncrona para evitar problemas de thread UI
            Platform.runLater(() -> {
                if (caixa != null && caixa.stage != null) {
                    if (caixa.stage.isIconified()) {
                        caixa.stage.setIconified(false);
                    }

                    caixa.stage.toFront();
                    caixa.stage.requestFocus();
                    Timeline timelineVoltar = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
                        caixa.stage.setFullScreen(true);
                    }));
                    timelineVoltar.play();
                }
            });
        });

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        
        menuLateral.getChildren().addAll(espaco, btnVoltar);
        menuLateral.setPadding(new Insets(0, 0, 20, 0));

        return menuLateral;
    }

    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            if (img.isError()) {
                throw new Exception("Erro ao carregar imagem: " + caminhoIcone);
            }

            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            StackPane BarraAmarelaBtn = new StackPane(); // barra amarela lateral
            BarraAmarelaBtn.setMinWidth(3);
            BarraAmarelaBtn.setMaxWidth(3);
            BarraAmarelaBtn.setMinHeight(30);
            BarraAmarelaBtn.setMaxHeight(30);
            BarraAmarelaBtn.setStyle("-fx-background-color: transparent;");

            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);

            HBox content = new HBox(leftContent, new Region(), BarraAmarelaBtn);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

            Button btn = new Button();
            btn.setGraphic(content);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18)); -fx-border-radius: 4; -fx-background-radius: 4;");
                BarraAmarelaBtn.setStyle("-fx-background-color: rgba(255, 204, 0, 0.64); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
                BarraAmarelaBtn.setStyle("-fx-background-color: transparent;");
            });

            return btn;

        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            Button btn = new Button(texto);
            btn.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(40);
            return btn;
        }
    }

    private VBox criarAreaCentral() {
        VBox areaCentral = new VBox(20);
        areaCentral.setPadding(new Insets(20));

        Background centralBg = new Background(new BackgroundFill(
                Color.web(COR_AZUL_CLARO, 0.1),
                null,
                null
        ));
        areaCentral.setBackground(centralBg);

        VBox secaoItens = criarSecaoItens();
        VBox secaoPagamento = criarSecaoPagamento();

        areaCentral.getChildren().addAll(secaoItens, secaoPagamento);
        return areaCentral;
    }

    private VBox criarSecaoItens() {
        VBox secao = new VBox(15);

        Label titulo = new Label("Itens da Venda:");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox containerItens = new VBox(5);
        containerItens.setPadding(new Insets(15));
        containerItens.setPrefHeight(600);

        Background itemsBg = new Background(new BackgroundFill(
                Color.web(COR_AZUL_CLARO, 0.8),
                new CornerRadii(10),
                null
        ));
        containerItens.setBackground(itemsBg);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(6);
        shadow.setRadius(15);
        containerItens.setEffect(shadow);

        ScrollPane scrollItens = new ScrollPane();
        VBox listaItens = new VBox(5);

        for (Caixa.ItemVenda item : itens) {
            HBox itemBox = new HBox();
            itemBox.setPadding(new Insets(10, 12, 10, 12));
            itemBox.setAlignment(Pos.CENTER_LEFT);

            Background itemBg = new Background(new BackgroundFill(
                    Color.web("white", 0.1),
                    new CornerRadii(6),
                    null
            ));
            itemBox.setBackground(itemBg);
            itemBox.setStyle("-fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1; -fx-border-radius: 6;");

            Label itemLabel = new Label(String.format("Cód: %s | Qtd: %d | R$ %.2f",
                    item.codigoBarras, item.quantidade, item.preco * item.quantidade));
            itemLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

            itemBox.getChildren().add(itemLabel);
            listaItens.getChildren().add(itemBox);
        }

        scrollItens.setContent(listaItens);
        scrollItens.setFitToWidth(true);
        scrollItens.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        containerItens.getChildren().add(scrollItens);

        VBox.setVgrow(containerItens, Priority.ALWAYS);

        secao.getChildren().addAll(titulo, containerItens);
        return secao;
    }

    private VBox criarSecaoPagamento() {
        VBox secao = new VBox(15);

        Label titulo = new Label("Formas de Pagamento:");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox containerPagamento = new VBox(15);
        containerPagamento.setPadding(new Insets(15));

        Background pagBg = new Background(new BackgroundFill(
                Color.web(COR_AZUL_CLARO, 0.8),
                new CornerRadii(10),
                null
        ));
        containerPagamento.setBackground(pagBg);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(6);
        shadow.setRadius(15);
        containerPagamento.setEffect(shadow);

        HBox opcoesPagamento = new HBox(10);
        ToggleGroup pagamentoGroup = new ToggleGroup();

        RadioButton rdbDinheiro = criarRadioButton("Dinheiro", pagamentoGroup);
        RadioButton rdbDebito = criarRadioButton("Cartão de Débito", pagamentoGroup);
        RadioButton rdbCredito = criarRadioButton("Cartão de Crédito", pagamentoGroup);
        RadioButton rdbPix = criarRadioButton("Pix", pagamentoGroup);
        RadioButton rdbVoucher = criarRadioButton("Voucher", pagamentoGroup);

        opcoesPagamento.getChildren().addAll(rdbDinheiro, rdbDebito, rdbCredito, rdbPix, rdbVoucher);

        HBox inputGroup = new HBox(8);
        inputGroup.setAlignment(Pos.CENTER_LEFT);

        TextField valorField = new TextField();
        valorField.setPromptText("Valor");
        valorField.setPrefWidth(400);
        valorField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 6; -fx-padding: 10; -fx-font-size: 14px;"
        );

        pagamentoGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                double totalPago = pagamentos.stream().mapToDouble(p -> p.valor).sum();
                double restante = Math.max(totalVenda - totalPago, 0);
                valorField.setText(String.format("%.2f", restante).replace(".", ","));
                valorField.setEditable(true);
                
                valorField.selectAll();
                valorField.requestFocus();
            }
        });

        Button btnAdicionar = new Button("Adicionar");
        btnAdicionar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #c09727, #e8b923); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 15 10 15; -fx-text-alignment: center;"
        );
        btnAdicionar.setOnMouseEntered(e -> btnAdicionar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ae8922, #e2b72a); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 15 10 15;"
        ));
        btnAdicionar.setOnMouseExited(e -> btnAdicionar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #c09727, #e8b923); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 15 10 15;"
        ));

        btnAdicionar.setOnAction(e -> {
            RadioButton selected = (RadioButton) pagamentoGroup.getSelectedToggle();
            if (selected != null && !valorField.getText().isEmpty()) {
                try {
                    double valor = Double.parseDouble(valorField.getText().replace(",", "."));
                    if (valor > 0) {
                        adicionarPagamento(selected.getText(), valor);
                        valorField.clear();
                        pagamentoGroup.selectToggle(null);
                    } else {
                        mostrarAlerta("Erro", "O valor deve ser maior que zero.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException ex) {
                    mostrarAlerta("Erro", "Digite um valor numérico válido.", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Atenção", "Selecione a forma de pagamento e digite o valor.", Alert.AlertType.WARNING);
            }
        });

        inputGroup.getChildren().addAll(valorField, btnAdicionar);
        containerPagamento.getChildren().addAll(opcoesPagamento, inputGroup);

        secao.getChildren().addAll(titulo, containerPagamento);
        return secao;
    }

    private RadioButton criarRadioButton(String texto, ToggleGroup group) {
        RadioButton rb = new RadioButton(texto);
        rb.setToggleGroup(group);
        rb.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        rb.setPadding(new Insets(6, 10, 6, 10));

        rb.setOnMouseEntered(e -> {
            if (!rb.isSelected()) {
                rb.setBackground(new Background(new BackgroundFill(
                        Color.web("white", 0.1),
                        new CornerRadii(6),
                        null
                )));
                rb.setPadding(new Insets(6, 10, 6, 10));
            }
        });
        rb.setOnMouseExited(e -> {
            if (!rb.isSelected()) {
                rb.setBackground(Background.EMPTY);
            }
        });

        return rb;
    }

    private VBox criarPainelDireito() {
        VBox painel = new VBox(20);
        painel.setPrefWidth(420);
        painel.setPrefHeight(900);
        painel.setPadding(new Insets(20, 15, 20, 15));

        Background painelBg = new Background(new BackgroundFill(
                Color.web(COR_AZUL_CLARO, 0.1),
                null,
                null
        ));
        painel.setBackground(painelBg);

        Label titulo = new Label("Pagamentos Adicionados:");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox containerLista = new VBox(5);
        containerLista.setPadding(new Insets(15));
        containerLista.setPrefHeight(200);

        Background listaBg = new Background(new BackgroundFill(
                Color.web(COR_AZUL_CLARO, 0.8),
                new CornerRadii(10),
                null
        ));
        containerLista.setBackground(listaBg);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(6);
        shadow.setRadius(15);
        containerLista.setEffect(shadow);

        ScrollPane scrollPagamentos = new ScrollPane();
        pagamentosListaBox = new VBox(5);
        scrollPagamentos.setContent(pagamentosListaBox);
        scrollPagamentos.setFitToWidth(true);
        scrollPagamentos.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        containerLista.getChildren().add(scrollPagamentos);

        totalRestanteLabel = new Label("Valor Restante: R$ " + String.format("%.2f", totalVenda));
        totalRestanteLabel.setStyle(
                "-fx-background-color: " + COR_VERMELHO + "; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 15 10 15;"
        );
        totalRestanteLabel.setAlignment(Pos.CENTER);
        totalRestanteLabel.setMaxWidth(Double.MAX_VALUE);

        // CheckBox para imprimir cupom fiscal
        imprimirCupomCaixa = new CheckBox("Imprimir cupom fiscal");
        imprimirCupomCaixa.setSelected(true); // Por padrão, marcado para imprimir
        imprimirCupomCaixa.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        btnFinalizar = new Button("Faltam R$ " + String.format("%.2f", totalVenda));
        btnFinalizar.setPrefWidth(230);
        btnFinalizar.setDisable(true);
        btnFinalizar.setStyle(
                "-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10;"
        );
        btnFinalizar.setOnAction(e -> finalizarVenda());

        painel.getChildren().addAll(titulo, containerLista, totalRestanteLabel, imprimirCupomCaixa, btnFinalizar);

        VBox.setVgrow(containerLista, Priority.ALWAYS);
        return painel;
    }

    private void adicionarPagamento(String forma, double valor) {
        pagamentos.add(new Pagamento(forma, valor));
        atualizarListaPagamentos();
        atualizarTotalRestante();
    }

    private void atualizarListaPagamentos() {
        pagamentosListaBox.getChildren().clear();

        for (int i = 0; i < pagamentos.size(); i++) {
            final int index = i;
            Pagamento p = pagamentos.get(i);

            HBox itemPagamento = new HBox();
            itemPagamento.setPadding(new Insets(8, 12, 8, 12));
            itemPagamento.setAlignment(Pos.CENTER_LEFT);

            Background itemBg = new Background(new BackgroundFill(
                    Color.web("white", 0.1),
                    new CornerRadii(5),
                    null
            ));
            itemPagamento.setBackground(itemBg);
            itemPagamento.setStyle("-fx-border-color: " + COR_AMARELO + "; -fx-border-width: 0 0 0 2; -fx-border-radius: 5;");

            Label lblPagamento = new Label(p.forma + ": R$ " + String.format("%.2f", p.valor));
            lblPagamento.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            HBox.setHgrow(lblPagamento, Priority.ALWAYS);

            Button btnRemover = new Button("×");
            btnRemover.setStyle(
                    "-fx-background-color: " + 
                    COR_VERMELHO + 
                    "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 50%; " +
                    "-fx-padding: 0 4 0 4; " +
                    "-fx-font-size: 14px;" +
                    "-fx-alignment: center;"
            );
            btnRemover.setPrefSize(20, 20);
            btnRemover.setMinSize(20, 20);
            btnRemover.setMaxSize(20, 20);
            HBox.setHgrow(btnRemover, Priority.NEVER);

            btnRemover.setOnAction(e -> {
                pagamentos.remove(index);
                atualizarListaPagamentos();
                atualizarTotalRestante();
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            itemPagamento.getChildren().addAll(lblPagamento, spacer, btnRemover);
            pagamentosListaBox.getChildren().add(itemPagamento);
        }
    }

    private void atualizarTotalRestante() {
        double totalPago = pagamentos.stream().mapToDouble(p -> p.valor).sum();
        double restante = Math.max(totalVenda - totalPago, 0);

        totalRestanteLabel.setText("Valor Restante: R$ " + String.format("%.2f", restante));

        if (totalPago >= totalVenda) {
            btnFinalizar.setDisable(false);
            btnFinalizar.setText("Finalizar Venda");
            btnFinalizar.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, " + COR_VERDE + ", #2ecc71); " +
                            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10;"
            );
            totalRestanteLabel.setStyle(
                    "-fx-background-color: " + COR_VERDE + "; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 15 10 15;"
            );
        } else {
            btnFinalizar.setDisable(true);
            btnFinalizar.setText("Faltam R$ " + String.format("%.2f", restante));
            btnFinalizar.setStyle(
                    "-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10;"
            );
            totalRestanteLabel.setStyle(
                    "-fx-background-color: " + COR_VERMELHO + "; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-padding: 10 15 10 15;"
            );
        }
    }

    private void aplicarAnimacaoEntrada(BorderPane layout) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), layout);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void finalizarVenda() {
        double totalPago = pagamentos.stream().mapToDouble(p -> p.valor).sum();
        if (totalPago < totalVenda) {
            mostrarAlerta("Erro", "Valor restante: R$ " + String.format("%.2f", totalVenda - totalPago), Alert.AlertType.WARNING);
            return;
        }

        // salvar venda no banco de dados
        Connection conn = null;
        try {
            conn = ConexaoDB.getConnection();
            conn.setAutoCommit(false);

            Integer idCliente = null;
            if (!documento.isEmpty()) {
                idCliente = inserirCliente(conn);
            }

            int idCarrinho = inserirCarrinho(conn);
            inserirItensCarrinho(conn, idCarrinho);

            double troco = totalPago - totalVenda;
            List<Integer> idsPagamentos = new ArrayList<>();
            for (Pagamento p : pagamentos) {
                double trocoPagamento = (p.forma.equals("Dinheiro") && troco > 0) ? troco : 0;
                int idPagamento = inserirPagamento(conn, p.forma, p.valor, trocoPagamento);
                idsPagamentos.add(idPagamento);
            }

            int idVenda = inserirVenda(conn, idCarrinho, idCliente);

            for (int idPagamento : idsPagamentos) {
                inserirVendaPagamento(conn, idVenda, idPagamento);
            }

            conn.commit();

            // Gerar cupom fiscal se a opção estiver marcada
            if (imprimirCupomCaixa.isSelected()) {
                gerarCupomFiscal(troco);
            }

            caixa.limparVendaAtual();
            mostrarAlerta("Sucesso", "Venda finalizada com sucesso! Troco: R$ " + String.format("%.2f", troco), Alert.AlertType.INFORMATION);

            stage.close();

            Platform.runLater(() -> {
                if (caixa != null && caixa.stage != null) {
                    // Restaurar a janela se estiver minimizada
                    if (caixa.stage.isIconified()) {
                        caixa.stage.setIconified(false);
                    }

                    caixa.stage.toFront();
                    caixa.stage.requestFocus();

                    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                        caixa.stage.setFullScreen(true);
                    }));
                    timeline.play();
                }
            });
                
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao salvar a venda: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // gerar cupom fiscal usando a classe CupomFiscal
    private void gerarCupomFiscal(double troco) {
        try {
            // Converter ItemVenda para o formato esperado pela classe CupomFiscal
            List<CupomFiscal.ItemVenda> itensConvertidos = new ArrayList<>();
            for (Caixa.ItemVenda item : itens) {
                CupomFiscal.ItemVenda itemConvertido = new CupomFiscal.ItemVenda(
                    item.produto,
                    item.codigoBarras,
                    item.quantidade,
                    item.preco
                );
                itensConvertidos.add(itemConvertido);
            }
            
            // Converter pagamentos para o formato esperado pela classe CupomFiscal
            List<CupomFiscal.PagamentoInfo> pagamentosConvertidos = new ArrayList<>();
            for (Pagamento p : pagamentos) {
                CupomFiscal.PagamentoInfo pagamentoConvertido = new CupomFiscal.PagamentoInfo(p.forma, p.valor);
                pagamentosConvertidos.add(pagamentoConvertido);
            }
            
            // Gerar e imprimir cupom usando a classe CupomFiscal
            cupomFiscal.gerarEImprimirCupom(
                itensConvertidos, 
                totalVenda, 
                troco, 
                documento != null ? documento : "", 
                tipoDocumento != null ? tipoDocumento : "", 
                pagamentosConvertidos
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao gerar cupom fiscal: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private Integer inserirCliente(Connection conn) throws SQLException {
        String column = "";
        if ("CPF".equals(tipoDocumento)) {
            column = "CPF";
        } else if ("CNPJ".equals(tipoDocumento)) {
            column = "CNPJ";
        } else if ("RG".equals(tipoDocumento)) {
            column = "RG";
        }

        String sql = "INSERT INTO clientes (" + column + ") VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, documento);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private int inserirPagamento(Connection conn, String formaPagamento, double valor, double troco) throws SQLException {
        String sql = "INSERT INTO pagamentos (ID_Forma_Pagamento, Troco, Valor_Recebido) " +
        "VALUES ((SELECT TOP 1 ID_Forma_Pagamento FROM forma_pagamento WHERE Forma_Pagamento = ? ORDER BY ID_Forma_Pagamento ASC), ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, formaPagamento);
            stmt.setDouble(2, troco);
            stmt.setDouble(3, valor);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private int inserirCarrinho(Connection conn) throws SQLException {
        String sql = "INSERT INTO carrinho (Data_Criacao) VALUES (GETDATE())";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private void inserirItensCarrinho(Connection conn, int idCarrinho) throws SQLException {
        String sql = "INSERT INTO carrinho_itens (ID_Carrinho, ID_Produto, Quantidade, Preco_Unitario) " +
        "VALUES (?, (SELECT TOP 1 ID_Produto FROM produtos WHERE Cod_Barras = ? AND Ativo = 1 ORDER BY ID_Produto DESC), ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Caixa.ItemVenda item : itens) {
                stmt.setInt(1, idCarrinho);
                stmt.setString(2, item.codigoBarras);
                stmt.setInt(3, item.quantidade);
                stmt.setDouble(4, item.preco);
                stmt.executeUpdate();
            }
        }
    }

    private int inserirVenda(Connection conn, int idCarrinho, Integer idCliente) throws SQLException {
        // Obter o ID do funcionário
        int idLogin = AutenticarUser.getIdUsuario(); 
        
        if (idLogin == 0) {
            throw new SQLException("Usuário não autenticado. ID do login não encontrado.");
        }
        
        String insertVenda = "INSERT INTO vendas (ID_Carrinho, ID_Clientes, Subtotal, Total, Data_Venda, ID_Login, Desconto, Status) " +
                             "VALUES (?, ?, ?, ?, GETDATE(), ?, 0.00, 'Concluida')";
        try (PreparedStatement stmt = conn.prepareStatement(insertVenda, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, idCarrinho);
            stmt.setObject(2, idCliente, Types.INTEGER); // Aceita null
            stmt.setDouble(3, Subtotal);
            stmt.setDouble(4, totalVenda);
            stmt.setInt(5, idLogin);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Falha ao obter ID da venda gerado.");
                }
            }
        }
    }

    private void inserirVendaPagamento(Connection conn, int idVenda, int idPagamento) throws SQLException {
        String sql = "INSERT INTO venda_pagamentos (ID_Venda, ID_Pagamento) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVenda);
            stmt.setInt(2, idPagamento);
            stmt.executeUpdate();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    private static class Pagamento {
        String forma;
        double valor;

        Pagamento(String forma, double valor) {
            this.forma = forma;
            this.valor = valor;
        }
    }
}