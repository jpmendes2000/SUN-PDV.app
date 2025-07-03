package com.sunpdv.telas;

import com.sunpdv.AutenticarUser;
import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Usuarios {

    private Stage stage;
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "Senha@12345!";

    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                getClass().getResource("/img/css/style.css").toExternalForm()
            );
        }
    }

    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);

            Button btn = new Button();
            btn.setGraphic(leftContent);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

            return btn;
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            return new Button(texto);
        }
    }

    private static class User {
        int id;
        String nome;
        String cargo;
        String permissao;

        User(int id, String nome, String cargo, String permissao) {
            this.id = id;
            this.nome = (nome != null && !nome.trim().isEmpty() && !nome.matches("\\d+")) ? nome.trim() : "Usuário Desconhecido";
            this.cargo = (cargo != null && !cargo.trim().isEmpty()) ? cargo.trim() : "Sem Cargo";
            this.permissao = (permissao != null && !permissao.trim().isEmpty()) ? permissao.trim() : "Sem Permissão";
        }
    }

    private List<User> carregarUsuarios() {
        List<User> usuarios = new ArrayList<>();
        String query = "SELECT ls.ID_Login, ls.Nome, c.Cargo, p.permissao " +
                       "FROM login_sistema ls " +
                       "JOIN cargo c ON ls.ID_Cargo = c.ID_Cargo " +
                       "JOIN permissao p ON ls.ID_Permissao = p.ID_Permissao " +
                       "WHERE p.permissao = 'Aceito'";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String nome = rs.getString("Nome");
                System.out.println("Nome carregado do banco: " + (nome != null ? nome : "null")); // Depuração
                usuarios.add(new User(
                    rs.getInt("ID_Login"),
                    nome,
                    rs.getString("Cargo"),
                    rs.getString("permissao")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao carregar usuários");
            alert.setContentText("Detalhes: " + e.getMessage());
            alert.showAndWait();
        }
        return usuarios;
    }

    private List<String> carregarCargos() {
        List<String> cargos = new ArrayList<>();
        String query = "SELECT Cargo FROM cargo";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                cargos.add(rs.getString("Cargo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao carregar cargos");
            alert.setContentText("Detalhes: " + e.getMessage());
            alert.showAndWait();
        }
        return cargos;
    }

    private VBox criarPainelUsuario(User user) {
        VBox painel = new VBox(10);
        painel.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        painel.setPrefWidth(600);

        Label nomeLabel = new Label("Nome: " + user.nome);
        nomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label cargoLabel = new Label("Cargo: ");
        cargoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        ComboBox<String> cargoCombo = new ComboBox<>();
        List<String> cargos = carregarCargos();
        if (cargos.isEmpty()) {
            cargoCombo.getItems().add("Nenhum cargo disponível");
            cargoCombo.setDisable(true);
        } else {
            cargoCombo.getItems().addAll(cargos);
            cargoCombo.setValue(user.cargo);
        }
        cargoCombo.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5;");
        cargoCombo.setOnAction(e -> {
            String novoCargo = cargoCombo.getValue();
            if (novoCargo != null && !novoCargo.equals(user.cargo)) {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String sql = "UPDATE login_sistema SET ID_Cargo = (SELECT ID_Cargo FROM cargo WHERE Cargo = ?) WHERE ID_Login = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, novoCargo);
                    stmt.setInt(2, user.id);
                    stmt.executeUpdate();
                    Alert alert = new CustomConfirmationAlert(stage, "Sucesso", "Cargo alterado!", "O cargo foi atualizado para " + novoCargo + ".");
                    alert.showAndWait();
                    show(stage); // Recarrega a tela
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Falha ao mudar cargo");
                    alert.setContentText("Detalhes: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        HBox cargoBox = new HBox(10, cargoLabel, cargoCombo);
        cargoBox.setAlignment(Pos.CENTER_LEFT);

        TextField senhaField = new TextField();
        senhaField.setPromptText("Nova senha");
        senhaField.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-pref-width: 150;");
        Button btnSalvarSenha = new Button("Salvar");
        btnSalvarSenha.setStyle("-fx-background-color: #00536d; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        btnSalvarSenha.setOnAction(e -> {
            String novaSenha = senhaField.getText().trim();
            if (!novaSenha.isEmpty()) {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String sql = "UPDATE login_sistema SET Senha = ? WHERE ID_Login = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, novaSenha);
                    stmt.setInt(2, user.id);
                    stmt.executeUpdate();
                    Alert alert = new CustomConfirmationAlert(stage, "Sucesso", "Senha alterada!", "A senha foi atualizada com sucesso.");
                    alert.showAndWait();
                    senhaField.clear();
                    show(stage); // Recarrega a tela
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Falha ao mudar senha");
                    alert.setContentText("Detalhes: " + ex.getMessage());
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aviso");
                alert.setHeaderText("Campo vazio");
                alert.setContentText("Digite uma nova senha.");
                alert.showAndWait();
            }
        });

        HBox senhaBox = new HBox(10, senhaField, btnSalvarSenha);
        senhaBox.setAlignment(Pos.CENTER_LEFT);

        Button btnRemoverAcesso = new Button("Remover Acesso");
        btnRemoverAcesso.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        btnRemoverAcesso.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Confirmação", "Remover acesso de " + user.nome + "?", "Isso definirá a permissão como 'Negado'.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        String sql = "UPDATE login_sistema SET ID_Permissao = (SELECT ID_Permissao FROM permissao WHERE permissao = 'Negado') WHERE ID_Login = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, user.id);
                        stmt.executeUpdate();
                        Alert success = new CustomConfirmationAlert(stage, "Sucesso", "Acesso removido!", "O acesso foi removido com sucesso.");
                        success.showAndWait();
                        show(stage);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erro");
                        error.setHeaderText("Falha ao remover acesso");
                        alert.setContentText("Detalhes: " + ex.getMessage());
                        alert.showAndWait();
                    }
                }
            });
        });

        HBox botoes = new HBox(10, senhaBox, btnRemoverAcesso);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        painel.getChildren().addAll(nomeLabel, cargoBox, botoes);
        return painel;
    }

    public void show(Stage stage) {
        this.stage = stage;

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        VBox menu = new VBox(20);
        menu.setStyle("-fx-background-color: #00536d; -fx-padding: 20;");
        menu.setPrefWidth(280);

        // Logo SUN PDV
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);

        Label titulonaABA = new Label("Gerenciamento de Usuários");
        titulonaABA.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        Button btnHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair", "/img/icon/fechar.png");

        btnHome.setOnAction(e -> {
            try {
                String cargo = AutenticarUser.getCargo();
                switch (cargo) {
                    case "Administrador":
                        new TelaHomeADM(AutenticarUser.getNome(), cargo).mostrar(stage);
                        break;
                    case "Moderador":
                        new TelaHomeMOD(AutenticarUser.getNome(), cargo).mostrar(stage);
                        break;
                    case "Funcionário":
                        new TelaHomeFUN(AutenticarUser.getNome(), cargo).mostrar(stage);
                        break;
                    default:
                        System.out.println("Cargo não reconhecido: " + cargo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText("Falha ao carregar tela inicial");
                alert.setContentText("Detalhes: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Confirmação", "Deseja sair?", "Isso fechará o sistema.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        VBox buttonBox = new VBox(10, btnHome, btnSair);
        buttonBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        menu.getChildren().addAll(logoBox, espaco, buttonBox);

        VBox listaUsuarios = new VBox(10);
        listaUsuarios.setPadding(new Insets(20));

        List<User> usuarios = carregarUsuarios();
        if (usuarios.isEmpty()) {
            listaUsuarios.getChildren().add(new Label("Nenhum usuário encontrado com permissão 'Aceito'."));
        } else {
            for (User user : usuarios) {
                listaUsuarios.getChildren().add(criarPainelUsuario(user));
            }
        }

        ScrollPane scroll = new ScrollPane(listaUsuarios);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f4f4f4;");

        BorderPane layout = new BorderPane();
        layout.setLeft(menu);
        layout.setCenter(scroll);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Gerenciamento de Usuários");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }
}