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

                processarVenda(formaPagamento, troco);
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

    private void processarVenda(String formaPagamento, double troco) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);

            // Ajustes no esquema (executam uma vez se necessário)
            ajustarEsquema(conn);

            // Inserir cliente se aplicável
            Integer idCliente = null;
            if (!documento.isEmpty()) {
                idCliente = inserirCliente(conn);
            }

            // Inserir pagamento
            int idPagamento = inserirPagamento(conn, formaPagamento, troco);

            // Inserir carrinho
            int idCarrinho = inserirCarrinho(conn);

            // Inserir itens do carrinho
            inserirItensCarrinho(conn, idCarrinho);

            // Inserir venda
            inserirVenda(conn, idCarrinho, idPagamento, idCliente);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void ajustarEsquema(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        // Adicionar Data_Criacao a carrinho se não existir
        ResultSet rs = conn.getMetaData().getColumns(null, null, "carrinho", "Data_Criacao");
        if (!rs.next()) {
            stmt.execute("ALTER TABLE carrinho ADD Data_Criacao DATETIME");
        }

        // Tornar ID_Produto nullable em carrinho
        // (Não há comando direto para alterar nullability no SQL Server sem recriar, mas assumimos que é ajustado manualmente ou ignoramos se já nullable)

        // Criar tabela carrinho_itens se não existir
        rs = conn.getMetaData().getTables(null, null, "carrinho_itens", null);
        if (!rs.next()) {
            stmt.execute("CREATE TABLE carrinho_itens (" +
                "ID_Carrinho_itens INT PRIMARY KEY IDENTITY(1,1)," +
                "ID_Carrinho INT NOT NULL," +
                "ID_Produto INT NOT NULL," +
                "Quantidade INT NOT NULL," +
                "PrecoUnitario DECIMAL(10,2) NOT NULL," +
                "CONSTRAINT FK_carrinho_itens_carrinho FOREIGN KEY (ID_Carrinho) REFERENCES carrinho(ID_Carrinho)," +
                "CONSTRAINT FK_carrinho_itens_produtos FOREIGN KEY (ID_Produto) REFERENCES produtos(ID_Produto)" +
                ")");
        }

        stmt.close();
    }

    private Integer inserirCliente(Connection conn) throws SQLException {
        String column = "";
        if ("CPF".equals(tipoDocumento)) {
            column = "cpf";
        } else if ("CNPJ".equals(tipoDocumento)) {
            column = "cnpj";
        } else if ("RG".equals(tipoDocumento)) {
            column = "rg";
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

    private int inserirPagamento(Connection conn, String formaPagamento, double troco) throws SQLException {
        String sql = "INSERT INTO pagamentos (Qtd_Pagamentos, ID_Forma_Pagamento, Troco) " +
                     "VALUES (1, (SELECT ID_Forma_Pagamento FROM forma_pagamento WHERE Forma_Pagamento = ?), ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, formaPagamento);
            stmt.setDouble(2, troco);
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
        String sql = "INSERT INTO carrinho_itens (ID_Carrinho, ID_Produto, Quantidade, PrecoUnitario) " +
                     "VALUES (?, (SELECT ID_Produto FROM produtos WHERE Cod_Barras = ?), ?, ?)";
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

    private void inserirVenda(Connection conn, int idCarrinho, int idPagamento, Integer idCliente) throws SQLException {
        String sql = "INSERT INTO vendas (ID_Carrinho, ID_Pagamentos, Subtotal, Total, Data_Venda, ID_Clientes) " +
                     "VALUES (?, ?, ?, ?, GETDATE(), ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCarrinho);
            stmt.setInt(2, idPagamento);
            stmt.setDouble(3, totalVenda);
            stmt.setDouble(4, totalVenda);
            if (idCliente != null) {
                stmt.setInt(5, idCliente);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.executeUpdate();
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

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}