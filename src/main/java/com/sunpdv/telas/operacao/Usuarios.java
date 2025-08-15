package com.sunpdv.telas.operacao;

import com.sunpdv.model.AutenticarUser;
import com.sunpdv.telas.home.TelaHomeADM;
import com.sunpdv.telas.home.TelaHomeFUN;
import com.sunpdv.telas.home.TelaHomeMOD;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets; // Para definir margens e preenchimento nos layouts
import javafx.geometry.Pos; // Para alinhamento de componentes
import javafx.geometry.Rectangle2D; // Para obter dimensões da tela
import javafx.scene.Scene; // Para criar a cena da interface gráfica
import javafx.scene.control.*; // Para componentes como botões, labels, campos de texto, etc.
import javafx.scene.image.Image; // Para carregar imagens (ex.: logo)
import javafx.scene.image.ImageView; // Para exibir imagens na interface
import javafx.scene.layout.*; // Para layouts como VBox, HBox, BorderPane
import javafx.stage.Screen; // Para obter informações da tela do dispositivo
import javafx.stage.Stage; // Para criar janelas (palcos) da aplicação
import javafx.util.Duration;
import javafx.stage.Modality; // Para definir comportamento modal das janelas
import java.sql.*; // Para conexão com banco de dados SQL Server
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; // Para listas dinâmicas
import java.util.List; // Para interface de listas

public class Usuarios {
    // Variável para armazenar a janela principal da aplicação
    private Stage stage;
    
    // Constantes para conexão com o banco de dados SQL Server
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;trustServerCertificate=true";
    private static final String DB_USER = "sa"; // Usuário do banco
    private static final String DB_PASSWORD = "Senha@12345!"; // Senha do banco
    
    // Lista para armazenar os usuários carregados do banco de dados
    private List<User> usuarios;
    
    // Componente VBox para exibir a lista de usuários na interface
    private VBox listaUsuarios;
    
    // Campo de texto para pesquisa de usuários
    private TextField pesquisaField;
    
    // ComboBox para filtro de cargos
    private ComboBox<String> filtroCargo;

