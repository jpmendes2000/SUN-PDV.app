package com.sunpdv.telas.operacao;

import com.sunpdv.model.AutenticarUser;
import com.sunpdv.telas.operacao.Caixa.ItemVenda;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FinalizarVenda {

    private Stage stage;
    private double totalVenda;
    private List<ItemVenda> itensVenda;
    private List<Pagamento> pagamentos = new ArrayList<>();
    private Label totalRestanteLabel;
    private String documento;
    private String tipoDocumento;
    private Caixa caixa;
    private VBox pagamentosListaBox;
    private Button btnFinalizar;

    // Constantes de cores do design
    private static final String COR_FUNDO_PRINCIPAL = "#4834d4";
    private static final String COR_FUNDO_SECUNDARIO = "#686de0";
    private static final String COR_AZUL_CLARO = "#00a8cc";
    private static final String COR_AZUL_ESCURO = "#00536d";
    private static final String COR_AMARELO = "#f39c12";
    private static final String COR_VERDE = "#27ae60";
    private static final String COR_VERMELHO = "#e74c3c";

    public FinalizarVenda(String documento, String tipoDocumento, List<ItemVenda> itensVenda, double totalVenda) {
        this.documento = documento;
        this.tipoDocumento = tipoDocumento;
        this.itensVenda = new ArrayList<>(itensVenda);
        this.totalVenda = totalVenda;
    }

    public void mostrar(Stage stage, Caixa caixa) {
        this.stage = stage;
        this.caixa = caixa;

        // Layout principal com gradiente
        BorderPane layout = new BorderPane();
        aplicarGradienteFundo(layout);

        // Header superior
        HBox header = criarHeader();
        
        // Menu lateral esquerdo
        VBox menuLateral = criarMenuLateral();
        
        // Área central - Itens da venda e formas de pagamento
        VBox areaCentral = criarAreaCentral();
        
        // Painel direito - Lista de pagamentos
        VBox painelDireito = criarPainelDireito();

        layout.setTop(header);
        layout.setLeft(menuLateral);
        layout.setCenter(areaCentral);
        layout.setRight(painelDireito);

        Scene scene = new Scene(layout, 1200, 800);
        aplicarAnimacaoEntrada(layout);

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Finalizar Venda");
        stage.show();
    }

    private void aplicarGradienteFundo(BorderPane layout) {
        // Criar gradiente similar à imagem
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, null,
            new Stop(0, Color.web(COR_FUNDO_PRINCIPAL)),
            new Stop(1, Color.web(COR_FUNDO_SECUNDARIO))
        );
        
        Background background = new Background(new BackgroundFill(gradient, null, null));
        layout.setBackground(background);
    }

    private HBox criarHeader() {
        HBox header = new HBox();
        header.setPrefHeight(60);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 20, 0, 20));
        
        // Fundo do header
        Background headerBg = new Background(new BackgroundFill(
            Color.web(COR_AZUL_CLARO), 
            new CornerRadii(0, 0, 15, 15, false), 
            null
        ));
        header.setBackground(headerBg);
        
        // Aplicar sombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(4);
        shadow.setRadius(15);
        header.setEffect(shadow);

        Label titulo = new Label("SUN PDV - Finalizar Venda");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label valorTotal = new Label("R$ " + String.format("%.2f", totalVenda));
        valorTotal.setStyle(
            "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-background-color: rgba(255,255,255,0.2); " +
            "-fx-background-radius: 20; -fx-padding: 8 20 8 20;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titulo, spacer, valorTotal);
        return header;
    }

    private VBox criarMenuLateral() {
        VBox menu = new VBox();
        menu.setPrefWidth(250);
        menu.setPadding(new Insets(20, 0, 0, 0));
        
        // Fundo com blur effect
        Background menuBg = new Background(new BackgroundFill(
            Color.web(COR_AZUL_ESCURO, 0.8), 
            null, 
            null
        ));
        menu.setBackground(menuBg);

        // Botão Voltar
        Button btnVoltar = new Button("← Voltar");
        btnVoltar.setPrefWidth(250);
        btnVoltar.setPrefHeight(42);
        btnVoltar.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: center-left; " +
            "-fx-padding: 15 20 15 20;"
        );
        
        // Efeito hover
        btnVoltar.setOnMouseEntered(e -> {
            btnVoltar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: center-left; " +
                "-fx-padding: 15 20 15 20;"
            );
        });
        btnVoltar.setOnMouseExited(e -> {
            btnVoltar.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: center-left; " +
                "-fx-padding: 15 20 15 20;"
            );
        });

        btnVoltar.setOnAction(e -> {
            stage.close();
            caixa.show(stage);
        });

        // Elementos decorativos como na imagem
        VBox elementosDecorativos = criarElementosDecorativos();

        menu.getChildren().addAll(btnVoltar, elementosDecorativos);
        return menu;
    }

    private VBox criarElementosDecorativos() {
        VBox elementos = new VBox(20);
        elementos.setPadding(new Insets(20, 10, 0, 20));

        // Círculo amarelo como na imagem
        Circle circulo = new Circle(15);
        circulo.setFill(Color.web(COR_AMARELO));

        // Linhas decorativas
        Rectangle linha1 = new Rectangle(60, 2);
        linha1.setFill(Color.web("white", 0.3));
        
        Rectangle linha2 = new Rectangle(40, 2);
        linha2.setFill(Color.web("white", 0.3));
        
        Rectangle linha3 = new Rectangle(80, 2);
        linha3.setFill(Color.web("white", 0.3));

        elementos.getChildren().addAll(circulo, linha1, linha2, linha3);
        return elementos;
    }

    private VBox criarAreaCentral() {
        VBox areaCentral = new VBox(30);
        areaCentral.setPadding(new Insets(30));
        
        // Fundo com blur
        Background centralBg = new Background(new BackgroundFill(
            Color.web(COR_AZUL_CLARO, 0.1), 
            null, 
            null
        ));
        areaCentral.setBackground(centralBg);

        // Seção de itens
        VBox secaoItens = criarSecaoItens();
        
        // Seção de formas de pagamento
        VBox secaoPagamento = criarSecaoPagamento();

        areaCentral.getChildren().addAll(secaoItens, secaoPagamento);
        return areaCentral;
    }

    private VBox criarSecaoItens() {
        VBox secao = new VBox(20);

        Label titulo = new Label("Itens da Venda:");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0.0, 0, 2);");

        VBox containerItens = new VBox(5);
        containerItens.setPadding(new Insets(20));
        containerItens.setPrefHeight(200);
        containerItens.setMaxHeight(200);
        
        // Estilo do container
        Background itemsBg = new Background(new BackgroundFill(
            Color.web(COR_AZUL_CLARO, 0.8), 
            new CornerRadii(15), 
            null
        ));
        containerItens.setBackground(itemsBg);
        
        // Sombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(8);
        shadow.setRadius(25);
        containerItens.setEffect(shadow);

        // Adicionar itens da venda
        ScrollPane scrollItens = new ScrollPane();
        VBox listaItens = new VBox(5);
        
        for (ItemVenda item : itensVenda) {
            HBox itemBox = new HBox();
            itemBox.setPadding(new Insets(12, 15, 12, 15));
            itemBox.setAlignment(Pos.CENTER_LEFT);
            
            Background itemBg = new Background(new BackgroundFill(
                Color.web("white", 0.1), 
                new CornerRadii(8), 
                null
            ));
            itemBox.setBackground(itemBg);
            
            // Borda sutil
            itemBox.setStyle("-fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1; -fx-border-radius: 8;");

            Label itemLabel = new Label(String.format("%s | Qtd: %d | R$ %.2f", 
                item.produto, item.quantidade, item.preco * item.quantidade));
            itemLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            itemBox.getChildren().add(itemLabel);
            listaItens.getChildren().add(itemBox);
        }

        scrollItens.setContent(listaItens);
        scrollItens.setFitToWidth(true);
        scrollItens.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        containerItens.getChildren().add(scrollItens);

        secao.getChildren().addAll(titulo, containerItens);
        return secao;
    }

    private VBox criarSecaoPagamento() {
        VBox secao = new VBox(20);

        Label titulo = new Label("Formas de Pagamento:");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0.0, 0, 2);");

        VBox containerPagamento = new VBox(20);
        containerPagamento.setPadding(new Insets(20));
        
        Background pagBg = new Background(new BackgroundFill(
            Color.web(COR_AZUL_CLARO, 0.8), 
            new CornerRadii(15), 
            null
        ));
        containerPagamento.setBackground(pagBg);
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(8);
        shadow.setRadius(25);
        containerPagamento.setEffect(shadow);

        // Opções de pagamento
        HBox opcoesPagamento = new HBox(15);
        ToggleGroup pagamentoGroup = new ToggleGroup();
        
        RadioButton rdbDinheiro = criarRadioButton("Dinheiro", pagamentoGroup);
        RadioButton rdbDebito = criarRadioButton("Débito", pagamentoGroup);
        RadioButton rdbVoucher = criarRadioButton("Voucher", pagamentoGroup);
        
        opcoesPagamento.getChildren().addAll(rdbDinheiro, rdbDebito, rdbVoucher);

        // Campo de valor e botão adicionar
        HBox inputGroup = new HBox(10);
        inputGroup.setAlignment(Pos.CENTER_LEFT);
        
        TextField valorField = new TextField();
        valorField.setPromptText("Valor");
        valorField.setPrefWidth(150);
        valorField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 8; " +
            "-fx-padding: 12; -fx-font-size: 16px;"
        );

        Button btnAdicionar = new Button("Adicionar");
        btnAdicionar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + COR_AMARELO + ", #e67e22); " +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; " +
            "-fx-padding: 12 20 12 20; -fx-effect: dropshadow(gaussian, rgba(243,156,18,0.3), 15, 0.0, 0, 4);"
        );
        
        btnAdicionar.setOnMouseEntered(e -> {
            btnAdicionar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #e67e22, #d35400); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-padding: 10 20 14 20; -fx-effect: dropshadow(gaussian, rgba(243,156,18,0.4), 20, 0.0, 0, 6);"
            );
        });
        
        btnAdicionar.setOnMouseExited(e -> {
            btnAdicionar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + COR_AMARELO + ", #e67e22); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-padding: 12 20 12 20; -fx-effect: dropshadow(gaussian, rgba(243,156,18,0.3), 15, 0.0, 0, 4);"
            );
        });

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
                        mostrarAlerta("Valor inválido", "O valor deve ser maior que zero.", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException ex) {
                    mostrarAlerta("Valor inválido", "Digite um valor numérico válido.", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Dados incompletos", "Selecione a forma de pagamento e digite o valor.", Alert.AlertType.WARNING);
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
        rb.setStyle("-fx-text-fill: white; -fx-font-weight: 500; -fx-font-size: 14px;");
        
        // Customizar o círculo do radio button
        rb.setOnMouseEntered(e -> {
            if (!rb.isSelected()) {
                Background hoverBg = new Background(new BackgroundFill(
                    Color.web("white", 0.1), 
                    new CornerRadii(8), 
                    null
                ));
                rb.setBackground(hoverBg);
                rb.setPadding(new Insets(8, 12, 8, 12));
            }
        });
        
        rb.setOnMouseExited(e -> {
            if (!rb.isSelected()) {
                rb.setBackground(Background.EMPTY);
                rb.setPadding(new Insets(8, 12, 8, 12));
            }
        });

        return rb;
    }

    private VBox criarPainelDireito() {
        VBox painel = new VBox(30);
        painel.setPrefWidth(350);
        painel.setPadding(new Insets(30, 20, 30, 20));
        
        Background painelBg = new Background(new BackgroundFill(
            Color.web(COR_AZUL_CLARO, 0.1), 
            null, 
            null
        ));
        painel.setBackground(painelBg);

        Label titulo = new Label("Pagamentos Adicionados:");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0.0, 0, 2);");

        // Lista de pagamentos
        VBox containerLista = new VBox(5);
        containerLista.setPadding(new Insets(20));
        containerLista.setPrefHeight(300);
        containerLista.setMaxHeight(300);
        
        Background listaBg = new Background(new BackgroundFill(
            Color.web(COR_AZUL_CLARO, 0.8), 
            new CornerRadii(15), 
            null
        ));
        containerLista.setBackground(listaBg);
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.2));
        shadow.setOffsetY(8);
        shadow.setRadius(25);
        containerLista.setEffect(shadow);

        ScrollPane scrollPagamentos = new ScrollPane();
        pagamentosListaBox = new VBox(5);
        scrollPagamentos.setContent(pagamentosListaBox);
        scrollPagamentos.setFitToWidth(true);
        scrollPagamentos.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        containerLista.getChildren().add(scrollPagamentos);

        // Total restante
        totalRestanteLabel = new Label("Valor Restante: R$ " + String.format("%.2f", totalVenda));
        totalRestanteLabel.setStyle(
            "-fx-background-color: " + COR_VERMELHO + "; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; " +
            "-fx-padding: 15 20 15 20; -fx-alignment: center; " +
            "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.3), 15, 0.0, 0, 4);"
        );
        totalRestanteLabel.setAlignment(Pos.CENTER);
        totalRestanteLabel.setMaxWidth(Double.MAX_VALUE);

        // Botão finalizar
        btnFinalizar = new Button("Finalizar Venda");
        btnFinalizar.setPrefWidth(310);
        btnFinalizar.setDisable(true);
        btnFinalizar.setStyle(
            "-fx-background-color: #95a5a6; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 10; " +
            "-fx-padding: 15;"
        );
        btnFinalizar.setOnAction(e -> finalizarVenda());

        painel.getChildren().addAll(titulo, containerLista, totalRestanteLabel, btnFinalizar);
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
            itemPagamento.setPadding(new Insets(10, 15, 10, 15));
            itemPagamento.setAlignment(Pos.CENTER_LEFT);
            
            Background itemBg = new Background(new BackgroundFill(
                Color.web("white", 0.1), 
                new CornerRadii(6), 
                null
            ));
            itemPagamento.setBackground(itemBg);
            itemPagamento.setStyle("-fx-border-color: " + COR_AMARELO + "; -fx-border-width: 0 0 0 3; -fx-border-radius: 6;");

            Label lblPagamento = new Label(p.forma + ": R$ " + String.format("%.2f", p.valor));
            lblPagamento.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            Button btnRemover = new Button("×");
            btnRemover.setStyle(
                "-fx-background-color: " + COR_VERMELHO + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 3; -fx-padding: 2 6 2 6;"
            );
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
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; " +
                "-fx-background-radius: 10; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.3), 20, 0.0, 0, 6);"
            );
            
            totalRestanteLabel.setStyle(
                "-fx-background-color: " + COR_VERDE + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; " +
                "-fx-padding: 15 20 15 20; -fx-alignment: center; " +
                "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.3), 15, 0.0, 0, 4);"
            );
        } else {
            btnFinalizar.setDisable(true);
            btnFinalizar.setText("Faltam R$ " + String.format("%.2f", restante));
            btnFinalizar.setStyle(
                "-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 10; " +
                "-fx-padding: 15;"
            );
            
            totalRestanteLabel.setStyle(
                "-fx-background-color: " + COR_VERMELHO + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; " +
                "-fx-padding: 15 20 15 20; -fx-alignment: center; " +
                "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.3), 15, 0.0, 0, 4);"
            );
        }
    }

    private void aplicarAnimacaoEntrada(BorderPane layout) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), layout);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void finalizarVenda() {
        double totalPago = pagamentos.stream().mapToDouble(p -> p.valor).sum();
        if (totalPago < totalVenda) {
            mostrarAlerta("Pagamento incompleto", "Valor restante: R$ " + String.format("%.2f", totalVenda - totalPago), Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;trustServerCertificate=true", "sa", "Senha@12345!");
             PreparedStatement stmtCarrinho = conn.prepareStatement("INSERT INTO carrinho (Data_Criacao) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtCarrinhoItens = conn.prepareStatement("INSERT INTO carrinho_itens (ID_Carrinho, ID_Produto, Quantidade, PrecoUnitario) VALUES (?, ?, ?, ?)");
             PreparedStatement stmtVenda = conn.prepareStatement("INSERT INTO vendas (Subtotal, Total, Data_Venda, ID_Login, ID_Clientes) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtPagamento = conn.prepareStatement("INSERT INTO pagamentos (ID_Forma_Pagamento, Valor_Recebido, Troco) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            // Inserir carrinho
            stmtCarrinho.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            stmtCarrinho.executeUpdate();
            ResultSet rsCarrinho = stmtCarrinho.getGeneratedKeys();
            int idCarrinho = rsCarrinho.next() ? rsCarrinho.getInt(1) : 1;

            // Inserir itens do carrinho
            for (ItemVenda item : itensVenda) {
                String queryProduto = "SELECT ID_Produto FROM produtos WHERE Cod_Barras = ?";
                try (PreparedStatement stmtProduto = conn.prepareStatement(queryProduto)) {
                    stmtProduto.setString(1, item.codigoBarras);
                    ResultSet rsProduto = stmtProduto.executeQuery();
                    if (rsProduto.next()) {
                        int idProduto = rsProduto.getInt("ID_Produto");
                        stmtCarrinhoItens.setInt(1, idCarrinho);
                        stmtCarrinhoItens.setInt(2, idProduto);
                        stmtCarrinhoItens.setInt(3, item.quantidade);
                        stmtCarrinhoItens.setDouble(4, item.preco);
                        stmtCarrinhoItens.executeUpdate();
                    }
                }
            }

            // Inserir venda
            int idCliente = documento.isEmpty() ? 1 : inserirOuBuscarCliente(documento, tipoDocumento, conn);
            stmtVenda.setDouble(1, totalVenda);
            stmtVenda.setDouble(2, totalVenda);
            stmtVenda.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            stmtVenda.setInt(4, AutenticarUser.getIdPermissao());
            stmtVenda.setInt(5, idCliente);
            stmtVenda.executeUpdate();
            ResultSet rsVenda = stmtVenda.getGeneratedKeys();
            int idVenda = rsVenda.next() ? rsVenda.getInt(1) : 1;

            // Inserir pagamento
            double troco = totalPago - totalVenda;
            for (Pagamento p : pagamentos) {
                stmtPagamento.setInt(1, getFormaPagamentoId(p.forma));
                stmtPagamento.setDouble(2, p.valor);
                stmtPagamento.setDouble(3, troco > 0 ? troco : 0);
                stmtPagamento.executeUpdate();
                
                // Pegar o ID do pagamento inserido para associar à venda
                ResultSet rsPagamento = stmtPagamento.getGeneratedKeys();
                if (rsPagamento.next()) {
                    int idPagamento = rsPagamento.getInt(1);
                    
                    // Atualizar a venda com o ID do pagamento
                    try (PreparedStatement stmtUpdateVenda = conn.prepareStatement("UPDATE vendas SET ID_Pagamentos = ?, ID_Carrinho = ? WHERE ID_Vendas = ?")) {
                        stmtUpdateVenda.setInt(1, idPagamento);
                        stmtUpdateVenda.setInt(2, idCarrinho);
                        stmtUpdateVenda.setInt(3, idVenda);
                        stmtUpdateVenda.executeUpdate();
                    }
                }
                break; // Usar apenas o primeiro pagamento para simplicidade
            }

            conn.commit();

            // Mostrar mensagem de sucesso com troco se houver
            String mensagem = "Venda finalizada com sucesso!";
            if (troco > 0) {
                mensagem += "\nTroco: R$ " + String.format("%.2f", troco);
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK);
            alert.initOwner(stage);
            alert.setTitle("Venda Finalizada");
            alert.showAndWait();
            
            stage.close();
            caixa.limparVendaAtual();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao finalizar venda", "Detalhes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private int inserirOuBuscarCliente(String documento, String tipoDocumento, Connection conn) throws SQLException {
        String coluna = tipoDocumento.equals("CPF") ? "cpf" : 
                       tipoDocumento.equals("CNPJ") ? "cnpj" : "rg";
        
        String query = "SELECT ID_Clientes FROM clientes WHERE " + coluna + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, documento);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID_Clientes");
            } else {
                // Inserir novo cliente
                int novoId = getNovoIdCliente(conn);
                String insertQuery = "INSERT INTO clientes (ID_Clientes, " + coluna + ") VALUES (?, ?)";
                try (PreparedStatement stmtInsert = conn.prepareStatement(insertQuery)) {
                    stmtInsert.setInt(1, novoId);
                    stmtInsert.setString(2, documento);
                    stmtInsert.executeUpdate();
                    return novoId;
                }
            }
        }
    }

    private int getNovoIdCliente(Connection conn) throws SQLException {
        String query = "SELECT ISNULL(MAX(ID_Clientes), 0) + 1 AS novoId FROM clientes";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("novoId") : 1;
        }
    }

    private int getFormaPagamentoId(String forma) {
        switch (forma.toLowerCase()) {
            case "dinheiro": return 1;
            case "débito": return 2;
            case "crédito": return 3;
            case "pix": return 4;
            case "voucher": return 5;
            default: return 1;
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo, mensagem, ButtonType.OK);
        alert.initOwner(stage);
        alert.setTitle(titulo);
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