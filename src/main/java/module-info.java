module com.sunpdv {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    //requires mssql.jdbc;    
 
    opens com.sunpdv to javafx.fxml;
    exports com.sunpdv;
}
