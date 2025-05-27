package com.sunpdv.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDB {

    private static final String URL = "jdbc:sqlserver://localhost:1433;"
                                    + "databaseName=SUN_PDVlocal;"
                                    + "encrypt=false;"
                                    + "trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "Mendes@12345!";

    private static Connection conn = null;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                System.out.println("Tentando conectar ao banco de dados...");
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexão estabelecida com sucesso!");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            }
        }
        return conn;
    }
}
