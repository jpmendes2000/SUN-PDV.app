module com.sunpdv {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;  // Adicionado explicitamente
    requires java.sql;
    // requires mssql.jdbc;  // Mantido comentado caso precise no futuro
    
    // Permissões para FXML (se aplicável)
    opens com.sunpdv to javafx.fxml;
    
    // Permissões para a tela HomeADM
    opens com.sunpdv.home to javafx.graphics, javafx.fxml;
    
    // Exportações
    exports com.sunpdv;
    exports com.sunpdv.home;  // Adicionado para permitir acesso do JavaFX
    exports com.sunpdv.model;

    opens com.sunpdv.model to javafx.base;
}