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

import com.sunpdv.connection.ConexaoDB;

import java.util.ArrayList;
import java.util.List;
public class DashboardAdministrativo {
    
    private static class ProdutoVenda {
        String nome;
        int quantidade;
        
        ProdutoVenda(String nome, int quantidade) {
            this.nome = nome;
            this.quantidade = quantidade;
        }
    }
    
    // Cria o container principal do dashboard
    public ScrollPane criarDashboard() {
        VBox dashboardContent = new VBox();
        dashboardContent.setSpacing(20);
        dashboardContent.setPadding(new Insets(10));
        
        // Título do Dashboard
        Label tituloDashboard = new Label("Dashboard Administrativo");
        tituloDashboard.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #a9cce3;");
        dashboardContent.getChildren().add(tituloDashboard);
        
        try {
            // cards
            HBox metricsContainer = criarCardsMetricas();
            dashboardContent.getChildren().add(metricsContainer);
            // gráfico de linha
            LineChart<String, Number> mainChart = criarGraficoLinha("Receitas Mensais");
            carregarDadosReceitasYoY(mainChart);
            dashboardContent.getChildren().add(mainChart);
            // gráfico de barras    
            BarChart<String, Number> topProdutosChart = criarGraficoBarras("Top 10 Produtos Mais Vendidos");
            carregarTop10ProdutosOrdenado(topProdutosChart);
            dashboardContent.getChildren().add(topProdutosChart);
            // funcionário do mês
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
    
    // cards de metricas
    private HBox criarCardsMetricas() throws SQLException {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 0, 20, 0));
        
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int previousYear = currentYear - 1;
        int currentMonth = now.getMonthValue();
        
        // Card Receita Mensal
        double receitaMensal = getReceitaMensal();
        double receitaMensalYoY = getMonthlyYoY("SUM(v.Subtotal)", currentYear, currentMonth, previousYear, currentMonth);
        VBox cardReceita = criarCardMetrica("Receita Mensal", receitaMensal, "R$", receitaMensalYoY);
        
        // Card Quantidade Vendas
        double qtdVendasMensal = getQuantidadeVendasMensal();
        double qtdVendasYoY = getMonthlyYoY("COUNT(v.ID_Vendas)", currentYear, currentMonth, previousYear, currentMonth);
        VBox cardQuantidade = criarCardMetrica("Quantidade Vendas", qtdVendasMensal, "", qtdVendasYoY);
        
        // Card Receita Anual
        double receitaAnual = getReceitaAnual();
        double receitaAnualYoY = getYearlyYoY("SUM(v.Subtotal)", currentYear, previousYear);
        VBox cardReceitaAnual = criarCardMetrica("Receita Anual", receitaAnual, "R$", receitaAnualYoY);
        
        container.getChildren().addAll(cardReceita, cardQuantidade, cardReceitaAnual);
        return container;
    }
    
    // cards
    private VBox criarCardMetrica(String titulo, double valor, String prefixo, double yoYVariation) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setPrefHeight(120);
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
        
        // Label para variação YoY
        String yoYText = String.format("%.1f%%", yoYVariation);
        String yoYColor = yoYVariation >= 0 ? "#28a745" : "#dc3545"; // Verde para positivo, vermelho para negativo
        Label labelYoY = new Label("YoY: " + (yoYVariation == 0 ? "N/A" : yoYText));
        labelYoY.setStyle("-fx-text-fill: " + yoYColor + "; -fx-font-size: 12px; -fx-font-weight: normal;");
        
