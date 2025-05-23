package com.sunpdv.telas;

// Importações necessárias
import com.sunpdv.AutenticarUser;
import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;
import com.sunpdv.model.Produto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;

public class Produtos {

    // Componentes da interface
    private TableView<Produto> table;  // Tabela para exibir os produtos
    private ObservableList<Produto> listaProdutos;  // Lista observável de produtos

    // Método para estabelecer conexão com o banco de dados
    private Connection getConnection() throws SQLException {
        // Configuração da conexão JDBC
        String url = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;encrypt=true;trustServerCertificate=true";
        String user = "sa";    
        String password = "Jp081007!";    
        return DriverManager.getConnection(url, user, password);
    }

    // Método principal que exibe a tela de produtos
    public void show(Stage stage) {
        // Configuração do logo da aplicação
        Image logo = new Image(getClass().getResourceAsStream("/img/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(130);
        logoView.setPreserveRatio(true);
        
        // Container para o logo
        VBox logoBox = new VBox(logoView);
        logoBox.setPadding(new Insets(20));
        logoBox.setAlignment(Pos.BOTTOM_LEFT);

        // Criação e configuração dos botões da interface
        Button btnAdd = new Button("Adicionar");
        Button btnEditar = new Button("Editar Produto");
        Button btnApagar = new Button("Apagar Produto");
        Button btnVoltar = new Button("Home");
        Button btnSair = new Button("Sair do Sistema");

        // Configuração de largura padrão para os botões
        double larguraPadrao = 200;
        btnAdd.setPrefWidth(larguraPadrao);
        btnEditar.setPrefWidth(larguraPadrao);
        btnApagar.setPrefWidth(larguraPadrao);
        btnVoltar.setPrefWidth(larguraPadrao);
        btnSair.setPrefWidth(larguraPadrao);

        /* 
         * CONFIGURAÇÃO DA TABLEVIEW (TABELA DE PRODUTOS)
         * 
         * - Define o estilo básico da tabela
         * - Configura política de redimensionamento das colunas
         * - Define tamanhos preferenciais, mínimos e máximos
         */
        // Configuração da TableView com bordas arredondadas
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefSize(1600, 500);
        table.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        table.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Aplica estilo CSS para bordas arredondadas
        table.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d3d3d3;");

        // Configuração das colunas da tabela
        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(400);

        TableColumn<Produto, String> colCodBarras = new TableColumn<>("Código de Barras");
        colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras"));
        colCodBarras.setPrefWidth(250);

        TableColumn<Produto, Double> colPreco = new TableColumn<>("Preço (R$)");
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colPreco.setPrefWidth(150);

        table.getColumns().addAll(colNome, colCodBarras, colPreco);

        // 1. Reduzir o padding do BorderPane principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(2));  // Reduzido drasticamente

        // 2. Criar um container VBox sem espaçamento
        VBox tableContainer = new VBox();
        tableContainer.setSpacing(0);
        tableContainer.setPadding(new Insets(0));
        tableContainer.getChildren().add(table);

        // 3. Configurar ScrollPane sem margens
        ScrollPane scrollPane = new ScrollPane(tableContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(0));  // Sem padding interno
        scrollPane.setStyle("-fx-background: transparent;");

        // 4. Aplicar ao BorderPane com margens mínimas
        root.setCenter(scrollPane);
        BorderPane.setMargin(scrollPane, new Insets(20, 5, 5, 5));  // Margem superior mínima (2px)

        // Carrega os produtos do banco de dados
        carregarProdutos();

        // Configuração dos eventos dos botões:

        // Botão Adicionar - Abre formulário para novo produto
        btnAdd.setOnAction(e -> abrirFormularioProduto(null));

        // Botão Editar - Abre formulário com produto selecionado
        btnEditar.setOnAction(e -> {
            Produto selecionado = table.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                abrirFormularioProduto(selecionado);
            } else {
                alerta("Selecione um produto para editar.");
            }
        });

        // Botão Apagar - Remove produto selecionado
        btnApagar.setOnAction(e -> {
            Produto selecionado = table.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                apagarProduto(selecionado);
            } else {
                alerta("Selecione um produto para apagar.");
            }
        });

        // Botão Voltar - Retorna à tela inicial conforme o cargo do usuário
        btnVoltar.setOnAction(e -> {
            try {
                String cargo = AutenticarUser.getCargo();
                switch (cargo) {
                    case "Administrador":
                        new TelaHomeADM(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                        break;
                    case "Moderador":
                        new TelaHomeMOD(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                        break;
                    case "Funcionário":
                        new TelaHomeFUN(AutenticarUser.getNome(), AutenticarUser.getCargo()).mostrar(stage);
                        break;
                    default:
                        System.out.println("Cargo não reconhecido: " + cargo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                alerta("Erro ao retornar para a tela principal.");
            }
        });

        // Botão Sair - Fecha a aplicação com confirmação
        btnSair.setOnAction(e -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Saída");
            alert.setHeaderText("Deseja realmente sair do sistema?");
            alert.initOwner(stage);
            alert.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) {
                    stage.close();
                }
            });
        });

        // Container para os botões (lado direito da tela)
        VBox botoes = new VBox(10, btnAdd, btnEditar, btnApagar, btnVoltar, btnSair);
        botoes.setPadding(new Insets(10));
        botoes.setAlignment(Pos.BOTTOM_LEFT);

        // Layout final da interface
        BorderPane layout = new BorderPane();
        layout.setTop(logoBox);
        layout.setCenter(table);
        layout.setRight(botoes);
        BorderPane.setMargin(table, new Insets(10));
        BorderPane.setMargin(botoes, new Insets(20));
        layout.getStyleClass().add("produtos");

        // Configuração da cena principal
        Scene scene = new Scene(layout, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Configuração da janela principal
        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Produtos");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }

    // Método para carregar produtos do banco de dados
    private void carregarProdutos() {
        listaProdutos = FXCollections.observableArrayList();
        String sql = "SELECT ID_Produto, Nome, Cod_Barras, Preco FROM produtos ORDER BY Nome";
        System.out.println("Executando consulta: " + sql); // Log da consulta
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                count++;
                Produto p = new Produto(
                        rs.getInt("ID_Produto"),
                        rs.getString("Nome"),
                        rs.getString("Cod_Barras"),
                        rs.getDouble("Preco")
                );
                System.out.println("Produto encontrado: " + p.getNome()); // Log dos produtos
                listaProdutos.add(p);
            }
            System.out.println("Total de produtos carregados: " + count); // Log do total
            table.setItems(listaProdutos);
            table.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            alerta("Erro ao carregar produtos do banco: " + e.getMessage());
        }
    }

