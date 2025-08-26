package com.sunpdv.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AutenticarUser {
    private static String nome;
    private static String cargo;
    private static int idPermissao;
    private static final String AES_KEY = "MinhaChaveSuperSegura1234567890!";
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "Senha@12345!";

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

    /**
     * Método para autenticar usuário com e-mail e senha no banco de dados.
     * Se válido, salva dados estáticos e retorna true.
     * Se inválido, retorna false.
     */
    public static boolean autenticar(String email, String senha) {
        if (email == null || senha == null || email.trim().isEmpty() || senha.trim().isEmpty()) {
            limparDados();
            return false;
        }

        try {
            String emailCriptografado = criptografarAES(email);
            String senhaHash = hashSHA256(senha);

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String sql = "SELECT l.Nome, c.Cargo, p.ID_Permissao, p.permissao " +
                             "FROM login_sistema l " +
                             "INNER JOIN Cargo c ON l.ID_Cargo = c.ID_Cargo " +
                             "INNER JOIN Permissao p ON l.ID_Permissao = p.ID_Permissao " +
                             "WHERE l.Email = ? AND l.Senha = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, emailCriptografado);
                    stmt.setString(2, senhaHash);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String permissao = rs.getString("permissao");
                            if (!"Aceito".equalsIgnoreCase(permissao)) {
                                System.err.println("Acesso negado: permissão não concedida para o usuário: " + email);
                                limparDados();
                                return false;
                            }
                            nome = rs.getString("Nome");
                            cargo = rs.getString("Cargo");
                            idPermissao = rs.getInt("ID_Permissao");
                            System.out.println("Usuário autenticado: " + nome + ", Cargo: " + cargo + ", ID_Permissao: " + idPermissao);
                            return true;
                        } else {
                            System.err.println("Nenhum usuário encontrado para o e-mail: " + email);
                            limparDados();
                            return false;
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Erro ao executar consulta SQL: " + e.getMessage());
                    limparDados();
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Erro na conexão com o banco: " + e.getMessage());
                limparDados();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Erro ao autenticar usuário: " + e.getMessage());
            limparDados();
            return false;
        }
    }

    /**
     * Método para cadastrar um novo usuário no banco de dados.
     */
    public static boolean cadastrar(String nome, String email, String senha, String cargo) {
        if (nome == null || email == null || senha == null || cargo == null ||
            nome.trim().isEmpty() || email.trim().isEmpty() || senha.trim().isEmpty() || cargo.trim().isEmpty()) {
            System.err.println("Dados de cadastro inválidos!");
            return false;
        }

        try {
            String emailCriptografado = criptografarAES(email);
            String senhaHash = hashSHA256(senha);

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                // Obter ID do cargo
                String sqlCargo = "SELECT ID_Cargo FROM Cargo WHERE Cargo = ?";
                int idCargo = -1;
                try (PreparedStatement stmtCargo = conn.prepareStatement(sqlCargo)) {
                    stmtCargo.setString(1, cargo);
                    try (ResultSet rs = stmtCargo.executeQuery()) {
                        if (rs.next()) {
                            idCargo = rs.getInt("ID_Cargo");
                            System.out.println("ID_Cargo encontrado: " + idCargo);
                        } else {
                            System.err.println("Cargo não encontrado: " + cargo);
                            return false;
                        }
                    }
                }

                // Verificar se o e-mail já existe
                String sqlCheckEmail = "SELECT COUNT(*) AS count FROM login_sistema WHERE Email = ?";
                try (PreparedStatement stmtCheck = conn.prepareStatement(sqlCheckEmail)) {
                    stmtCheck.setString(1, emailCriptografado);
                    try (ResultSet rsCheck = stmtCheck.executeQuery()) {
                        if (rsCheck.next() && rsCheck.getInt("count") > 0) {
                            System.err.println("E-mail já cadastrado: " + email);
                            return false;
                        }
                    }
                }

                // Inserir usuário
                String sql = "INSERT INTO login_sistema (Nome, Email, Senha, ID_Cargo, ID_Permissao) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nome);
                    stmt.setString(2, emailCriptografado);
                    stmt.setString(3, senhaHash);
                    stmt.setInt(4, idCargo);
                    stmt.setInt(5, 1); // ID_Permissao padrão (ajuste conforme necessário)
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Usuário cadastrado com sucesso: " + nome);
                        return true;
                    } else {
                        System.err.println("Falha ao cadastrar usuário: nenhuma linha afetada.");
                        return false;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erro ao cadastrar usuário: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar cadastro: " + e.getMessage());
            return false;
        }
    }

    private static String criptografarAES(String texto) throws Exception {
        SecretKeySpec chave = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, chave);
        byte[] textoCriptografado = cipher.doFinal(texto.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(textoCriptografado);
    }

    private static String hashSHA256(String texto) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(texto.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}