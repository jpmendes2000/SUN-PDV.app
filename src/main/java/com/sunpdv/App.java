package com.sunpdv;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    // Cena principal da aplicação que será reutilizada para trocar telas
    private static Scene scene;

    /**
     * Método start é chamado automaticamente quando a aplicação inicia.
     * Configura a janela principal (Stage), carrega a cena inicial e exibe.
     */
    @Override
    public void start(Stage stage) {
        try {
            // Carrega o arquivo FXML "primary.fxml" e cria a cena inicial de 640x480 px
            scene = new Scene(loadFXML("primary"), 640, 480);

            // Define a cena no palco (janela)
            stage.setScene(scene);

            // Define título da janela
            stage.setTitle("SUN PDV");

            // Mostra a janela na tela
            stage.show();
        } catch (IOException e) {
            // Caso não encontre ou não consiga carregar o arquivo FXML, imprime erro no console
            e.printStackTrace();
            System.err.println("Erro ao carregar o arquivo FXML: " + e.getMessage());
        }
    }

    /**
     * Método para trocar o conteúdo da cena principal, alterando a raiz para outro FXML.
     * @param fxml nome do arquivo FXML (sem extensão) a ser carregado
     */
    public static void setRoot(String fxml) {
        try {
            // Troca o root da cena principal para o novo carregado pelo loadFXML
            scene.setRoot(loadFXML(fxml));
        } catch (IOException e) {
            // Em caso de falha na troca, imprime erro no console
            e.printStackTrace();
            System.err.println("Erro ao trocar o root para: " + fxml);
        }
    }

    /**
     * Método auxiliar para carregar arquivos FXML pelo nome.
     * @param fxml nome do arquivo FXML (sem extensão)
     * @return o Parent raiz do layout carregado
     * @throws IOException se o arquivo não for encontrado ou não puder ser carregado
     */
    private static Parent loadFXML(String fxml) throws IOException {
        // Cria um FXMLLoader apontando para o recurso dentro do pacote da classe App
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));

        // Verifica se o recurso foi encontrado
        if (loader.getLocation() == null) {
            throw new IOException("Arquivo FXML não encontrado: " + fxml + ".fxml");
        }

        // Carrega e retorna a raiz do layout
        return loader.load();
    }

    /**
     * Método main que inicia a aplicação JavaFX
     */
    public static void main(String[] args) {
        // Chama o launch para iniciar o JavaFX e executar start()
        launch();
    }
}
