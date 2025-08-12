package com.sunpdv.telas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

public class Caixa {

    private Stage stage;
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "Senha@12345!";
    
    private List<Venda> vendas;
    private VBox listaVendas;
    private TextField pesquisaField;
    private ComboBox<String> filtroStatus;

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

    private static class Venda {
        int id;
        String cliente;
        String data;
        double total;
        String status;
        List<ItemVenda> itens;

        public Venda(int id, String cliente, String data, double total, String status) {
            this.id = id;
            this.cliente = cliente;
            this.data = data;
            this.total = total;
            this.status = status;
            this.itens = new ArrayList<>();
        }

        public String getStatusColor() {
            switch (status) {
                case "Finalizada": return "green";
                case "Cancelada": return "red";
                case "Pendente": return "orange";
                default: return "gray";
            }
        }
    }

    private static class ItemVenda {
        String produto;
        int quantidade;
        double preco;

        public ItemVenda(String produto, int quantidade, double preco) {
            this.produto = produto;
            this.quantidade = quantidade;
            this.preco = preco;
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
            indicatorContainer.setVisible(false);
            indicatorContainer.setManaged(false);

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
                indicatorContainer.setVisible(true);
                indicatorContainer.setManaged(true);
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-color: transparent;");
                indicatorContainer.setStyle("-fx-background-color: transparent;");
                indicatorContainer.setVisible(false);
                indicatorContainer.setManaged(false);
            });

