package com.sunpdv.telas;

import com.sunpdv.AutenticarUser;
import com.sunpdv.home.TelaHomeADM;
import com.sunpdv.home.TelaHomeFUN;
import com.sunpdv.home.TelaHomeMOD;
import com.sunpdv.model.Produto;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.util.Duration;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.Optional;

public class Produtos {

    private TableView<Produto> table;
    private ObservableList<Produto> listaProdutos;
    private TextField campoPesquisa;
    private Label lblMensagemSucesso;
    private Produto produtoSelecionado;

    private Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;encrypt=true;trustServerCertificate=true";
        String user = "sa";
        String password = "Senha@1234";
        return DriverManager.getConnection(url, user, password);
    }

    public void show(Stage stage) {
        // --- Configuração do Layout Principal ---
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(20);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(15));
        
        // Configuração das colunas (60% para tabela, 40% para espaço vazio/botões)
        ColumnConstraints colEsquerda = new ColumnConstraints();
        colEsquerda.setPercentWidth(60);
        ColumnConstraints colDireita = new ColumnConstraints();
        colDireita.setPercentWidth(40);
        mainGrid.getColumnConstraints().addAll(colEsquerda, colDireita);

        // Configuração das linhas
        RowConstraints rowTopo = new RowConstraints();
        rowTopo.setPrefHeight(190);
        RowConstraints rowConteudo = new RowConstraints();
        rowConteudo.setVgrow(Priority.ALWAYS);
        mainGrid.getRowConstraints().addAll(rowTopo, rowConteudo);

        // --- Topo (ocupa as duas colunas) ---
        // Imagem logo
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(130);
        logoView.setPreserveRatio(true);

        // Imagem título
        Image tituloImagem = new Image(getClass().getResourceAsStream("/img/logo/produto.png"));
        ImageView tituloView = new ImageView(tituloImagem);
        tituloView.setPreserveRatio(true);
        tituloView.setFitHeight(120);

        // Label mensagem sucesso
        lblMensagemSucesso = new Label();
        lblMensagemSucesso.getStyleClass().add("mensagem-sucesso");
        lblMensagemSucesso.setVisible(false);

        // Campo pesquisa
        campoPesquisa = new TextField();
        campoPesquisa.setPromptText("Pesquisar produto...");
        campoPesquisa.setPrefWidth(400);
        campoPesquisa.textProperty().addListener((obs, oldVal, newVal) -> filtrarProdutos(newVal));

        // Botão adicionar
        Image imgAdd = new Image(getClass().getResourceAsStream("/img/icon/lista.png"));
        ImageView iconAdd = new ImageView(imgAdd);
        iconAdd.setFitWidth(20);
        iconAdd.setFitHeight(20);
        Button btnAdd = new Button("", iconAdd);
        btnAdd.setTooltip(new Tooltip("Adicionar Produto"));
        btnAdd.setPrefSize(40, 40);

        // Botão editar
        Image imgEdit = new Image(getClass().getResourceAsStream("/img/icon/lapis.png"));
        ImageView iconEdit = new ImageView(imgEdit);
        iconEdit.setFitWidth(20);
        iconEdit.setFitHeight(20);
        Button btnEdit = new Button("", iconEdit);
        btnEdit.setTooltip(new Tooltip("Editar Produto"));
        btnEdit.setPrefSize(40, 40);
        btnEdit.setDisable(true);

        // Botão apagar
        Image imgDelete = new Image(getClass().getResourceAsStream("/img/icon/fechar.png"));
        ImageView iconDelete = new ImageView(imgDelete);
        iconDelete.setFitWidth(20);
        iconDelete.setFitHeight(20);
        Button btnDelete = new Button("", iconDelete);
        btnDelete.setTooltip(new Tooltip("Apagar Produto"));
        btnDelete.setPrefSize(40, 40);
        btnDelete.setDisable(true);

        // Organização do topo
        HBox tituloMensagemBox = new HBox(10, tituloView, lblMensagemSucesso);
        tituloMensagemBox.setAlignment(Pos.CENTER_LEFT);

        HBox logoTituloBox = new HBox(20, logoView, tituloMensagemBox);
        logoTituloBox.setAlignment(Pos.CENTER_LEFT);
        logoTituloBox.setPadding(new Insets(10, 10, 5, 10));

        HBox pesquisaAcoesBox = new HBox(10, campoPesquisa, btnAdd, btnEdit, btnDelete);
        pesquisaAcoesBox.setAlignment(Pos.CENTER_LEFT);
        pesquisaAcoesBox.setPadding(new Insets(5, 0, 15, 10));

        VBox topoBox = new VBox(5, logoTituloBox, pesquisaAcoesBox);
        mainGrid.add(topoBox, 0, 0, 2, 1);

        // --- Tabela (coluna esquerda) ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Colunas
        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(400);

        TableColumn<Produto, String> colCodBarras = new TableColumn<>("Código de Barras");
        colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras"));
        colCodBarras.setPrefWidth(100);

        TableColumn<Produto, String> colPreco = new TableColumn<>("Preço (R$)");
        colPreco.setCellValueFactory(cell -> {
            Double preco = cell.getValue().getPreco();
            DecimalFormat df = new DecimalFormat("R$ #,##0.00");
            return new SimpleStringProperty(df.format(preco));
        });
        colPreco.setPrefWidth(50);

        table.getColumns().addAll(colNome, colCodBarras, colPreco);
        
        // Adiciona a tabela em um ScrollPane
        ScrollPane scrollTable = new ScrollPane(table);
        scrollTable.setFitToWidth(true);
        scrollTable.setFitToHeight(true);
        mainGrid.add(scrollTable, 0, 1);

        // --- Botões Home e Sair (coluna direita, inferior) ---
        ImageView iconHome = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/casa.png")));
        iconHome.setFitWidth(32);
        iconHome.setFitHeight(32);

        ImageView iconSair = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/fechar.png")));
        iconSair.setFitWidth(32);
        iconSair.setFitHeight(32);

        Button btnVoltar = new Button("Home", iconHome);
        btnVoltar.setPrefWidth(150);
        
        Button btnSair = new Button("Sair do Sistema", iconSair);
        btnSair.setPrefWidth(150);

        VBox rightButtonsBox = new VBox(15, btnVoltar, btnSair);
        rightButtonsBox.setAlignment(Pos.BOTTOM_RIGHT);
        rightButtonsBox.setPadding(new Insets(0, 20, 20, 0));
        mainGrid.add(rightButtonsBox, 1, 1);

        // --- Carregar dados e configurar eventos ---
        carregarProdutos();

        // Listener para seleção na tabela
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                produtoSelecionado = newSelection;
                btnEdit.setDisable(false);
                btnDelete.setDisable(false);
            } else {
                produtoSelecionado = null;
                btnEdit.setDisable(true);
                btnDelete.setDisable(true);
            }
        });

        // Eventos dos botões
        btnAdd.setOnAction(e -> abrirFormularioProduto(null));
        
        btnEdit.setOnAction(e -> {
            if (produtoSelecionado != null) {
                abrirFormularioProduto(produtoSelecionado);
            }
        });
        
        btnDelete.setOnAction(e -> {
            if (produtoSelecionado != null) {
                apagarProduto(produtoSelecionado);
            }
        });

        btnVoltar.setOnAction(e -> voltarParaHome(stage));
        btnSair.setOnAction(e -> confirmarSaida(stage));

        // --- Cena e Stage ---
        Scene scene = new Scene(mainGrid, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SUN PDV - Módulo de Produtos");
        stage.setFullScreen(false);
        stage.setResizable(true);
        stage.show();
    }

    private void carregarProdutos() {
        listaProdutos = FXCollections.observableArrayList();
        String sql = "SELECT ID_Produto, Nome, Cod_Barras, Preco FROM produtos ORDER BY Nome";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Produto p = new Produto(
                        rs.getInt("ID_Produto"),
                        rs.getString("Nome"),
                        rs.getString("Cod_Barras"),
                        rs.getDouble("Preco")
                );
                listaProdutos.add(p);
            }
            table.setItems(listaProdutos);
        } catch (SQLException e) {
            e.printStackTrace();
            alerta("Erro ao carregar produtos do banco: " + e.getMessage());
        }
    }

    private void filtrarProdutos(String filtro) {
        if (listaProdutos == null || filtro == null) return;

        ObservableList<Produto> filtrados = FXCollections.observableArrayList();
        String filtroLower = filtro.toLowerCase();

        for (Produto p : listaProdutos) {
            if (p.getNome().toLowerCase().contains(filtroLower)
                    || p.getCodBarras().toLowerCase().contains(filtroLower)) {
                filtrados.add(p);
            }
        }
        table.setItems(filtrados);
    }

    private void abrirFormularioProduto(Produto produto) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(produto == null ? "Adicionar Produto" : "Editar Produto");

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome do Produto");

        TextField txtCodBarras = new TextField();
        txtCodBarras.setPromptText("Código de Barras");

        TextField txtPreco = new TextField();
        txtPreco.setPromptText("Preço");

        if (produto != null) {
            txtNome.setText(produto.getNome());
            txtCodBarras.setText(produto.getCodBarras());
            txtPreco.setText(String.valueOf(produto.getPreco()));
        }

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setDefaultButton(true);

        btnSalvar.setOnAction(e -> {
            String nome = txtNome.getText().trim();
            String codBarras = txtCodBarras.getText().trim();
            String precoStr = txtPreco.getText().trim();

            if (nome.isEmpty() || codBarras.isEmpty() || precoStr.isEmpty()) {
                alerta("Preencha todos os campos.");
                return;
            }

            double preco;
            try {
                preco = Double.parseDouble(precoStr.replace(",", "."));
            } catch (NumberFormatException ex) {
                alerta("Preço inválido. Use números com ponto ou vírgula decimal.");
                return;
            }

            // Validar se produto já existe
            int idProduto = produto != null ? produto.getId() : 0;
            if (validarProdutoExistente(nome, codBarras, idProduto)) {
                return;
            }

            if (produto == null) {
                inserirProduto(new Produto(0, nome, codBarras, preco));
            } else {
                produto.setNome(nome);
                produto.setCodBarras(codBarras);
                produto.setPreco(preco);
                atualizarProduto(produto);
            }
            dialog.close();
        });

        // Layout do formulário
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        
        formGrid.add(new Label("Nome:"), 0, 0);
        formGrid.add(txtNome, 1, 0);
        formGrid.add(new Label("Código de Barras:"), 0, 1);
        formGrid.add(txtCodBarras, 1, 1);
        formGrid.add(new Label("Preço (R$):"), 0, 2);
        formGrid.add(txtPreco, 1, 2);
        formGrid.add(btnSalvar, 1, 3);

        box.getChildren().add(formGrid);
        Scene scene = new Scene(box, 350, 200);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

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
                    
                    // Alerta personalizado
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Produto Existente");
                    alert.setHeaderText("Conflito ao salvar produto");
                    alert.setContentText(mensagem);
                    
                    ButtonType btnIrParaProduto = new ButtonType("Ir para o Produto", ButtonBar.ButtonData.OTHER);
                    ButtonType btnFechar = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(btnIrParaProduto, btnFechar);
                    
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == btnIrParaProduto) {
                        // Selecionar o produto existente na tabela
                        for (Produto p : listaProdutos) {
                            if (p.getId() == idExistente) {
                                table.getSelectionModel().select(p);
                                table.scrollTo(p);
                                break;
                            }
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alerta("Erro ao validar produto: " + e.getMessage());
        }
        return false;
    }

    private void inserirProduto(Produto produto) {
        String sql = "INSERT INTO produtos (Nome, Cod_Barras, Preco) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getCodBarras());
            ps.setDouble(3, produto.getPreco());
            
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
                alerta("Erro ao adicionar produto.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alerta("Erro ao adicionar produto: " + e.getMessage());
        }
    }

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
                // Atualiza o item na lista
                int index = listaProdutos.indexOf(produto);
                if (index >= 0) {
                    listaProdutos.set(index, produto);
                }
                mostrarMensagemSucesso("Produto atualizado com sucesso!");
            } else {
                alerta("Erro ao atualizar produto.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alerta("Erro ao atualizar produto: " + e.getMessage());
        }
    }

    private void apagarProduto(Produto produto) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText("Deseja apagar o produto: " + produto.getNome() + "?");
        confirm.setContentText("Esta ação não pode ser desfeita.");
        
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
                    alerta("Erro ao apagar produto.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                alerta("Erro ao apagar produto: " + e.getMessage());
            }
        }
    }

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
                    alerta("Cargo não reconhecido: " + cargo);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Erro ao retornar para a tela principal.");
        }
    }

    private void confirmarSaida(Stage stage) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Saída");
        alert.setHeaderText("Deseja realmente sair do sistema?");
        alert.initOwner(stage);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stage.close();
        }
    }

    private void mostrarMensagemSucesso(String texto) {
        lblMensagemSucesso.setText(texto);
        lblMensagemSucesso.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> lblMensagemSucesso.setVisible(false));
        pause.play();
    }

    private void alerta(String mensagem) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}