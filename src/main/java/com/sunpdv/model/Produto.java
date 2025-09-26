package com.sunpdv.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// Classe produto para definir o table view usando a funcionalidade: (SimpleDoubleProperty / SimpleIntegerProperty / SimpleStringProperty) para exibir os valores
public class Produto {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty nome;
    private final SimpleStringProperty codBarras;
    private final SimpleDoubleProperty preco;

    public Produto(int id, String nome, double preco, String codBarras) {
        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(nome);
        this.preco = new SimpleDoubleProperty(preco);
        this.codBarras = new SimpleStringProperty(codBarras);
    }

    // Getters para PropertyValueFactory
    public int getId() { return id.get(); }
    public String getNome() { return nome.get(); }
    public double getPreco() { return preco.get(); }
    public String getCodBarras() { return codBarras.get(); }


    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setNome(String nome) { this.nome.set(nome); }
    public void setPreco(double preco) { this.preco.set(preco); }
    public void setCodBarras(String codBarras) { this.codBarras.set(codBarras); }


    // Property getters
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty nomeProperty() { return nome; }
    public SimpleDoubleProperty precoProperty() { return preco; }
    public SimpleStringProperty codBarrasProperty() { return codBarras; }

}