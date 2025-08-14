<img src="src/main/resources/img/logo/logo.png" />

<p align="center">
  <strong>Sistema de Ponto de Venda (PDV)</strong><br>
  Uma aplicação Java completa com JavaFX, JDBC e MySQL, feita para controle de produtos, usuários e autenticação segura.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-red?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/JavaFX-Framework-blue?style=for-the-badge&logo=java" />
  <img src="https://img.shields.io/badge/MySQL-Database-orange?style=for-the-badge&logo=mysql" />
  <img src="https://img.shields.io/badge/Secure-Login-green?style=for-the-badge&logo=lock" />
</p>

---

## 📸 Demonstração

<img src="https://github.com/seuusuario/sun-pdv/assets/demo.png" alt="Tela de Login" width="700" />

---

## ✨ Recursos

- 🔐 **Login Seguro** com SHA-256 e AES
- 🧍‍♂️ **Gestão de Usuários** com diferentes níveis de acesso
- 📦 **Controle de Produtos**: entrada e saída com código de barras
- 📊 **Interface Moderna** com JavaFX + CSS externo
- ☁️ **Banco de Dados em Nuvem** (MySQL/Azure)
- ⏳ **Bloqueio Temporário** após tentativas inválidas
- 🔄 **Feedback Visual** com animações de transição

---

## 🛠️ Tecnologias

| Tecnologia | Descrição |
|------------|-----------|
| `Java 17` | Linguagem base do projeto |
| `JavaFX` | Framework gráfico para UI moderna |
| `MySQL` | Banco de dados relacional |
| `JDBC` | Conexão entre Java e MySQL |
| `CSS` | Estilização da interface via arquivo externo |
| `Azure` | Hospedagem em nuvem do banco de dados |

---

## 🧩 Estrutura do Projeto

```bash
sun-pdv/
│
├── 📂 .vscode/
│
├── 📂 src/main/
│   ├── 📂 java/com/sunpdv/
│   │   ├── 📂 connection/
│   │   ├── 📂 model/
│   │   ├── 📂 telas/
│   │   │    ├── 📂 home/
│   │   └── 📜 App.java 
│   └── 📂 resources/
│       ├── 📂 css/
│       └── 📂 img/
│
├── 📂 target/
│
├── 📜 .gitignore
├── 📜 SUN-PDV.app.jar
├── 📜 README.md
├── 📜 Package-lock.json
└── 📜 pom.xml