            return btn;
        } catch (Exception e) {
            return new Button(texto);
        }
    }

    private List<Venda> carregarVendas() {
        List<Venda> vendas = new ArrayList<>();
        String query = "SELECT v.ID_Venda, ISNULL(c.Nome, 'Consumidor') AS Cliente, " +
                      "FORMAT(v.DataHora, 'dd/MM/yyyy HH:mm') AS Data, v.Total, v.Status " +
                      "FROM Venda v LEFT JOIN Cliente c ON v.ID_Cliente = c.ID_Cliente " +
                      "ORDER BY v.DataHora DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Venda venda = new Venda(
                    rs.getInt("ID_Venda"),
                    rs.getString("Cliente"),
                    rs.getString("Data"),
                    rs.getDouble("Total"),
                    rs.getString("Status")
                );
                
                // Carrega os itens da venda
                carregarItensVenda(venda);
                vendas.add(venda);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erro ao carregar vendas", "Detalhes: " + e.getMessage());
        }
        return vendas;
    }

    private void carregarItensVenda(Venda venda) throws SQLException {
        String query = "SELECT p.Nome AS Produto, iv.Quantidade, iv.PrecoUnitario " +
                       "FROM ItemVenda iv JOIN Produto p ON iv.ID_Produto = p.ID_Produto " +
                       "WHERE iv.ID_Venda = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, venda.id);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                venda.itens.add(new ItemVenda(
                    rs.getString("Produto"),
                    rs.getInt("Quantidade"),
                    rs.getDouble("PrecoUnitario")
                ));
            }
        }
    }

    private void aplicarFiltros() {
        String textoBusca = pesquisaField.getText().toLowerCase().trim();
        String statusSelecionado = filtroStatus.getValue();

        listaVendas.getChildren().clear();
        boolean achou = false;
        
        for (Venda venda : vendas) {
            boolean clienteMatch = venda.cliente.toLowerCase().contains(textoBusca);
            boolean statusMatch = statusSelecionado.equals("Todos") || venda.status.equalsIgnoreCase(statusSelecionado);

            if (clienteMatch && statusMatch) {
                listaVendas.getChildren().add(criarPainelVenda(venda));
                achou = true;
            }
        }
        
        if (!achou) {
            listaVendas.getChildren().add(new Label("Nenhuma venda corresponde à pesquisa."));
        }
    }

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showNovaVendaDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Nova Venda");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setStyle("-fx-background-color: #006989;");
        dialogVBox.setAlignment(Pos.CENTER);

        // Campo para cliente
        TextField clienteField = new TextField();
        clienteField.setPromptText("Nome do cliente (opcional)");
        clienteField.setMaxWidth(400);

        // Lista de produtos
        ListView<String> listaProdutos = new ListView<>();
        listaProdutos.setPrefHeight(200);

        // Campo para adicionar produto
        TextField codigoProdutoField = new TextField();
        codigoProdutoField.setPromptText("Código ou nome do produto");
        codigoProdutoField.setMaxWidth(400);

        Button btnAdicionar = new Button("Adicionar Produto");
        btnAdicionar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnAdicionar.setOnAction(e -> {
            String codigo = codigoProdutoField.getText().trim();
            if (!codigo.isEmpty()) {
                // Aqui você implementaria a busca do produto no banco
                listaProdutos.getItems().add("Produto: " + codigo + " - R$ 10,00");
                codigoProdutoField.clear();
            }
        });

        // Total da venda
        Label totalLabel = new Label("Total: R$ 0,00");
        totalLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Botões
        Button btnFinalizar = new Button("Finalizar Venda");
        btnFinalizar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnFinalizar.setOnAction(e -> {
            // Implementar lógica para finalizar venda no banco
            dialog.close();
            vendas = carregarVendas();
            aplicarFiltros();
        });

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnCancelar.setOnAction(e -> dialog.close());

        HBox botoes = new HBox(10, btnFinalizar, btnCancelar);
        botoes.setAlignment(Pos.CENTER);

        dialogVBox.getChildren().addAll(
            new Label("Cliente:"), clienteField,
            new Label("Produtos:"), listaProdutos,
            new Label("Adicionar Produto:"), codigoProdutoField, btnAdicionar,
            totalLabel, botoes
        );

        Scene dialogScene = new Scene(dialogVBox, 500, 500);
        dialogScene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private VBox criarPainelVenda(Venda venda) {
        VBox painel = new VBox(5);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d3d3d3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label idLabel = new Label("Venda #" + venda.id);
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00536d;");

        Label clienteLabel = new Label("Cliente: " + venda.cliente);
        clienteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        Label dataLabel = new Label("Data: " + venda.data);
        dataLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        Label totalLabel = new Label("Total: R$ " + String.format("%.2f", venda.total));
        totalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00536d;");

        Label statusLabel = new Label("Status: " + venda.status);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + venda.getStatusColor() + ";");

        // Botão para ver detalhes
        Button btnDetalhes = new Button("Detalhes");
        btnDetalhes.setStyle("-fx-background-color: #0c5b74; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");
        btnDetalhes.setOnAction(e -> showDetalhesVenda(venda));

        // Botão para cancelar venda (se não estiver cancelada)
        Button btnCancelar = null;
        if (!venda.status.equals("Cancelada")) {
            btnCancelar = new Button("Cancelar");
            btnCancelar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 4;");
            btnCancelar.setOnAction(e -> cancelarVenda(venda));
        }

        HBox botoes = new HBox(10, btnDetalhes);
        if (btnCancelar != null) {
            botoes.getChildren().add(btnCancelar);
        }
        botoes.setAlignment(Pos.CENTER_RIGHT);

        painel.getChildren().addAll(idLabel, clienteLabel, dataLabel, totalLabel, statusLabel, botoes);

        return painel;
    }

    private void showDetalhesVenda(Venda venda) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Detalhes da Venda #" + venda.id);

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setStyle("-fx-background-color: #006989;");
        dialogVBox.setAlignment(Pos.CENTER);

        Label idLabel = new Label("Venda #" + venda.id);
        idLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label clienteLabel = new Label("Cliente: " + venda.cliente);
        clienteLabel.setStyle("-fx-text-fill: white;");

        Label dataLabel = new Label("Data: " + venda.data);
        dataLabel.setStyle("-fx-text-fill: white;");

        Label statusLabel = new Label("Status: " + venda.status);
        statusLabel.setStyle("-fx-text-fill: " + venda.getStatusColor() + ";");

        // Tabela de itens
        TableView<ItemVenda> tabelaItens = new TableView<>();
        
        TableColumn<ItemVenda, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().produto));
        
        TableColumn<ItemVenda, Integer> colQuantidade = new TableColumn<>("Quantidade");
        colQuantidade.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().quantidade).asObject());
        
        TableColumn<ItemVenda, Double> colPreco = new TableColumn<>("Preço Unitário");
        colPreco.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().preco).asObject());
        
        TableColumn<ItemVenda, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().quantidade * cellData.getValue().preco).asObject());
        
        tabelaItens.getColumns().addAll(colProduto, colQuantidade, colPreco, colTotal);
        tabelaItens.getItems().addAll(venda.itens);
        tabelaItens.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label totalLabel = new Label("Total da Venda: R$ " + String.format("%.2f", venda.total));
        totalLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle("-fx-background-color: #0c5b74; -fx-text-fill: white;");
        btnFechar.setOnAction(e -> dialog.close());

        dialogVBox.getChildren().addAll(
            idLabel, clienteLabel, dataLabel, statusLabel,
            new Label("Itens da Venda:"), tabelaItens, totalLabel, btnFechar
        );

        Scene dialogScene = new Scene(dialogVBox, 600, 500);
        dialogScene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void cancelarVenda(Venda venda) {
        CustomConfirmationAlert alert = new CustomConfirmationAlert(
            stage,
            "Confirmar Cancelamento",
            "Deseja realmente cancelar a venda #" + venda.id + "?",
            "Esta ação não pode ser desfeita."
        );
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String query = "UPDATE Venda SET Status = 'Cancelada' WHERE ID_Venda = ?";
                
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    
                    stmt.setInt(1, venda.id);
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        vendas = carregarVendas();
                        aplicarFiltros();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Erro ao cancelar venda", "Detalhes: " + e.getMessage());
                }
            }
        });
    }

    public void show(Stage stage) {
        this.stage = stage;
        vendas = carregarVendas();

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

        Label titulonaABA = new Label("Caixa");
        titulonaABA.setStyle("-fx-text-fill: #a9cce3; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0));

        Button btnVoltarHome = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        VBox buttonBox = new VBox(10, btnVoltarHome, btnSair);
        buttonBox.setAlignment(Pos.TOP_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        leftMenu.getChildren().addAll(logoBox, new Region(), buttonBox);
        VBox.setVgrow(leftMenu.getChildren().get(1), Priority.ALWAYS);

        Label titulo = new Label("Módulo de Caixa");
        titulo.setStyle("-fx-text-fill: #062e3aff; -fx-font-size: 24px; -fx-font-weight: bold;");

        pesquisaField = new TextField();
        pesquisaField.setPromptText("Pesquisar cliente...");
        pesquisaField.setMaxWidth(300);

        filtroStatus = new ComboBox<>();
        filtroStatus.getItems().addAll("Todos", "Finalizada", "Cancelada", "Pendente");
        filtroStatus.setValue("Todos");
        filtroStatus.setPrefWidth(150);

        Label statusLabel = new Label("Status:");
        pesquisaField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroStatus.setOnAction(e -> aplicarFiltros());
        HBox.setMargin(pesquisaField, new Insets(5, 10, 0, 10));
        pesquisaField.setPrefWidth(1600);

        HBox filtroBox = new HBox(5, pesquisaField, statusLabel, filtroStatus);
        filtroBox.setPadding(new Insets(5));
        HBox.setMargin(filtroStatus, new Insets(0, 10, 0, 0));
        HBox.setMargin(statusLabel, new Insets(0, 5, 0, 5));

        listaVendas = new VBox(10);
        listaVendas.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(listaVendas);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f4f4f4;");

        Button btnNovaVenda = new Button("Nova Venda");
        btnNovaVenda.setStyle(
            "-fx-background-color: #e8ba23; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px; " +
            "-fx-background-radius: 6px; -fx-padding: 10 60 10 60;"
        );
        btnNovaVenda.setOnAction(e -> showNovaVendaDialog());

        VBox centerBox = new VBox(10, titulo, filtroBox, scroll);
        centerBox.setPadding(new Insets(10));

        AnchorPane containerCentral = new AnchorPane(centerBox, btnNovaVenda);
        AnchorPane.setTopAnchor(centerBox, 0.0);
        AnchorPane.setBottomAnchor(centerBox, 0.0);
        AnchorPane.setLeftAnchor(centerBox, 0.0);
        AnchorPane.setRightAnchor(centerBox, 0.0);

        AnchorPane.setBottomAnchor(btnNovaVenda, 20.0);
        AnchorPane.setRightAnchor(btnNovaVenda, 20.0);

        BorderPane root = new BorderPane();
        root.setLeft(leftMenu);
        root.setCenter(containerCentral);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Caixa");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();

        btnVoltarHome.setOnAction(e -> {
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

        aplicarFiltros();
    }
}