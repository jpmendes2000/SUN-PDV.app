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

import java.security.PublicKey;
import java.sql.*;
import java.text.DecimalFormat;

public class Produtos {

    // Tabela e lista de produtos
    private TableView<Produto> table;
    private ObservableList<Produto> listaProdutos;

    // Método para conexão com o banco de dados
    private Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;encrypt=true;trustServerCertificate=true";
        String user = "sa";
        String password = "Senha@1234";
        return DriverManager.getConnection(url, user, password);
    }

    // Método principal para mostrar a tela
    public void show(Stage stage) {

        // Carregando a logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(130);
        logoView.setPreserveRatio(true);

        // Carregando a imagem do título (produto.png)
        Image tituloImagem = new Image(getClass().getResourceAsStream("/img/produto.png"));
        ImageView tituloView = new ImageView(tituloImagem);
        tituloView.setPreserveRatio(true);
        tituloView.setFitHeight(200); // Ajuste conforme desejado

        // Colocando logo e título lado a lado no topo
        HBox topoBox = new HBox(20, logoView, tituloView); // 20 é espaçamento entre as imagens
        topoBox.setAlignment(Pos.CENTER_LEFT);
        topoBox.setPadding(new Insets(10, 20, 10, 20));


        // Criando botões
        Button btnAdd = new Button("Adicionar");
        Button btnEditar = new Button("Editar Produto");
        Button btnApagar = new Button("Apagar Produto");
        Button btnVoltar = new Button("Home");
        Button btnSair = new Button("Sair do Sistema");

        // Ajuste de largura dos botões
        double larguraPadrao = 200;
        btnAdd.setPrefWidth(larguraPadrao);
        btnEditar.setPrefWidth(larguraPadrao);
        btnApagar.setPrefWidth(larguraPadrao);
        btnVoltar.setPrefWidth(larguraPadrao);
        btnSair.setPrefWidth(larguraPadrao);

        // Configuração da tabela
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPrefSize(875, 800);
        table.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        table.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // Colunas da tabela
        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(420);

        TableColumn<Produto, String> colCodBarras = new TableColumn<>("Código de Barras");
        colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras"));
        colCodBarras.setPrefWidth(250);

        //coluna de preços com formatação
        TableColumn<Produto, String> colPreco = new TableColumn<>("Preço (R$)");
        colPreco.setCellValueFactory(cellData -> {
            Double preco = cellData.getValue().getPreco();
            DecimalFormat df = new DecimalFormat("#,##0.00");
            return new javafx.beans.property.SimpleStringProperty(df.format(preco));
        });
        colPreco.setPrefWidth(183);

        table.getColumns().addAll(colNome, colCodBarras, colPreco);

        // Carrega os dados na tabela
        carregarProdutos();

        // Ações dos botões
        btnAdd.setOnAction(e -> abrirFormularioProduto(null));
        btnEditar.setOnAction(e -> {
            Produto selecionado = table.getSelectionModel().getSelectedItem();
                        if (selecionado != null) {
                abrirFormularioProduto(selecionado);
            } else {
                alerta("Selecione um produto para editar.");
            }
        });
        btnApagar.setOnAction(e -> {
            Produto selecionado = table.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                apagarProduto(selecionado);
            } else {
                alerta("Selecione um produto para apagar.");
            }
        });

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

        // Layout dos botões
        VBox botoes = new VBox(10, btnAdd, btnEditar, btnApagar, btnVoltar, btnSair);
        botoes.setPadding(new Insets(10));
        botoes.setAlignment(Pos.BOTTOM_LEFT);

        // Container da tabela, alinhada no canto superior esquerdo
        VBox tabelaContainer = new VBox(table);
        tabelaContainer.setAlignment(Pos.TOP_LEFT);
        tabelaContainer.setPadding(new Insets(35)); // menos padding para não criar muito espaço

        // Layout principal
        BorderPane layout = new BorderPane();

        // Topo com logo e imagem título
        layout.setTop(topoBox);

        // Tabela no centro e botões à direita
        layout.setCenter(tabelaContainer);
        layout.setRight(botoes);

        // Ajuste de margem para os botões
        BorderPane.setMargin(botoes, new Insets(20));

        // Estilo CSS
        layout.getStyleClass().add("produtos");

        // Cena e configurações do stage
        Scene scene = new Scene(layout, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Produtos");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }

    // Método para carregar produtos do banco na tabela
    public void carregarProdutos() {
        listaProdutos = FXCollections.observableArrayList();
        String sql = "SELECT ID_Produto, Nome, Cod_Barras, Preco FROM produtos ORDER BY Nome";
        System.out.println("Executando consulta: " + sql);
        
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
                System.out.println("Produto encontrado: " + p.getNome());
                listaProdutos.add(p);
            }
            System.out.println("Total de produtos carregados: " + count);
            table.setItems(listaProdutos);
            table.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            alerta("Erro ao carregar produtos do banco: " + e.getMessage());
        }
    }

    // Abre o formulário para adicionar ou editar produto
    private void abrirFormularioProduto(Produto produto) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(produto == null ? "Adicionar Produto" : "Editar Produto");

        // Campos do formulário
        Label lblNome = new Label("Nome:");
        TextField tfNome = new TextField();
        tfNome.getStyleClass().add("text-fill-tbl");

        Label lblCodBarras = new Label("Código de Barras:");
        TextField tfCodBarras = new TextField();
        Label lblPreco = new Label("Preço (R$):");
        TextField tfPreco = new TextField();

        // Se for edição, preencher os campos
        if (produto != null) {
            tfNome.setText(produto.getNome());
            tfCodBarras.setText(produto.getCodBarras());
            DecimalFormat df = new DecimalFormat("#,##0.00");
            tfPreco.setText(df.format(produto.getPreco()));

        }

        // Botões do formulário
        Button btnSalvar = new Button("Salvar");
        Button btnCancelar = new Button("Cancelar");

        // Ação do botão salvar
        btnSalvar.setOnAction(e -> {
            String nome = tfNome.getText().trim();
            String codBarras = tfCodBarras.getText().trim();
            String precoStr = tfPreco.getText().trim();

            // Validação
            if (nome.isEmpty() || codBarras.isEmpty() || precoStr.isEmpty()) {
                alerta("Preencha todos os campos!");
                return;
            }

            double preco;
            try {
            preco = Double.parseDouble(precoStr.replace(",", "."));
            } catch (NumberFormatException ex) {
                alerta("Preço inválido!");
                return;
            }

            if (produto == null) {
                if (inserirProduto(new Produto(0, nome, codBarras, preco))) {
                    carregarProdutos();
                    dialog.close();
                }
            } else {
                produto.setNome(nome);
                produto.setCodBarras(codBarras);
                produto.setPreco(preco);
                if (atualizarProduto(produto)) {
                    carregarProdutos();
                    dialog.close();
                }
            }
        });

        // Ação do botão cancelar
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

        Scene scene = new Scene(grid);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // Insere novo produto no banco
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

    // Atualiza produto no banco
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

    // Apaga produto
    private void apagarProduto(Produto p) {
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

    // Método utilitário para mostrar alertas
    private void alerta(String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
