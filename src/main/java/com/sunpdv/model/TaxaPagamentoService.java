package com.sunpdv.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaxaPagamentoService {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "Senha@12345!";

    // Obtém a taxa de pagamento do banco de dados
    public double obterTaxa(String formaPagamento) throws SQLException {
        String query = "SELECT Taxa FROM pagamento WHERE Forma_Pagamento = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, formaPagamento);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble("Taxa") : 0.0;
        }
    }

    // Salva a taxa de pagamento no banco de dados
    public void salvarTaxa(String formaPagamento, double taxa) throws SQLException {
        String query = "UPDATE pagamento SET Taxa = ? WHERE Forma_Pagamento = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, taxa);
            stmt.setString(2, formaPagamento);
            stmt.executeUpdate();
        }
    }
}