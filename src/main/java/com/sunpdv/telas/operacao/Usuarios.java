package com.sunpdv.telas.operacao;

import com.sunpdv.connection.ConexaoDB;
import com.sunpdv.model.AutenticarUser;
import com.sunpdv.telas.home.TelaHomeADM;
import com.sunpdv.telas.home.TelaHomeFUN;
import com.sunpdv.telas.home.TelaHomeMOD;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import javafx.stage.Modality;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Usuarios {
    private Stage stage;

    private List<User> usuarios;
    
    private VBox listaUsuarios;

    private TextField pesquisaField;

    private ComboBox<String> filtroCargo;

    // Classe de alerta personalizado
    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();

            stage.getScene().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
        }
    }

    // Cria botão lateral
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

    // Classe Usuário
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

    // Carrega cargos do BD
    private List<String> carregarCargos() {
        List<String> cargos = new ArrayList<>();
        String query = "SELECT Cargo FROM cargo";
        try (Connection conn = ConexaoDB.getConnection();
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

    // Aplica filtros
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

    // Exibe alerta de erro
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Exibe tela de gerenciamento de usuários
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

        VBox logoBox = new VBox(10, logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 5, 0));

        Label horaLabel = new Label();
        horaLabel.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 16px; -fx-font-weight: bold;");
        horaLabel.setAlignment(Pos.CENTER);
        horaLabel.setMaxWidth(Double.MAX_VALUE);

        Label dataLabel = new Label();
        dataLabel.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 14px; -fx-font-weight: bold;");
        dataLabel.setAlignment(Pos.CENTER);
        dataLabel.setMaxWidth(Double.MAX_VALUE);

        VBox dataHoraBox = new VBox(5, horaLabel, dataLabel);
        dataHoraBox.setAlignment(Pos.CENTER);
        dataHoraBox.setPadding(new Insets(0, 0, 5, 0));

        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDateTime now = LocalDateTime.now();
        horaLabel.setText(now.format(horaFormatter));
        dataLabel.setText(now.format(dataFormatter));

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            horaLabel.setText(currentTime.format(horaFormatter));
            dataLabel.setText(currentTime.format(dataFormatter));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);

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
        buttonBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        leftMenu.getChildren().addAll(logoBox, dataHoraBox, espaco, buttonBox);

        StackPane centro = new StackPane();
        centro.setPadding(new Insets(20));

        pesquisaField = new TextField();
        pesquisaField.setPromptText("Pesquisar usuário...");
        pesquisaField.setMaxWidth(300);

        filtroCargo = new ComboBox<>();
        filtroCargo.getItems().add("Todos");
        filtroCargo.getItems().addAll(carregarCargos());
        filtroCargo.setValue("Todos");
        filtroCargo.setPrefWidth(150);

        Label cargoLabel = new Label("Cargo:");
        pesquisaField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroCargo.setOnAction(e -> aplicarFiltros());
        HBox.setMargin(pesquisaField, new Insets(5, 10, 0, 10));
        pesquisaField.setPrefWidth(1600);

        HBox filtroBox = new HBox(5, pesquisaField, cargoLabel, filtroCargo);
        filtroBox.setPadding(new Insets(5));
        HBox.setMargin(filtroCargo, new Insets(0, 10, 0, 0));
        HBox.setMargin(cargoLabel, new Insets(0, 5, 0, 5));

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
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                if (!btnSair.isDisabled()) {
                    btnSair.fire();
                }
                event.consume();
            }
        });

        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Gerenciamento de Usuários");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();

        aplicarFiltros();
    }

    // Carrega usuários do BD
    private List<User> carregarUsuarios() {
        List<User> usuarios = new ArrayList<>();
        String query = "SELECT ls.ID_Login, ISNULL(ls.Nome, '') AS Nome, ISNULL(c.Cargo, '') AS Cargo, ISNULL(p.permissao, '') AS permissao " +
                      "FROM login_sistema ls " +
                      "LEFT JOIN cargo c ON ls.ID_Cargo = c.ID_Cargo " +
                      "LEFT JOIN permissao p ON ls.ID_Permissao = p.ID_Permissao";

        try (Connection conn = ConexaoDB.getConnection();
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

    // Atualiza usuário no BD
    private void atualizarUsuario(int id, String nome, String cargo, String permissao, String senha) {
        String query = "UPDATE login_sistema SET Nome = ?, ID_Cargo = (SELECT ID_Cargo FROM cargo WHERE Cargo = ?), " +
                      "ID_Permissao = (SELECT ID_Permissao FROM permissao WHERE permissao = ?), Senha = ? WHERE ID_Login = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nome);
            stmt.setString(2, cargo);
            stmt.setString(3, permissao);
            stmt.setString(4, senha);
            stmt.setInt(5, id);
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

    // Alterna status do usuário
    private void toggleStatusUsuario(int id, String novoStatus) {
        String query = "UPDATE login_sistema SET ID_Permissao = (SELECT ID_Permissao FROM permissao WHERE permissao = ?) WHERE ID_Login = ?";
        try (Connection conn = ConexaoDB.getConnection();
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

    // Exibe diálogo de edição de usuário
    private void showEditUserDialog(User user) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Editar Usuário: " + user.nome);

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setStyle("-fx-background-color: #006989;");
        dialogVBox.setAlignment(Pos.CENTER);

        TextField nomeField = new TextField(user.nome);
        nomeField.setPromptText("Nome do usuário");
        nomeField.setMaxWidth(400);

        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Nova senha (opcional)");
        senhaField.setMaxWidth(400);

        PasswordField confirmarSenhaField = new PasswordField();
        confirmarSenhaField.setPromptText("Confirme a nova senha");
        confirmarSenhaField.setMaxWidth(400);

        ComboBox<String> cargoCombo = new ComboBox<>();
        cargoCombo.getItems().addAll(carregarCargos());
        cargoCombo.setValue(user.cargo);
        cargoCombo.setPrefWidth(150);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Aceito", "Negado");
        statusCombo.setValue(user.permissao);
        statusCombo.setPrefWidth(150);

        Label nomeLabel = new Label("Nome:");
        nomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Label senhaLabel = new Label("Nova Senha:");
        senhaLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Label confirmarSenhaLabel = new Label("Confirmar Senha:");
        confirmarSenhaLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Label cargoLabel = new Label("Cargo:");
        cargoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px; -fx-font-weight: bold;");
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setVisible(false);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(errorLabel, new Insets(10, 0, 10, 0));

        VBox cargoBox = new VBox(5, cargoLabel, cargoCombo);
        cargoBox.setAlignment(Pos.CENTER);
        VBox statusBox = new VBox(5, statusLabel, statusCombo);
        statusBox.setAlignment(Pos.CENTER);

        HBox cargoStatusBox = new HBox(20, cargoBox, statusBox);
        cargoStatusBox.setAlignment(Pos.CENTER);

        Button salvarButton = new Button("Salvar");
        salvarButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");
        salvarButton.setOnAction(e -> {
            errorLabel.setVisible(false);
            errorLabel.setText("");

            String novoNome = nomeField.getText().trim();
            String novoCargo = cargoCombo.getValue();
            String novoStatus = statusCombo.getValue();
            String novaSenha = senhaField.getText().trim();
            String confirmarSenha = confirmarSenhaField.getText().trim();

            if (novoNome.isEmpty()) {
                errorLabel.setText("O nome não pode estar vazio.");
                errorLabel.setVisible(true);
                return;
            }
            if (!novaSenha.isEmpty() && novaSenha.length() < 6) {
                errorLabel.setText("A senha deve ter pelo menos 6 caracteres.");
                errorLabel.setVisible(true);
                return;
            }
            if (!novaSenha.equals(confirmarSenha)) {
                errorLabel.setText("As senhas não coincidem.");
                errorLabel.setVisible(true);
                return;
            }

            String senhaFinal = novaSenha.isEmpty() ? user.permissao : novaSenha;
            atualizarUsuario(user.id, novoNome, novoCargo, novoStatus, senhaFinal);
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
            nomeLabel, nomeField,
            senhaLabel, senhaField,
            confirmarSenhaLabel, confirmarSenhaField,
            cargoStatusBox,
            errorLabel,
            botoes
        );

        Scene dialogScene = new Scene(dialogVBox, 400, 400);
        dialogScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // Cria painel de usuário
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
        btnEditar.setStyle("-fx-background-color: #0c5b74; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");
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