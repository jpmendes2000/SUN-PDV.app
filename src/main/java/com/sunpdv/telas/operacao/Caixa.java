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
import javafx.scene.input.KeyCombination;
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
    private Label totalLabel; // Movido para o topo
    private Button toggleButton; // Botão único para alternar entre telas
    private boolean isHistoricoAtivo = true; // Histórico como padrão
    private ListView<ItemVenda> listaProdutos; // Para acesso global

    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
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

    private static class ItemVenda {
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
    }

    public Caixa() {
        // Inicializa os containers principais
        novaVendaContainer = new VBox(10);
        historicoContainer = new VBox(10);
        listaVendas = new VBox(10);
        vendas = new ArrayList<>();
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

            HBox content = new HBox(leftContent, new Region(), indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

            Button btn = new Button();
            btn.setGraphic(content);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

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

    private List<Venda> carregarVendas() {
        List<Venda> vendas = new ArrayList<>();
        String query = "SELECT v.ID_Vendas, p.Forma_Pagamento, v.Subtotal, v.Total, " +
                      "CONVERT(VARCHAR, v.DataHora_Venda, 103) AS Data " +
                      "FROM vendas v " +
                      "JOIN pagamento p ON v.ID_Pagamento = p.ID_Pagamento " +
                      "ORDER BY v.DataHora_Venda DESC";

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
        String query = "SELECT p.Nome, p.Cod_Barras, c.Quantidade, c.PrecoUnitario " +
                       "FROM carrinho c " +
                       "JOIN produtos p ON c.ID_Produto = p.ID_Produto " +
                       "WHERE c.ID_Carrinho = (SELECT ID_Carrinho FROM vendas WHERE ID_Vendas = ?)";
        
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
        // Inicializa todos os componentes primeiro
        clienteNaoIdentificadoCheck = new CheckBox("Cliente não identificado");
        clienteNaoIdentificadoCheck.setSelected(true);
        clienteNaoIdentificadoCheck.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Total da venda - agora no topo
        totalLabel = new Label("SUBTOTAL: R$ 0,00");
        totalLabel.setStyle("-fx-text-fill: #c7eefaff; -fx-font-weight: bold; -fx-font-size: 30px;");

        // Seção de identificação do cliente + Total
        VBox clienteBox = new VBox(15);
        clienteBox.setStyle("-fx-background-color: #00536d; -fx-padding: 20; -fx-background-radius: 5;");
        
        // Container para o subtotal
        HBox subtotalContainer = new HBox();
        subtotalContainer.setAlignment(Pos.CENTER);
        subtotalContainer.getChildren().add(totalLabel);
        
        ToggleGroup clienteGroup = new ToggleGroup();
        RadioButton rbCPF = new RadioButton("CPF");
        rbCPF.setToggleGroup(clienteGroup);
        rbCPF.setStyle("-fx-text-fill: white;");
        RadioButton rbCNPJ = new RadioButton("CNPJ");
        rbCNPJ.setToggleGroup(clienteGroup);
        rbCNPJ.setStyle("-fx-text-fill: white;");
        RadioButton rbRG = new RadioButton("RG");
        rbRG.setToggleGroup(clienteGroup);
        rbRG.setStyle("-fx-text-fill: white;");
        
        TextField documentoField = new TextField();
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

        // Lista de produtos - agora como variável de instância
        listaProdutos = new ListView<>();
        listaProdutos.setPrefHeight(450); // Aumentada a altura
        listaProdutos.setCellFactory(lv -> new ItemVendaCell());
        
        // Menu de contexto para itens
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
            }
        });

        // Campo para adicionar produto
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
        btnAdicionar.setOnAction(e -> {
            String codigo = codigoProdutoField.getText().trim();
            if (!codigo.isEmpty()) {
                try {
                    String produto = buscarProdutoPorCodigo(codigo);
                    if (produto != null) {
                        double preco = buscarPrecoProduto(codigo);
                        int quantidade = quantidadeSpinner.getValue();
                        
                        Optional<ItemVenda> existente = listaProdutos.getItems().stream()
                            .filter(item -> item.codigoBarras.equals(codigo))
                            .findFirst();
                        
                        if (existente.isPresent()) {
                            ItemVenda item = existente.get();
                            item.quantidade += quantidade;
                            listaProdutos.refresh();
                        } else {
                            listaProdutos.getItems().add(new ItemVenda(produto, codigo, quantidade, preco));
                        }
                        
                        codigoProdutoField.clear();
                        codigoProdutoField.requestFocus();
                    } else {
                        mostrarAlerta("Produto não encontrado", "Nenhum produto encontrado com o código: " + codigo, AlertType.ERROR);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    mostrarAlerta("Erro ao buscar produto", "Detalhes: " + ex.getMessage(), AlertType.ERROR);
                }
            }
        });

        adicionarProdutoBox.getChildren().addAll(
            new Label("Código:"), codigoProdutoField,
            new Label("Qtd:"), quantidadeSpinner, btnAdicionar
        );

        // Botões (apenas Finalizar e Cancelar)
        Button btnFinalizar = new Button("Finalizar Venda");
        btnFinalizar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnFinalizar.setPadding(new Insets(10, 60, 10, 60));
        btnFinalizar.setOnAction(e -> {
            try {
                if (listaProdutos.getItems().isEmpty()) {
                    mostrarAlerta("Venda vazia", "Adicione pelo menos um produto para finalizar a venda.", AlertType.ERROR);
                    return;
                }

                double totalVenda = calcularTotal(listaProdutos);

                if (rbCPF.isSelected() && !documentoField.getText().isEmpty() && !validarCPF(documentoField.getText())) {
                    mostrarAlerta("CPF inválido", "O CPF digitado não é válido.", AlertType.ERROR);
                    return;
                }

                String documento = clienteNaoIdentificadoCheck.isSelected() ? "" : documentoField.getText();
                String tipoDocumento = clienteNaoIdentificadoCheck.isSelected() ? "" : 
                                     (rbCPF.isSelected() ? "CPF" : (rbCNPJ.isSelected() ? "CNPJ" : "RG"));

                // Por enquanto, salvamos sem forma de pagamento (será implementado em outra tela)
                // salvarVendaNoBanco(documento, tipoDocumento, listaProdutos, null, 0, null, 0, totalVenda);
                
                // Limpar campos após venda
                listaProdutos.getItems().clear();
                totalLabel.setText("SUBTOTAL: R$ 0,00");
                documentoField.clear();
                clienteNaoIdentificadoCheck.setSelected(true);
                
                // Recarregar histórico
                vendas = carregarVendas();
                aplicarFiltros();
                
                mostrarAlerta("Venda registrada", "Produtos adicionados ao carrinho! Finalize o pagamento na próxima tela.", AlertType.INFORMATION);
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("Erro ao registrar venda", "Detalhes: " + ex.getMessage(), AlertType.ERROR);
            }
        });

        Button btnCancelar = new Button("Cancelar Venda");
        btnCancelar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnCancelar.setOnAction(e -> {
            listaProdutos.getItems().clear();
            totalLabel.setText("SUBTOTAL: R$ 0,00");
            documentoField.clear();
            clienteNaoIdentificadoCheck.setSelected(true);
        });

        HBox botoes = new HBox(10, btnFinalizar, btnCancelar);
        botoes.setAlignment(Pos.CENTER);

        // Adicionar componentes ao clienteBox
        clienteBox.getChildren().addAll(
            subtotalContainer, // Subtotal no topo
            clienteNaoIdentificadoCheck,
            new Label("Identificação do Cliente:"),
            tipoDocumentoBox,
            documentoField
        );

        // Container de produtos
        VBox produtosBox = new VBox(10);
        produtosBox.setStyle("-fx-background-color: #00536d; -fx-padding: 15; -fx-background-radius: 5;");
        produtosBox.getChildren().addAll(
            new Label("Produtos:"),
            adicionarProdutoBox,
            listaProdutos
        );

        // Atualizar total quando itens mudarem
        listaProdutos.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends ItemVenda> c) -> {
            double total = calcularTotal(listaProdutos);
            totalLabel.setText("SUBTOTAL: R$ " + String.format("%.2f", total));
        });

        novaVendaContainer.getChildren().addAll(
            clienteBox,
            produtosBox,
            botoes
        );
    }

    private class ItemVendaCell extends ListCell<ItemVenda> {
        @Override
        protected void updateItem(ItemVenda item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hbox = new HBox(10);
                Label nomeLabel = new Label(item.produto);
                nomeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                
                Label detalhesLabel = new Label(
                    String.format("Cód: %s | Qtd: %d | R$ %.2f (un) | R$ %.2f", 
                                item.codigoBarras, item.quantidade, item.preco, 
                                item.quantidade * item.preco)
                );
                detalhesLabel.setStyle("-fx-text-fill: white;");
                
                hbox.getChildren().addAll(nomeLabel, detalhesLabel);
                setGraphic(hbox);
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
        painel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d3d3d3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label idLabel = new Label("Venda #" + venda.id);
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00536d;");

        Label pagamentoLabel = new Label("Pagamento: " + venda.formaPagamento);
        pagamentoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        Label subtotalLabel = new Label("Subtotal: R$ " + String.format("%.2f", venda.subtotal));
        subtotalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        Label totalLabel = new Label("Total: R$ " + String.format("%.2f", venda.total));
        totalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d; -fx-font-weight: bold;");

        Label dataLabel = new Label("Data: " + venda.data);
        dataLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        Button btnDetalhes = new Button("Detalhes");
        btnDetalhes.setStyle("-fx-background-color: #0c5b74; -fx-text-fill: white; -fx-font-size: 12px;");
        btnDetalhes.setOnAction(e -> mostrarDetalhesVenda(venda));

        HBox botoes = new HBox(10, btnDetalhes);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        painel.getChildren().addAll(idLabel, pagamentoLabel, subtotalLabel, totalLabel, dataLabel, botoes);

        return painel;
    }

    private void mostrarDetalhesVenda(Venda venda) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Detalhes da Venda #" + venda.id);

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setStyle("-fx-background-color: #006989;");

        Label idLabel = new Label("Venda #" + venda.id);
        idLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label pagamentoLabel = new Label("Forma de Pagamento: " + venda.formaPagamento);
        pagamentoLabel.setStyle("-fx-text-fill: white;");

        Label dataLabel = new Label("Data: " + venda.data);
        dataLabel.setStyle("-fx-text-fill: white;");

        Label subtotalLabel = new Label("Subtotal: R$ " + String.format("%.2f", venda.subtotal));
        subtotalLabel.setStyle("-fx-text-fill: white;");

        Label totalLabel = new Label("Total: R$ " + String.format("%.2f", venda.total));
        totalLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Tabela de itens
        TableView<ItemVenda> tabelaItens = new TableView<>();
        
        TableColumn<ItemVenda, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().produto));
        
        TableColumn<ItemVenda, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().codigoBarras));
        
        TableColumn<ItemVenda, Integer> colQuantidade = new TableColumn<>("Quantidade");
        colQuantidade.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().quantidade).asObject());
        
        TableColumn<ItemVenda, Double> colPreco = new TableColumn<>("Preço Unitário");
        colPreco.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().preco).asObject());
        
        TableColumn<ItemVenda, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().quantidade * cellData.getValue().preco).asObject());
        
        tabelaItens.getColumns().addAll(colProduto, colCodigo, colQuantidade, colPreco, colTotal);
        tabelaItens.getItems().addAll(venda.itens);
        tabelaItens.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle("-fx-background-color: #0c5b74; -fx-text-fill: white;");
        btnFechar.setOnAction(e -> dialog.close());

        dialogVBox.getChildren().addAll(
            idLabel, pagamentoLabel, dataLabel, subtotalLabel, totalLabel,
            new Label("Itens da Venda:"), tabelaItens, btnFechar
        );

        Scene dialogScene = new Scene(dialogVBox, 600, 400);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void setupHistoricoUI() {
        historicoContainer = new VBox(10);
        historicoContainer.setPadding(new Insets(20));

        Label titulo = new Label("Histórico de Vendas");
        titulo.setStyle("-fx-text-fill: #062e3aff; -fx-font-size: 24px; -fx-font-weight: bold;");

        pesquisaField = new TextField();
        pesquisaField.setPromptText("Pesquisar por ID da venda...");
        pesquisaField.setMaxWidth(300);

        filtroPagamento = new ComboBox<>();
        filtroPagamento.getItems().add("Todos");
        carregarFormasPagamento(filtroPagamento);
        filtroPagamento.setValue("Todos");
        filtroPagamento.setPrefWidth(150);

        Label pagamentoLabel = new Label("Pagamento:");
        pesquisaField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroPagamento.setOnAction(e -> aplicarFiltros());

        HBox filtroBox = new HBox(10, pesquisaField, pagamentoLabel, filtroPagamento);
        filtroBox.setPadding(new Insets(5));

        listaVendas = new VBox(10);
        listaVendas.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(listaVendas);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f4f4f4;");

        historicoContainer.getChildren().addAll(titulo, filtroBox, scroll);
    }

    private void carregarFormasPagamento(ComboBox<String> combo) {
        String query = "SELECT Forma_Pagamento FROM pagamento";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                combo.getItems().add(rs.getString("Forma_Pagamento"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao carregar formas de pagamento", "Detalhes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void alternarTela() {
        if (isHistoricoAtivo) {
            // Mudando para Nova Venda
            mostrarNovaVenda();
            toggleButton.setText("Histórico");
            isHistoricoAtivo = false;
        } else {
            // Mudando para Histórico
            mostrarHistorico();
            toggleButton.setText("Nova Venda");
            isHistoricoAtivo = true;
        }
    }

    private void mostrarNovaVenda() {
        novaVendaContainer.setVisible(true);
        historicoContainer.setVisible(false);
        // Focar no campo de código quando abrir nova venda
        if (codigoProdutoField != null) {
            codigoProdutoField.requestFocus();
        }
    }

    private void mostrarHistorico() {
        novaVendaContainer.setVisible(false);
        historicoContainer.setVisible(true);
        // Recarregar dados quando mostrar histórico
        vendas = carregarVendas();
        aplicarFiltros();
    }

    public void show(Stage stage) {
        this.stage = stage;
        
        // Inicializa os dados
        vendas = carregarVendas();
        
        // Configura a UI
        setupNovaVendaUI();
        setupHistoricoUI();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // Menu lateral
        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        try {
            // Logo SUN PDV
            Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(120);
            logoView.setPreserveRatio(true);
            logoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");

            // Título da tela
            Label titulonaABA = new Label("Caixa");
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
            dataHoraBox.setPadding(new Insets(0, 0, 20, 0));

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

            // Botão único para alternar entre as telas
            toggleButton = criarBotaoLateral("", "/img/icon/casa.png");
            toggleButton.setOnAction(e -> alternarTela());
            
            Button btnVoltarHome = criarBotaoLateral("Home", "/img/icon/casa.png");
            Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

            // Espaço para empurrar os botões de navegação para baixo
            Region espaco = new Region();
            VBox.setVgrow(espaco, Priority.ALWAYS);

            // Ações dos botões - removendo as ações antigas de nova venda e histórico
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

            // Organizar botões do menu - apenas o botão toggle
            VBox menuButtonsBox = new VBox(10, toggleButton);
            menuButtonsBox.setAlignment(Pos.CENTER_LEFT);
            menuButtonsBox.setPadding(new Insets(0, 0, 10, 0));

            VBox navigationButtonsBox = new VBox(10, btnVoltarHome, btnSair);
            navigationButtonsBox.setAlignment(Pos.BOTTOM_LEFT);
            navigationButtonsBox.setPadding(new Insets(0, 0, 20, 0));

            // Adicionar elementos ao menu lateral
            leftMenu.getChildren().addAll(logoBox, dataHoraBox, menuButtonsBox, espaco, navigationButtonsBox);
        } catch (Exception e) {
            System.err.println("Erro ao carregar recursos: " + e.getMessage());
            leftMenu.getChildren().add(new Label("Erro ao carregar recursos"));
        }

        // Container principal que alterna entre as views
        StackPane centerContainer = new StackPane();
        centerContainer.getChildren().addAll(historicoContainer, novaVendaContainer);
        
        // Iniciar com histórico visível (padrão)
        mostrarHistorico();

        BorderPane root = new BorderPane();
        root.setLeft(leftMenu);
        root.setCenter(centerContainer);

        Scene scene = new Scene(root, 1200, 700);
        
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Erro ao carregar CSS: " + e.getMessage());
        }

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                // Impedir que o evento seja processado pelo sistema (evita sair do fullscreen)
                event.consume();
                
                // Executar a mesma ação do botão sair
                CustomConfirmationAlert alert = new CustomConfirmationAlert(
                    stage, 
                    "Confirmação", 
                    "Deseja sair?", 
                    "Isso fechará o sistema."
                );
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        stage.close();
                    }
                });
            }
        });

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Caixa");
        stage.setFullScreenExitHint(""); // Remove a mensagem de ESC
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
        stage.show();
    }
}