module com.sunpdv {
    // Requisitos básicos
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    
    // Abre pacotes necessários para o JavaFX
    opens com.sunpdv to javafx.fxml;
    opens com.sunpdv.model to javafx.base, javafx.fxml;
    opens com.sunpdv.home to javafx.fxml;
    
    // Exportações
    exports com.sunpdv;
    exports com.sunpdv.home;
    exports com.sunpdv.model;  // Adicionado para permitir reflexão
}