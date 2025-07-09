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
import javafx.stage.Modality;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Usuarios {

    private Stage stage;
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "Senha@12345!";

    private List<User> usuarios;
    private VBox listaUsuarios;
    private TextField pesquisaField;
    private ComboBox<String> filtroCargo;

    // Alerta customizado para confirmação
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

    // Botão lateral estilizado com ícone e texto
    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            StackPane indicatorContainer = new StackPane();
            indicatorContainer.setMinWidth(3);
            indicatorContainer.setMaxWidth(3);
            indicatorContainer.setMinHeight(30);
            indicatorContainer.setMaxHeight(30);
            indicatorContainer.setStyle("-fx-background-color: transparent;");

            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);

            HBox content = new HBox(leftContent, new Region(), indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

            Button btn = new Button();
            btn.setGraphic(content);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18)); -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color: rgba(255, 204, 0, 0.64); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });

            return btn;
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            return new Button(texto);
        }
    }

    // Modelo interno para usuário
    private static class User {
        int id;
        String nome;
        String cargo;
        String permissao;

        User(int id, String nome, String cargo, String permissao) {
            this.id = id;
            this.nome = (nome != null && !nome.trim().isEmpty()) ? nome.trim() : "Usuário Desconhecido";
            this.cargo = (cargo != null && !cargo.trim().isEmpty()) ? cargo.trim() : "Sem Cargo";
            this.permissao = (permissao != null && !permissao.trim().isEmpty()) ? permissao.trim() : "Sem Permissão";
        }

        public String getStatusLabel() {
            if ("Aceito".equalsIgnoreCase(permissao)) {
                return "Ativo";
            } else if ("Negado".equalsIgnoreCase(permissao)) {
                return "Desativado";
            } else {
                return "Desconhecido";
            }
        }

        public String getStatusColor() {
            if ("Aceito".equalsIgnoreCase(permissao)) {
                return "green";
            } else if ("Negado".equalsIgnoreCase(permissao)) {
                return "red";
            } else {
                return "gray";
            }
        }
    }

    private List<String> carregarCargos() {
        List<String> cargos = new ArrayList<>();
        String query = "SELECT Cargo FROM cargo";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cargos.add(rs.getString("Cargo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erro ao carregar cargos", "Detalhes: " + e.getMessage());
        }
        return cargos;
    }

    private void aplicarFiltros() {
        String textoBusca = pesquisaField.getText().toLowerCase().trim();
        String cargoSelecionado = filtroCargo.getValue();

        listaUsuarios.getChildren().clear();
        boolean achou = false;
        for (User user : usuarios) {
            boolean nomeMatch = user.nome.toLowerCase().contains(textoBusca);
            boolean cargoMatch = cargoSelecionado.equals("Todos") || user.cargo.equalsIgnoreCase(cargoSelecionado);

            if (nomeMatch && cargoMatch) {
                listaUsuarios.getChildren().add(criarPainelUsuario(user));
                achou = true;
            }
        }
        if (!achou) {
            listaUsuarios.getChildren().add(new Label("Nenhum usuário corresponde à pesquisa."));
        }
    }

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void show(Stage stage) {
        this.stage = stage;
        usuarios = carregarUsuarios();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        logoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        Label titulonaABA = new Label("Gerenciamento de Usuários");
        titulonaABA.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        Button btnHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

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
                showErrorAlert("Falha ao carregar tela inicial", "Detalhes: " + ex.getMessage());
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

        VBox buttonBox = new VBox(10, btnHome, btnSair);
        buttonBox.setAlignment(Pos.TOP_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        leftMenu.getChildren().addAll(logoBox, new Region(), buttonBox);
        VBox.setVgrow(leftMenu.getChildren().get(1), Priority.ALWAYS);

        pesquisaField = new TextField();
        pesquisaField.setPromptText("Pesquisar usuário...");
        pesquisaField.setMaxWidth(300);

        filtroCargo = new ComboBox<>();
        filtroCargo.getItems().add("Todos");
        filtroCargo.getItems().addAll(carregarCargos());
        filtroCargo.setValue("Todos");

        pesquisaField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroCargo.setOnAction(e -> aplicarFiltros());

        HBox filtroBox = new HBox(10, pesquisaField, new Label("Cargo:"), filtroCargo);
        filtroBox.setAlignment(Pos.CENTER_LEFT);
        filtroBox.setPadding(new Insets(10));

        listaUsuarios = new VBox(10);
        listaUsuarios.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(listaUsuarios);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f4f4f4;");

        VBox centerBox = new VBox(10, filtroBox, scroll);
        centerBox.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setLeft(leftMenu);
        layout.setCenter(centerBox);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Gerenciamento de Usuários");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();

        aplicarFiltros();
    }

    private List<User> carregarUsuarios() {
        List<User> usuarios = new ArrayList<>();
        String query = "SELECT ls.ID_Login, ISNULL(ls.Nome, '') AS Nome, ISNULL(c.Cargo, '') AS Cargo, ISNULL(p.permissao, '') AS permissao " +
                      "FROM login_sistema ls " +
                      "LEFT JOIN cargo c ON ls.ID_Cargo = c.ID_Cargo " +
                      "LEFT JOIN permissao p ON ls.ID_Permissao = p.ID_Permissao";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(new User(
                    rs.getInt("ID_Login"),
                    rs.getString("Nome"),
                    rs.getString("Cargo"),
                    rs.getString("permissao")
                ));
            }
            System.out.println("Total de usuários carregados: " + usuarios.size());
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erro ao carregar usuários", "Detalhes: " + e.getMessage());
        }
        return usuarios;
    }

    private void atualizarUsuario(int id, String nome, String cargo, String permissao) {
        String query = "UPDATE login_sistema SET Nome = ?, ID_Cargo = (SELECT ID_Cargo FROM cargo WHERE Cargo = ?), " +
                      "ID_Permissao = (SELECT ID_Permissao FROM permissao WHERE permissao = ?) WHERE ID_Login = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nome);
            stmt.setString(2, cargo);
            stmt.setString(3, permissao);
            stmt.setInt(4, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Usuário atualizado com sucesso: ID " + id);
            } else {
                System.out.println("Nenhum usuário encontrado para o ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erro ao atualizar usuário", "Detalhes: " + e.getMessage());
        }
    }

    private void toggleStatusUsuario(int id, String novoStatus) {
        String query = "UPDATE login_sistema SET ID_Permissao = (SELECT ID_Permissao FROM permissao WHERE permissao = ?) WHERE ID_Login = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, novoStatus);
            stmt.setInt(2, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Status do usuário atualizado para: " + novoStatus);
            } else {
                System.out.println("Nenhum usuário encontrado para o ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erro ao alterar status do usuário", "Detalhes: " + e.getMessage());
        }
    }

    private void showEditUserDialog(User user) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Editar Usuário: " + user.nome);

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setStyle("-fx-background-color: #f4f4f4;");

        TextField nomeField = new TextField(user.nome);
        nomeField.setPromptText("Nome do usuário");

        ComboBox<String> cargoCombo = new ComboBox<>();
        cargoCombo.getItems().addAll(carregarCargos());
        cargoCombo.setValue(user.cargo);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Aceito", "Negado");
        statusCombo.setValue(user.permissao);

        Button salvarButton = new Button("Salvar");
        salvarButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");
        salvarButton.setOnAction(e -> {
            String novoNome = nomeField.getText().trim();
            String novoCargo = cargoCombo.getValue();
            String novoStatus = statusCombo.getValue();

            if (novoNome.isEmpty()) {
                showErrorAlert("Erro", "O nome não pode estar vazio.");
                return;
            }

            atualizarUsuario(user.id, novoNome, novoCargo, novoStatus);
            usuarios = carregarUsuarios();
            aplicarFiltros();
            dialog.close();
        });

        Button cancelarButton = new Button("Cancelar");
        cancelarButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");
        cancelarButton.setOnAction(e -> dialog.close());

        HBox botoes = new HBox(10, salvarButton, cancelarButton);
        botoes.setAlignment(Pos.CENTER);

        dialogVBox.getChildren().addAll(
            new Label("Nome:"), nomeField,
            new Label("Cargo:"), cargoCombo,
            new Label("Status:"), statusCombo,
            botoes
        );

        Scene dialogScene = new Scene(dialogVBox, 400, 300);
        dialogScene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private VBox criarPainelUsuario(User user) {
        VBox painel = new VBox(5);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d3d3d3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label nomeLabel = new Label("Nome: " + user.nome);
        nomeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00536d;");

        Label cargoLabel = new Label("Cargo: " + user.cargo);
        cargoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        Label statusLabel = new Label("Status: " + user.getStatusLabel());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + user.getStatusColor() + ";");

        Button btnEditar = new Button("Editar");
        btnEditar.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");
        btnEditar.setOnAction(e -> showEditUserDialog(user));

        Button btnToggleStatus = new Button(user.permissao.equalsIgnoreCase("Aceito") ? "Desativar" : "Ativar");
        btnToggleStatus.setStyle("-fx-background-color: " + (user.permissao.equalsIgnoreCase("Aceito") ? "#dc3545" : "#28a745") + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");
        btnToggleStatus.setOnAction(e -> {
            String novoStatus = user.permissao.equalsIgnoreCase("Aceito") ? "Negado" : "Aceito";
            toggleStatusUsuario(user.id, novoStatus);
            usuarios = carregarUsuarios();
            aplicarFiltros();
        });

        HBox botoes = new HBox(10, btnEditar, btnToggleStatus);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        painel.getChildren().addAll(nomeLabel, cargoLabel, statusLabel, botoes);

        return painel;
    }
}