package com.sunpdv.telas.operacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.util.Duration;
import com.sunpdv.model.AutenticarUser;
import com.sunpdv.telas.home.TelaHomeADM;
import com.sunpdv.telas.home.TelaHomeFUN;
import com.sunpdv.telas.home.TelaHomeMOD;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Caixa {

    private Stage stage;
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "Senha@12345!";
    
    private List<Venda> vendas;
    private VBox listaVendas;
    private TextField pesquisaField;
    private ComboBox<String> filtroPagamento;
    private VBox novaVendaContainer;
    private VBox historicoContainer;
    private CheckBox clienteNaoIdentificadoCheck;
    private TextField codigoProdutoField;
    private Label totalLabel;
    private Button toggleButton;
    private boolean isHistoricoAtivo = true;
    private ListView<ItemVenda> listaProdutos;
    private TextField documentoField;
    private ToggleGroup clienteGroup;
    private Button btnFinalizar;
    private Button btnCancelar;

    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            try {
                stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
                );
            } catch (Exception e) {
                System.err.println("Erro ao carregar CSS: " + e.getMessage());
            }
        }
    }

    private static class Venda {
        int id;
        String formaPagamento;
        double subtotal;
        double total;
        String data;
        List<ItemVenda> itens;

        public Venda(int id, String formaPagamento, double subtotal, double total, String data) {
            this.id = id;
            this.formaPagamento = formaPagamento;
            this.subtotal = subtotal;
            this.total = total;
            this.data = data;
            this.itens = new ArrayList<>();
        }
    }

    public static class ItemVenda {
        String produto;
        String codigoBarras;
        int quantidade;
        double preco;

        public ItemVenda(String produto, String codigoBarras, int quantidade, double preco) {
            this.produto = produto;
            this.codigoBarras = codigoBarras;
            this.quantidade = quantidade;
            this.preco = preco;
        }
        
        public double getTotal() {
            return quantidade * preco;
        }
    }

    public Caixa() {
        novaVendaContainer = new VBox(10);
        historicoContainer = new VBox(10);
        listaVendas = new VBox(10);
        vendas = new ArrayList<>();
    }

    private void atualizarEstadoBotoes() {
        boolean temProdutos = listaProdutos != null && !listaProdutos.getItems().isEmpty();
        if (btnFinalizar != null) {
            btnFinalizar.setDisable(!temProdutos);
        }
        if (btnCancelar != null) {
            btnCancelar.setDisable(!temProdutos);
        }
    }

    private void atualizarTotal() {
        if (listaProdutos != null && totalLabel != null) {
            double total = listaProdutos.getItems().stream()
                .mapToDouble(item -> item.quantidade * item.preco)
                .sum();
            totalLabel.setText("SUBTOTAL: R$ " + String.format("%.2f", total));
        }
        atualizarEstadoBotoes();
    }

    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            StackPane indicatorContainer = new StackPane();
            indicatorContainer.setMinWidth(3);
            indicatorContainer.setMaxWidth(3);
            indicatorContainer.setMinHeight(30);
            indicatorContainer.setMaxHeight(30);
            indicatorContainer.setStyle("-fx-background-color: transparent;");

            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);

            HBox content = new HBox(leftContent, indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setSpacing(10);

            Button btn = new Button();
            btn.setGraphic(content);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18)); -fx-text-fill: white; -fx-font-weight: bold;");
                indicatorContainer.setStyle("-fx-background-color: #FFCC00;");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });

            return btn;
        } catch (Exception e) {
            Button btn = new Button(texto);
            btn.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            return btn;
        }
    }

    private List<Venda> carregarVendas() {
        List<Venda> vendas = new ArrayList<>();
        String query = "SELECT v.ID_Vendas, fp.Forma_Pagamento, v.Subtotal, v.Total, " +
                      "CONVERT(VARCHAR, v.Data_Venda, 103) AS Data " +
                      "FROM vendas v " +
                      "JOIN pagamentos p ON v.ID_Pagamentos = p.ID_Pagamentos " +
                      "JOIN forma_pagamento fp ON p.ID_Forma_Pagamento = fp.ID_Forma_Pagamento " +
                      "ORDER BY v.Data_Venda DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Venda venda = new Venda(
                    rs.getInt("ID_Vendas"),
                    rs.getString("Forma_Pagamento"),
                    rs.getDouble("Subtotal"),
                    rs.getDouble("Total"),
                    rs.getString("Data")
                );
                
                carregarItensVenda(venda);
                vendas.add(venda);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao carregar vendas", "Detalhes: " + e.getMessage(), AlertType.ERROR);
        }
        return vendas;
    }

    private void carregarItensVenda(Venda venda) throws SQLException {
        String query = "SELECT p.Nome, p.Cod_Barras, ci.Quantidade, ci.PrecoUnitario " +
                      "FROM carrinho_itens ci " +
                      "JOIN produtos p ON ci.ID_Produto = p.ID_Produto " +
                      "WHERE ci.ID_Carrinho = (SELECT ID_Carrinho FROM vendas WHERE ID_Vendas = ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, venda.id);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                venda.itens.add(new ItemVenda(
                    rs.getString("Nome"),
                    rs.getString("Cod_Barras"),
                    rs.getInt("Quantidade"),
                    rs.getDouble("PrecoUnitario")
                ));
            }
        }
    }

    private void aplicarFiltros() {
        String textoBusca = pesquisaField.getText().toLowerCase().trim();
        String pagamentoSelecionado = filtroPagamento.getValue();

        listaVendas.getChildren().clear();
        boolean achou = false;
        
        for (Venda venda : vendas) {
            boolean idMatch = String.valueOf(venda.id).contains(textoBusca);
            boolean pagamentoMatch = pagamentoSelecionado == null || pagamentoSelecionado.equals("Todos") || 
                                   venda.formaPagamento.equalsIgnoreCase(pagamentoSelecionado);

            if (idMatch && pagamentoMatch) {
                listaVendas.getChildren().add(criarPainelVenda(venda));
                achou = true;
            }
        }
        
        if (!achou) {
            Label lblNenhumaVenda = new Label("Nenhuma venda corresponde à pesquisa.");
            lblNenhumaVenda.setStyle("-fx-text-fill: #00536d; -fx-font-size: 14px;");
            listaVendas.getChildren().add(lblNenhumaVenda);
        }
    }

    private void setupNovaVendaUI() {
        clienteNaoIdentificadoCheck = new CheckBox("Cliente não identificado");
        clienteNaoIdentificadoCheck.setSelected(true);
        clienteNaoIdentificadoCheck.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        totalLabel = new Label("SUBTOTAL: R$ 0,00");
        totalLabel.setStyle("-fx-text-fill: #c7eefaff; -fx-font-weight: bold; -fx-font-size: 30px;");

        VBox clienteBox = new VBox(15);
        clienteBox.setStyle("-fx-background-color: #00536d; -fx-padding: 20; -fx-background-radius: 5;");
        
        HBox subtotalContainer = new HBox();
        subtotalContainer.setAlignment(Pos.CENTER);
        subtotalContainer.getChildren().add(totalLabel);
        
        clienteGroup = new ToggleGroup();
        RadioButton rbCPF = new RadioButton("CPF");
        rbCPF.setToggleGroup(clienteGroup);
        rbCPF.setStyle("-fx-text-fill: white;");
        RadioButton rbCNPJ = new RadioButton("CNPJ");
        rbCNPJ.setToggleGroup(clienteGroup);
        rbCNPJ.setStyle("-fx-text-fill: white;");
        RadioButton rbRG = new RadioButton("RG");
        rbRG.setToggleGroup(clienteGroup);
        rbRG.setStyle("-fx-text-fill: white;");
        
        documentoField = new TextField();
        documentoField.setPromptText("Número do documento");
        documentoField.setMaxWidth(300);
        documentoField.setDisable(true);
        
        HBox tipoDocumentoBox = new HBox(10, rbCPF, rbCNPJ, rbRG);
        tipoDocumentoBox.setDisable(true);
        
        clienteNaoIdentificadoCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            tipoDocumentoBox.setDisable(newVal);
            documentoField.setDisable(newVal);
            if (newVal) {
                clienteGroup.selectToggle(null);
                documentoField.clear();
            }
        });

        rbCPF.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                documentoField.setPromptText("Digite o CPF (somente números)");
            }
        });
        
        documentoField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (rbCPF.isSelected() && !newVal.isEmpty()) {
                if (!validarCPF(newVal)) {
                    documentoField.setStyle("-fx-border-color: red;");
                } else {
                    documentoField.setStyle("-fx-border-color: green;");
                }
            } else {
                documentoField.setStyle("");
            }
        });

        listaProdutos = new ListView<>();
        listaProdutos.setPrefHeight(450);
        listaProdutos.setCellFactory(lv -> new ItemVendaCell());
        
        ContextMenu contextMenu = new ContextMenu();
        MenuItem alterarQuantidadeItem = new MenuItem("Alterar Quantidade");
        MenuItem removerItem = new MenuItem("Remover");
        contextMenu.getItems().addAll(alterarQuantidadeItem, removerItem);
        
        listaProdutos.setContextMenu(contextMenu);
        listaProdutos.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                ItemVenda selected = listaProdutos.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    contextMenu.show(listaProdutos, e.getScreenX(), e.getScreenY());
                }
            }
        });
        
        alterarQuantidadeItem.setOnAction(e -> {
            ItemVenda selected = listaProdutos.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.quantidade));
                dialog.setTitle("Alterar Quantidade");
                dialog.setHeaderText("Alterar quantidade de " + selected.produto);
                dialog.setContentText("Nova quantidade:");
                
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(quantidadeStr -> {
                    try {
                        int novaQuantidade = Integer.parseInt(quantidadeStr);
                        if (novaQuantidade > 0) {
                            selected.quantidade = novaQuantidade;
                            listaProdutos.refresh();
                            atualizarTotal();
                        } else {
                            mostrarAlerta("Quantidade inválida", "A quantidade deve ser maior que zero.", AlertType.ERROR);
                        }
                    } catch (NumberFormatException ex) {
                        mostrarAlerta("Valor inválido", "Digite um número válido para a quantidade.", AlertType.ERROR);
                    }
                });
            }
        });
        
        removerItem.setOnAction(e -> {
            ItemVenda selected = listaProdutos.getSelectionModel().getSelectedItem();
            if (selected != null) {
                listaProdutos.getItems().remove(selected);
                atualizarTotal();
            }
        });

        HBox adicionarProdutoBox = new HBox(10);
        adicionarProdutoBox.setAlignment(Pos.CENTER_LEFT);
        
        codigoProdutoField = new TextField();
        codigoProdutoField.setPromptText("Código de barras do produto");
        HBox.setHgrow(codigoProdutoField, Priority.ALWAYS);
        
        Spinner<Integer> quantidadeSpinner = new Spinner<>(1, 100, 1);
        quantidadeSpinner.setPrefWidth(80);
        quantidadeSpinner.setEditable(true);
        
        Button btnAdicionar = new Button("Adicionar");
        btnAdicionar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnAdicionar.setOnAction(e -> adicionarProduto(quantidadeSpinner));

        codigoProdutoField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                adicionarProduto(quantidadeSpinner);
            }
        });

        adicionarProdutoBox.getChildren().addAll(
            new Label("Código:"), codigoProdutoField,
            new Label("Qtd:"), quantidadeSpinner, btnAdicionar
        );

        btnFinalizar = new Button("  Finalizar");
        btnFinalizar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnFinalizar.setPadding(new Insets(10, 60, 10, 60));
        btnFinalizar.setDisable(true);
        btnFinalizar.setOnAction(e -> finalizarVenda());

        btnCancelar = new Button(" Cancelar");
        btnCancelar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnCancelar.setDisable(true);
        btnCancelar.setOnAction(e -> cancelarVenda());

        HBox botoes = new HBox(10, btnFinalizar, btnCancelar);
        botoes.setAlignment(Pos.CENTER);

        clienteBox.getChildren().addAll(
            subtotalContainer,
            clienteNaoIdentificadoCheck,
            new Label("Identificação do Cliente:"),
            tipoDocumentoBox,
            documentoField
        );

        VBox produtosBox = new VBox(10);
        produtosBox.setStyle("-fx-background-color: #00536d; -fx-padding: 15; -fx-background-radius: 5;");
        produtosBox.getChildren().addAll(
            new Label("Produtos:"),
            adicionarProdutoBox,
            listaProdutos
        );

        listaProdutos.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends ItemVenda> c) -> {
            atualizarTotal();
        });

        novaVendaContainer.getChildren().addAll(
            clienteBox,
            produtosBox,
            botoes
        );
    }

    private void adicionarProduto(Spinner<Integer> quantidadeSpinner) {
        String codigo = codigoProdutoField.getText().trim();
        if (codigo.isEmpty()) {
            mostrarAlerta("Código vazio", "Digite um código de produto.", AlertType.WARNING);
            codigoProdutoField.requestFocus();
            return;
        }

        try {
            String nomeProduto = buscarProdutoPorCodigo(codigo);
            if (nomeProduto == null) {
                mostrarAlerta("Produto não encontrado", "Nenhum produto encontrado com o código: " + codigo, AlertType.ERROR);
                codigoProdutoField.selectAll();
                codigoProdutoField.requestFocus();
                return;
            }

            double preco = buscarPrecoProduto(codigo);
            if (preco <= 0) {
                mostrarAlerta("Preço inválido", "Produto com preço inválido.", AlertType.ERROR);
                return;
            }

            int quantidade = quantidadeSpinner.getValue();
            if (quantidade <= 0) {
                mostrarAlerta("Quantidade inválida", "A quantidade deve ser maior que zero.", AlertType.ERROR);
                return;
            }

            Optional<ItemVenda> existente = listaProdutos.getItems().stream()
                .filter(item -> item.codigoBarras.equals(codigo))
                .findFirst();

            if (existente.isPresent()) {
                ItemVenda item = existente.get();
                item.quantidade += quantidade;
                listaProdutos.refresh();
            } else {
                listaProdutos.getItems().add(new ItemVenda(nomeProduto, codigo, quantidade, preco));
            }

            codigoProdutoField.clear();
            quantidadeSpinner.getValueFactory().setValue(1);
            codigoProdutoField.requestFocus();

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarAlerta("Erro ao buscar produto", "Detalhes: " + ex.getMessage(), AlertType.ERROR);
        }
    }

    private void cancelarVenda() {
        if (!listaProdutos.getItems().isEmpty()) {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(
                stage, 
                "Confirmação", 
                "Cancelar Venda", 
                "Tem certeza que deseja cancelar a venda atual? Todos os produtos serão removidos."
            );
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                limparVendaAtual();
            }
        }
    }

    private void finalizarVenda() {
        if (listaProdutos.getItems().isEmpty()) {
            mostrarAlerta("Venda vazia", "Adicione pelo menos um produto para finalizar a venda.", AlertType.ERROR);
            return;
        }

        if (!clienteNaoIdentificadoCheck.isSelected()) {
            RadioButton selectedToggle = (RadioButton) clienteGroup.getSelectedToggle();
            if (selectedToggle == null) {
                mostrarAlerta("Tipo de documento", "Selecione o tipo de documento do cliente.", AlertType.ERROR);
                return;
            }

            String documento = documentoField.getText().trim();
            if (documento.isEmpty()) {
                mostrarAlerta("Documento obrigatório", "Digite o documento do cliente.", AlertType.ERROR);
                documentoField.requestFocus();
                return;
            }

            if (selectedToggle.getText().equals("CPF") && !validarCPF(documento)) {
                mostrarAlerta("CPF inválido", "O CPF digitado não é válido.", AlertType.ERROR);
                documentoField.selectAll();
                documentoField.requestFocus();
                return;
            }
        }

        try {
            double totalVenda = calcularTotal(listaProdutos);
            String documento = clienteNaoIdentificadoCheck.isSelected() ? "" : documentoField.getText().trim();
            String tipoDocumento = "";
            
            if (!clienteNaoIdentificadoCheck.isSelected()) {
                RadioButton selectedToggle = (RadioButton) clienteGroup.getSelectedToggle();
                tipoDocumento = selectedToggle.getText();
            }

            FinalizarVenda finalizarVenda = new FinalizarVenda(
                documento, 
                tipoDocumento, 
                new ArrayList<>(listaProdutos.getItems()), 
                totalVenda
            );
            
            finalizarVenda.mostrar(stage, this);

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta("Erro ao finalizar venda", "Detalhes: " + ex.getMessage(), AlertType.ERROR);
        }
    }

    public void limparVendaAtual() {
        if (listaProdutos != null) {
            listaProdutos.getItems().clear();
        }
        if (totalLabel != null) {
            totalLabel.setText("SUBTOTAL: R$ 0,00");
        }
        if (documentoField != null) {
            documentoField.clear();
        }
        if (clienteNaoIdentificadoCheck != null) {
            clienteNaoIdentificadoCheck.setSelected(true);
        }
        if (clienteGroup != null) {
            clienteGroup.selectToggle(null);
        }
        if (codigoProdutoField != null) {
            codigoProdutoField.requestFocus();
        }
        
        atualizarEstadoBotoes();
        
        vendas = carregarVendas();
        aplicarFiltros();
    }

    private class ItemVendaCell extends ListCell<ItemVenda> {
        @Override
        protected void updateItem(ItemVenda item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox vbox = new VBox(2);
                
                Label nomeLabel = new Label(item.produto);
                nomeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                
                Label detalhesLabel = new Label(
                    String.format("Cód: %s | Qtd: %d | R$ %.2f (un) | Total: R$ %.2f", 
                                item.codigoBarras, item.quantidade, item.preco, 
                                item.quantidade * item.preco)
                );
                detalhesLabel.setStyle("-fx-text-fill: #c7eefaff; -fx-font-size: 11px;");
                
                vbox.getChildren().addAll(nomeLabel, detalhesLabel);
                setGraphic(vbox);
            }
        }
    }

    private boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");
        
        if (cpf.length() != 11) {
            return false;
        }
        
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (10 - i) * Character.getNumericValue(cpf.charAt(i));
        }
        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : (11 - resto);
        
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (11 - i) * Character.getNumericValue(cpf.charAt(i));
        }
        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : (11 - resto);
        
        return (Character.getNumericValue(cpf.charAt(9)) == digito1 && 
               Character.getNumericValue(cpf.charAt(10)) == digito2);
    }

    private String buscarProdutoPorCodigo(String codigo) throws SQLException {
        String query = "SELECT Nome FROM produtos WHERE Cod_Barras = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("Nome") : null;
        }
    }

    private double buscarPrecoProduto(String codigo) throws SQLException {
        String query = "SELECT Preco FROM produtos WHERE Cod_Barras = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("Preco") : 0;
        }
    }

    private double calcularTotal(ListView<ItemVenda> listaProdutos) {
        return listaProdutos.getItems().stream()
            .mapToDouble(item -> item.quantidade * item.preco)
            .sum();
    }

    private VBox criarPainelVenda(Venda venda) {
        VBox painel = new VBox(5);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #00536d; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label idLabel = new Label("Venda #" + venda.id);
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00536d;");

        Label pagamentoLabel = new Label("Pagamento: " + venda.formaPagamento);
        pagamentoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        Label subtotalLabel = new Label("Subtotal: R$ " + String.format("%.2f", venda.subtotal));
        subtotalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        Label totalLabel = new Label("Total: R$ " + String.format("%.2f", venda.total));
        totalLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #00536d;");

        Label dataLabel = new Label("Data: " + venda.data);
        dataLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        VBox itensBox = new VBox(5);
        for (ItemVenda item : venda.itens) {
            Label itemLabel = new Label(
                String.format("%s | Qtd: %d | R$ %.2f (un) | Total: R$ %.2f", 
                    item.produto, item.quantidade, item.preco, item.getTotal())
            );
            itemLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            itensBox.getChildren().add(itemLabel);
        }

        painel.getChildren().addAll(idLabel, pagamentoLabel, subtotalLabel, totalLabel, dataLabel, itensBox);

        painel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                mostrarDetalhesVenda(venda);
            }
        });

        return painel;
    }

    private void mostrarDetalhesVenda(Venda venda) {
        Stage detalhesStage = new Stage();
        detalhesStage.initModality(Modality.APPLICATION_MODAL);
        detalhesStage.setTitle("Detalhes da Venda #" + venda.id);

        VBox detalhesBox = new VBox(10);
        detalhesBox.setPadding(new Insets(20));
        detalhesBox.setStyle("-fx-background-color: trasparent;");

        Label idLabel = new Label("Venda #" + venda.id);
        idLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #00536d;");

        Label pagamentoLabel = new Label("Forma de Pagamento: " + venda.formaPagamento);
        pagamentoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00536d;");

        Label subtotalLabel = new Label("Subtotal: R$ " + String.format("%.2f", venda.subtotal));
        subtotalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00536d;");

        Label totalLabel = new Label("Total: R$ " + String.format("%.2f", venda.total));
        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00536d;");

        Label dataLabel = new Label("Data: " + venda.data);
        dataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00536d;");

        ListView<ItemVenda> itensListView = new ListView<>();
        itensListView.getItems().addAll(venda.itens);
        itensListView.setCellFactory(lv -> new ItemVendaCell());
        itensListView.setPrefHeight(200);

        Button fecharButton = new Button("Fechar");
        fecharButton.setStyle("-fx-background-color: #00536d; -fx-text-fill: white;");
        fecharButton.setOnAction(e -> detalhesStage.close());

        detalhesBox.getChildren().addAll(
            idLabel, pagamentoLabel, subtotalLabel, totalLabel, dataLabel,
            new Label("Itens:"),
            itensListView,
            fecharButton
        );

        Scene scene = new Scene(detalhesBox, 400, 400);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSS: " + e.getMessage());
        }

        detalhesStage.setScene(scene);
        detalhesStage.show();
    }

    private void setupHistoricoUI() {
        pesquisaField = new TextField();
        pesquisaField.setPromptText("Pesquisar por ID da venda...");
        pesquisaField.setMaxWidth(300);
        pesquisaField.textProperty().addListener((obs, oldValue, newValue) -> aplicarFiltros());

        filtroPagamento = new ComboBox<>();
        filtroPagamento.getItems().addAll("Todos", "Dinheiro", "Cartão de Crédito", "Cartão de Débito", "Pix", "Voucher");
        filtroPagamento.setValue("Todos");
        filtroPagamento.setMaxWidth(200);
        filtroPagamento.setOnAction(e -> aplicarFiltros());

        HBox filtroBox = new HBox(10, new Label("Filtro:"), pesquisaField, new Label("Pagamento:"), filtroPagamento);
        filtroBox.setAlignment(Pos.CENTER_LEFT);

        listaVendas.setPadding(new Insets(10));
        listaVendas.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(listaVendas);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        historicoContainer.getChildren().addAll(filtroBox, scrollPane);

        vendas = carregarVendas();
        aplicarFiltros();
    }

    private void mostrarAlerta(String titulo, String mensagem, AlertType tipo) {
        Alert alert = new CustomConfirmationAlert(stage, titulo, null, mensagem);
        alert.setAlertType(tipo);
        alert.showAndWait();
    }

    public void show(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Caixa - SUN PDV");

        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #00435a;");

        // ===== MENU LATERAL ESTILIZADO =====
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

            // Espaço para centralizar verticalmente
        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        menuLateral.getChildren().add(espaco);




        Button btnNovaVenda = criarBotaoLateral("Nova Venda", "/icons/nova-venda.png");
        btnNovaVenda.setOnAction(e -> {
            // toggleButton.setText("Histórico"); // Remover esta linha problemática
            isHistoricoAtivo = false;
            root.setCenter(novaVendaContainer);
            
            // Focar no campo de código de produto quando entrar na nova venda
            if (codigoProdutoField != null) {
                codigoProdutoField.requestFocus();
            }
        });

        Button btnHistorico = criarBotaoLateral("Histórico", "/icons/historico.png");
        btnHistorico.setOnAction(e -> {
            // toggleButton.setText("Nova Venda"); // Remover esta linha problemática
            isHistoricoAtivo = true;
            root.setCenter(historicoContainer);
            vendas = carregarVendas();
            aplicarFiltros();
        });
        

        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Confirmação", "Deseja sair?", "Isso fechará o sistema.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });

        menuLateral.getChildren().addAll(btnNovaVenda, btnHistorico, btnSair);

        setupNovaVendaUI();
        setupHistoricoUI();

        root.setLeft(menuLateral);
        root.setCenter(isHistoricoAtivo ? historicoContainer : novaVendaContainer);

        Scene scene = new Scene(root, 1280, 720);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSS: " + e.getMessage());
        }

        stage.setScene(scene);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - 1280) / 2);
        stage.setY((screenBounds.getHeight() - 720) / 2);

        stage.setFullScreen(true);

        stage.show();
    }
}