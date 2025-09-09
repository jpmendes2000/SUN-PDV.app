package com.sunpdv.telas.operacao;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Classe responsável por gerar e gerenciar o dashboard administrativo
 * do sistema SUN PDV.
 */
public class DashboardAdministrativo {
    
    // Constantes para conexão com banco de dados
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "Senha@12345!";
    
    /**
     * Estabelece conexão com o banco de dados
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * Cria o container principal do dashboard
     */
    public ScrollPane criarDashboard() {
        VBox dashboardContent = new VBox();
        dashboardContent.setSpacing(20);
        dashboardContent.setPadding(new Insets(10));
        
        // Título do Dashboard
        Label tituloDashboard = new Label("Dashboard Administrativo");
        tituloDashboard.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #a9cce3;");
        dashboardContent.getChildren().add(tituloDashboard);
        
        try {
            // Cards de métricas no topo
            HBox metricsContainer = criarCardsMetricas();
            dashboardContent.getChildren().add(metricsContainer);
            
            // Gráfico principal (vendas temporais - LINHA)
            LineChart<String, Number> mainChart = criarGraficoLinha("Dashboard de Vendas");
            carregarDadosCombinados(mainChart);
            dashboardContent.getChildren().add(mainChart);

            // Gráfico de top 10 produtos
            BarChart<String, Number> topProdutosChart = criarGraficoBarras("Top 10 Produtos Mais Vendidos");
            carregarTop10ProdutosGeral(topProdutosChart);
            dashboardContent.getChildren().add(topProdutosChart);

            // Informação do funcionário do mês
            String employeeInfo = getTopEmployee();
            if (employeeInfo != null) {
                Label employeeLabel = new Label(employeeInfo);
                employeeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a9cce3; -fx-padding: 10;");
                dashboardContent.getChildren().add(employeeLabel);
            }
            
        } catch (SQLException e) {
            Label erroLabel = new Label("Erro ao carregar dados do dashboard: " + e.getMessage());
            erroLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px; -fx-padding: 20;");
            dashboardContent.getChildren().add(erroLabel);
        }
        
        ScrollPane dashboardScroll = new ScrollPane(dashboardContent);
        dashboardScroll.setFitToWidth(true);
        dashboardScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dashboardScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dashboardScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        return dashboardScroll;
    }
    
