package com.sunpdv.telas.operacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import com.sunpdv.model.AutenticarUser;
import com.sunpdv.telas.home.TelaHomeADM;
import com.sunpdv.telas.home.TelaHomeFUN;
import com.sunpdv.telas.home.TelaHomeMOD;

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
    private ToggleButton toggleViewButton;
    private CheckBox clienteNaoIdentificadoCheck;
    private TextField primeiraFormaValor;
    private ComboBox<String> primeiraFormaCombo;
    private ComboBox<String> segundaFormaCombo;
    private TextField segundaFormaValor;
    private CheckBox usarSegundaFormaCheck;

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
                      "CONVERT(VARCHAR, v.Data_Venda, 103) AS Data " +
                      "FROM vendas v " +
                      "JOIN pagamento p ON v.ID_Pagamento = p.ID_Pagamento " +
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
            boolean pagamentoMatch = pagamentoSelecionado.equals("Todos") || 
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
        novaVendaContainer = new VBox(10);
        novaVendaContainer.setPadding(new Insets(20));
        
        // Seção de identificação do cliente
        VBox clienteBox = new VBox(10);
        clienteBox.setStyle("-fx-background-color: #00536d; -fx-padding: 15; -fx-background-radius: 5;");
        
        clienteNaoIdentificadoCheck = new CheckBox("Cliente não identificado");
        clienteNaoIdentificadoCheck.setSelected(true);
        clienteNaoIdentificadoCheck.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
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

        // Lista de produtos
        ListView<ItemVenda> listaProdutos = new ListView<>();
        listaProdutos.setPrefHeight(200);
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
        TextField codigoProdutoField = new TextField();
        codigoProdutoField.setPromptText("Código de barras do produto");
        codigoProdutoField.setMaxWidth(300);
        
        // Adicionar produto ao pressionar Enter
        codigoProdutoField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                adicionarProduto(codigoProdutoField, listaProdutos);
            }
        });

        Spinner<Integer> quantidadeSpinner = new Spinner<>(1, 100, 1);
        quantidadeSpinner.setEditable(true);
        
        Button btnAdicionar = new Button("Adicionar Produto");
        btnAdicionar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnAdicionar.setOnAction(e -> adicionarProduto(codigoProdutoField, listaProdutos));

        // Forma de pagamento
        primeiraFormaCombo = new ComboBox<>();
        carregarFormasPagamento(primeiraFormaCombo);
        
        primeiraFormaValor = new TextField();
        primeiraFormaValor.setPromptText("Valor");
        primeiraFormaValor.setPrefWidth(100);
        
        // Segunda forma de pagamento
        segundaFormaCombo = new ComboBox<>();
        carregarFormasPagamento(segundaFormaCombo);
        
        segundaFormaValor = new TextField();
        segundaFormaValor.setPromptText("Valor");
        segundaFormaValor.setPrefWidth(100);
        segundaFormaValor.setDisable(true);
        segundaFormaCombo.setDisable(true);
        
        usarSegundaFormaCheck = new CheckBox("Usar segunda forma de pagamento");
        usarSegundaFormaCheck.setStyle("-fx-text-fill: white;");
        usarSegundaFormaCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            segundaFormaCombo.setDisable(!newVal);
            segundaFormaValor.setDisable(!newVal);
            if (!newVal) {
                segundaFormaCombo.getSelectionModel().clearSelection();
                segundaFormaValor.clear();
            }
        });

        // Total da venda
        Label totalLabel = new Label("SUBTOTAL: R$ 0,00");
        totalLabel.setStyle("-fx-text-fill: #c7eefaff; -fx-font-weight: bold; -fx-font-size: 30px;");

        // Botão para calcular troco
        Button btnCalcularTroco = new Button("Calcular Troco");
        btnCalcularTroco.setStyle("-fx-background-color: #0c5b74; -fx-text-fill: white;");
        btnCalcularTroco.setOnAction(e -> {
            try {
                double total = calcularTotal(listaProdutos);
                double valor1 = primeiraFormaValor.getText().isEmpty() ? 0 : Double.parseDouble(primeiraFormaValor.getText());
                double valor2 = segundaFormaValor.getText().isEmpty() || !usarSegundaFormaCheck.isSelected() ? 0 : 
                              Double.parseDouble(segundaFormaValor.getText());
                
                double troco = (valor1 + valor2) - total;
                
                if (troco > 0) {
                    mostrarAlerta("Troco", String.format("Troco: R$ %.2f", troco), AlertType.INFORMATION);
                } else if (troco == 0) {
                    mostrarAlerta("Pagamento exato", "Valor pago igual ao total da venda.", AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Pagamento insuficiente", 
                        String.format("Faltam R$ %.2f para completar o pagamento.", Math.abs(troco)), AlertType.ERROR);
                }
            } catch (NumberFormatException ex) {
                mostrarAlerta("Erro", "Digite valores válidos nos campos de pagamento.", AlertType.ERROR);
            }
        });

        // Botões
        Button btnFinalizar = new Button("Finalizar Venda");
        btnFinalizar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnFinalizar.setPadding(new Insets(10, 60, 10, 60));
        btnFinalizar.setOnAction(e -> {
            try {
                if (listaProdutos.getItems().isEmpty()) {
                    mostrarAlerta("Venda vazia", "Adicione pelo menos um produto para finalizar a venda.", AlertType.ERROR);
                    return;
                }

                if (primeiraFormaCombo.getValue() == null) {
                    mostrarAlerta("Forma de pagamento", "Selecione pelo menos uma forma de pagamento.", AlertType.ERROR);
                    return;
                }

                double totalVenda = calcularTotal(listaProdutos);
                double valor1 = primeiraFormaValor.getText().isEmpty() ? 0 : Double.parseDouble(primeiraFormaValor.getText());
                double valor2 = 0;
                
                if (usarSegundaFormaCheck.isSelected()) {
                    if (segundaFormaCombo.getValue() == null) {
                        mostrarAlerta("Forma de pagamento", "Selecione a segunda forma de pagamento.", AlertType.ERROR);
                        return;
                    }
                    valor2 = segundaFormaValor.getText().isEmpty() ? 0 : Double.parseDouble(segundaFormaValor.getText());
                }
                
                if ((valor1 + valor2) < totalVenda) {
                    mostrarAlerta("Pagamento insuficiente", 
                        String.format("Valor pago (R$ %.2f) é menor que o total da venda (R$ %.2f)", 
                        (valor1 + valor2), totalVenda), AlertType.ERROR);
                    return;
                }

                if (rbCPF.isSelected() && !documentoField.getText().isEmpty() && !validarCPF(documentoField.getText())) {
                    mostrarAlerta("CPF inválido", "O CPF digitado não é válido.", AlertType.ERROR);
                    return;
                }

                String documento = clienteNaoIdentificadoCheck.isSelected() ? "" : documentoField.getText();
                String tipoDocumento = clienteNaoIdentificadoCheck.isSelected() ? "" : 
                                     (rbCPF.isSelected() ? "CPF" : (rbCNPJ.isSelected() ? "CNPJ" : "RG"));

                // Salvar venda no banco de dados
                salvarVendaNoBanco(documento, tipoDocumento, listaProdutos, primeiraFormaCombo.getValue(), 
                                   valor1, usarSegundaFormaCheck.isSelected() ? segundaFormaCombo.getValue() : null, 
                                   valor2, totalVenda);
                
                // Limpar campos após venda
                listaProdutos.getItems().clear();
                totalLabel.setText("SUBTOTAL: R$ 0,00");
                documentoField.clear();
                clienteNaoIdentificadoCheck.setSelected(true);
                primeiraFormaCombo.getSelectionModel().clearSelection();
                primeiraFormaValor.clear();
                usarSegundaFormaCheck.setSelected(false);
                
                // Recarregar histórico
                vendas = carregarVendas();
                aplicarFiltros();
                
                mostrarAlerta("Venda finalizada", "Venda registrada com sucesso!", AlertType.INFORMATION);
            } catch (SQLException ex) {
                ex.printStackTrace();
                mostrarAlerta("Erro ao finalizar venda", "Detalhes: " + ex.getMessage(), AlertType.ERROR);
            } catch (NumberFormatException ex) {
                mostrarAlerta("Valor inválido", "Digite valores numéricos válidos para os pagamentos.", AlertType.ERROR);
            }
        });

        Button btnCancelar = new Button("Cancelar Venda");
        btnCancelar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnCancelar.setOnAction(e -> {
            listaProdutos.getItems().clear();
            totalLabel.setText("SUBTOTAL: R$ 0,00");
            documentoField.clear();
            clienteNaoIdentificadoCheck.setSelected(true);
            primeiraFormaCombo.getSelectionModel().clearSelection();
            primeiraFormaValor.clear();
            usarSegundaFormaCheck.setSelected(false);
        });

        HBox botoes = new HBox(10, btnFinalizar, btnCancelar, btnCalcularTroco);
        botoes.setAlignment(Pos.CENTER);

        clienteBox.getChildren().addAll(
            clienteNaoIdentificadoCheck,
            new Label("Identificação do Cliente:"),
            tipoDocumentoBox,
            documentoField
        );

        VBox produtosBox = new VBox(10);
        produtosBox.setStyle("-fx-background-color: #00536d; -fx-padding: 15; -fx-background-radius: 5;");
        produtosBox.getChildren().addAll(
            new Label("Produtos:"),
            listaProdutos,
            new Label("Adicionar Produto:"),
            new HBox(10, codigoProdutoField, new Label("Qtd:"), quantidadeSpinner),
            btnAdicionar
        );

        VBox pagamentoBox = new VBox(10);
        pagamentoBox.setStyle("-fx-background-color: #00536d; -fx-padding: 15; -fx-background-radius: 5;");
        pagamentoBox.getChildren().addAll(
            new Label("Forma de Pagamento:"),
            new HBox(10, new Label("1ª Forma:"), primeiraFormaCombo, primeiraFormaValor),
            usarSegundaFormaCheck,
            new HBox(10, new Label("2ª Forma:"), segundaFormaCombo, segundaFormaValor),
            totalLabel
        );

        // Atualizar total quando itens mudarem
        listaProdutos.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends ItemVenda> c) -> {
            double total = calcularTotal(listaProdutos);
            totalLabel.setText("TOTAL: R$ " + String.format("%.2f", total));
            
            // Atualizar automaticamente o valor da primeira forma de pagamento
            if (primeiraFormaValor.getText().isEmpty() || 
                (primeiraFormaValor.getText().equals("0") || primeiraFormaValor.getText().equals("0.00"))) {
                primeiraFormaValor.setText(String.format("%.2f", total));
            }
        });

        novaVendaContainer.getChildren().addAll(
            clienteBox,
            produtosBox,
            pagamentoBox,
            botoes
        );
    }

    private void adicionarProduto(TextField codigoProdutoField, ListView<ItemVenda> listaProdutos) {
        String codigo = codigoProdutoField.getText().trim();
        if (!codigo.isEmpty()) {
            try {
                String produto = buscarProdutoPorCodigo(codigo);
                if (produto != null) {
                    double preco = buscarPrecoProduto(codigo);
                    
                    // Verificar se o produto já está na lista
                    Optional<ItemVenda> existente = listaProdutos.getItems().stream()
                        .filter(item -> item.codigoBarras.equals(codigo))
                        .findFirst();
                    
                    if (existente.isPresent()) {
                        // Se já existe, apenas aumentar a quantidade
                        ItemVenda item = existente.get();
                        item.quantidade++;
                        listaProdutos.refresh();
                    } else {
                        // Se não existe, adicionar novo item
                        listaProdutos.getItems().add(new ItemVenda(produto, codigo, 1, preco));
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

    private double calcularTotal(ListView<ItemVenda> listaProdutos) {
        return listaProdutos.getItems().stream()
            .mapToDouble(item -> item.quantidade * item.preco)
            .sum();
    }

    private void salvarVendaNoBanco(String documento, String tipoDocumento, ListView<ItemVenda> itens, 
                                  String formaPagamento1, double valor1, String formaPagamento2, 
                                  double valor2, double total) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            // 1. Salvar o carrinho
            String insertCarrinho = "INSERT INTO carrinho (ID_Produto, CodBarras, Quantidade, PrecoUnitario, SubTotal) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmtCarrinho = conn.prepareStatement(insertCarrinho, Statement.RETURN_GENERATED_KEYS);
            
            for (ItemVenda item : itens.getItems()) {
                String queryProduto = "SELECT ID_Produto FROM produtos WHERE Cod_Barras = ?";
                PreparedStatement stmtProduto = conn.prepareStatement(queryProduto);
                stmtProduto.setString(1, item.codigoBarras);
                ResultSet rsProduto = stmtProduto.executeQuery();
                
                if (rsProduto.next()) {
                    int idProduto = rsProduto.getInt("ID_Produto");
                    
                    stmtCarrinho.setInt(1, idProduto);
                    stmtCarrinho.setString(2, item.codigoBarras);
                    stmtCarrinho.setInt(3, item.quantidade);
                    stmtCarrinho.setDouble(4, item.preco);
                    stmtCarrinho.setDouble(5, item.quantidade * item.preco);
                    stmtCarrinho.addBatch();
                }
            }
            stmtCarrinho.executeBatch();
            ResultSet rsCarrinho = stmtCarrinho.getGeneratedKeys();
            int idCarrinho = rsCarrinho.next() ? rsCarrinho.getInt(1) : 0;

            // 2. Salvar a venda (usando apenas a primeira forma de pagamento na tabela vendas)
            String queryPagamento = "SELECT ID_Pagamento FROM pagamento WHERE Forma_Pagamento = ?";
            PreparedStatement stmtPagamento = conn.prepareStatement(queryPagamento);
            stmtPagamento.setString(1, formaPagamento1);
            ResultSet rsPagamento = stmtPagamento.executeQuery();
            int idPagamento = rsPagamento.next() ? rsPagamento.getInt("ID_Pagamento") : 0;

            String insertVenda = "INSERT INTO vendas (Subtotal, ID_Pagamento, Total, Data_Venda, ID_Carrinho, ID_Login) " +
                               "VALUES (?, ?, ?, GETDATE(), ?, ?)";
            PreparedStatement stmtVenda = conn.prepareStatement(insertVenda);
            stmtVenda.setDouble(1, total);
            stmtVenda.setInt(2, idPagamento);
            stmtVenda.setDouble(3, total);
            stmtVenda.setInt(4, idCarrinho);
            stmtVenda.setInt(5, AutenticarUser.getIdPermissao());
            stmtVenda.executeUpdate();
            
            // Se houver documento, salvar no cliente (opcional)
            if (!documento.isEmpty() && !tipoDocumento.isEmpty()) {
                // Implementação opcional conforme necessidade
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
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

    private void mostrarAlerta(String titulo, String mensagem, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void show(Stage stage) {
        this.stage = stage;
        vendas = carregarVendas();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        logoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        Label titulonaABA = new Label("Caixa");
        titulonaABA.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        Button btnVoltarHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        VBox buttonBox = new VBox(10, btnVoltarHome, btnSair);
        buttonBox.setAlignment(Pos.TOP_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        leftMenu.getChildren().addAll(logoBox, new Region(), buttonBox);
        VBox.setVgrow(leftMenu.getChildren().get(1), Priority.ALWAYS);

        // Configurar as duas views
        setupNovaVendaUI();
        setupHistoricoUI();

        // Botão para alternar entre as views
        toggleViewButton = new ToggleButton("Nova Venda");
        toggleViewButton.setStyle("-fx-background-color: #e8ba23; -fx-text-fill: black; -fx-font-weight: bold;");
        toggleViewButton.setSelected(false);
        toggleViewButton.setOnAction(e -> {
            if (toggleViewButton.isSelected()) {
                toggleViewButton.setText("Histórico");
                novaVendaContainer.setVisible(true);
                historicoContainer.setVisible(false);
            } else {
                toggleViewButton.setText("Nova Venda");
                novaVendaContainer.setVisible(false);
                historicoContainer.setVisible(true);
            }
        });

        // Container principal que alterna entre as views
        StackPane centerContainer = new StackPane();
        centerContainer.getChildren().addAll(historicoContainer, novaVendaContainer);
        novaVendaContainer.setVisible(false);

        // Posicionar o botão de alternância
        AnchorPane containerCentral = new AnchorPane(centerContainer, toggleViewButton);
        AnchorPane.setTopAnchor(centerContainer, 0.0);
        AnchorPane.setBottomAnchor(centerContainer, 0.0);
        AnchorPane.setLeftAnchor(centerContainer, 0.0);
        AnchorPane.setRightAnchor(centerContainer, 0.0);

        AnchorPane.setTopAnchor(toggleViewButton, 10.0);
        AnchorPane.setRightAnchor(toggleViewButton, 10.0);

        BorderPane root = new BorderPane();
        root.setLeft(leftMenu);
        root.setCenter(containerCentral);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Caixa");
        stage.setFullScreen(true);
        stage.show();

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
    }
}