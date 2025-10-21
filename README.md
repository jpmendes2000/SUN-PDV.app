# 📦 SUN PDV - Sistema de Ponto de Venda

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue?style=for-the-badge&logo=java)
![SQL Server](https://img.shields.io/badge/SQL%20Server-2019+-red?style=for-the-badge&logo=microsoft-sql-server)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**Sistema completo de PDV com gestão de vendas, produtos e usuários**

[Demonstração](#-demonstração) • [Instalação](#-instalação) • [Documentação](#-documentação) • [Licença](#-licença)

</div>

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Como Usar](#-como-usar)
- [Estrutura do Banco](#-estrutura-do-banco)
- [Screenshots](#-screenshots)
- [Segurança](#-segurança)
- [Contribuindo](#-contribuindo)
- [Licença](#-licença)

---

## 🎯 Sobre o Projeto

O **SUN PDV** é um sistema de ponto de venda desenvolvido em JavaFX para pequenos e médios estabelecimentos comerciais. Oferece uma interface intuitiva e moderna com controle completo de vendas, estoque e usuários.

### ✨ Destaques

- 🎨 Interface moderna e responsiva
- 🔐 Sistema de autenticação robusto com múltiplos níveis de acesso
- 📊 Dashboard administrativo com métricas em tempo real
- 🧾 Geração automática de cupons fiscais
- 💳 Suporte a múltiplas formas de pagamento
- 📈 Análise Year-over-Year (YoY)

---

## 🚀 Funcionalidades

### 👥 Gestão de Usuários
- ✅ Cadastro de usuários com diferentes níveis de acesso
- ✅ Autenticação segura (AES + SHA-256)
- ✅ Controle de permissões por cargo
- ✅ Bloqueio automático após tentativas falhadas
- ✅ Histórico de último login

### 🛒 Sistema de Vendas
- ✅ Adicionar produtos por código de barras
- ✅ Identificação opcional do cliente (CPF/CNPJ/RG)
- ✅ Validação automática de documentos
- ✅ Múltiplas formas de pagamento na mesma venda
- ✅ Cálculo automático de troco
- ✅ Histórico completo de vendas

### 📦 Gestão de Produtos
- ✅ CRUD completo de produtos
- ✅ Busca por nome ou código de barras
- ✅ Validação de duplicatas
- ✅ Soft delete (desativação)
- ✅ Preços formatados em Real (R$)

### 📊 Dashboard Administrativo
- ✅ Receita mensal e anual
- ✅ Quantidade de vendas
- ✅ Variação Year-over-Year (YoY)
- ✅ Top 10 produtos mais vendidos
- ✅ Funcionário do mês
- ✅ Gráficos interativos (Linha e Barras)

### 🧾 Cupom Fiscal
- ✅ Geração automática em formato texto
- ✅ Comandos ESC/POS para impressoras térmicas
- ✅ Armazenamento local de cupons
- ✅ Impressão opcional (checkbox)

### ⚙️ Configurações
- ✅ Upload de logo da empresa
- ✅ Exibição na tela inicial
- ✅ Armazenamento no AppData do usuário

---

## 🛠️ Tecnologias

### Backend
- **Java 17+** - Linguagem principal
- **JavaFX 17+** - Interface gráfica
- **JDBC** - Conexão com banco de dados
- **SQL Server 2019+** - Banco de dados relacional

### Segurança
- **AES** - Criptografia de emails
- **SHA-256** - Hash de senhas
- **Validação de documentos** - CPF, CNPJ, RG

### Bibliotecas
- **javax.crypto** - Criptografia
- **java.security** - Hashing
- **javafx.animation** - Animações
- **javafx.scene.chart** - Gráficos

---

## 🏗️ Arquitetura
```
com.sunpdv/
│
├── 📁 connection/
│   └── ConexaoDB.java              # Gerenciamento de conexões
│
├── 📁 model/
│   ├── AutenticarUser.java         # Autenticação e cadastro
│   ├── CupomFiscal.java            # Geração de cupons
│   └── Produto.java                # Modelo de produtos
│
├── 📁 telas/
│   ├── 📁 home/
│   │   ├── TelaHomeADM.java        # Tela inicial - Administrador
│   │   ├── TelaHomeMOD.java        # Tela inicial - Moderador
│   │   └── TelaHomeFUN.java        # Tela inicial - Funcionário
│   │
│   └── 📁 operation/
│       ├── LoginApp.java           # Tela de login
│       ├── Cadastro.java           # Cadastro de usuários
│       ├── Caixa.java              # Sistema de vendas
│       ├── FinalizarVenda.java     # Finalização e pagamento
│       ├── Gestao.java             # Gestão de produtos
│       ├── DashboardAdministrativo.java  # Dashboard gerencial
│       ├── Configurar.java         # Configurações do sistema
│       └── Usuarios.java           # Gerenciamento de usuários
│
└── Launcher.java                   # Inicializador da aplicação
```

---

## 💻 Instalação

### Pré-requisitos

- **Java JDK 17+** instalado
- **SQL Server 2019+** em execução
- **Maven** (opcional, para build)

### Passo a Passo

1. **Clone o repositório**
```bash
git clone https://github.com/seu-usuario/sun-pdv.git
cd sun-pdv
```

2. **Configure o banco de dados**
```sql
-- Crie o banco de dados
CREATE DATABASE SUN_PDVlocal;
GO

-- Execute o script de criação de tabelas (fornecido em /sql/schema.sql)
```

3. **Configure a conexão**

Edite o arquivo `ConexaoDB.java`:
```java
private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=SUN_PDVlocal;...";
private static final String USER = "seu_usuario";
private static final String PASSWORD = "sua_senha";
```

4. **Compile o projeto**
```bash
mvn clean install
```

5. **Execute a aplicação**
```bash
java -jar target/sun-pdv-1.0.jar
```

---

## ⚙️ Configuração

### Credenciais Padrão
Após criar o banco, você pode cadastrar o primeiro usuário diretamente pela tela de cadastro ou via SQL:
```sql
-- Inserir usuário administrador padrão
-- Senha: "admin123" (já em SHA-256)
INSERT INTO login_sistema (Nome, Email, Senha, ID_Cargo, ID_Permissao, Ativo)
VALUES (
    'Administrador',
    'ENCRYPTED_EMAIL_HERE', -- Use a função de criptografia
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    1, -- ID do cargo Administrador
    1, -- ID da permissão Aceito
    1
);
```

### Estrutura de Diretórios

O sistema cria automaticamente as seguintes pastas:
```
%APPDATA%/SunPDV/
└── config/
    └── logo_empresa.png         # Logo configurável

C:/Users/User/.../NotasFiscais/
└── cupom_fiscal_*.txt          # Cupons fiscais gerados
```

---

## 📖 Como Usar

### 1. Login
1. Abra a aplicação
2. Digite email e senha
3. O sistema redireciona conforme o cargo

### 2. Realizar uma Venda
1. Clique em **"Vendas"** no menu
2. Clique em **"Nova Venda"**
3. (Opcional) Identifique o cliente
4. Digite o código de barras do produto
5. Adicione quantidade e confirme
6. Clique em **"Finalizar"**
7. Selecione forma(s) de pagamento
8. Confirme a venda

### 3. Gerenciar Produtos
1. Acesse **"Gestão"**
2. Use o botão **"+"** para adicionar
3. Duplo clique para editar
4. Botão **lixeira** para desativar

### 4. Dashboard
1. Em **"Gestão"**, clique em **"Acompanhamento"**
2. Visualize métricas e gráficos
3. Clique em **"Produtos"** para voltar

### 5. Configurar Logo
1. Acesse **"Configurações"**
2. Clique em **"Selecionar"**
3. Escolha uma imagem PNG/JPG
4. A logo aparecerá na tela inicial

---

## 🗄️ Estrutura do Banco

### Diagrama ER Simplificado
```
┌─────────────────┐       ┌──────────────┐       ┌─────────────┐
│ login_sistema   │───────│    cargo     │       │  permissao  │
│                 │       │              │       │             │
│ • ID_Login (PK) │       │ • ID_Cargo   │       │ • ID_Perm.  │
│ • Nome          │       │ • Cargo      │       │ • permissao │
│ • Email (AES)   │       └──────────────┘       └─────────────┘
│ • Senha (SHA256)│
│ • ID_Cargo (FK) │
│ • ID_Permissao  │
└─────────────────┘
         │
         │ (Funcionário)
         ▼
┌─────────────────┐       ┌──────────────┐
│     vendas      │───────│   carrinho   │
│                 │       │              │
│ • ID_Vendas (PK)│       │ • ID_Carrinho│
│ • ID_Carrinho   │       └──────┬───────┘
│ • ID_Clientes   │              │
│ • ID_Login (FK) │              │
│ • Total         │              ▼
│ • Data_Venda    │       ┌──────────────────┐
└────────┬────────┘       │ carrinho_itens   │
         │                │                  │
         │                │ • ID_Carrinho    │
         │                │ • ID_Produto (FK)│
         │                │ • Quantidade     │
         │                │ • Preco_Unitario │
         │                └──────────────────┘
         │                         │
         │                         ▼
         │                ┌──────────────┐
         │                │   produtos   │
         │                │              │
         │                │ • ID_Produto │
         │                │ • Nome       │
         │                │ • Cod_Barras │
         │                │ • Preco      │
         │                │ • Ativo      │
         │                └──────────────┘
         │
         ▼
┌────────────────────┐
│ venda_pagamentos   │──┐
│                    │  │
│ • ID_Venda         │  │
│ • ID_Pagamento     │  │
└────────────────────┘  │
                        │
                        ▼
                 ┌──────────────┐       ┌──────────────────┐
                 │  pagamentos  │───────│ forma_pagamento  │
                 │              │       │                  │
                 │ • ID_Pagto.  │       │ • ID_Forma_Pagt. │
                 │ • ID_Forma   │       │ • Forma_Pagto.   │
                 │ • Valor_Rec. │       │   (Dinheiro,     │
                 │ • Troco      │       │    Débito, etc.) │
                 └──────────────┘       └──────────────────┘
```

### Tabelas Principais

| Tabela | Descrição | Campos Principais |
|--------|-----------|-------------------|
| `login_sistema` | Usuários do sistema | Nome, Email (criptografado), Senha (hash), Cargo, Permissão |
| `produtos` | Catálogo de produtos | Nome, Código de Barras, Preço, Ativo |
| `vendas` | Vendas finalizadas | Total, Data, Cliente, Funcionário, Status |
| `carrinho` | Carrinhos de compra | Data de Criação |
| `carrinho_itens` | Itens dos carrinhos | Produto, Quantidade, Preço Unitário |
| `pagamentos` | Pagamentos realizados | Forma, Valor Recebido, Troco |
| `venda_pagamentos` | Vínculo N:N vendas-pagamentos | ID_Venda, ID_Pagamento |
| `clientes` | Dados dos clientes | CPF, CNPJ, RG, Nome |

---

## 📸 Screenshots

### Tela de Login
![Login](docs/images/login.png)
*Sistema de autenticação com bloqueio por tentativas*

### Dashboard Administrativo
![Dashboard](docs/images/dashboard.png)
*Métricas, gráficos e análise YoY*

### Sistema de Vendas
![Vendas](docs/images/vendas.png)
*Interface de caixa com histórico*

### Gestão de Produtos
![Produtos](docs/images/produtos.png)
*CRUD completo com filtros*

---

## 🔐 Segurança

### Criptografia
- **Emails**: AES com chave de 256 bits
- **Senhas**: SHA-256 (não reversível)

### Validações
```java
// CPF: Validação completa com dígitos verificadores
✓ Formato: XXX.XXX.XXX-XX ou apenas números
✓ Rejeita sequências repetidas (111.111.111-11)
✓ Calcula e valida ambos os dígitos verificadores

// CNPJ: Validação empresarial
✓ Formato: XX.XXX.XXX/XXXX-XX ou apenas números
✓ Rejeita sequências repetidas
✓ Validação com pesos específicos

// RG: Validação básica
✓ Entre 7 e 10 dígitos numéricos
✓ Rejeita sequências repetidas
```

### Transações
- `setAutoCommit(false)` em operações críticas
- `commit()` apenas após todas as inserções
- `rollback()` em caso de erro

### Controle de Acesso

| Cargo | Vendas | Produtos | Usuários | Dashboard | Config |
|-------|--------|----------|----------|-----------|--------|
| **Administrador** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Moderador** | ✅ | ✅ | ❌ | ✅ | ✅ |
| **Funcionário** | ✅ | ❌ | ❌ | ❌ | ❌ |

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Siga os passos:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

### Padrões de Código
- Use nomes descritivos para variáveis e métodos
- Comente código complexo
- Siga convenções JavaFX e Java
- Teste antes de enviar

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👨‍💻 Autores

**João Pedro de Moraes Mendes**
- GitHub: [@jpmendes](https://github.com/jpmendes2000)
- LinkedIn: [jpmendes](https://www.linkedin.com/in/jo%C3%A3o-pedro-de-moraes-mendes-1311bb31a/)
- Email: jpmoraes.mendes22@gmail.com

**João Pedro Silva Schinato**
- GitHub: [@jpschinato](https://github.com/JP-schinato)
- LinkedIn: [jpschinato](https://www.linkedin.com/in/jo%C3%A3o-pedro-silva-schinato-249010332/)
- Email: jpsilvaschinato@gmail.com

---

## 🙏 Agradecimentos

- Equipe de desenvolvimento
- Bibliotecas open-source utilizadas
- Comunidade JavaFX
- Testadores e colaboradores

---

<div align="center">

**⭐ Se este projeto foi útil, considere dar uma estrela!**

</div>