    // Método para abrir formulário de edição/cadastro de produto
    private void abrirFormularioProduto(Produto produto) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(produto == null ? "Adicionar Produto" : "Editar Produto");

        // Componentes do formulário
        Label lblNome = new Label("Nome:");
        TextField tfNome = new TextField();
        tfNome.getStyleClass().add("text-fill-tbl");

        Label lblCodBarras = new Label("Código de Barras:");
        TextField tfCodBarras = new TextField();
        Label lblPreco = new Label("Preço (R$):");
        TextField tfPreco = new TextField();

        // Preenche campos se estiver editando
        if (produto != null) {
            tfNome.setText(produto.getNome());
            tfCodBarras.setText(produto.getCodBarras());
            tfPreco.setText(String.valueOf(produto.getPreco()));
        }

        // Botões do formulário
        Button btnSalvar = new Button("Salvar");
        Button btnCancelar = new Button("Cancelar");

        // Evento do botão Salvar
        btnSalvar.setOnAction(e -> {
            String nome = tfNome.getText().trim();
            String codBarras = tfCodBarras.getText().trim();
            String precoStr = tfPreco.getText().trim();

            // Validação dos campos
            if (nome.isEmpty() || codBarras.isEmpty() || precoStr.isEmpty()) {
                alerta("Preencha todos os campos!");
                return;
            }

            // Conversão do preço
            double preco;
            try {
                preco = Double.parseDouble(precoStr);
            } catch (NumberFormatException ex) {
                alerta("Preço inválido!");
                return;
            }

            // Lógica para inserção ou atualização
            if (produto == null) {
                // Inserir novo produto
                if (inserirProduto(new Produto(0, nome, codBarras, preco))) {
                    carregarProdutos();
                    dialog.close();
                }
            } else {
                // Atualizar produto existente
                produto.setNome(nome);
                produto.setCodBarras(codBarras);
                produto.setPreco(preco);
                if (atualizarProduto(produto)) {
                    carregarProdutos();
                    dialog.close();
                }
            }
        });

        // Evento do botão Cancelar
        btnCancelar.setOnAction(e -> dialog.close());

        // Layout do formulário
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(15));
        grid.add(lblNome, 0, 0);
        grid.add(tfNome, 1, 0);
        grid.add(lblCodBarras, 0, 1);
        grid.add(tfCodBarras, 1, 1);
        grid.add(lblPreco, 0, 2);
        grid.add(tfPreco, 1, 2);
        HBox botoes = new HBox(10, btnSalvar, btnCancelar);
        grid.add(botoes, 1, 3);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        // Configuração da cena do diálogo
        Scene scene = new Scene(grid);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // Método para inserir novo produto no banco de dados
    private boolean inserirProduto(Produto p) {
        String sql = "INSERT INTO produtos (Nome, Cod_Barras, Preco) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getCodBarras());
            ps.setDouble(3, p.getPreco());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            alerta("Erro ao inserir produto: " + e.getMessage());
            return false;
        }
    }

    // Método para atualizar produto existente no banco de dados
    private boolean atualizarProduto(Produto p) {
        String sql = "UPDATE produtos SET Nome = ?, Cod_Barras = ?, Preco = ? WHERE ID_Produto = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getCodBarras());
            ps.setDouble(3, p.getPreco());
            ps.setInt(4, p.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            alerta("Erro ao atualizar produto: " + e.getMessage());
            return false;
        }
    } 

    // Método para apagar produto do banco de dados
    private void apagarProduto(Produto p) {
        // Diálogo de confirmação
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText("Deseja realmente apagar o produto '" + p.getNome() + "'?");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                String sql = "DELETE FROM produtos WHERE ID_Produto = ?";
                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, p.getId());
                    ps.executeUpdate();
                    carregarProdutos();
                } catch (SQLException e) {
                    e.printStackTrace();
                    alerta("Erro ao apagar produto: " + e.getMessage());
                }
            }
        });
    }

    // Método auxiliar para exibir mensagens de alerta
    private void alerta(String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}