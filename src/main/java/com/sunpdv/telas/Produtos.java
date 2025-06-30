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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.Optional;

public class Produtos {

    private TableView<Produto> table;
    private ObservableList<Produto> listaProdutos;
    private TextField campoPesquisa;
    private Label lblMensagemSucesso;
    private Produto produtoSelecionado;

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "Senha@12345!";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void show(Stage stage) {
        // Configuração do Layout Principal usando BorderPane
        BorderPane mainPane = new BorderPane();
        
        // Área esquerda (menu lateral) - TRECHO MODIFICADO
        VBox leftMenu = new VBox();
        leftMenu.setPadding(new Insets(0));
        leftMenu.setStyle("-fx-background-color: #00536d;");
        leftMenu.setPrefWidth(280);
        leftMenu.setMinWidth(280);

        // Logo no topo do menu lateral
        Image logo = new Image(getClass().getResourceAsStream("/img/logo/logo.png"));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(120);
        logoView.setPreserveRatio(true);
        logoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");

        Label titulonaABA = new Label("Gerenciamento de Produtos");
        titulonaABA.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox logoBox = new VBox(logoView, titulonaABA);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(20, 0, 20, 0)); // Padding ajustado

        // Botões na parte inferior do menu lateral
        Button btnVoltar = criarBotaoLateral("Home", "/img/icon/casa.png");
        Button btnSair = criarBotaoLateral("Sair do Sistema", "/img/icon/fechar.png");

        VBox buttonBox = new VBox(10, btnVoltar, btnSair); // Espaçamento reduzido
        buttonBox.setAlignment(Pos.TOP_LEFT);
        buttonBox.setPadding(new Insets(0, 0, 20, 0));

        // Organização do menu lateral
        leftMenu.getChildren().addAll(logoBox, new Region(), buttonBox);
        VBox.setVgrow(leftMenu.getChildren().get(1), Priority.ALWAYS); // Empurra os botões para baixo
        
        // Área central (conteúdo principal)
        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(20);
        contentGrid.setVgap(10);
        contentGrid.setPadding(new Insets(15));
        contentGrid.setAlignment(Pos.TOP_CENTER);

        lblMensagemSucesso = new Label();
        lblMensagemSucesso.getStyleClass().add("mensagem-sucesso");
        lblMensagemSucesso.setVisible(false);

        campoPesquisa = new TextField();
        campoPesquisa.setPromptText("Pesquisar produto...");
        campoPesquisa.setPrefWidth(400);
        campoPesquisa.textProperty().addListener((obs, oldVal, newVal) -> filtrarProdutos(newVal));

