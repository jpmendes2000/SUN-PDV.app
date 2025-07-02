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
            btn.setStyle("-fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(42);

            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18));");
                indicatorContainer.setStyle("-fx-background-color: rgba(255, 204, 0, 0.64);");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });

            return btn;
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            return new Button(texto);
        }
    }

    // Classe para representar dados de usuário
    private static class User {
        int id;
        String nome;
        String cargo;
        String permissao;

        User(int id, String nome, String cargo, String permissao) {
            this.id = id;
            this.nome = (nome != null && !nome.isEmpty()) ? nome : "Usuário Desconhecido";
            this.cargo = cargo;
            this.permissao = permissao;
        }
    }

    // Método para carregar usuários do banco de dados
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
            alert.setContentText("Detalhes do erro: " + e.getMessage());
            alert.showAndWait();
        }
        return usuarios;
    }

    // Método para carregar cargos disponíveis
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
            alert.setContentText("Detalhes do erro: " + e.getMessage());
            alert.showAndWait();
        }
        return cargos;
    }

    // Método para criar painel de cada usuário
    private VBox criarPainelUsuario(User user) {
        VBox painel = new VBox(10);
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        painel.setPadding(new Insets(15));
        painel.setPrefWidth(600);

        Label nomeLabel = new Label("Nome: " + (user.nome != null ? user.nome : "Sem Nome"));
        nomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label cargoLabel = new Label("Cargo: " + (user.cargo != null ? user.cargo : "Sem Cargo"));
        cargoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Button btnMudarSenha = new Button("Mudar Senha");
        btnMudarSenha.setStyle("-fx-background-color: #00536d; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnMudarSenha.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Mudar Senha");
            dialog.setHeaderText("Mudar senha para " + user.nome);
            dialog.setContentText("Nova senha:");
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/img/css/style.css").toExternalForm()
            );
            dialog.showAndWait().ifPresent(senha -> {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String sql = "UPDATE login_sistema SET Senha = ? WHERE ID_Login = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, senha); // Em produção, use hash para a senha
                    stmt.setInt(2, user.id);
                    stmt.executeUpdate();

                    Alert alert = new CustomConfirmationAlert(
                        stage,
                        "Sucesso",
                        "Senha alterada com sucesso!",
                        "A senha do usuário " + user.nome + " foi atualizada."
                    );
                    alert.showAndWait();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Erro ao atualizar senha");
                    alert.setContentText("Não foi possível atualizar a senha do usuário.");
                    alert.showAndWait();
                }
            });
        });

        Button btnRemoverAcesso = new Button("Remover Acesso");
        btnRemoverAcesso.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnRemoverAcesso.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(
                stage,
                "Confirmação de Remoção",
                "Deseja realmente remover o acesso de " + user.nome + "?",
                "Esta ação alterará a permissão para 'Negado'."
            );
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        String sql = "UPDATE login_sistema SET ID_Permissao = (SELECT ID_Permissao FROM permissao WHERE permissao = 'Negado') WHERE ID_Login = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, user.id);
                        stmt.executeUpdate();

                        Alert success = new CustomConfirmationAlert(
                            stage,
                            "Sucesso",
                            "Acesso removido!",
                            "O acesso do usuário " + user.nome + " foi removido com sucesso."
                        );
                        success.showAndWait();
                        // Atualizar a lista após remoção
                        show(stage);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erro");
                        error.setHeaderText("Erro ao remover acesso");
                        error.setContentText("Não foi possível remover o acesso do usuário.");
                        error.showAndWait();
                    }
                }
            });
        });

        Button btnMudarCargo = new Button("Mudar Cargo");
        btnMudarCargo.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;");
        btnMudarCargo.setOnAction(e -> {
            List<String> cargosDisponiveis = carregarCargos();
            ChoiceDialog<String> dialog = new ChoiceDialog<>(user.cargo, cargosDisponiveis);
            dialog.setTitle("Mudar Cargo");
            dialog.setHeaderText("Selecione o novo cargo para " + user.nome);
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/img/css/style.css").toExternalForm()
            );
            dialog.showAndWait().ifPresent(novoCargo -> {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String sql = "UPDATE login_sistema SET ID_Cargo = (SELECT ID_Cargo FROM cargo WHERE Cargo = ?) WHERE ID_Login = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, novoCargo);
                    stmt.setInt(2, user.id);
                    stmt.executeUpdate();

                    Alert alert = new CustomConfirmationAlert(
                        stage,
                        "Sucesso",
                        "Cargo alterado com sucesso!",
                        "O cargo do usuário " + user.nome + " foi atualizado para " + novoCargo + "."
                    );
                    alert.showAndWait();
                    // Atualizar a lista após alteração
                    show(stage);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Erro ao atualizar cargo");
                    alert.setContentText("Não foi possível atualizar o cargo do usuário.");
                    alert.showAndWait();
                }
            });
        });

        HBox botoes = new HBox(10, btnMudarSenha, btnRemoverAcesso, btnMudarCargo);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        painel.getChildren().addAll(nomeLabel, cargoLabel, botoes);
        return painel;
    }

    public void show(Stage stage) {
        this.stage = stage;

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // Menu lateral
        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

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

        // Botões
        Button btnHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        // Ações
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
                alert.setHeaderText(null);
                alert.setContentText("Erro ao retornar para a tela principal.");
                alert.showAndWait();
            }
        });

        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(
                stage,
                "Confirmação de Saída",
                "Deseja realmente sair do sistema?",
                ""
            );
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });

        VBox buttonBox = new VBox(10, btnHome, btnSair);
        buttonBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        leftMenu.getChildren().addAll(logoBox, espaco, buttonBox);

        // Conteúdo da tela - Lista de usuários
        VBox listaUsuarios = new VBox(10);
        listaUsuarios.setPadding(new Insets(20));

        // Carregar usuários do banco
        List<User> usuarios = carregarUsuarios();
        if (usuarios.isEmpty()) {
            Label semUsuarios = new Label("Nenhum usuário encontrado com permissão 'Aceito'.");
            semUsuarios.setStyle("-fx-font-size: 16px; -fx-text-fill: #999;");
            listaUsuarios.getChildren().add(semUsuarios);
        } else {
            for (User user : usuarios) {
                listaUsuarios.getChildren().add(criarPainelUsuario(user));
            }
        }

        ScrollPane scrollPane = new ScrollPane(listaUsuarios);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f4f4f4; -fx-border-color: transparent;");

        // Mensagem inferior direita
        String nomeAutenticado = AutenticarUser.getNome() != null && !AutenticarUser.getNome().isEmpty() ?
                                AutenticarUser.getNome() : "Usuário Desconhecido";
        String cargo = AutenticarUser.getCargo() != null ? AutenticarUser.getCargo() : "Sem Cargo";

        Label mensagemFixa = new Label("Bem-vindo(a), " + nomeAutenticado + " você é " + cargo);
        mensagemFixa.getStyleClass().add("mensagem-bemvindo");

        StackPane posMensagem = new StackPane(mensagemFixa);
        posMensagem.setAlignment(Pos.BOTTOM_RIGHT);
        posMensagem.setPadding(new Insets(0, 20, 20, 280));

        StackPane centroComMensagem = new StackPane(scrollPane, posMensagem);

        // Layout principal
        BorderPane layout = new BorderPane();
        layout.setLeft(leftMenu);
        layout.setCenter(centroComMensagem);

        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Gerenciamento de Usuários");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }
}