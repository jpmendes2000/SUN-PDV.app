package com.sunpdv.model;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class CupomFiscal {
    
    // Informações de pagamento
    public static class PagamentoInfo {
        public String formaPagamento;
        public double valor;
        
        public PagamentoInfo(String formaPagamento, double valor) {
            this.formaPagamento = formaPagamento;
            this.valor = valor;
        }
    }
    
    // Item de venda
    public static class ItemVenda {
        public String produto;
        public String codigoBarras;
        public int quantidade;
        public double preco;
        
        public ItemVenda(String produto, String codigoBarras, int quantidade, double preco) {
            this.produto = produto;
            this.codigoBarras = codigoBarras;
            this.quantidade = quantidade;
            this.preco = preco;
        }
        
        public double getTotal() {
            return preco * quantidade;
        }
    }
    
    // Para mandar o cupom para a impressora
    public void imprimirCupom(byte[] data, File arquivoTexto) {
        try {
            // Procura a impressora
            PrintService impressora = PrintServiceLookup.lookupDefaultPrintService();
            
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(data, flavor, null);
            DocPrintJob job = impressora.createPrintJob();
            PrintRequestAttributeSet atributos = new HashPrintRequestAttributeSet();
            job.print(doc, atributos);
            
            System.out.println("Cupom enviado para impressão com sucesso!");
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao imprimir cupom: " + e.getMessage() + "\nCupom salvo em: " + arquivoTexto.getAbsolutePath(), AlertType.ERROR);
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(arquivoTexto);
                }
            } catch (IOException ex) {
                mostrarAlerta("Erro", "Erro ao abrir arquivo de cupom: " + ex.getMessage(), AlertType.ERROR);
            }
        }
    }
    
    // Gera e imprime o cupom fiscal
    public void gerarEImprimirCupom(List<ItemVenda> itens, double total, double troco, 
                                   String documento, String tipoDocumento, List<PagamentoInfo> pagamentos) {
        try {
            // Diretório para salvar os cupons
            String diretorio = "C:\\Users\\User\\OneDrive\\Documentos\\TCC\\TCCsystem\\SUN-PDV.app\\NotasFiscais\\";
            File dir = new File(diretorio);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    mostrarAlerta("Erro", "Não foi possível criar o diretório: " + diretorio, AlertType.ERROR);
                    return;
                }
            }
            
            // Criar arquivo de texto para visualização
            String nomeArquivoTexto = "cupom_fiscal_" + System.currentTimeMillis() + ".txt";
            File arquivoTexto = new File(diretorio + nomeArquivoTexto);
            
            // Gerar texto do cupom
            StringBuilder textoCupom = new StringBuilder();
            textoCupom.append("=======================================\n");
            textoCupom.append("           SUN PDV - CUPOM FISCAL      \n");
            textoCupom.append("=======================================\n");
            textoCupom.append("Data: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
            textoCupom.append("---------------------------------------\n");
            
            if (documento != null && !documento.trim().isEmpty()) {
                textoCupom.append("Cliente: ").append(tipoDocumento).append(" ").append(documento).append("\n");
            } else {
                textoCupom.append("Cliente: Não identificado\n");
            }
            textoCupom.append("---------------------------------------\n");
            
            textoCupom.append("ITENS:\n");
            for (ItemVenda item : itens) {
                textoCupom.append(String.format("%-25s\n", limitarString(item.produto, 25)));
                textoCupom.append(String.format("Cód: %-15s Qtd: %3d\n", 
                    limitarString(item.codigoBarras, 15), item.quantidade));
                textoCupom.append(String.format("R$ %7.2f x %3d = R$ %7.2f\n", 
                    item.preco, item.quantidade, item.getTotal()));
                textoCupom.append("---------------------------------------\n");
            }
            
            textoCupom.append(String.format("SUBTOTAL:                R$ %7.2f\n", total));
            textoCupom.append("---------------------------------------\n");
            
            if (pagamentos != null && !pagamentos.isEmpty()) {
                textoCupom.append("FORMAS DE PAGAMENTO:\n");
                for (PagamentoInfo pag : pagamentos) {
                    textoCupom.append(String.format("%-20s R$ %7.2f\n", 
                        limitarString(pag.formaPagamento + ":", 20), pag.valor));
                }
                textoCupom.append("---------------------------------------\n");
            }
            
            textoCupom.append(String.format("TOTAL:                   R$ %7.2f\n", total));
            if (troco > 0) {
                textoCupom.append(String.format("TROCO:                   R$ %7.2f\n", troco));
            }
            
            textoCupom.append("=======================================\n");
            textoCupom.append("     Obrigado pela preferência!       \n");
            textoCupom.append("          Volte sempre!               \n");
            textoCupom.append("=======================================\n");
            textoCupom.append("\nCupom gerado automaticamente pelo SUN PDV\n");
            textoCupom.append("Este documento não possui valor fiscal\n");
            
            // Salvar texto do cupom
            try (FileWriter writer = new FileWriter(arquivoTexto, java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(textoCupom.toString());
            }
            
            System.out.println("Cupom fiscal (texto) gerado: " + arquivoTexto.getAbsolutePath());
            
            // Gerar dados ESC/POS
            byte[] data = gerarDadosEscPos(itens, total, troco, documento, tipoDocumento, pagamentos);
            
            // Tentar imprimir cupom
            imprimirCupom(data, arquivoTexto);
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao gerar cupom fiscal: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    // Gera os dados ESC/POS para o cupom fiscal
    private byte[] gerarDadosEscPos(List<ItemVenda> itens, double total, double troco, 
                                    String documento, String tipoDocumento, List<PagamentoInfo> pagamentos) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        // Charset para texto (CP850 para acentos em português, comum em impressoras térmicas)
        String charset = "CP850"; // Ajuste para "UTF-8" ou "CP437" se necessário
        
        // Inicializar impressora
        bos.write(new byte[] {0x1B, 0x40});
        
        // Centralizar para cabeçalho
        bos.write(new byte[] {0x1B, 0x61, 0x01});
        writeText(bos, "SUN PDV - CUPOM FISCAL\n", charset);
        writeText(bos, "=======================================\n", charset);
        
        // Alinhar à esquerda
        bos.write(new byte[] {0x1B, 0x61, 0x00});
        
        // Data
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        writeText(bos, "Data: " + dataHora + "\n", charset);
        writeText(bos, "---------------------------------------\n", charset);
        
        // Dados do cliente
        if (documento != null && !documento.trim().isEmpty()) {
            writeText(bos, "Cliente: " + tipoDocumento + " " + documento + "\n", charset);
        } else {
            writeText(bos, "Cliente: Não identificado\n", charset);
        }
        writeText(bos, "---------------------------------------\n", charset);
        
        // Itens
        writeText(bos, "ITENS:\n", charset);
        for (ItemVenda item : itens) {
            writeText(bos, limitarString(item.produto, 38) + "\n", charset);
            writeText(bos, String.format("Cód: %-15s Qtd: %3d\n", 
                limitarString(item.codigoBarras, 15), item.quantidade), charset);
            writeText(bos, String.format("R$ %7.2f x %3d = R$ %7.2f\n", 
                item.preco, item.quantidade, item.getTotal()), charset);
            writeText(bos, "---------------------------------------\n", charset);
        }
        
        // Subtotal
        writeText(bos, String.format("SUBTOTAL:                R$ %7.2f\n", total), charset);
        writeText(bos, "---------------------------------------\n", charset);
        
        // Formas de pagamento
        if (pagamentos != null && !pagamentos.isEmpty()) {
            writeText(bos, "FORMAS DE PAGAMENTO:\n", charset);
            for (PagamentoInfo pag : pagamentos) {
                writeText(bos, String.format("%-20s R$ %7.2f\n", 
                    limitarString(pag.formaPagamento + ":", 20), pag.valor), charset);
            }
            writeText(bos, "---------------------------------------\n", charset);
        }
        
        // Total e Troco
        bos.write(new byte[] {0x1B, 0x45, 0x01}); // Negrito ON
        writeText(bos, String.format("TOTAL:                   R$ %7.2f\n", total), charset);
        bos.write(new byte[] {0x1B, 0x45, 0x00}); // Negrito OFF
        if (troco > 0) {
            writeText(bos, String.format("TROCO:                   R$ %7.2f\n", troco), charset);
        }
        
        // Rodapé centralizado
        bos.write(new byte[] {0x1B, 0x61, 0x01});
        writeText(bos, "=======================================\n", charset);
        writeText(bos, "     Obrigado pela preferência!       \n", charset);
        writeText(bos, "          Volte sempre!               \n", charset);
        writeText(bos, "=======================================\n", charset);
        
        // Alinhar à esquerda
        bos.write(new byte[] {0x1B, 0x61, 0x00});
        writeText(bos, "\nCupom gerado automaticamente pelo SUN PDV\n", charset);
        writeText(bos, "Este documento não possui valor fiscal\n", charset);
        
        // Alimentar papel (3 linhas)
        bos.write(new byte[] {0x1B, 0x64, 0x03});
        
        // Corte parcial
        bos.write(new byte[] {0x1D, 0x56, 0x01});
        
        return bos.toByteArray();
    }
    
    // Método auxiliar para escrever texto com charset específico
    private void writeText(ByteArrayOutputStream bos, String text, String charset) throws IOException {
        bos.write(text.getBytes(charset));
    }

    // Limita o tamanho da string para evitar quebras na impressão
    private String limitarString(String texto, int tamanhoMax) {
        if (texto == null) return "";
        if (texto.length() <= tamanhoMax) return texto;
        return texto.substring(0, tamanhoMax - 3) + "...";
    }
    
    // Mostrar alertas (JavaFX ou console)
    private void mostrarAlerta(String titulo, String mensagem, AlertType tipo) {
        try {
            Alert alert = new Alert(tipo);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        } catch (Exception e) {
            System.out.println(titulo + ": " + mensagem);
        }
    }
}