    /**
     * Cria os cards de métricas no topo do dashboard
     */
    private HBox criarCardsMetricas() throws SQLException {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 0, 20, 0));
        
        // Card Receita Mensal
        VBox cardReceita = criarCardMetrica("receita mensal", getReceitaMensal(), "R$");
        
        // Card Quantidade Vendas
        VBox cardQuantidade = criarCardMetrica("quantidade vendas", getQuantidadeVendasMensal(), "");
        
        // Card Receita Anual
        VBox cardReceitaAnual = criarCardMetrica("receita anual", getReceitaAnual(), "R$");
        
        container.getChildren().addAll(cardReceita, cardQuantidade, cardReceitaAnual);
        return container;
    }
    
    /**
     * Cria um card individual de métrica
     */
    private VBox criarCardMetrica(String titulo, double valor, String prefixo) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setPrefHeight(100);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #00536d, #00536d);" +
            "-fx-border-color: #6c757d;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 15;" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);"
        );
        
        Label labelTitulo = new Label(titulo);
        labelTitulo.setStyle("-fx-text-fill: #ffffffff; -fx-font-size: 12px; -fx-font-weight: normal;");
        
        String valorFormatado;
        if (prefixo.equals("R$")) {
            if (valor >= 1000) {
                valorFormatado = String.format("R$ %.2fk", valor / 1000);
            } else {
                valorFormatado = String.format("R$ %.2f", valor);
            }
        } else {
            if (valor >= 1000) {
                valorFormatado = String.format("%.0fk", valor / 1000);
            } else {
                valorFormatado = String.format("%.0f", valor);
            }
        }
        
        Label labelValor = new Label(valorFormatado);
        labelValor.setStyle("-fx-text-fill: #ffffffff; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(labelTitulo, labelValor);
        return card;
    }
    
    /**
     * Cria gráfico de LINHA para dados temporais
     */
    private LineChart<String, Number> criarGraficoLinha(String titulo) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        
        lineChart.setTitle(titulo);
        lineChart.setLegendVisible(true);
        lineChart.setPrefHeight(300);
        lineChart.setMinHeight(300);
        lineChart.setCreateSymbols(true);
        lineChart.setStyle(
            "-fx-background-color: #00536d;" +
            "-fx-padding: 20;" +
            "-fx-border-color: #dee2e6;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        
        xAxis.setTickLabelRotation(0);
        xAxis.setStyle("-fx-tick-label-fill: #ffffffff; -fx-font-size: 11px;");
        yAxis.setStyle("-fx-tick-label-fill: #ffffffff; -fx-font-size: 11px;");
        yAxis.setForceZeroInRange(true);
        
        return lineChart;
    }
    
    /**
     * Cria gráfico de BARRAS para produtos
     */
    private BarChart<String, Number> criarGraficoBarras(String titulo) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        
        barChart.setTitle(titulo);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(350);
        barChart.setMinHeight(350);
        barChart.setStyle(
            "-fx-background-color: #00536d;" +
            "-fx-padding: 20;" +
            "-fx-border-color: #dee2e6;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        
        xAxis.setTickLabelRotation(-45);
        xAxis.setStyle("-fx-tick-label-fill: #ffffffff; -fx-font-size: 10px;");
        yAxis.setStyle("-fx-tick-label-fill: #ffffffff; -fx-font-size: 11px;");
        yAxis.setForceZeroInRange(true);
        
        return barChart;
    }
    
    /**
     * Carrega dados combinados para o gráfico de linhas
     */
    private void carregarDadosCombinados(LineChart<String, Number> chart) throws SQLException {
        chart.getData().clear();
        
        XYChart.Series<String, Number> seriesReceita = new XYChart.Series<>();
        seriesReceita.setName("Receita");

        XYChart.Series<String, Number> seriesQuantidade = new XYChart.Series<>();
        seriesQuantidade.setName("Quantidade");

        XYChart.Series<String, Number> seriesVendas = new XYChart.Series<>();
        seriesVendas.setName("Número de Vendas");

        String[] months = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", 
                          "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        int year = LocalDate.now().getYear();

        for (int m = 1; m <= 12; m++) {
            String monthName = months[m - 1];

            double receita = getMonthlyValue("SUM(v.Subtotal)", year, m);
            seriesReceita.getData().add(new XYChart.Data<>(monthName, receita));

            double quantidade = getMonthlyValue("SUM(ci.Quantidade)", year, m);
            seriesQuantidade.getData().add(new XYChart.Data<>(monthName, quantidade));

            double vendas = getMonthlyValue("COUNT(v.ID_Vendas)", year, m);
            seriesVendas.getData().add(new XYChart.Data<>(monthName, vendas));
        }

        chart.getData().addAll(seriesReceita, seriesQuantidade, seriesVendas);
    }
    
    /**
     * Carrega top 10 produtos mais vendidos (ordenado decrescente)
     */
    private void carregarTop10ProdutosGeral(BarChart<String, Number> chart) throws SQLException {
        String sql = "SELECT TOP 10 p.Nome, SUM(ci.Quantidade) as TotalVendas " +
                    "FROM carrinho_itens ci " +
                    "JOIN produtos p ON ci.ID_Produto = p.ID_Produto " +
                    "JOIN vendas v ON ci.ID_Carrinho = v.ID_Carrinho " +
                    "WHERE p.Ativo = 1 " +
                    "GROUP BY p.Nome " +
                    "ORDER BY TotalVendas DESC";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int posicao = 1;
            
            while (rs.next()) {
                String nome = rs.getString("Nome");
                Number total = rs.getInt("TotalVendas");
                
                String displayName = nome.length() > 15 ? nome.substring(0, 12) + "..." : nome;
                
                XYChart.Data<String, Number> data = new XYChart.Data<>(displayName, total);
                String cor = getCorGradiente(posicao);
                configurarBarraComCor(data, cor, nome + ": " + total + " vendas (Posição: " + posicao + ")");
                
                series.getData().add(data);
                posicao++;
            }
            
            chart.getData().add(series);
        }
    }
    
    /**
     * Retorna cor em gradiente baseada na posição
     */
    private String getCorGradiente(int posicao) {
        switch (posicao) {
            case 1: return "#1e7e34";
            case 2: return "#28a745";
            case 3: return "#34ce57";
            case 4: return "#48d668";
            case 5: return "#5dd879";
            case 6: return "#71da89";
            case 7: return "#85dc99";
            case 8: return "#99dea9";
            case 9: return "#ade0b9";
            case 10: return "#c1e2c9";
            default: return "#28a745";
        }
    }
    
    /**
     * Configura cor e tooltip para uma barra do gráfico
     */
    private void configurarBarraComCor(XYChart.Data<String, Number> data, String cor, String tooltipText) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-bar-fill: " + cor + ";");
                
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: white;");
                Tooltip.install(newNode, tooltip);
                
                newNode.setOnMouseEntered(e -> {
                    newNode.setStyle("-fx-bar-fill: derive(" + cor + ", -15%); " +
                                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
                });
                newNode.setOnMouseExited(e -> {
                    newNode.setStyle("-fx-bar-fill: " + cor + ";");
                });
            }
        });
    }
    
    // ===== MÉTODOS DE CONSULTA AO BANCO =====
    
    private double getReceitaMensal() throws SQLException {
        LocalDate now = LocalDate.now();
        return getMonthlyValue("SUM(v.Subtotal)", now.getYear(), now.getMonthValue());
    }
    
    private double getQuantidadeVendasMensal() throws SQLException {
        LocalDate now = LocalDate.now();
        return getMonthlyValue("COUNT(v.ID_Vendas)", now.getYear(), now.getMonthValue());
    }
    
    private double getReceitaAnual() throws SQLException {
        LocalDate now = LocalDate.now();
        String sql = "SELECT SUM(v.Subtotal) as total FROM vendas v WHERE YEAR(v.Data_Venda) = ?";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, now.getYear());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }
    
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
    
    private String getTopEmployee() throws SQLException {
        LocalDate current = LocalDate.now();
        String sql = "SELECT TOP 1 ls.Nome, COUNT(v.ID_Vendas) as TotalVendas " +
                    "FROM vendas v " +
                    "JOIN login_sistema ls ON v.ID_Login = ls.ID_Login " +
                    "WHERE YEAR(v.Data_Venda) = ? AND MONTH(v.Data_Venda) = ? " +
                    "GROUP BY ls.Nome " +
                    "ORDER BY TotalVendas DESC";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, current.getYear());
            ps.setInt(2, current.getMonthValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("Nome");
                    int total = rs.getInt("TotalVendas");
                    String monthName = current.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
                    return String.format("%s é funcionário(a) do mês de %s, realizando %d vendas", nome, monthName, total);
                }
            }
        }
        return null;
    }
}
