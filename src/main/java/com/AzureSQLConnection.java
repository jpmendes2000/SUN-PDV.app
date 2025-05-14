// Arquivo: AzureSQLConnection.java
package com;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AzureSQLConnection {
    private static final String 
        URL = "jdbc:sqlserver://serverpdv.database.windows.net:1433;"
            + "database=SUN_PDVcloud;"
            + "user=adminuser@serverpdv;"
            + "password=Jp081007!;"
            + "encrypt=true;"
            + "trustServerCertificate=false;"
            + "hostNameInCertificate=*.database.windows.net;"
            + "loginTimeout=30;";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
