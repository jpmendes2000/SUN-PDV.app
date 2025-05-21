package com.sunpdv;

import java.util.HashMap;
import java.util.Map;

public class AutenticarUser {
    private static String nome;
    private static String cargo;
    private static int idPermissao;

    // Simula um banco de dados interno com usuário -> dados (nome, cargo, idPermissao, senha)
    private static Map<String, String[]> baseUsuarios = new HashMap<>();

    static {
        // Estrutura: usuário -> {nome, cargo, idPermissao, senha}
        baseUsuarios.put("adm", new String[]{"Administrador", "ADM", "1", "123"});
        baseUsuarios.put("mod", new String[]{"Moderador", "MOD", "2", "123"});
        baseUsuarios.put("fun", new String[]{"Funcionário", "FUN", "3", "123"});
    }

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

    // Setters (geralmente não usados externamente)
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

    /**
     * Método para autenticar usuário e senha.
     * Se válido, salva dados estáticos e retorna true.
     * Se inválido, retorna false.
     */
    public static boolean autenticar(String usuario, String senha) {
        if (usuario == null || senha == null) {
            return false;
        }

        usuario = usuario.trim().toLowerCase();

        if (baseUsuarios.containsKey(usuario)) {
            String[] dados = baseUsuarios.get(usuario);
            String senhaEsperada = dados[3];

            if (senha.equals(senhaEsperada)) {
                // Atribui os dados ao usuário autenticado
                nome = dados[0];
                cargo = dados[1];
                idPermissao = Integer.parseInt(dados[2]);
                return true;
            }
        }

        // Falha na autenticação, limpa dados
        limparDados();
        return false;
    }
}