        // Botões de ação
        Button btnAdd = criarBotaoAcao("/img/icon/lista.png", "Adicionar Produto");
        Button btnEdit = criarBotaoAcao("/img/icon/lapis.png", "Editar Produto");
        Button btnDelete = criarBotaoAcao("/img/icon/lixeira.png", "Apagar Produto");
        
        
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);

        // Organização do topo
        HBox tituloMensagemBox = new HBox(10, lblMensagemSucesso);
        tituloMensagemBox.setAlignment(Pos.TOP_LEFT);

        HBox pesquisaAcoesBox = new HBox(12, campoPesquisa, btnAdd, btnEdit, btnDelete);
        pesquisaAcoesBox.setAlignment(Pos.CENTER_LEFT);
        pesquisaAcoesBox.setPadding(new Insets(5, 0, 15, 10));

        VBox topoBox = new VBox(5, tituloMensagemBox, pesquisaAcoesBox);
        contentGrid.add(topoBox, 0, 0, 2, 1);

        // Tabela de produtos (expandida)
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");
        table.setStyle("-fx-padding: 0;"); // Removido padding extra

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

        // Configuração do double-click para editar
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

        ScrollPane scrollTable = new ScrollPane(table);
        scrollTable.setFitToWidth(true);
        scrollTable.setFitToHeight(true);
        scrollTable.setPrefViewportHeight(1650);
        scrollTable.setStyle("-fx-padding: 0;"); // Removido padding extra
        contentGrid.add(scrollTable, 0, 1, 2, 1);

        // Configuração do layout principal
        mainPane.setLeft(leftMenu);
        mainPane.setCenter(contentGrid);

        btnAdd.setOnAction(e -> abrirFormularioProduto(null));
        btnEdit.setOnAction(e -> abrirFormularioProduto(produtoSelecionado));
        btnDelete.setOnAction(e -> {
            if (produtoSelecionado != null) {
                apagarProduto(produtoSelecionado);
            }
        });

        btnVoltar.setOnAction(e -> voltarParaHome(stage));
        btnSair.setOnAction(e -> confirmarSaida(stage));

        // Listener para seleção na tabela
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            produtoSelecionado = newVal;
            btnEdit.setDisable(newVal == null);
            btnDelete.setDisable(newVal == null);
        });

        // Carrega os produtos
        carregarProdutos();

        // Configuração da cena e stage
        Scene scene = new Scene(mainPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Gerenciamento de Produtos");
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();
    }

    private ContextMenu criarMenuContexto() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem editarItem = new MenuItem("Editar");
        editarItem.setOnAction(e -> {
            if (produtoSelecionado != null) {
                abrirFormularioProduto(produtoSelecionado);
            }
        });
        
        MenuItem apagarItem = new MenuItem("Apagar");
        apagarItem.setOnAction(e -> {
            if (produtoSelecionado != null) {
                apagarProduto(produtoSelecionado);
            }
        });
        
        contextMenu.getItems().addAll(editarItem, apagarItem);
        return contextMenu;
    }

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
            
            // Container for the yellow bar indicator (agora à direita)
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
            
            // Hover effect com a barra amarela à direita
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
                    rs.getString("Cod_Barras"),
                    rs.getDouble("Preco")
                ));
            }
            table.setItems(listaProdutos);
        } catch (SQLException e) {
            mostrarAlertaErro("Erro ao carregar produtos", "Erro ao carregar produtos do banco: " + e.getMessage());
        }
    }

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

    private void abrirFormularioProduto(Produto produto) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(produto == null ? "Adicionar Produto" : "Editar Produto");

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome do Produto");
        txtNome.setPrefWidth(250);

        TextField txtCodBarras = new TextField();
        txtCodBarras.setPromptText("Código de Barras");
        txtCodBarras.setPrefWidth(250);

        TextField txtPreco = new TextField();
        txtPreco.setPromptText("Preço (R$)");
        txtPreco.setPrefWidth(250);

        if (produto != null) {
            txtNome.setText(produto.getNome());
            txtCodBarras.setText(produto.getCodBarras());
            txtPreco.setText(String.format("%.2f", produto.getPreco()));
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
        formGrid.add(new Label("Código de Barras:"), 0, 1);
        formGrid.add(txtCodBarras, 1, 1);
        formGrid.add(new Label("Preço (R$):"), 0, 2);
        formGrid.add(txtPreco, 1, 2);
        formGrid.add(btnSalvar, 1, 3);

        Scene scene = new Scene(formGrid);
        scene.getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

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
            inserirProduto(new Produto(0, nome, codBarras, preco));
        } else {
            produto.setNome(nome);
            produto.setCodBarras(codBarras);
            produto.setPreco(preco);
            atualizarProduto(produto);
        }
        dialog.close();
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
                    
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Produto Existente");
                    alert.setHeaderText("Conflito ao salvar produto");
                    alert.setContentText(mensagem);
                    alert.getDialogPane().getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
                    
                    ButtonType btnIrParaProduto = new ButtonType("Ir para o Produto", ButtonBar.ButtonData.OTHER);
                    ButtonType btnFechar = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(btnIrParaProduto, btnFechar);
                    
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == btnIrParaProduto) {
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

    private void selecionarProdutoNaTabela(int idProduto) {
        for (Produto p : listaProdutos) {
            if (p.getId() == idProduto) {
                table.getSelectionModel().select(p);
                table.scrollTo(p);
                break;
            }
        }
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
                mostrarAlertaErro("Erro ao adicionar", "Erro ao adicionar produto.");
            }
        } catch (SQLException e) {
            mostrarAlertaErro("Erro no banco de dados", "Erro ao adicionar produto: " + e.getMessage());
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

    private void apagarProduto(Produto produto) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmação");
        confirm.setHeaderText("Deseja apagar o produto: " + produto.getNome() + "?");
        confirm.setContentText("Esta ação não pode ser desfeita.");
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
        
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

    private void confirmarSaida(Stage stage) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Saída");
        alert.setHeaderText("Deseja realmente sair do sistema?");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
        
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

    private void mostrarAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/img/css/style.css").toExternalForm());
        alert.showAndWait();
    }
}