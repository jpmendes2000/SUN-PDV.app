package com.sunpdv.telas.operacao;

import java.sql.*;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FinalizarVenda {

    private String documento;
    private String tipoDocumento;
    private List<Caixa.ItemVenda> itens;
    private double totalVenda;
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "Senha@12345!";

    public FinalizarVenda(String documento, String tipoDocumento, List<Caixa.ItemVenda> itens, double totalVenda) {
        this.documento = documento;
        this.tipoDocumento = tipoDocumento;
        this.itens = itens;
        this.totalVenda = totalVenda;
    }

    public void mostrar(Stage owner, Caixa caixa) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Finalizar Venda");

        // Layout principal
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #006989;");

        // Total da venda
        Label totalLabel = new Label("Total da Venda: R$ " + String.format("%.2f", totalVenda));
        totalLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Seleção de forma de pagamento
        Label pagamentoLabel = new Label("Forma de Pagamento:");
        pagamentoLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        ComboBox<String> formaPagamentoCombo = new ComboBox<>();
        formaPagamentoCombo.setPrefWidth(200);
        carregarFormasPagamento(formaPagamentoCombo);
        formaPagamentoCombo.setValue("Dinheiro"); // Valor padrão

        // Valor recebido
        Label valorRecebidoLabel = new Label("Valor Recebido:");
        valorRecebidoLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        TextField valorRecebidoField = new TextField();
        valorRecebidoField.setPromptText("Digite o valor recebido");
        valorRecebidoField.setMaxWidth(200);

        // Troco
        Label trocoLabel = new Label("Troco: R$ 0,00");
        trocoLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Atualizar troco dinamicamente
        valorRecebidoField.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                double valorRecebido = newValue.isEmpty() ? 0 : Double.parseDouble(newValue.replace(",", "."));
                double troco = valorRecebido - totalVenda;
                trocoLabel.setText("Troco: R$ " + String.format("%.2f", troco >= 0 ? troco : 0));
            } catch (NumberFormatException e) {
                trocoLabel.setText("Troco: R$ 0,00");
            }
        });

        // Botões
        Button btnConfirmar = new Button("Confirmar");
        btnConfirmar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnConfirmar.setOnAction(e -> {
            String formaPagamento = formaPagamentoCombo.getValue();
            String valorRecebidoStr = valorRecebidoField.getText().replace(",", ".");

            if (formaPagamento == null) {
                mostrarAlerta("Erro", "Selecione uma forma de pagamento.", Alert.AlertType.ERROR);
                return;
            }

            if (valorRecebidoStr.isEmpty() && !formaPagamento.equalsIgnoreCase("Cartão de Crédito") && 
                !formaPagamento.equalsIgnoreCase("Cartão de Débito")) {
                mostrarAlerta("Erro", "Digite o valor recebido.", Alert.AlertType.ERROR);
                return;
            }

            try {
                double valorRecebido = valorRecebidoStr.isEmpty() ? totalVenda : Double.parseDouble(valorRecebidoStr);
                double troco = valorRecebido - totalVenda;

                if (troco < 0 && !formaPagamento.equalsIgnoreCase("Cartão de Crédito") && 
                    !formaPagamento.equalsIgnoreCase("Cartão de Débito")) {
                    mostrarAlerta("Erro", "O valor recebido é insuficiente.", Alert.AlertType.ERROR);
                    return;
                }

                processarVenda(formaPagamento, valorRecebido, troco);
                caixa.limparVendaAtual();
                mostrarAlerta("Sucesso", "Venda finalizada com sucesso!", Alert.AlertType.INFORMATION);
                stage.close();
            } catch (NumberFormatException ex) {
                mostrarAlerta("Erro", "Valor recebido inválido.", Alert.AlertType.ERROR);
            } catch (SQLException ex) {
                ex.printStackTrace();
                mostrarAlerta("Erro", "Erro ao salvar a venda: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnCancelar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnConfirmar, btnCancelar);
        botoes.setAlignment(Pos.CENTER);

        // Adicionar componentes ao layout
        root.getChildren().addAll(
            totalLabel,
            pagamentoLabel,
            formaPagamentoCombo,
            valorRecebidoLabel,
            valorRecebidoField,
            trocoLabel,
            botoes
        );

        // Configurar a cena
        Scene scene = new Scene(root, 400, 350);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Erro ao carregar CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.showAndWait();
    }

    private void processarVenda(String formaPagamento, double valorRecebido, double troco) throws SQLException {
        Connection conn = null;
        PreparedStatement stmtVenda = null;
        PreparedStatement stmtCarrinho = null;
        PreparedStatement stmtPagamento = null;
        ResultSet generatedKeys = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            // Inserir na tabela pagamentos
            String sqlPagamento = "INSERT INTO pagamentos (ID_Forma_Pagamento, Valor_Recebido, Troco) " +
                                 "VALUES ((SELECT ID_Forma_Pagamento FROM forma_pagamento WHERE Forma_Pagamento = ?), ?, ?)";
            stmtPagamento = conn.prepareStatement(sqlPagamento, Statement.RETURN_GENERATED_KEYS);
            stmtPagamento.setString(1, formaPagamento);
            stmtPagamento.setDouble(2, valorRecebido);
            stmtPagamento.setDouble(3, troco);
            stmtPagamento.executeUpdate();

            generatedKeys = stmtPagamento.getGeneratedKeys();
            int idPagamento = generatedKeys.next() ? generatedKeys.getInt(1) : -1;

            // Inserir na tabela carrinho
            String sqlCarrinho = "INSERT INTO carrinho (ID_Produto, Quantidade, PrecoUnitario) VALUES (?, ?, ?)";
            stmtCarrinho = conn.prepareStatement(sqlCarrinho, Statement.RETURN_GENERATED_KEYS);
            int idCarrinho = -1;
            for (Caixa.ItemVenda item : itens) {
                stmtCarrinho.setInt(1, buscarIdProduto(item.codigoBarras, conn));
                stmtCarrinho.setInt(2, item.quantidade);
                stmtCarrinho.setDouble(3, item.preco);
                stmtCarrinho.executeUpdate();
                generatedKeys = stmtCarrinho.getGeneratedKeys();
                if (generatedKeys.next()) {
                    idCarrinho = generatedKeys.getInt(1);
                }
            }

            // Inserir na tabela vendas
            String sqlVenda = "INSERT INTO vendas (ID_Carrinho, ID_Pagamentos, Subtotal, Total, Data_Venda, Documento, Tipo_Documento) " +
                             "VALUES (?, ?, ?, ?, GETDATE(), ?, ?)";
            stmtVenda = conn.prepareStatement(sqlVenda);
            stmtVenda.setInt(1, idCarrinho);
            stmtVenda.setInt(2, idPagamento);
            stmtVenda.setDouble(3, totalVenda);
            stmtVenda.setDouble(4, totalVenda);
            stmtVenda.setString(5, documento.isEmpty() ? null : documento);
            stmtVenda.setString(6, tipoDocumento.isEmpty() ? null : tipoDocumento);
            stmtVenda.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (stmtVenda != null) stmtVenda.close();
            if (stmtCarrinho != null) stmtCarrinho.close();
            if (stmtPagamento != null) stmtPagamento.close();
            if (conn != null) conn.close();
        }
    }

    private void carregarFormasPagamento(ComboBox<String> combo) {
        String query = "SELECT Forma_Pagamento FROM forma_pagamento";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                combo.getItems().add(rs.getString("Forma_Pagamento"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao carregar formas de pagamento: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private int buscarIdProduto(String codigoBarras, Connection conn) throws SQLException {
        String query = "SELECT ID_Produto FROM produtos WHERE Cod_Barras = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codigoBarras);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("ID_Produto") : -1;
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}