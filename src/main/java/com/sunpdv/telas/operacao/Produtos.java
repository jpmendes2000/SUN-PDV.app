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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * Classe responsável por gerenciar a tela de produtos no sistema SUN PDV.
 * Esta tela permite visualizar, adicionar, editar e desativar produtos, além de oferecer
 * funcionalidades como pesquisa, navegação e dashboard administrativo.
 */
public class Produtos {

    // Atributos da classe para controle da interface e dados
    private Stage stage;
    private TableView<Produto> table;
    private ObservableList<Produto> listaProdutos;
    private TextField campoPesquisa;
    private Label lblMensagemSucesso;
    private Produto produtoSelecionado;
    private boolean modoAdminAtivo = false;
    private ScrollPane scrollTable;
    private VBox dashboardContainer;
    private Button btnAdmin;
    private Button btnAdd;
    private Button btnEdit;
    private Button btnDelete;

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
            this.initOwner(owner);
            this.initModality(Modality.WINDOW_MODAL);
            this.setTitle(title);
            this.setHeaderText(header);
            this.setContentText(content);
            
            try {
                this.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
                );
            } catch (Exception e) {
                System.err.println("Erro ao carregar CSS: " + e.getMessage());
            }
        }
    }

    /**
     * Exibe a tela de gerenciamento de produtos.
     * @param stage O palco (Stage) onde a tela será exibida
     */
    public void show(Stage stage) {
        this.stage = stage;
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #00435a;");

        // Configuração da área esquerda (menu lateral)
        VBox leftMenu = new VBox();
        leftMenu.setPrefWidth(280);
        leftMenu.setMinWidth(280);
        leftMenu.setStyle("-fx-background-color: #00536d;");

        // Logo SUN PDV
        Image logo = null;
        try {
            logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        } catch (Exception e) {
            System.err.println("Erro ao carregar logo: " + e.getMessage());
        }
        
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);

        // Cria o título da tela
        Label titulonaABA = new Label("Gerencia");
        titulonaABA.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

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
        dataHoraBox.setPadding(new Insets(0, 0, 15, 0));

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

        // Container para o conteúdo do menu (sem ScrollPane)
        VBox menuContent = new VBox();
        menuContent.setAlignment(Pos.TOP_CENTER);
        menuContent.setSpacing(10);
        menuContent.setPadding(new Insets(10, 0, 20, 0));

        // Botões do menu
        btnAdmin = criarBotaoLateral("Administrativo", "/img/icon/pasta.png");
        Button btnVoltar = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        // Ações dos botões
        btnAdmin.setOnAction(e -> toggleModoAdmin());
        btnVoltar.setOnAction(e -> voltarParaHome(stage));
        btnSair.setOnAction(e -> {
            CustomConfirmationAlert alert = new CustomConfirmationAlert(stage, "Confirmação", "Deseja sair?", "Isso fechará o sistema.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });

        // Adiciona botões ao container
        menuContent.getChildren().addAll(btnAdmin, btnVoltar, btnSair);

        // Espaço flexível para empurrar o menu para baixo
        Region espacoFlexivel = new Region();
        VBox.setVgrow(espacoFlexivel, Priority.ALWAYS);

        // Monta o menu lateral final (sem ScrollPane)
        leftMenu.getChildren().addAll(logoBox, dataHoraBox, espacoFlexivel, menuContent);

        // Configuração da área central (conteúdo principal)
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(20);
        contentGrid.setVgap(10);
        contentGrid.setPadding(new Insets(15));
        contentGrid.setAlignment(Pos.TOP_CENTER);

        // Configuração da label de mensagem de sucesso
        lblMensagemSucesso = new Label();
        lblMensagemSucesso.getStyleClass().add("mensagem-sucesso");
        lblMensagemSucesso.setVisible(false);

        // Configuração do campo de pesquisa
        campoPesquisa = new TextField();
        campoPesquisa.setPromptText("Pesquisar produto...");
        campoPesquisa.setPrefWidth(400);
        campoPesquisa.textProperty().addListener((obs, oldVal, newVal) -> filtrarProdutos(newVal));

        // Configuração dos botões de ação
        btnAdd = criarBotaoAcao("/img/icon/lista.png", "Adicionar Produto");
        btnEdit = criarBotaoAcao("/img/icon/lapis.png", "Editar Produto");
        btnDelete = criarBotaoAcao("/img/icon/lixeira.png", "Apagar Produto"); // Mudança no tooltip

        btnEdit.setDisable(true);
        btnDelete.setDisable(true);

        // Organização da área superior (mensagem e campo de pesquisa com botões)
        HBox tituloMensagemBox = new HBox(10, lblMensagemSucesso);
        tituloMensagemBox.setAlignment(Pos.TOP_LEFT);

        HBox pesquisaAcoesBox = new HBox(12, campoPesquisa, btnAdd, btnEdit, btnDelete);
        pesquisaAcoesBox.setAlignment(Pos.CENTER_LEFT);
        pesquisaAcoesBox.setPadding(new Insets(5, 0, 15, 10));

        VBox topoBox = new VBox(5, tituloMensagemBox, pesquisaAcoesBox);
        contentGrid.add(topoBox, 0, 0, 2, 1);

        // Configuração da tabela de produtos
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");
        table.setStyle("-fx-padding: 0;");

        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(950);

        TableColumn<Produto, String> colCodBarras = new TableColumn<>("Código de Barras");
        colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras"));
        colCodBarras.setPrefWidth(300);

        TableColumn<Produto, String> colPreco = new TableColumn<>("Preço (R$)");
        colPreco.setCellValueFactory(cell -> {
            Double preco = cell.getValue().getPreco();
            DecimalFormat df = new DecimalFormat("R$ #,##0.00");
            return new SimpleStringProperty(df.format(preco));
        });
        colPreco.setPrefWidth(150);

        table.getColumns().addAll(colNome, colCodBarras, colPreco);

        // Configuração do duplo clique para editar
        table.setRowFactory(tv -> {
            TableRow<Produto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    produtoSelecionado = row.getItem();
                    abrirFormularioProduto(produtoSelecionado);
                }
            });
            return row;
        });

        // Configuração do menu de contexto
        ContextMenu contextMenu = criarMenuContexto();
        table.setContextMenu(contextMenu);

        // Listener para menu de contexto com botão direito
        table.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Produto selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    contextMenu.show(table, event.getScreenX(), event.getScreenY());
                } else {
                    contextMenu.hide();
                }
            } else {
                contextMenu.hide();
            }
        });

        // Ajuste do tamanho da tabela
        table.setPrefHeight(1650);
        table.setPrefWidth(1200);

        scrollTable = new ScrollPane(table);
        scrollTable.setFitToWidth(true);
        scrollTable.setFitToHeight(true);
        scrollTable.setPrefViewportHeight(1650);
        scrollTable.setStyle("-fx-padding: 0;");

        // Inicializa o container do dashboard (inicialmente vazio)
        dashboardContainer = new VBox();
        dashboardContainer.setVisible(false);

        // Adiciona ambos ao GridPane
        contentGrid.add(scrollTable, 0, 1, 2, 1);
        contentGrid.add(dashboardContainer, 0, 1, 2, 1);

        // Configuração do layout principal
        layout.setLeft(leftMenu);
        layout.setCenter(contentGrid);

        // Associa ações aos botões
        btnAdd.setOnAction(e -> abrirFormularioProduto(null));
        btnEdit.setOnAction(e -> {
            if (produtoSelecionado != null) {
                abrirFormularioProduto(produtoSelecionado);
            } else {
                mostrarAlertaErro("Nenhum produto selecionado", "Por favor, selecione um produto para editar.");
            }
        });
        btnDelete.setOnAction(e -> {
            if (produtoSelecionado != null) {
                desativarProduto(produtoSelecionado);
            } else {
                mostrarAlertaErro("Nenhum produto selecionado", "Por favor, selecione um produto para Apagar.");
            }
        });

        // Listener para seleção na tabela
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            produtoSelecionado = newVal;
            btnEdit.setDisable(newVal == null || modoAdminAtivo);
            btnDelete.setDisable(newVal == null || modoAdminAtivo);
        });

        // Carrega os produtos do banco de dados
        carregarProdutos();

        // Configuração da cena e exibição do palco
        Scene scene = new Scene(layout, 1200, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSS: " + e.getMessage());
        }

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
        stage.setTitle("Gerenciamento de Produtos");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }

    /**
     * Alterna entre o modo de visualização de produtos e o dashboard administrativo
     */
    private void toggleModoAdmin() {
        modoAdminAtivo = !modoAdminAtivo;
        
        if (modoAdminAtivo) {
            atualizarBotaoLateral(btnAdmin, "Produtos", "/img/icon/lista.png");
            btnAdmin.setTooltip(new Tooltip("Produtos"));
            
            scrollTable.setVisible(false);
            criarDashboard();
            dashboardContainer.setVisible(true);
            
            btnAdd.setDisable(true);
            btnEdit.setDisable(true);
            btnDelete.setDisable(true);
            campoPesquisa.setDisable(true);
        } else {
            atualizarBotaoLateral(btnAdmin, "Administrativo", "/img/icon/pasta.png");
            btnAdmin.setTooltip(new Tooltip("Acessar dashboard administrativo"));
            
            dashboardContainer.setVisible(false);
            scrollTable.setVisible(true);
            
            btnAdd.setDisable(false);
            btnEdit.setDisable(produtoSelecionado == null);
            btnDelete.setDisable(produtoSelecionado == null);
            campoPesquisa.setDisable(false);
        }
    }

    /**
     * Atualiza o estilo do botão lateral para alternar entre ícones e textos
     */
    private void atualizarBotaoLateral(Button botao, String texto, String caminhoIcone) {
        try {
            Image img = new Image(getClass().getResourceAsStream(caminhoIcone));
            if (img.isError()) {
                throw new Exception("Erro ao carregar imagem: " + caminhoIcone);
            }

            ImageView icon = new ImageView(img);
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            Label textLabel = new Label(texto);
            textLabel.setStyle("-fx-text-fill: #a9cce3; -fx-font-weight: bold;");

            StackPane indicatorContainer = new StackPane();
            indicatorContainer.setPrefWidth(4);
            indicatorContainer.setPrefHeight(60);
            indicatorContainer.setStyle("-fx-background-color: transparent;");

            HBox leftContent = new HBox(10, icon, textLabel);
            leftContent.setAlignment(Pos.CENTER_LEFT);

            HBox content = new HBox(leftContent, new Region(), indicatorContainer);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(content.getChildren().get(1), Priority.ALWAYS);

            botao.setGraphic(content);
            botao.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            botao.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
            botao.setPrefWidth(280);
            botao.setPrefHeight(42);

            botao.setOnMouseEntered(e -> {
                botao.setStyle("-fx-background-color: linear-gradient(to left, rgba(192, 151, 39, 0.39), rgba(232, 186, 35, 0.18)); -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color: rgba(255, 204, 0, 0.64); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            });
            botao.setOnMouseExited(e -> {
                botao.setStyle("-fx-background-color: transparent; -fx-border-radius: 4; -fx-background-radius: 4;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
            });
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + caminhoIcone);
            botao.setText(texto);
            botao.setStyle("-fx-text-fill: #a9cce3; -fx-font-weight: bold; -fx-background-color: transparent;");
        }
    }

    /**
     * Cria o dashboard administrativo com gráficos
     */
    private void criarDashboard() {
        dashboardContainer.getChildren().clear();
        dashboardContainer.setSpacing(20);
        dashboardContainer.setPadding(new Insets(20));
        dashboardContainer.setStyle("-fx-background-color: transparent;");
        
        VBox dashboardContent = new VBox();
        dashboardContent.setSpacing(20);
        dashboardContent.setPadding(new Insets(10));
        
        Label tituloDashboard = new Label("Dashboard Administrativo");
        tituloDashboard.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #a9cce3;");
        dashboardContent.getChildren().add(tituloDashboard);
        
        try {
            BarChart<String, Number> combinedChart = criarGraficoColuna("Dashboard de Vendas", null);
            carregarDadosCombinados(combinedChart);
            dashboardContent.getChildren().add(combinedChart);

            LocalDate current = LocalDate.now();
            int year = current.getYear();
            int month = current.getMonthValue();
            String employeeInfo = getTopEmployee(year, month);
            if (employeeInfo != null) {
                Label employeeLabel = new Label(employeeInfo);
                employeeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a9cce3; -fx-padding: 10;");
                dashboardContent.getChildren().add(employeeLabel);
            }
            
            LocalDate currentDate = LocalDate.now();
            for (int i = 11; i >= 0; i--) {
                LocalDate monthDate = currentDate.minusMonths(i);
                int yearMonthly = monthDate.getYear();
                int monthMonthly = monthDate.getMonthValue();
                String monthName = monthDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")) + " " + yearMonthly;
                
                boolean hasData = hasDataForMonth(yearMonthly, monthMonthly);
                
                if (hasData) {
                    String employeeInfoMonthly = getTopEmployee(yearMonthly, monthMonthly);
                    BarChart<String, Number> monthlyChart = criarGraficoColuna(
                        "Top 10 Produtos - " + monthName, 
                        "#3498db"
                    );
                    carregarTopProdutosMonthly(monthlyChart, yearMonthly, monthMonthly, 10);
                    if (employeeInfoMonthly != null) {
                        Label employeeLabelMonthly = new Label(employeeInfoMonthly);
                        employeeLabelMonthly.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #a9cce3; -fx-padding: 10 0 5 0;");
                        dashboardContent.getChildren().add(employeeLabelMonthly);
                    }
                    dashboardContent.getChildren().add(monthlyChart);
                }
            }
        } catch (SQLException e) {
            Label erroLabel = new Label("Erro ao carregar dados do dashboard: " + e.getMessage());
            erroLabel.setStyle("-fx-text-fill: #a9cce3;");
            dashboardContent.getChildren().add(erroLabel);
        }
        
        ScrollPane dashboardScroll = new ScrollPane(dashboardContent);
        dashboardScroll.setFitToWidth(true);
        dashboardScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dashboardScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dashboardScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        dashboardContainer.getChildren().clear();
        dashboardContainer.getChildren().add(dashboardScroll);
    }

    /**
     * Cria um gráfico de coluna (barras verticais) com estilo similar à imagem
     */
    private BarChart<String, Number> criarGraficoColuna(String titulo, String cor) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        
        barChart.setTitle(titulo);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(400);
        barChart.setMinHeight(400);
        barChart.setStyle("-fx-background-color: linear-gradient(to bottom right, #025a74ff, #0b5167ff, #17353fff); -fx-padding: 15; -fx-border-color: linear-gradient(to bottom right, #0280a6ff, #0f7392ff, #255565ff); -fx-border-width: 1;");
        
        barChart.lookup(".chart-title").setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelFont(javafx.scene.text.Font.font(12));
        xAxis.setTickLabelFill(javafx.scene.paint.Color.BLACK);
        xAxis.setStyle("-fx-tick-label-fill: black;");
        
        yAxis.setTickLabelFont(javafx.scene.text.Font.font(12));
        yAxis.setTickLabelFill(javafx.scene.paint.Color.BLACK);
        yAxis.setForceZeroInRange(true);
        
        return barChart;
    }

    /**
     * Verifica se há dados de vendas para um mês específico
     */
    private boolean hasDataForMonth(int year, int month) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM vendas v " +
                     "WHERE YEAR(v.Data_Venda) = ? AND MONTH(v.Data_Venda) = ?";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        }
        return false;
    }

    /**
     * Carrega dados combinados para o gráfico principal
     */
    private void carregarDadosCombinados(BarChart<String, Number> chart) throws SQLException {
        chart.getData().clear();
        
        XYChart.Series<String, Number> seriesReceita = new XYChart.Series<>();
        seriesReceita.setName("Receita");
        String colorReceita = "#FFD700";
        
        XYChart.Series<String, Number> seriesQuantidade = new XYChart.Series<>();
        seriesQuantidade.setName("Quantidade Vendida");
        String colorQuantidade = "#FF0000";
        
        XYChart.Series<String, Number> seriesVendas = new XYChart.Series<>();
        seriesVendas.setName("Número de Vendas");
        String colorVendas = "#0000FF";
        
        String[] months = {"janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"};
        int year = LocalDate.now().getYear();
        
        for (int m = 1; m <= 12; m++) {
            String monthName = months[m - 1];
            
            double receita = getMonthlyValue("SUM(v.Subtotal)", year, m);
            XYChart.Data<String, Number> dataReceita = new XYChart.Data<>(monthName, receita);
            addHoverAndStyle(dataReceita, colorReceita, "Receita em " + monthName + ": R$ " + String.format("%.2f", receita));
            seriesReceita.getData().add(dataReceita);
            
            double quantidade = getMonthlyValue("SUM(ci.Quantidade)", year, m);
            XYChart.Data<String, Number> dataQuantidade = new XYChart.Data<>(monthName, quantidade);
            addHoverAndStyle(dataQuantidade, colorQuantidade, "Quantidade vendida em " + monthName + ": " + (int) quantidade);
            seriesQuantidade.getData().add(dataQuantidade);
            
            double vendas = getMonthlyValue("COUNT(v.ID_Vendas)", year, m);
            XYChart.Data<String, Number> dataVendas = new XYChart.Data<>(monthName, vendas);
            addHoverAndStyle(dataVendas, colorVendas, "Número de vendas em " + monthName + ": " + (int) vendas);
            seriesVendas.getData().add(dataVendas);
        }
        
        chart.getData().addAll(seriesReceita, seriesQuantidade, seriesVendas);
        chart.setLegendVisible(true);
        chart.lookup(".chart-legend").setStyle("-fx-background-color: transparent;");
    }

    /**
     * Obtém o valor agregado para um mês específico
     */
    private double getMonthlyValue(String aggregate, int year, int month) throws SQLException {
        String join = aggregate.contains("ci.") ? "JOIN carrinho_itens ci ON v.ID_Carrinho = ci.ID_Carrinho " : "";
        String sql = "SELECT " + aggregate + " as total FROM vendas v " + join +
                     "WHERE YEAR(v.Data_Venda) = ? AND MONTH(v.Data_Venda) = ?";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    /**
     * Adiciona hover e estilo à barra
     */
    private void addHoverAndStyle(XYChart.Data<String, Number> data, String color, String tooltipText) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-bar-fill: " + color + ";");
                
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                Tooltip.install(newNode, tooltip);
                
                newNode.setOnMouseEntered(e -> {
                    newNode.setStyle("-fx-bar-fill: derive(" + color + ", -20%); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 1);");
                });
                newNode.setOnMouseExited(e -> {
                    newNode.setStyle("-fx-bar-fill: " + color + ";");
                });
            }
        });
    }

    /**
     * Carrega top produtos para um mês específico
     */
    private void carregarTopProdutosMonthly(BarChart<String, Number> chart, int year, int month, int topN) throws SQLException {
        String sql = "SELECT TOP " + topN + " p.Nome, SUM(ci.Quantidade) as TotalVendas " +
                    "FROM carrinho_itens ci " +
                    "JOIN produtos p ON ci.ID_Produto = p.ID_Produto " +
                    "JOIN vendas v ON ci.ID_Carrinho = v.ID_Carrinho " +
                    "WHERE YEAR(v.Data_Venda) = ? AND MONTH(v.Data_Venda) = ? AND p.Ativo = 1 " +
                    "GROUP BY p.Nome " +
                    "ORDER BY TotalVendas DESC";
        
        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                boolean hasData = false;
                
                while (rs.next()) {
                    hasData = true;
                    String name = rs.getString("Nome");
                    Number total = rs.getInt("TotalVendas");
                    
                    String displayName = name.length() > 20 ? name.substring(0, 17) + "..." : name;
                    
                    XYChart.Data<String, Number> data = new XYChart.Data<>(displayName, total);
                    
                    String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
                    addHoverAndStyle(data, "#3498db", name + ": " + total + " vendas em " + monthName + "/" + year);
                    series.getData().add(data);
                }
                
                if (hasData) {
                    chart.getData().add(series);
                }
            }
        }
    }

    /**
     * Obtém o top funcionário para um mês específico
     */
    private String getTopEmployee(int year, int month) throws SQLException {
        String sql = "SELECT TOP 1 ls.Nome, COUNT(v.ID_Vendas) as TotalVendas " +
                    "FROM vendas v " +
                    "JOIN login_sistema ls ON v.ID_Login = ls.ID_Login " +
                    "WHERE YEAR(v.Data_Venda) = ? AND MONTH(v.Data_Venda) = ? " +
                    "GROUP BY ls.Nome " +
                    "ORDER BY TotalVendas DESC";
        
        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("Nome");
                    int total = rs.getInt("TotalVendas");
                    String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
                    return "Funcionário do mês (" + monthName + "/" + year + "): " + nome + " - " + total + " vendas";
                }
            }
        }
        return null;
    }

    /**
     * Cria um menu de contexto para a tabela de produtos.
     */
    private ContextMenu criarMenuContexto() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editarItem = new MenuItem("Editar");
        editarItem.setOnAction(e -> {
            if (produtoSelecionado != null) {
                abrirFormularioProduto(produtoSelecionado);
            } else {
                mostrarAlertaErro("Nenhum produto selecionado", "Por favor, selecione um produto para editar.");
            }
        });

        MenuItem desativarItem = new MenuItem("Apagar");
        desativarItem.setOnAction(e -> {
            if (produtoSelecionado != null) {
                desativarProduto(produtoSelecionado);
            } else {
                mostrarAlertaErro("Nenhum produto selecionado", "Por favor, selecione um produto para Apagar.");
            }
        });

        contextMenu.getItems().addAll(editarItem, desativarItem);
        return contextMenu;
    }

    /**
     * Cria um botão de ação com ícone e tooltip.
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
            btn.setAlignment(Pos.CENTER);
            btn.getStyleClass().add("acao");

            if (tooltip.toLowerCase().contains("Apagar")) {
                btn.getStyleClass().add("delete"); // Mantém o estilo visual de "apagar"
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
            textLabel.setStyle("-fx-text-fill: #a9cce3; -fx-font-weight: bold;");

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
            Button btn = new Button(texto);
            btn.setStyle("-fx-text-fill: #a9cce3; -fx-font-weight: bold; -fx-background-color: transparent;");
            btn.setPrefWidth(280);
            btn.setPrefHeight(40);
            return btn;
        }
    }

    /**
     * Carrega os produtos do banco de dados e popula a tabela (apenas ativos).
     */
    private void carregarProdutos() {
        listaProdutos = FXCollections.observableArrayList();
        String sql = "SELECT ID_Produto, Nome, Cod_Barras, Preco, Ativo FROM produtos WHERE Ativo = 1 ORDER BY Nome";

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
     */
    private void abrirFormularioProduto(Produto produto) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
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
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSS: " + e.getMessage());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Salva ou atualiza um produto no banco de dados.
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
     */
    private boolean validarProdutoExistente(String nome, String codBarras, int idProduto) {
        String sql = "SELECT ID_Produto, Nome, Cod_Barras FROM produtos WHERE (Nome = ? OR Cod_Barras = ?) AND ID_Produto != ? AND Ativo = 1";

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
                        mensagem = "Já existe um produto ativo com este nome e código de barras!";
                    } else if (nomeExistente.equalsIgnoreCase(nome)) {
                        mensagem = "Já existe um produto ativo com este nome!";
                    } else {
                        mensagem = "Já existe um produto ativo com este código de barras!";
                    }

                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Produto Existente");
                    alert.setHeaderText("Conflito ao salvar produto");
                    alert.setContentText(mensagem);
                    try {
                        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                    } catch (Exception e) {
                        System.err.println("Erro ao carregar CSS: " + e.getMessage());
                    }

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
     */
    private void inserirProduto(Produto produto) {
        String sql = "INSERT INTO produtos (Nome, Cod_Barras, Preco, Ativo) VALUES (?, ?, ?, 1)";
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
     * Desativa um produto no banco de dados após confirmação.
     */
    private void desativarProduto(Produto produto) {
        if (produto == null) {
            System.err.println("Erro: Tentativa de Apagar um produto nulo.");
            mostrarAlertaErro("Erro", "Nenhum produto selecionado para Apagar.");
            return;
        }

        System.out.println("Tentando desativar produto: ID=" + produto.getId() + ", Nome=" + produto.getNome());

        // Diálogo de confirmação
        CustomConfirmationAlert confirm = new CustomConfirmationAlert(stage, "Confirmação", 
            "Deseja apagar o produto: " + produto.getNome() + "?", "O produto será oculto da lista");
        confirm.initModality(Modality.WINDOW_MODAL);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE produtos SET Ativo = 0 WHERE ID_Produto = ?";
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, produto.getId());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Produto ID=" + produto.getId() + " desativado com sucesso.");
                    listaProdutos.remove(produto);
                    table.refresh();
                    mostrarMensagemSucesso("Produto desativado com sucesso!");
                    produtoSelecionado = null;
                    btnEdit.setDisable(true);
                    btnDelete.setDisable(true);
                } else {
                    System.err.println("Erro: Nenhum produto desativado para ID=" + produto.getId());
                    mostrarAlertaErro("Erro ao Apagar", "Nenhum produto foi Apagar. Verifique se o produto ainda existe.");
                }
            } catch (SQLException e) {
                System.err.println("SQLException ao desativar produto: " + e.getMessage());
                mostrarAlertaErro("Erro no banco de dados", "Erro ao Apagar produto: " + e.getMessage());
            }
        }
    }

    /**
     * Volta para a tela inicial com base no cargo do usuário.
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
     * Exibe uma mensagem de sucesso temporária na interface.
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
     */
    private void mostrarAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(stage);
        alert.setContentText(mensagem);
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSS: " + e.getMessage());
        }
        alert.showAndWait();
    }
}