        card.getChildren().addAll(labelTitulo, labelValor, labelYoY);
        return card;
    }
    
    // grafico de receitas mensais com YoY
    private LineChart<String, Number> criarGraficoLinha(String titulo) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        
        lineChart.setTitle(titulo);
        lineChart.setLegendVisible(false);
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
    
   // grafico de top 10 produtos mais vendidos
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
    
   // dados de receitas mensais com YoY 
    private void carregarDadosReceitasYoY(LineChart<String, Number> chart) throws SQLException {
        chart.getData().clear();
        
        int anoAtual = LocalDate.now().getYear();
        int anoAnterior = anoAtual - 1;
        
        XYChart.Series<String, Number> seriesAnoAtual = new XYChart.Series<>();
        seriesAnoAtual.setName(String.valueOf(anoAtual));

        XYChart.Series<String, Number> seriesAnoAnterior = new XYChart.Series<>();
        seriesAnoAnterior.setName(String.valueOf(anoAnterior));

        String[] months = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", 
                          "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

        for (int m = 1; m <= 12; m++) {
            String monthName = months[m - 1];
            
            double receitaAtual = getMonthlyValue("SUM(v.Subtotal)", anoAtual, m);
            double receitaAnterior = getMonthlyValue("SUM(v.Subtotal)", anoAnterior, m);
            
            XYChart.Data<String, Number> dataAtual = new XYChart.Data<>(monthName, receitaAtual);
            XYChart.Data<String, Number> dataAnterior = new XYChart.Data<>(monthName, receitaAnterior);
            
            // Calcular YoY
            double variacaoYoY = receitaAnterior > 0 ? ((receitaAtual - receitaAnterior) / receitaAnterior) * 100 : 0;
            String sinalVariacao = variacaoYoY >= 0 ? "+" : "";
            
            // informações YoY
            configurarTooltipYoY(dataAtual, receitaAtual, variacaoYoY, sinalVariacao, anoAtual, monthName);
            configurarTooltipAnterior(dataAnterior, receitaAnterior, anoAnterior, monthName);
            
            seriesAnoAtual.getData().add(dataAtual);
            seriesAnoAnterior.getData().add(dataAnterior);
            chart.getData().addAll(seriesAnoAtual, seriesAnoAnterior);
        }
        
        // Estilo das linhas
        chart.lookupAll(".chart-series-line").forEach(node -> {
            if (chart.getData().indexOf(node.getUserData()) == 0) {
                node.setStyle("-fx-stroke: #c8c966; -fx-stroke-width: 3px;");
            } else {
                node.setStyle("-fx-stroke: #6c9bd1; -fx-stroke-width: 2px; -fx-stroke-dash-array: 5 5;");
            }
        });
    }
    
    
     // informações YoY
     
    private void configurarTooltipYoY(XYChart.Data<String, Number> data, double receita, 
                                     double variacaoYoY, String sinalVariacao, int ano, String mes) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                String corVariacao = variacaoYoY >= 0 ? "#28a745" : "#dc3545";
                String textoVariacao = String.format("%s%.1f%%", sinalVariacao, Math.abs(variacaoYoY));
                
                String tooltipText = String.format(
                    "%s %d: R$ %.2f\nVariação YoY: %s", 
                    mes, ano, receita, textoVariacao
                );
                
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setStyle(String.format(
                    "-fx-font-size: 11px; -fx-background-color: rgba(0,0,0,0.9); " +
                    "-fx-text-fill: white; -fx-border-color: %s; -fx-border-width: 2px;", 
                    corVariacao
                ));
                Tooltip.install(newNode, tooltip);
                
                // Estilo do símbolo
                newNode.setStyle("-fx-background-color: #28a745; -fx-background-radius: 4px;");
            }
        });
    }
    
    /**
     * Configura tooltip para dados do ano anterior
     */
    private void configurarTooltipAnterior(XYChart.Data<String, Number> data, double receita, int ano, String mes) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                String tooltipText = String.format("%s %d: R$ %.2f", mes, ano, receita);
                
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setStyle(
                    "-fx-font-size: 11px; -fx-background-color: rgba(0,0,0,0.9); " +
                    "-fx-text-fill: white; -fx-border-color: #6c9bd1; -fx-border-width: 2px;"
                );
                Tooltip.install(newNode, tooltip);
                
                // Estilo do símbolo
                newNode.setStyle("-fx-background-color: #6c9bd1; -fx-background-radius: 4px;");
            }
        });
    }
    
    /**
     * Carrega top 10 produtos mais vendidos GARANTINDO ordem decrescente
     */
    private void carregarTop10ProdutosOrdenado(BarChart<String, Number> chart) throws SQLException {
        // SQL já com LIMIT para SQL Server e ordenação garantida
        String sql = "SELECT TOP 10 p.Nome, SUM(ci.Quantidade) as TotalVendas " +
                    "FROM carrinho_itens ci " +
                    "JOIN produtos p ON ci.ID_Produto = p.ID_Produto " +
                    "JOIN vendas v ON ci.ID_Carrinho = v.ID_Carrinho " +
                    "WHERE p.Ativo = 1 AND v.Status = 'Concluida' " +
                    "GROUP BY p.Nome " +
                    "ORDER BY SUM(ci.Quantidade) DESC";
        
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            // Coletamos os dados em ordem
            List<ProdutoVenda> produtos = new ArrayList<>();
            while (rs.next()) {
                produtos.add(new ProdutoVenda(
                    rs.getString("Nome"), 
                    rs.getInt("TotalVendas")
                ));
            }
            
            // Criar CategoryAxis com ordem fixa para evitar reordenação automática
            chart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Vendas");
            
            // Adicionar dados na ordem correta (já vem ordenado do BD)
            for (int i = 0; i < produtos.size(); i++) {
                ProdutoVenda produto = produtos.get(i);
                
                // Prefixo numérico para forçar ordem + nome do produto
                String displayName = (i + 1) + "º " + 
                    (produto.nome.length() > 12 ? 
                     produto.nome.substring(0, 9) + "..." : 
                     produto.nome);
                
                XYChart.Data<String, Number> data = new XYChart.Data<>(displayName, produto.quantidade);
                
                String cor = getCorGradiente(i + 1);
                String tooltipText = produto.nome + ": " + produto.quantidade + 
                                   " vendas (Posição: " + (i + 1) + "º)";
                
                configurarBarraComCor(data, cor, tooltipText);
                series.getData().add(data);
            }
            
            chart.getData().add(series);
        }
    }
    
    // Define cores para as barras em cada posição
    private String getCorGradiente(int posicao) {
        switch (posicao) {
            case 1: return "#1e7e34";  // Verde mais escuro para 1º
            case 2: return "#28a745";  // Verde escuro para 2º
            case 3: return "#34ce57";  // Verde médio para 3º
            case 4: return "#48d668";
            case 5: return "#5dd879";
            case 6: return "#71da89";
            case 7: return "#85dc99";
            case 8: return "#99dea9";
            case 9: return "#ade0b9";
            case 10: return "#c1e2c9"; // Verde mais claro para 10º
            default: return "#28a745";
        }
    }
    
    // Configura a cor e o tooltip de cada barra
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
    
    // Querys
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
        return getYearlyValue("SUM(v.Subtotal)", now.getYear());
    }
    
    private double getMonthlyValue(String aggregate, int year, int month) throws SQLException {
        String join = aggregate.contains("ci.") ? "JOIN carrinho_itens ci ON v.ID_Carrinho = ci.ID_Carrinho " : "";
        String sql = "SELECT " + aggregate + " as total FROM vendas v " + join +
                     "WHERE YEAR(v.Data_Venda) = ? AND MONTH(v.Data_Venda) = ? AND v.Status = 'Concluida'";
        
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
    
    private double getYearlyValue(String aggregate, int year) throws SQLException {
        String sql = "SELECT " + aggregate + " as total FROM vendas v WHERE YEAR(v.Data_Venda) = ? AND v.Status = 'Concluida'";
        
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }
    
    private double getMonthlyYoY(String aggregate, int currentYear, int currentMonth, int previousYear, int previousMonth) throws SQLException {
        double currentValue = getMonthlyValue(aggregate, currentYear, currentMonth);
        double previousValue = getMonthlyValue(aggregate, previousYear, previousMonth);
        
        if (previousValue == 0) return 0; // Evita divisão por zero
        return ((currentValue - previousValue) / previousValue) * 100;
    }
    
    private double getYearlyYoY(String aggregate, int currentYear, int previousYear) throws SQLException {
        double currentValue = getYearlyValue(aggregate, currentYear);
        double previousValue = getYearlyValue(aggregate, previousYear);
        
        if (previousValue == 0) return 0; // Evita divisão por zero
        return ((currentValue - previousValue) / previousValue) * 100;
    }
    
    private String getTopEmployee() throws SQLException {
        LocalDate current = LocalDate.now();
        String sql = "SELECT TOP 1 ls.Nome, COUNT(v.ID_Vendas) as TotalVendas " +
                    "FROM vendas v " +
                    "JOIN login_sistema ls ON v.ID_Login = ls.ID_Login " +
                    "WHERE YEAR(v.Data_Venda) = ? AND MONTH(v.Data_Venda) = ? " +
                    "AND v.Status = 'Concluida' " +
                    "GROUP BY ls.Nome " +
                    "ORDER BY TotalVendas DESC";
        
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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