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

        // --- Imagem logo ---
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(130);
        logoView.setPreserveRatio(true);

        // --- Imagem título ---
        Image tituloImagem = new Image(getClass().getResourceAsStream("/img/logo/produto.png"));
        ImageView tituloView = new ImageView(tituloImagem);
        tituloView.setPreserveRatio(true);
        tituloView.setFitHeight(120);

        // --- Label mensagem sucesso (inicialmente invisível) ---
        lblMensagemSucesso = new Label();
        lblMensagemSucesso.getStyleClass().add("mensagem-sucesso");
        lblMensagemSucesso.setVisible(false);

        // --- Campo pesquisa ---
        campoPesquisa = new TextField();
        campoPesquisa.setPromptText("Pesquisar produto...");
        campoPesquisa.setPrefWidth(400);
        campoPesquisa.textProperty().addListener((obs, oldVal, newVal) -> filtrarProdutos(newVal));

        // --- Botão adicionar com imagem ---
        Image imgAdd = new Image(getClass().getResourceAsStream("/img/icon/lista.png"));
        ImageView iconAdd = new ImageView(imgAdd);
        iconAdd.setFitWidth(20);
        iconAdd.setFitHeight(20);
        Button btnAdd = new Button("", iconAdd);
        btnAdd.setTooltip(new Tooltip("Adicionar Produto"));
        btnAdd.setPrefSize(40, 40);

        // --- Botão editar com imagem ---
        Image imgEdit = new Image(getClass().getResourceAsStream("/img/icon/lapis.png"));
        ImageView iconEdit = new ImageView(imgEdit);
        iconEdit.setFitWidth(20);
        iconEdit.setFitHeight(20);
        Button btnEdit = new Button("", iconEdit);
        btnEdit.setTooltip(new Tooltip("Editar Produto"));
        btnEdit.setPrefSize(40, 40);
        btnEdit.setDisable(true); // Inicialmente desabilitado

        // --- Botão apagar com imagem ---
        Image imgDelete = new Image(getClass().getResourceAsStream("/img/icon/fechar.png"));
        ImageView iconDelete = new ImageView(imgDelete);
        iconDelete.setFitWidth(20);
        iconDelete.setFitHeight(20);
        Button btnDelete = new Button("", iconDelete);
        btnDelete.setTooltip(new Tooltip("Apagar Produto"));
        btnDelete.setPrefSize(40, 40);
        btnDelete.setDisable(true); // Inicialmente desabilitado

        // --- Box título + mensagem sucesso lado a lado ---
        HBox tituloMensagemBox = new HBox(10, tituloView, lblMensagemSucesso);
        tituloMensagemBox.setAlignment(Pos.CENTER_LEFT);

        // --- Box com logo + título+mensagem lado a lado ---
        HBox logoTituloBox = new HBox(20, logoView, tituloMensagemBox);
        logoTituloBox.setAlignment(Pos.CENTER_LEFT);
        logoTituloBox.setPadding(new Insets(10, 10, 5, 10));

        // --- Box pesquisa e botões de ação lado a lado ---
        HBox pesquisaAcoesBox = new HBox(10, campoPesquisa, btnAdd, btnEdit, btnDelete);
        pesquisaAcoesBox.setAlignment(Pos.CENTER_LEFT);
        pesquisaAcoesBox.setPadding(new Insets(5, 0, 15, 10));

        // --- VBox topo contendo logoTituloBox + pesquisaAcoesBox (vertical) ---
        VBox topoBox = new VBox(5, logoTituloBox, pesquisaAcoesBox);
        topoBox.setPadding(new Insets(0));
        topoBox.setAlignment(Pos.TOP_LEFT);

        // --- Botões Home e Sair ---
        ImageView iconHome = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/casa.png")));
        iconHome.setFitWidth(32);
        iconHome.setFitHeight(32);

        ImageView iconSair = new ImageView(new Image(getClass().getResourceAsStream("/img/icon/fechar.png")));
        iconSair.setFitWidth(32);
        iconSair.setFitHeight(32);

        Button btnVoltar = new Button("Home", iconHome);
        Button btnSair = new Button("Sair do Sistema", iconSair);
        btnVoltar.setPrefWidth(150);
        btnSair.setPrefWidth(150);

        // --- TableView ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefWidth(800);

        // Colunas
        TableColumn<Produto, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(400);

        TableColumn<Produto, String> colCodBarras = new TableColumn<>("Código de Barras");
        colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras"));
        colCodBarras.setPrefWidth(250);

        TableColumn<Produto, String> colPreco = new TableColumn<>("Preço (R$)");
        colPreco.setCellValueFactory(cell -> {
            Double preco = cell.getValue().getPreco();
            DecimalFormat df = new DecimalFormat("R$ #,##0.00");
            return new SimpleStringProperty(df.format(preco));
        });
        colPreco.setPrefWidth(150);

        table.getColumns().addAll(colNome, colCodBarras, colPreco);

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

        carregarProdutos();

        // --- Eventos dos botões ---
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

        btnVoltar.setOnAction(e -> {
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
        });

        btnSair.setOnAction(e -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Saída");
            alert.setHeaderText("Deseja realmente sair do sistema?");
            alert.initOwner(stage);
            alert.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) stage.close();
            });
        });

        // --- Layout dos botões Home e Sair na direita ---
        VBox boxBotoes = new VBox(15, btnVoltar, btnSair);
        boxBotoes.setPadding(new Insets(10));
        boxBotoes.setAlignment(Pos.TOP_CENTER);

        // --- Layout principal ---
        BorderPane root = new BorderPane();
        root.setTop(topoBox);
        
        // Adicionando a tabela em um StackPane para centralizar
        StackPane tableContainer = new StackPane(table);
        tableContainer.setPadding(new Insets(0, 10, 10, 10));
        root.setCenter(tableContainer);
        
        root.setRight(boxBotoes);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("produtos");

        // --- Cena e stage ---
        Scene scene = new Scene(root, 1100, 700);
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

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));

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
                alerta("Preço inválido.");
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

        box.getChildren().addAll(new Label("Nome:"), txtNome,
                new Label("Código de Barras:"), txtCodBarras,
                new Label("Preço:"), txtPreco,
                btnSalvar);

        Scene scene = new Scene(box, 300, 250);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void inserirProduto(Produto produto) {
        String sql = "INSERT INTO produtos (Nome, Cod_Barras, Preco) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, produto.getNome());
            ps.setString(2, produto.getCodBarras());
            ps.setDouble(3, produto.getPreco());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                carregarProdutos();
                mostrarMensagemSucesso("Produto adicionado com sucesso!");
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
                carregarProdutos();
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
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                String sql = "DELETE FROM produtos WHERE ID_Produto = ?";
                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, produto.getId());
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        carregarProdutos();
                        mostrarMensagemSucesso("Produto apagado com sucesso!");
                    } else {
                        alerta("Erro ao apagar produto.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    alerta("Erro ao apagar produto: " + e.getMessage());
                }
            }
        });
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