    // Classe interna para alertas de confirmação personalizados
    private static class CustomConfirmationAlert extends Alert {
        // Construtor do alerta personalizado
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION); // Define o tipo de alerta como confirmação
            this.initOwner(owner); // Define a janela pai do alerta
            this.setTitle(title); // Define o título do alerta
            this.setHeaderText(header); // Define o cabeçalho do alerta
            this.setContentText(content); // Define o conteúdo do alerta
            // Obtém a janela do alerta para aplicar estilos
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            // Adiciona o arquivo CSS para estilização
            stage.getScene().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
        }
    }

    // Método para criar botões laterais estilizados com ícone e texto
    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            // Carrega a imagem do ícone a partir do caminho fornecido
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            // Cria uma visualização para a imagem
            ImageView icon = new ImageView(img);
            icon.setFitWidth(20); // Define largura do ícone
            icon.setFitHeight(20); // Define altura do ícone

            // Cria um rótulo para o texto do botão
            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); // Estiliza o texto como branco e negrito

            // Cria um contêiner para o indicador visual (uma linha colorida ao passar o mouse)
            StackPane indicatorContainer = new StackPane();
            indicatorContainer.setMinWidth(3); // Largura mínima do indicador
            indicatorContainer.setMaxWidth(3); // Largura máxima do indicador
            indicatorContainer.setMinHeight(30); // Altura mínima do indicador
            indicatorContainer.setMaxHeight(30); // Altura máxima do indicador
            indicatorContainer.setStyle("-fx-background-color: transparent;"); // Inicialmente transparente

            // Cria um HBox para alinhar o ícone e o texto à esquerda
            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT); // Alinha à esquerda

            // Cria um HBox para combinar o conteúdo à esquerda com o indicador
            HBox content = new HBox(leftContent, new Region(), indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT); // Alinha o conteúdo à esquerda
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS); // Faz o Region crescer para empurrar o indicador à direita

            // Cria o botão e define o conteúdo gráfico
            Button btn = new Button();
            btn.setGraphic(content); // Define o HBox como conteúdo do botão
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY); // Usa apenas o gráfico, sem texto padrão
            btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;"); // Estilo inicial
            btn.setPrefWidth(280); // Largura preferida do botão
            btn.setPrefHeight(42); // Altura preferida do botão

            // Evento de mouse para mudar o estilo ao passar o mouse
            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18)); -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color: rgba(255, 204, 0, 0.64); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            });
            // Evento de mouse para restaurar o estilo ao sair
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });

            return btn; // Retorna o botão criado
        } catch (Exception e) {
            // Em caso de erro ao carregar o ícone, exibe mensagem e retorna um botão simples
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            return new Button(texto);
        }
    }

    // Classe interna para representar um usuário
    private static class User {
        int id; // ID do usuário no banco de dados
        String nome; // Nome do usuário
        String cargo; // Cargo do usuário
        String permissao; // Permissão (ex.: "Aceito", "Negado")

        // Construtor da classe User
        User(int id, String nome, String cargo, String permissao) {
            this.id = id;
            // Garante que o nome não seja nulo ou vazio, usando um valor padrão
            this.nome = (nome != null && !nome.trim().isEmpty()) ? nome.trim() : "Usuário Desconhecido";
            // Garante que o cargo não seja nulo ou vazio
            this.cargo = (cargo != null && !cargo.trim().isEmpty()) ? cargo.trim() : "Sem Cargo";
            // Garante que a permissão não seja nula ou vazia
            this.permissao = (permissao != null && !permissao.trim().isEmpty()) ? permissao.trim() : "Sem Permissão";
        }

        // Retorna o rótulo do status para exibição (ex.: "Ativo" ou "Desativado")
        public String getStatusLabel() {
            if ("Aceito".equalsIgnoreCase(permissao)) {
                return "Ativo";
            } else if ("Negado".equalsIgnoreCase(permissao)) {
                return "Desativado";
            } else {
                return "Desconhecido";
            }
        }

        // Retorna a cor do status para estilização
        public String getStatusColor() {
            if ("Aceito".equalsIgnoreCase(permissao)) {
                return "green"; // Verde para ativo
            } else if ("Negado".equalsIgnoreCase(permissao)) {
                return "red"; // Vermelho para desativado
            } else {
                return "gray"; // Cinza para desconhecido
            }
        }
    }

    // Método para carregar os cargos do banco de dados
    private List<String> carregarCargos() {
        // Lista para armazenar os cargos
        List<String> cargos = new ArrayList<>();
        // Query SQL para selecionar todos os cargos
        String query = "SELECT Cargo FROM cargo";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Conexão com o banco
             PreparedStatement stmt = conn.prepareStatement(query); // Prepara a query
             ResultSet rs = stmt.executeQuery()) { // Executa a query
            // Itera sobre os resultados e adiciona cada cargo à lista
            while (rs.next()) {
                cargos.add(rs.getString("Cargo"));
            }
        } catch (SQLException e) {
            // Em caso de erro, exibe mensagem no console e um alerta
            e.printStackTrace();
            showErrorAlert("Erro ao carregar cargos", "Detalhes: " + e.getMessage());
        }
        return cargos; // Retorna a lista de cargos
    }

    // Método para aplicar filtros de pesquisa e cargo
    private void aplicarFiltros() {
        // Obtém o texto de pesquisa em minúsculas e sem espaços
        String textoBusca = pesquisaField.getText().toLowerCase().trim();
        // Obtém o cargo selecionado no ComboBox
        String cargoSelecionado = filtroCargo.getValue();

        // Limpa a lista de usuários exibida
        listaUsuarios.getChildren().clear();
        boolean achou = false; // Flag para verificar se algum usuário foi encontrado
        // Itera sobre todos os usuários
        for (User user : usuarios) {
            // Verifica se o nome do usuário contém o texto de busca
            boolean nomeMatch = user.nome.toLowerCase().contains(textoBusca);
            // Verifica se o cargo corresponde ao filtro selecionado
            boolean cargoMatch = cargoSelecionado.equals("Todos") || user.cargo.equalsIgnoreCase(cargoSelecionado);

            // Se o usuário atende aos filtros, adiciona seu painel à lista
            if (nomeMatch && cargoMatch) {
                listaUsuarios.getChildren().add(criarPainelUsuario(user));
                achou = true;
            }
        }
        // Se nenhum usuário for encontrado, exibe uma mensagem
        if (!achou) {
            listaUsuarios.getChildren().add(new Label("Nenhum usuário corresponde à pesquisa."));
        }
    }

    // Método para exibir alertas de erro
    private void showErrorAlert(String header, String content) {
        // Cria um alerta do tipo erro
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro"); // Define o título
        alert.setHeaderText(header); // Define o cabeçalho
        alert.setContentText(content); // Define o conteúdo
        alert.showAndWait(); // Exibe o alerta e espera a interação do usuário
    }

    // Método principal para exibir a tela de gerenciamento de usuários
    public void show(Stage stage) {
        // Armazena a janela principal
        this.stage = stage;
        // Carrega os usuários do banco de dados
        usuarios = carregarUsuarios();

        // Obtém as dimensões da tela para configurar a janela em tela cheia
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        // Cria o menu lateral
        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        // Carrega o logo da aplicação
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        logoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        // Cria o título da tela
        Label titulonaABA = new Label("Gerenciamento de Usuários");
        titulonaABA.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Organiza o logo e o título em um VBox
        VBox logoBox = new VBox(10, logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 5, 0));

        // Labels para hora e data
        Label horaLabel = new Label();
        horaLabel.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 16px; -fx-font-weight: bold;");
        horaLabel.setAlignment(Pos.CENTER);
        horaLabel.setMaxWidth(Double.MAX_VALUE);

        Label dataLabel = new Label();
        dataLabel.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 14px; -fx-font-weight: bold;");
        dataLabel.setAlignment(Pos.CENTER);
        dataLabel.setMaxWidth(Double.MAX_VALUE);

        // VBox para organizar hora acima da data
        VBox dataHoraBox = new VBox(5, horaLabel, dataLabel);
        dataHoraBox.setAlignment(Pos.CENTER);
        dataHoraBox.setPadding(new Insets(0, 0, 5, 0));

        // Formatadores para hora e data
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Definir texto inicial
        LocalDateTime now = LocalDateTime.now();
        horaLabel.setText(now.format(horaFormatter));
        dataLabel.setText(now.format(dataFormatter));

        // Atualizar hora e data
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            horaLabel.setText(currentTime.format(horaFormatter));
            dataLabel.setText(currentTime.format(dataFormatter));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Espaço para empurrar os botões para baixo
        Region espaco = new Region();
        VBox.setVgrow(espaco, Priority.ALWAYS);

        // Cria botões laterais para navegação
        Button btnHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        // Define a ação do botão Home
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

        // Define a ação do botão Sair
        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Confirmação", "Deseja sair?", "Isso fechará o sistema.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });

        // Organiza os botões em um VBox
        VBox buttonBox = new VBox(10, btnHome, btnSair);
        buttonBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        // Adicionar elementos ao menu lateral, com dataHoraBox abaixo do logoBox
        leftMenu.getChildren().addAll(logoBox, dataHoraBox, espaco, buttonBox);

        // Conteúdo central - logo do mercado configurada (se existir)
        StackPane centro = new StackPane();
        centro.setPadding(new Insets(20));

        // Cria o campo de pesquisa
        pesquisaField = new TextField();
        pesquisaField.setPromptText("Pesquisar usuário..."); // Texto de placeholder
        pesquisaField.setMaxWidth(300); // Largura máxima do campo

        // Cria o ComboBox para filtro de cargos
        filtroCargo = new ComboBox<>();
        filtroCargo.getItems().add("Todos"); // Adiciona opção "Todos"
        filtroCargo.getItems().addAll(carregarCargos()); // Adiciona cargos do banco
        filtroCargo.setValue("Todos"); // Define valor inicial
        filtroCargo.setPrefWidth(150); // Define largura do ComboBox

        // Cria o rótulo para o filtro de cargo
        Label cargoLabel = new Label("Cargo:");
        // Adiciona listener para atualizar a lista ao digitar
        pesquisaField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        // Adiciona listener para atualizar a lista ao mudar o cargo
        filtroCargo.setOnAction(e -> aplicarFiltros());
        HBox.setMargin(pesquisaField, new Insets(5, 10, 0, 10)); // Margens do campo de pesquisa
        pesquisaField.setPrefWidth(1600); // Largura preferida do campo

        // Organiza o campo de pesquisa e o filtro em um HBox
        HBox filtroBox = new HBox(5, pesquisaField, cargoLabel, filtroCargo);
        filtroBox.setPadding(new Insets(5)); // Define margens
        HBox.setMargin(filtroCargo, new Insets(0, 10, 0, 0)); // Margem do ComboBox
        HBox.setMargin(cargoLabel, new Insets(0, 5, 0, 5)); // Margem do rótulo

        // Cria o contêiner para a lista de usuários
        listaUsuarios = new VBox(10);
        listaUsuarios.setPadding(new Insets(20)); // Define margens

        // Adiciona a lista a um ScrollPane para rolagem
        ScrollPane scroll = new ScrollPane(listaUsuarios);
        scroll.setFitToWidth(true); // Ajusta a largura ao conteúdo
        scroll.setStyle("-fx-background: #f4f4f4;"); // Define a cor de fundo

        // Organiza o filtro e a lista em um VBox
        VBox centerBox = new VBox(10, filtroBox, scroll);
        centerBox.setPadding(new Insets(10)); // Define margens

        // Cria o layout principal com BorderPane
        BorderPane layout = new BorderPane();
        layout.setLeft(leftMenu); // Define o menu lateral à esquerda
        layout.setCenter(centerBox); // Define o conteúdo central

        // Cria a cena principal
        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); // Aplica o CSS

        // Configura a janela principal
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Gerenciamento de Usuários"); // Define o título
        stage.setFullScreen(true); // Define tela cheia
        stage.setResizable(true); // Permite redimensionamento
        stage.show(); // Exibe a janela

        // Aplica os filtros iniciais para exibir os usuários
        aplicarFiltros();
    }

    // Método para carregar os usuários do banco de dados
    private List<User> carregarUsuarios() {
        // Lista para armazenar os usuários
        List<User> usuarios = new ArrayList<>();
        // Query SQL para selecionar dados dos usuários
        String query = "SELECT ls.ID_Login, ISNULL(ls.Nome, '') AS Nome, ISNULL(c.Cargo, '') AS Cargo, ISNULL(p.permissao, '') AS permissao " +
                      "FROM login_sistema ls " +
                      "LEFT JOIN cargo c ON ls.ID_Cargo = c.ID_Cargo " +
                      "LEFT JOIN permissao p ON ls.ID_Permissao = p.ID_Permissao";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Conexão com o banco
             PreparedStatement stmt = conn.prepareStatement(query); // Prepara a query
             ResultSet rs = stmt.executeQuery()) { // Executa a query
            // Itera sobre os resultados e cria objetos User
            while (rs.next()) {
                usuarios.add(new User(
                    rs.getInt("ID_Login"), // ID do usuário
                    rs.getString("Nome"), // Nome do usuário
                    rs.getString("Cargo"), // Cargo do usuário
                    rs.getString("permissao") // Permissão do usuário
                ));
            }
            // Exibe no console a quantidade de usuários carregados
            System.out.println("Total de usuários carregados: " + usuarios.size());
        } catch (SQLException e) {
            // Em caso de erro, exibe mensagem no console e um alerta
            e.printStackTrace();
            showErrorAlert("Erro ao carregar usuários", "Detalhes: " + e.getMessage());
        }
        return usuarios; // Retorna a lista de usuários
    }

    // Método para atualizar os dados de um usuário no banco
    private void atualizarUsuario(int id, String nome, String cargo, String permissao, String senha) {
        // Query SQL para atualizar os dados do usuário
        String query = "UPDATE login_sistema SET Nome = ?, ID_Cargo = (SELECT ID_Cargo FROM cargo WHERE Cargo = ?), " +
                      "ID_Permissao = (SELECT ID_Permissao FROM permissao WHERE permissao = ?), Senha = ? WHERE ID_Login = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Conexão com o banco
             PreparedStatement stmt = conn.prepareStatement(query)) { // Prepara a query
            // Define os parâmetros da query
            stmt.setString(1, nome); // Nome do usuário
            stmt.setString(2, cargo); // Cargo do usuário
            stmt.setString(3, permissao); // Permissão do usuário
            stmt.setString(4, senha); // Senha do usuário
            stmt.setInt(5, id); // ID do usuário
            // Executa a atualização
            int rowsAffected = stmt.executeUpdate();
            // Verifica se a atualização foi bem-sucedida
            if (rowsAffected > 0) {
                System.out.println("Usuário atualizado com sucesso: ID " + id);
            } else {
                System.out.println("Nenhum usuário encontrado para o ID: " + id);
            }
        } catch (SQLException e) {
            // Em caso de erro, exibe mensagem no console e um alerta
            e.printStackTrace();
            showErrorAlert("Erro ao atualizar usuário", "Detalhes: " + e.getMessage());
        }
    }

    // Método para alternar o status de permissão de um usuário
    private void toggleStatusUsuario(int id, String novoStatus) {
        // Query SQL para atualizar a permissão do usuário
        String query = "UPDATE login_sistema SET ID_Permissao = (SELECT ID_Permissao FROM permissao WHERE permissao = ?) WHERE ID_Login = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Conexão com o banco
             PreparedStatement stmt = conn.prepareStatement(query)) { // Prepara a query
            // Define os parâmetros da query
            stmt.setString(1, novoStatus); // Nova permissão
            stmt.setInt(2, id); // ID do usuário
            // Executa a atualização
            int rowsAffected = stmt.executeUpdate();
            // Verifica se a atualização foi bem-sucedida
            if (rowsAffected > 0) {
                System.out.println("Status do usuário atualizado para: " + novoStatus);
            } else {
                System.out.println("Nenhum usuário encontrado para o ID: " + id);
            }
        } catch (SQLException e) {
            // Em caso de erro, exibe mensagem no console e um alerta
            e.printStackTrace();
            showErrorAlert("Erro ao alterar status do usuário", "Detalhes: " + e.getMessage());
        }
    }

    // Método para exibir o diálogo de edição de usuário
    private void showEditUserDialog(User user) {
        // Cria uma nova janela modal para edição
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL); // Define como modal, bloqueando a janela pai
        dialog.initOwner(stage); // Define a janela principal como pai
        dialog.setTitle("Editar Usuário: " + user.nome); // Define o título com o nome do usuário

        // Cria o contêiner principal do diálogo (VBox para layout vertical)
        VBox dialogVBox = new VBox(10); // Espaçamento vertical de 10px entre elementos
        dialogVBox.setPadding(new Insets(20)); // Define margens internas de 20px
        dialogVBox.setStyle("-fx-background-color: #006989;"); // Define a cor de fundo azul escura
        dialogVBox.setAlignment(Pos.CENTER); // Centraliza os elementos verticalmente

        // Cria o campo de texto para o nome do usuário
        TextField nomeField = new TextField(user.nome); // Inicializa com o nome atual
        nomeField.setPromptText("Nome do usuário"); // Texto de placeholder
        nomeField.setMaxWidth(400); // Define largura máxima para consistência visual

        // Cria o campo de senha (opcional)
        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Nova senha (opcional)"); // Texto de placeholder
        senhaField.setMaxWidth(400); // Define largura máxima para consistência visual

        // Cria o campo de confirmação de senha
        PasswordField confirmarSenhaField = new PasswordField();
        confirmarSenhaField.setPromptText("Confirme a nova senha"); // Texto de placeholder
        confirmarSenhaField.setMaxWidth(400); // Define largura máxima para consistência visual

        // Cria o ComboBox para seleção de cargo
        ComboBox<String> cargoCombo = new ComboBox<>();
        cargoCombo.getItems().addAll(carregarCargos()); // Carrega os cargos do banco
        cargoCombo.setValue(user.cargo); // Define o cargo atual do usuário
        cargoCombo.setPrefWidth(150); // Define largura fixa

        // Cria o ComboBox para seleção de status (permissão)
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Aceito", "Negado"); // Adiciona opções de status
        statusCombo.setValue(user.permissao); // Define o status atual do usuário
        statusCombo.setPrefWidth(150); // Define largura fixa

        // Cria os rótulos (Labels) para cada campo
        Label nomeLabel = new Label("Nome:");
        nomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;"); // Texto branco, tamanho 12px

        Label senhaLabel = new Label("Nova Senha:");
        senhaLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;"); // Texto branco, tamanho 12px

        Label confirmarSenhaLabel = new Label("Confirmar Senha:");
        confirmarSenhaLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;"); // Texto branco, tamanho 12px

        Label cargoLabel = new Label("Cargo:");
        cargoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;"); // Texto branco, tamanho 12px

        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;"); // Texto branco, tamanho 12px

        // Cria o Label para mensagens de erro
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px; -fx-font-weight: bold;"); // Texto vermelho, negrito
        errorLabel.setAlignment(Pos.CENTER); // Centraliza o texto
        errorLabel.setVisible(false); // Inicialmente invisível
        errorLabel.setMaxWidth(Double.MAX_VALUE); // Permite que o Label use toda a largura disponível
        VBox.setMargin(errorLabel, new Insets(10, 0, 10, 0)); // Margens para espaçamento

        // Cria VBoxes para organizar Cargo e Status com seus rótulos acima
        VBox cargoBox = new VBox(5, cargoLabel, cargoCombo); // Espaçamento vertical de 5px
        cargoBox.setAlignment(Pos.CENTER); // Centraliza os elementos
        VBox statusBox = new VBox(5, statusLabel, statusCombo); // Espaçamento vertical de 5px
        statusBox.setAlignment(Pos.CENTER); // Centraliza os elementos

        // Cria um HBox para organizar os VBoxes de Cargo e Status lado a lado
        HBox cargoStatusBox = new HBox(20, cargoBox, statusBox); // Espaçamento horizontal de 20px
        cargoStatusBox.setAlignment(Pos.CENTER); // Alinha os elementos ao centro

        // Cria o botão Salvar
        Button salvarButton = new Button("Salvar");
        salvarButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;"); // Estilo verde
        salvarButton.setOnAction(e -> {
            // Limpa a mensagem de erro anterior
            errorLabel.setVisible(false);
            errorLabel.setText("");

            // Obtém os valores dos campos
            String novoNome = nomeField.getText().trim(); // Nome do usuário
            String novoCargo = cargoCombo.getValue(); // Cargo selecionado
            String novoStatus = statusCombo.getValue(); // Status selecionado
            String novaSenha = senhaField.getText().trim(); // Nova senha
            String confirmarSenha = confirmarSenhaField.getText().trim(); // Confirmação da senha

            // Validações dos campos
            if (novoNome.isEmpty()) {
                // Exibe mensagem de erro no Label se o nome estiver vazio
                errorLabel.setText("O nome não pode estar vazio.");
                errorLabel.setVisible(true);
                return;
            }
            if (!novaSenha.isEmpty() && novaSenha.length() < 6) {
                // Exibe mensagem de erro no Label se a senha tiver menos de 6 caracteres
                errorLabel.setText("A senha deve ter pelo menos 6 caracteres.");
                errorLabel.setVisible(true);
                return;
            }
            if (!novaSenha.equals(confirmarSenha)) {
                // Exibe mensagem de erro no Label se as senhas não coincidem
                errorLabel.setText("As senhas não coincidem.");
                errorLabel.setVisible(true);
                return;
            }

            // Determina a senha final (mantém a atual se vazia)
            String senhaFinal = novaSenha.isEmpty() ? user.permissao : novaSenha;
            // Atualiza o usuário no banco de dados
            atualizarUsuario(user.id, novoNome, novoCargo, novoStatus, senhaFinal);
            // Recarrega a lista de usuários
            usuarios = carregarUsuarios();
            // Aplica os filtros para atualizar a interface
            aplicarFiltros();
            // Fecha o diálogo
            dialog.close();
        });

        // Cria o botão Cancelar
        Button cancelarButton = new Button("Cancelar");
        cancelarButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;"); // Estilo vermelho
        cancelarButton.setOnAction(e -> dialog.close()); // Fecha o diálogo ao clicar

        // Organiza os botões em um HBox
        HBox botoes = new HBox(10, salvarButton, cancelarButton); // Espaçamento de 10px
        botoes.setAlignment(Pos.CENTER); // Centraliza os botões

        // Adiciona todos os elementos ao VBox principal na ordem desejada
        dialogVBox.getChildren().addAll(
            nomeLabel, nomeField, // Campo Nome
            senhaLabel, senhaField, // Campo Nova Senha
            confirmarSenhaLabel, confirmarSenhaField, // Campo Confirmar Senha
            cargoStatusBox, // HBox com VBoxes de Cargo e Status
            errorLabel, // Label para mensagens de erro
            botoes // Botões Salvar e Cancelar
        );

        // Cria a cena do diálogo com largura 400px e altura 400px
        Scene dialogScene = new Scene(dialogVBox, 400, 400);
        dialogScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); // Aplica o CSS
        dialog.setScene(dialogScene); // Define a cena na janela
        dialog.showAndWait(); // Exibe o diálogo e aguarda interação
    }

    // Método para criar o painel de exibição de um usuário
    private VBox criarPainelUsuario(User user) {
        // Cria um VBox para o painel do usuário
        VBox painel = new VBox(5); // Espaçamento vertical de 5px
        painel.setPadding(new Insets(10)); // Margens internas de 10px
        painel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d3d3d3; -fx-border-radius: 5; -fx-background-radius: 5;"); // Estilo do painel

        // Cria o rótulo para o nome
        Label nomeLabel = new Label("Nome: " + user.nome);
        nomeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00536d;"); // Estilo do rótulo

        // Cria o rótulo para o cargo
        Label cargoLabel = new Label("Cargo: " + user.cargo);
        cargoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;"); // Estilo do rótulo

        // Cria o rótulo para o status
        Label statusLabel = new Label("Status: " + user.getStatusLabel());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + user.getStatusColor() + ";"); // Estilo com cor dinâmica

        // Cria o botão Editar
        Button btnEditar = new Button("Editar");
        btnEditar.setStyle("-fx-background-color: #0c5b74; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;"); // Estilo azul
        btnEditar.setOnAction(e -> showEditUserDialog(user)); // Abre o diálogo de edição

        // Cria o botão para alternar o status
        Button btnToggleStatus = new Button(user.permissao.equalsIgnoreCase("Aceito") ? "Desativar" : "Ativar");
        btnToggleStatus.setStyle("-fx-background-color: " + (user.permissao.equalsIgnoreCase("Aceito") ? "#dc3545" : "#28a745") + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;"); // Estilo dinâmico
        btnToggleStatus.setOnAction(e -> {
            // Alterna o status entre "Aceito" e "Negado"
            String novoStatus = user.permissao.equalsIgnoreCase("Aceito") ? "Negado" : "Aceito";
            toggleStatusUsuario(user.id, novoStatus); // Atualiza o status no banco
            usuarios = carregarUsuarios(); // Recarrega a lista de usuários
            aplicarFiltros(); // Atualiza a interface
        });

        // Organiza os botões em um HBox
        HBox botoes = new HBox(10, btnEditar, btnToggleStatus); // Espaçamento de 10px
        botoes.setAlignment(Pos.CENTER_RIGHT); // Alinha à direita

        // Adiciona os elementos ao painel
        painel.getChildren().addAll(nomeLabel, cargoLabel, statusLabel, botoes);

        return painel; // Retorna o painel do usuário
    }
}