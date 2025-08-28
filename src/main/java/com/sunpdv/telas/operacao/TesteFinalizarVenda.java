package com.sunpdv.telas.operacao;

import javafx.application.Application;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class TesteFinalizarVenda extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 🔹 Simula alguns itens de venda
        List<Caixa.ItemVenda> itens = new ArrayList<>();
        itens.add(new Caixa.ItemVenda("7891234567890", "Produto Teste", 2, 5.50));
        itens.add(new Caixa.ItemVenda("7899876543210", "Outro Produto", 1, 10.00));

        // 🔹 Documento e tipo de documento fictício
        String documento = "12345678900";
        String tipoDocumento = "CPF";

        // 🔹 Total da venda
        double totalVenda = 21.00;

        // 🔹 Cria tela de finalizar venda
        FinalizarVenda finalizarVenda = new FinalizarVenda(documento, tipoDocumento, itens, totalVenda);

        // 🔹 Aqui você ainda não tem o Caixa real, mas pode passar null só pra testar a interface
        finalizarVenda.mostrar(primaryStage, null);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
