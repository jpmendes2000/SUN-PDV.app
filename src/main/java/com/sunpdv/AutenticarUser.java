package com.sunpdv;

public class AutenticarUser {
    private static String nome;
    private static String cargo;
    private static int idPermissao;

    // Getters
    public static String getNome() {
        return nome;
    }

    public static String getCargo() {
        return cargo;
    }

    public static int getIdPermissao() {
        return idPermissao;
    }

    // Setters
    public static void setNome(String nome) {
        AutenticarUser.nome = nome;
    }

    public static void setCargo(String cargo) {
        AutenticarUser.cargo = cargo;
    }

    public static void setIdPermissao(int idPermissao) {
        AutenticarUser.idPermissao = idPermissao;
    }

    // Limpa os dados do usuário (para logout)
    public static void limparDados() {
        nome = null;
        cargo = null;
        idPermissao = 0;
    }

    // Verifica se há um usuário autenticado
    public static boolean isAutenticado() {
        return nome != null && !nome.trim().isEmpty() && 
               cargo != null && !cargo.trim().isEmpty();
    }
}