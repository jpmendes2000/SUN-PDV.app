module com.sunpdv {
    // Requisitos básicos
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.desktop;
    
    // Abre pacotes necessários para o JavaFX
    opens com.sunpdv to javafx.fxml;
    opens com.sunpdv.model to javafx.base, javafx.fxml;
    opens com.sunpdv.telas.home to javafx.fxml;
    opens com.sunpdv.telas.operacao to javafx.fxml;
    
    // Exportações
    exports com.sunpdv;
    exports com.sunpdv.telas.home;
    exports com.sunpdv.model;  // Adicionado para permitir reflexão
    exports com.sunpdv.telas.operacao to javafx.graphics;
}