package com.sunpdv.telas.operacao;

import com.sunpdv.model.AutenticarUser;
import com.sunpdv.model.Produto;
import com.sunpdv.telas.home.TelaHomeADM;
import com.sunpdv.telas.home.TelaHomeFUN;
import com.sunpdv.telas.home.TelaHomeMOD;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Classe responsável por gerenciar a tela de produtos no sistema SUN PDV.
 * Esta tela permite visualizar, adicionar, editar e apagar produtos, além de oferecer
 * funcionalidades como pesquisa, navegação e dashboard administrativo.
 */
public class Produtos {

    // Atributos da classe para controle da interface e dados
    private Stage stage;
    private TableView<Produto> table;              // Tabela que exibe a lista de produtos
    private ObservableList<Produto> listaProdutos; // Lista observável para armazenar produtos
    private TextField campoPesquisa;               // Campo para filtrar produtos por nome ou código
    private Label lblMensagemSucesso;              // Label para exibir mensagens de sucesso temporárias
    private Produto produtoSelecionado;            // Produto atualmente selecionado na tabela
    private boolean modoAdminAtivo = false;        // Controla se o dashboard está ativo
    private ScrollPane scrollTable;                // ScrollPane da tabela para alternar com dashboard
    private VBox dashboardContainer;               // Container do dashboard administrativo
    private Button btnAdmin;                       // Botão de administração

    // Constantes para conexão com o banco de dados
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "Senha@12345!";

    /**
     * Estabelece uma conexão com o banco de dados usando as constantes definidas.
     * @return Conexão ativa com o banco de dados
     * @throws SQLException Se houver erro na conexão
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    private static class CustomConfirmationAlert extends Alert {
        public CustomConfirmationAlert(Stage owner, String title, String header, String content) {
            super(AlertType.CONFIRMATION);
            this.initOwner(owner); // IMPORTANTE: deve ser o Stage correto
            this.initModality(Modality.WINDOW_MODAL); // ou APPLICATION_MODAL
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            
            Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
            stage.getScene().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
        }
    }

    /**
     * Exibe a tela de gerenciamento de produtos.
     * @param stage O palco (Stage) onde a tela será exibida
     */
    public void show(Stage stage) {
        this.stage = stage;
        // Criação do layout principal usando BorderPane
        // BorderPane permite dividir a tela em cinco áreas: topo, inferior, esquerda, direita e centro
        BorderPane layout = new BorderPane();

        // Configuração da área esquerda (menu lateral)
        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setMinWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        // Logo SUN PDV
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);

