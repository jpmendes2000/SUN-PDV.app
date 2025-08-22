package com.sunpdv.telas.operacao;

import java.sql.*;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FinalizarVenda {

    private String documento;
    private String tipoDocumento;
    private List<Caixa.ItemVenda> itens;
    private double totalVenda;

    public FinalizarVenda(String documento, String tipoDocumento, List<Caixa.ItemVenda> itens, double totalVenda) {
        this.documento = documento;
        this.tipoDocumento = tipoDocumento;
        this.itens = itens;
        this.totalVenda = totalVenda;
    }

    public void mostrar(Stage owner, Caixa caixa) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Finalizar Venda");

        // Implementar aqui a interface de seleção de forma de pagamento
        // e processamento da venda

        stage.showAndWait();
    }

    private void processarVenda(String formaPagamento, double valorRecebido, double troco) {
        // Implementar a lógica para salvar a venda no banco de dados
    }
}