package com.sunpdv.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Produto {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty nome;
    private final SimpleStringProperty codBarras;
    private final SimpleDoubleProperty preco;

    public Produto(int id, String nome, String codBarras, double preco) {
        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(nome);
        this.codBarras = new SimpleStringProperty(codBarras);
        this.preco = new SimpleDoubleProperty(preco);
    }

    // Getters para PropertyValueFactory
    public int getId() { return id.get(); }
    public String getNome() { return nome.get(); }
    public String getCodBarras() { return codBarras.get(); }
    public double getPreco() { return preco.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setNome(String nome) { this.nome.set(nome); }
    public void setCodBarras(String codBarras) { this.codBarras.set(codBarras); }
    public void setPreco(double preco) { this.preco.set(preco); }

    // Property getters
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty nomeProperty() { return nome; }
    public SimpleStringProperty codBarrasProperty() { return codBarras; }
    public SimpleDoubleProperty precoProperty() { return preco; }
}