package com.sunpdv.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Método para conexão com o banco de dados

public class ConexaoDB {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "SUNserver!";

    // Try para caso haja erro de conexão
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro na conexão: " + e.getMessage());
            return null;
        }
    }
}