        // Cria o título da tela
        Label titulonaABA = new Label("Gerenciamento de Produtos");
        titulonaABA.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(10, logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 5, 0)); // Espaço mínimo abaixo do logotipo

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
        dataHoraBox.setPadding(new Insets(0, 0, 5, 0)); // Espaço mínimo abaixo do dataHoraBox

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

        // Botões do menu
        Button btnVoltar = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        // Ações dos botões
        btnVoltar.setOnAction(e -> voltarParaHome(stage));

        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Confirmação", "Deseja sair?", "Isso fechará o sistema.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });

        VBox buttonBox = new VBox(10, btnVoltar, btnSair);
        buttonBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        // Adicionar elementos ao menu lateral, com dataHoraBox abaixo do logo
        leftMenu.getChildren().addAll(logoBox, dataHoraBox, espaco, buttonBox);

        // Configuração da área central (conteúdo principal)
        // GridPane organiza os elementos em uma grade
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(20); // Espaçamento horizontal entre colunas
        contentGrid.setVgap(10); // Espaçamento vertical entre linhas
        contentGrid.setPadding(new Insets(15)); // Padding externo
        contentGrid.setAlignment(Pos.TOP_CENTER); // Alinha ao topo e centro

        // Configuração da label de mensagem de sucesso
        lblMensagemSucesso = new Label();
        lblMensagemSucesso.getStyleClass().add("mensagem-sucesso"); // Aplica estilo CSS
        lblMensagemSucesso.setVisible(false); // Inicialmente invisível

        // Configuração do campo de pesquisa
        campoPesquisa = new TextField();
        campoPesquisa.setPromptText("Pesquisar produto..."); // Texto de dica
        campoPesquisa.setPrefWidth(400); // Largura preferida
        // Listener para filtrar produtos conforme o texto é digitado
        campoPesquisa.textProperty().addListener((obs, oldVal, newVal) -> filtrarProdutos(newVal));

        // Configuração dos botões de ação
        Button btnAdd = criarBotaoAcao("/img/icon/lista.png", "Adicionar Produto");
        Button btnEdit = criarBotaoAcao("/img/icon/lapis.png", "Editar Produto");
        Button btnDelete = criarBotaoAcao("/img/icon/lixeira.png", "Apagar Produto");
        btnAdmin = criarBotaoAcao("/img/icon/lista.png", "Dashboard Administrativo");

        btnEdit.setDisable(true); // Desabilita até que um produto seja selecionado
        btnDelete.setDisable(true); // Desabilita até que um produto seja selecionado

        // Organização da área superior (mensagem e campo de pesquisa com botões)
        HBox tituloMensagemBox = new HBox(10, lblMensagemSucesso);
        tituloMensagemBox.setAlignment(Pos.TOP_LEFT); // Alinha à esquerda

        HBox pesquisaAcoesBox = new HBox(12, campoPesquisa, btnAdd, btnEdit, btnDelete, btnAdmin);
        pesquisaAcoesBox.setAlignment(Pos.CENTER_LEFT); // Alinha à esquerda com centralização vertical
        pesquisaAcoesBox.setPadding(new Insets(5, 0, 15, 10)); // Padding

        VBox topoBox = new VBox(5, tituloMensagemBox, pesquisaAcoesBox);
        contentGrid.add(topoBox, 0, 0, 2, 1); // Adiciona ao GridPane na linha 0, colunas 0-1

        // Configuração da tabela de produtos
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Ajusta colunas automaticamente
        table.getStyleClass().add("table-view"); // Aplica estilo CSS
        table.setStyle("-fx-padding: 0;"); // Remove padding extra

        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome")); // Mapeia o campo "nome"
        colNome.setPrefWidth(950); // Define a largura preferida

        TableColumn<Produto, String> colCodBarras = new TableColumn<>("Código de Barras");
        colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras")); // Mapeia o campo "codBarras"
        colCodBarras.setPrefWidth(300); // Define a largura preferida

        TableColumn<Produto, String> colPreco = new TableColumn<>("Preço (R$)");
        colPreco.setCellValueFactory(cell -> {
            Double preco = cell.getValue().getPreco(); // Obtém o preço do produto
            DecimalFormat df = new DecimalFormat("R$ #,##0.00"); // Formata como moeda
            return new SimpleStringProperty(df.format(preco)); // Retorna como string formatada
        });
        colPreco.setPrefWidth(150); // Define a largura preferida

        table.getColumns().addAll(colNome, colCodBarras, colPreco);

        // Configuração do duplo clique para editar
        table.setRowFactory(tv -> {
            TableRow<Produto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) { // Verifica duplo clique
                    produtoSelecionado = row.getItem(); // Define o produto selecionado
                    abrirFormularioProduto(produtoSelecionado); // Abre o formulário de edição
                }
            });
            return row;
        });

        // Configuração do menu de contexto
        ContextMenu contextMenu = criarMenuContexto();
        table.setContextMenu(contextMenu);

        // Listener para menu de contexto com botão direito
        table.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) { // Verifica clique direito
                Produto selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    contextMenu.show(table, event.getScreenX(), event.getScreenY()); // Exibe o menu
                } else {
                    contextMenu.hide(); // Oculta se nada estiver selecionado
                }
            } else {
                contextMenu.hide(); // Oculta em outros cliques
            }
        });

        // Ajuste do tamanho da tabela
        table.setPrefHeight(1650); // Define a altura preferida
        table.setPrefWidth(1200);  // Define a largura preferida

        scrollTable = new ScrollPane(table);
        scrollTable.setFitToWidth(true); // Ajusta a largura ao container
        scrollTable.setFitToHeight(true); // Ajusta a altura ao container
        scrollTable.setPrefViewportHeight(1650); // Define a altura da área visível
        scrollTable.setStyle("-fx-padding: 0;"); // Remove padding extra
        
        // Inicializa o container do dashboard (inicialmente vazio)
        dashboardContainer = new VBox();
        dashboardContainer.setVisible(false);
        
        // Adiciona ambos ao GridPane
        contentGrid.add(scrollTable, 0, 1, 2, 1); // Adiciona ao GridPane na linha 1, colunas 0-1

        // Configuração do layout principal
        layout.setLeft(leftMenu); // Define o menu lateral à esquerda
        layout.setCenter(contentGrid); // Define o conteúdo central

        // Associa ações aos botões
        btnAdd.setOnAction(e -> abrirFormularioProduto(null)); // Abre formulário para adicionar
        btnEdit.setOnAction(e -> abrirFormularioProduto(produtoSelecionado)); // Abre formulário para editar
        btnDelete.setOnAction(e -> {
            if (produtoSelecionado != null) {
                apagarProduto(produtoSelecionado); // Apaga o produto selecionado
            }
        });
        
        // Ação do botão de administração
        btnAdmin.setOnAction(e -> toggleModoAdmin());

        btnVoltar.setOnAction(e -> voltarParaHome(stage)); // Volta para a tela inicial
                
        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Confirmação", "Deseja sair?", "Isso fechará o sistema.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });
     // Confirma saída do sistema

        // Listener para seleção na tabela
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            produtoSelecionado = newVal; // Atualiza o produto selecionado
            btnEdit.setDisable(newVal == null); // Habilita/desabilita botão Editar
            btnDelete.setDisable(newVal == null); // Habilita/desabilita botão Apagar
        });

        // Carrega os produtos do banco de dados
        carregarProdutos();

        // Configuração da cena e exibição do palco
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
        stage.setTitle("Gerenciamento de Produtos"); // Define o título da janela
        stage.setFullScreen(true); // Ativa tela cheia
        stage.setResizable(true); // Permite redimensionamento
        stage.show(); // Exibe a janela
    }

    /**
     * Alterna entre o modo de visualização de produtos e o dashboard administrativo
     */
    private void toggleModoAdmin() {
        modoAdminAtivo = !modoAdminAtivo;
        
        if (modoAdminAtivo) {
            // Entrar no modo admin - mostrar dashboard
            btnAdmin.setText("Voltar para Produtos");
            btnAdmin.setTooltip(new Tooltip("Voltar para lista de produtos"));
            
            // Esconder tabela e mostrar dashboard
            scrollTable.setVisible(false);
            criarDashboard();
            dashboardContainer.setVisible(true);
        } else {
            // Sair do modo admin - mostrar produtos
            btnAdmin.setText("Dashboard Administrativo");
            btnAdmin.setTooltip(new Tooltip("Acessar dashboard administrativo"));
            
            // Esconder dashboard e mostrar tabela
            dashboardContainer.setVisible(false);
            scrollTable.setVisible(true);
        }
        
        // Desabilitar outros botões no modo admin
        campoPesquisa.setDisable(modoAdminAtivo);
    }

    /**
     * Cria o dashboard administrativo com gráficos
     */
    private void criarDashboard() {
        dashboardContainer.getChildren().clear();
        dashboardContainer.setSpacing(20);
        dashboardContainer.setPadding(new Insets(20));
        dashboardContainer.setStyle("-fx-background-color: #f0f0f0;");
        
        // Título do dashboard
        Label tituloDashboard = new Label("Dashboard Administrativo");
        tituloDashboard.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00536d;");
        dashboardContainer.getChildren().add(tituloDashboard);
        
        try {
            // Gráfico 1: Produtos mais vendidos (verde)
            BarChart<String, Number> chartMaisVendidos = criarGraficoBarra(
                "Produtos Mais Vendidos", 
                "green"
            );
            carregarDadosProdutosMaisVendidos(chartMaisVendidos);
            dashboardContainer.getChildren().add(chartMaisVendidos);
            
            // Gráfico 2: Produtos menos vendidos (vermelho)
            BarChart<String, Number> chartMenosVendidos = criarGraficoBarra(
                "Produtos Menos Vendidos", 
                "red"
            );
            carregarDadosProdutosMenosVendidos(chartMenosVendidos);
            dashboardContainer.getChildren().add(chartMenosVendidos);
            
            // Gráfico 3: Funcionário que mais realizou vendas
            BarChart<String, Number> chartTopFuncionario = criarGraficoBarra(
                "Funcionário com Mais Vendas", 
                "blue"
            );
            carregarDadosTopFuncionario(chartTopFuncionario);
            dashboardContainer.getChildren().add(chartTopFuncionario);
            
        } catch (SQLException e) {
            Label erroLabel = new Label("Erro ao carregar dados do dashboard: " + e.getMessage());
            erroLabel.setStyle("-fx-text-fill: red;");
            dashboardContainer.getChildren().add(erroLabel);
        }
    }

    /**
     * Cria um gráfico de barras com título e cor específica
     */
    private BarChart<String, Number> criarGraficoBarra(String titulo, String cor) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        
        barChart.setTitle(titulo);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(400);
        barChart.setStyle("-fx-background-color: white; -fx-padding: 15;");
        
        return barChart;
    }

    /**
     * Carrega dados dos produtos mais vendidos
     */
    private void carregarDadosProdutosMaisVendidos(BarChart<String, Number> chart) throws SQLException {
        String sql = "SELECT TOP 5 p.Nome, SUM(iv.Quantidade) as TotalVendas " +
                     "FROM ItensVenda iv " +
                     "JOIN Produtos p ON iv.ID_Produto = p.ID_Produto " +
                     "GROUP BY p.Nome " +
                     "ORDER BY TotalVendas DESC";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            while (rs.next()) {
                String nomeProduto = rs.getString("Nome");
                Number totalVendas = rs.getInt("TotalVendas");
                XYChart.Data<String, Number> data = new XYChart.Data<>(nomeProduto, totalVendas);
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-bar-fill: green;");
                    }
                });
                series.getData().add(data);
            }
            chart.getData().add(series);
        }
    }

    /**
     * Carrega dados dos produtos menos vendidos
     */
    private void carregarDadosProdutosMenosVendidos(BarChart<String, Number> chart) throws SQLException {
        String sql = "SELECT TOP 5 p.Nome, SUM(iv.Quantidade) as TotalVendas " +
                     "FROM ItensVenda iv " +
                     "JOIN Produtos p ON iv.ID_Produto = p.ID_Produto " +
                     "GROUP BY p.Nome " +
                     "ORDER BY TotalVendas ASC";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            while (rs.next()) {
                String nomeProduto = rs.getString("Nome");
                Number totalVendas = rs.getInt("TotalVendas");
                XYChart.Data<String, Number> data = new XYChart.Data<>(nomeProduto, totalVendas);
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-bar-fill: red;");
                    }
                });
                series.getData().add(data);
            }
            chart.getData().add(series);
        }
    }

    /**
     * Carrega dados do funcionário com mais vendas
     */
    private void carregarDadosTopFuncionario(BarChart<String, Number> chart) throws SQLException {
        String sql = "SELECT TOP 1 u.Nome, COUNT(v.ID_Venda) as TotalVendas " +
                     "FROM Vendas v " +
                     "JOIN Usuarios u ON v.ID_Usuario = u.ID_Usuario " +
                     "GROUP BY u.Nome " +
                     "ORDER BY TotalVendas DESC";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            while (rs.next()) {
                String nomeFuncionario = rs.getString("Nome");
                Number totalVendas = rs.getInt("TotalVendas");
                XYChart.Data<String, Number> data = new XYChart.Data<>(nomeFuncionario, totalVendas);
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-bar-fill: blue;");
                    }
                });
                series.getData().add(data);
            }
            chart.getData().add(series);
        }
    }

    /**
     * Cria um menu de contexto para a tabela de produtos.
     * @return ContextMenu com opções de edição e exclusão
     */
    private ContextMenu criarMenuContexto() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editarItem = new MenuItem("Editar");
        editarItem.setOnAction(e -> {
            if (produtoSelecionado != null) {
                abrirFormularioProduto(produtoSelecionado); // Abre formulário para editar
            }
        });

        MenuItem apagarItem = new MenuItem("Apagar");
        apagarItem.setOnAction(e -> {
            if (produtoSelecionado != null) {
                apagarProduto(produtoSelecionado); // Apaga o produto selecionado
            }
        });

        contextMenu.getItems().addAll(editarItem, apagarItem); // Adiciona itens ao menu
        return contextMenu;
    }

    /**
     * Cria um botão de ação com ícone e tooltip.
     * @param caminhoIcone Caminho do arquivo de imagem do ícone
     * @param tooltip Texto do tooltip a ser exibido
     * @return Button configurado com ícone e estilo
     */
    private Button criarBotaoAcao(String caminhoIcone, String tooltip) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            if (img.isError()) {
                throw new Exception("Error loading image: " + caminhoIcone);
            }

            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Button btn = new Button();
            btn.setGraphic(icon);
            btn.setAlignment(Pos.CENTER); // Centraliza o ícone dentro do botão
            btn.getStyleClass().add("acao");

            if (tooltip.toLowerCase().contains("apagar")) {
                btn.getStyleClass().add("delete");
            }

            btn.setTooltip(new Tooltip(tooltip));
            btn.setPrefSize(40, 40);
            return btn;
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            Button btn = new Button(tooltip);
            btn.getStyleClass().add("acao");
            return btn;
        }
    }

    /**
     * Cria um botão lateral com ícone, texto e efeito de hover.
     * @param texto Texto a ser exibido no botão
     * @param caminhoIcone Caminho do arquivo de imagem do ícone
     * @return Button configurado para o menu lateral
     */
    private Button criarBotaoLateral(String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            if (img.isError()) {
                throw new Exception("Error loading image: " + caminhoIcone);
            }

            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            icon.setStyle("-fx-fill: white;");

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            // Container para a barra indicadora amarela (agora à direita)
            StackPane indicatorContainer = new StackPane();
            indicatorContainer.setMinWidth(3);
            indicatorContainer.setMaxWidth(3);
            indicatorContainer.setMinHeight(30);
            indicatorContainer.setMaxHeight(30);
            indicatorContainer.setStyle("-fx-background-color: transparent;");

            // HBox para organizar ícone e texto à esquerda
            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);

            // HBox principal que empurra o indicador para a direita
            HBox content = new HBox(leftContent, new Region(), indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

            Button btn = new Button();
            btn.setGraphic(content);
            btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btn.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
            btn.setPrefWidth(280); // Ajustado para a largura do menu
            btn.setPrefHeight(42);

            // Efeito de hover com a barra amarela à direita
            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: linear-gradient(to left,rgba(192, 151, 39, 0.39),rgba(232, 186, 35, 0.18)); -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color:rgba(255, 204, 0, 0.64); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });

            return btn;
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            Button btn = new Button(texto);
            btn.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(40);
            return btn;
        }
    }

    /**
     * Carrega os produtos do banco de dados e popula a tabela.
     */
    private void carregarProdutos() {
        listaProdutos = FXCollections.observableArrayList();
        String sql = "SELECT ID_Produto, Nome, Cod_Barras, Preco FROM produtos ORDER BY Nome";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaProdutos.add(new Produto(
                    rs.getInt("ID_Produto"),
                    rs.getString("Nome"),
                    rs.getDouble("Preco"),
                    rs.getString("Cod_Barras")
                ));
            }
            table.setItems(listaProdutos);
        } catch (SQLException e) {
            mostrarAlertaErro("Erro ao carregar produtos", "Erro ao carregar produtos do banco: " + e.getMessage());
        }
    }

    /**
     * Filtra os produtos na tabela com base no texto digitado no campo de pesquisa.
     * @param filtro Texto digitado para filtragem
     */
    private void filtrarProdutos(String filtro) {
        if (listaProdutos == null || filtro == null || filtro.isEmpty()) {
            table.setItems(listaProdutos);
            return;
        }

        String filtroLower = filtro.toLowerCase();
        ObservableList<Produto> filtrados = listaProdutos.filtered(p -> 
            p.getNome().toLowerCase().contains(filtroLower) || 
            p.getCodBarras().toLowerCase().contains(filtroLower)
        );
        table.setItems(filtrados);
    }

     /**
     * Abre um formulário para adicionar ou editar um produto.
     * @param produto Produto a ser editado (null para adicionar novo)
     */
    private void abrirFormularioProduto(Produto produto) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.NONE); // Alterado para não bloquear a janela pai
        dialog.initOwner(stage);
        dialog.setTitle(produto == null ? "Adicionar Produto" : "Editar Produto");

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome do Produto");
        txtNome.setPrefWidth(350);

        TextField txtPreco = new TextField();
        txtPreco.setPromptText("Preço (R$)");
        txtPreco.setPrefWidth(350);

        TextField txtCodBarras = new TextField();
        txtCodBarras.setPromptText("Código de Barras");
        txtCodBarras.setPrefWidth(350);

        if (produto != null) {
            txtNome.setText(produto.getNome());
            txtPreco.setText(String.format("%.2f", produto.getPreco()));
            txtCodBarras.setText(produto.getCodBarras());
        }

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setDefaultButton(true);
        btnSalvar.setOnAction(e -> salvarProduto(produto, txtNome, txtCodBarras, txtPreco, dialog));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        formGrid.add(new Label("Nome:"), 0, 0);
        formGrid.add(txtNome, 1, 0);
        formGrid.add(new Label("Preço (R$):"), 0, 1);
        formGrid.add(txtPreco, 1, 1);
        formGrid.add(new Label("Código de Barras:"), 0, 2);
        formGrid.add(txtCodBarras, 1, 2);
        formGrid.add(btnSalvar, 1, 3);

        Scene scene = new Scene(formGrid);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    /**
     * Salva ou atualiza um produto no banco de dados.
     * @param produto Produto a ser salvo ou atualizado
     * @param txtNome Campo de texto para o nome
     * @param txtCodBarras Campo de texto para o código de barras
     * @param txtPreco Campo de texto para o preço
     * @param dialog Janela de diálogo a ser fechada após salvar
     */
    private void salvarProduto(Produto produto, TextField txtNome, TextField txtCodBarras, 
                             TextField txtPreco, Stage dialog) {
        String nome = txtNome.getText().trim();
        String codBarras = txtCodBarras.getText().trim();
        String precoStr = txtPreco.getText().trim();

        if (nome.isEmpty() || codBarras.isEmpty() || precoStr.isEmpty()) {
            mostrarAlertaErro("Campos obrigatórios", "Preencha todos os campos.");
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoStr.replace(",", "."));
            if (preco <= 0) {
                mostrarAlertaErro("Preço inválido", "O preço deve ser maior que zero.");
                return;
            }
        } catch (NumberFormatException ex) {
            mostrarAlertaErro("Preço inválido", "Use números com ponto ou vírgula decimal.");
            return;
        }

        int idProduto = produto != null ? produto.getId() : 0;
        if (validarProdutoExistente(nome, codBarras, idProduto)) {
            return;
        }

        if (produto == null) {
            inserirProduto(new Produto(0, nome, preco, codBarras));
        } else {
            produto.setNome(nome);
            produto.setCodBarras(codBarras);
            produto.setPreco(preco);
            atualizarProduto(produto);
        }
        dialog.close();
    }

    /**
     * Valida se já existe um produto com o mesmo nome ou código de barras.
     * @param nome Nome do produto a ser validado
     * @param codBarras Código de barras a ser validado
     * @param idProduto ID do produto (para ignorar o próprio produto em edição)
     * @return true se já existe, false caso contrário
     */
    private boolean validarProdutoExistente(String nome, String codBarras, int idProduto) {
        String sql = "SELECT ID_Produto, Nome, Cod_Barras FROM produtos WHERE (Nome = ? OR Cod_Barras = ?) AND ID_Produto != ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setString(2, codBarras);
            ps.setInt(3, idProduto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idExistente = rs.getInt("ID_Produto");
                    String nomeExistente = rs.getString("Nome");
                    String codBarrasExistente = rs.getString("Cod_Barras");

                    String mensagem;
                    if (nomeExistente.equalsIgnoreCase(nome) && codBarrasExistente.equalsIgnoreCase(codBarras)) {
                        mensagem = "Já existe um produto com este nome e código de barras!";
                    } else if (nomeExistente.equalsIgnoreCase(nome)) {
                        mensagem = "Já existe um produto com este nome!";
                    } else {
                        mensagem = "Já existe um produto com este código de barras!";
                    }

                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Produto Existente");
                    alert.setHeaderText("Conflito ao salvar produto");
                    alert.setContentText(mensagem);
                    alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                    ButtonType btnFechar = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(btnFechar);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent()) {
                        selecionarProdutoNaTabela(idExistente);
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            mostrarAlertaErro("Erro de validação", "Erro ao validar produto: " + e.getMessage());
        }
        return false;
    }

    /**
     * Seleciona um produto na tabela com base no ID.
     * @param idProduto ID do produto a ser selecionado
     */
    private void selecionarProdutoNaTabela(int idProduto) {
        for (Produto p : listaProdutos) {
            if (p.getId() == idProduto) {
                table.getSelectionModel().select(p);
                table.scrollTo(p);
                break;
            }
        }
    }

    /**
     * Insere um novo produto no banco de dados.
     * @param produto Produto a ser inserido
     */
    private void inserirProduto(Produto produto) {
        String sql = "INSERT INTO produtos (Nome, Cod_Barras, Preco) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, produto.getNome());
            ps.setDouble(3, produto.getPreco());
            ps.setString(2, produto.getCodBarras());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        produto.setId(rs.getInt(1));
                        listaProdutos.add(produto);
                        mostrarMensagemSucesso("Produto adicionado com sucesso!");
                    }
                }
            } else {
                mostrarAlertaErro("Erro ao adicionar", "Erro ao adicionar produto.");
            }
        } catch (SQLException e) {
            mostrarAlertaErro("Erro no banco de dados", "Erro ao adicionar produto: " + e.getMessage());
        }
    }

    /**
     * Atualiza um produto existente no banco de dados.
     * @param produto Produto a ser atualizado
     */
    private void atualizarProduto(Produto produto) {
        String sql = "UPDATE produtos SET Nome=?, Cod_Barras=?, Preco=? WHERE ID_Produto=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getCodBarras());
            ps.setDouble(3, produto.getPreco());
            ps.setInt(4, produto.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                int index = listaProdutos.indexOf(produto);
                if (index >= 0) {
                    listaProdutos.set(index, produto);
                }
                mostrarMensagemSucesso("Produto atualizado com sucesso!");
            } else {
                mostrarAlertaErro("Erro ao atualizar", "Erro ao atualizar produto.");
            }
        } catch (SQLException e) {
            mostrarAlertaErro("Erro no banco de dados", "Erro ao atualizar produto: " + e.getMessage());
        }
    }

    /**
     * Apaga um produto do banco de dados após confirmação.
     * @param produto Produto a ser apagado
     */
    private void apagarProduto(Produto produto) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText("Deseja apagar o produto: " + produto.getNome() + "?");
        confirm.setContentText("Esta ação não pode ser desfeita.");
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        confirm.initModality(Modality.NONE); // Não bloqueia a janela pai
        confirm.initOwner(stage);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM produtos WHERE ID_Produto = ?";
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, produto.getId());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    listaProdutos.remove(produto);
                    mostrarMensagemSucesso("Produto apagado com sucesso!");
                } else {
                    mostrarAlertaErro("Erro ao apagar", "Erro ao apagar produto.");
                }
            } catch (SQLException e) {
                mostrarAlertaErro("Erro no banco de dados", "Erro ao apagar produto: " + e.getMessage());
            }
        }
    }

    /**
     * Volta para a tela inicial com base no cargo do usuário.
     * @param stage Palco atual a ser substituído
     */
    private void voltarParaHome(Stage stage) {
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
                    mostrarAlertaErro("Cargo inválido", "Cargo não reconhecido: " + cargo);
            }
        } catch (Exception ex) {
            mostrarAlertaErro("Erro de navegação", "Erro ao retornar para a tela principal.");
        }
    }

    /**
     * Confirma a saída do sistema.
     * @param stage Palco a ser fechado
     */
    private void confirmarSaida(Stage stage) {
        CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Confirmação de Saída", "Deseja realmente sair do sistema?", null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stage.close();
        }
    }

    /**
     * Exibe uma mensagem de sucesso temporária na interface.
     * @param texto Texto da mensagem a ser exibida
     */
    private void mostrarMensagemSucesso(String texto) {
        lblMensagemSucesso.setText(texto);
        lblMensagemSucesso.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> lblMensagemSucesso.setVisible(false));
        pause.play();
    }

    /**
     * Exibe um alerta de erro na interface.
     * @param titulo Título do alerta
     * @param mensagem Mensagem a ser exibida
     */
    private void mostrarAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.initModality(Modality.NONE); // Adicionar esta linha
        alert.initOwner(stage);
        alert.setContentText(mensagem);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        alert.showAndWait();
    }
}