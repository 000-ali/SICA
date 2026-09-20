# SiCA - Sistema de Compartilhamento de Arquivos

## 1. Descrição do projeto

O SiCA (Sistema de Compartilhamento de Arquivos) é uma aplicação desenvolvida em Java que permite a transferência de arquivos entre um cliente e um servidor por meio de uma conexão de rede utilizando sockets TCP.

O sistema foi desenvolvido como exercício prático da disciplina de Desenvolvimento de Software C-S da Pontifícia Universidade Católica de Goiás (PUC Goiás).

A aplicação utiliza uma arquitetura cliente-servidor, na qual o cliente envia solicitações e o servidor é responsável por processá-las.

## 2. Objetivo

Desenvolver uma aplicação que permita o compartilhamento de arquivos por meio de uma conexão TCP.

O cliente pode enviar arquivos, consultar os arquivos armazenados no servidor e realizar downloads.

## 3. Tecnologias utilizadas

- Java
- Sockets TCP
- Java NIO (manipulação de arquivos)
- Git e GitHub
- Visual Studio Code

## 4. Funcionalidades

### Enviar arquivos

O cliente seleciona um arquivo do seu computador e envia seu conteúdo ao servidor.

O servidor recebe os dados e armazena o arquivo na pasta arquivos_servidor.

### Listar arquivos

O cliente solicita uma listagem dos arquivos disponíveis no servidor.

O servidor consulta a pasta de armazenamento e retorna os nomes dos arquivos encontrados.

### Baixar arquivos

O cliente informa o nome de um arquivo disponível no servidor.

O servidor localiza o arquivo e envia seu conteúdo ao cliente, que o salva na pasta downloads.

Caso o arquivo não exista, o servidor retorna uma mensagem informando que ele não foi encontrado.

## 5. Estrutura do projeto

SiCA/
|
|-- src/
|   |-- Servidor.java
|   |-- Cliente.java
|
|-- arquivos_servidor/
|-- downloads/
|
|-- README.md
|-- .gitignore

### Descrição dos arquivos e diretórios

- Servidor.java: recebe conexões TCP e processa as operações solicitadas pelos clientes.
- Cliente.java: apresenta o menu e realiza as solicitações ao servidor.
- arquivos_servidor: armazena os arquivos recebidos pelo servidor.
- downloads: armazena os arquivos baixados pelo cliente.
- README.md: contém a documentação do projeto.
- .gitignore: define os arquivos que não devem ser enviados ao GitHub.

## 6. Funcionamento da comunicação

O sistema utiliza o protocolo TCP, que fornece uma comunicação confiável e ordenada entre cliente e servidor.

O servidor permanece aguardando conexões na porta 5000.

O cliente utiliza o endereço 127.0.0.1 para se conectar ao servidor quando ambos são executados no mesmo computador.

Cada operação é identificada por um comando:

- ENVIAR: solicita o envio de um arquivo.
- LISTAR: solicita a lista de arquivos disponíveis.
- BAIXAR: solicita o download de um arquivo.

### Fluxo de envio

1. O cliente estabelece uma conexão TCP.
2. Envia o comando ENVIAR.
3. Envia o nome e o tamanho do arquivo.
4. Transmite o conteúdo em blocos de bytes.
5. O servidor recebe e armazena os dados.
6. O servidor retorna uma confirmação.

### Fluxo de listagem

1. O cliente estabelece uma conexão TCP.
2. Envia o comando LISTAR.
3. O servidor consulta os arquivos disponíveis.
4. Envia a quantidade de arquivos encontrados.
5. Envia o nome de cada arquivo.
6. O cliente apresenta a lista no terminal.

### Fluxo de download

1. O cliente estabelece uma conexão TCP.
2. Envia o comando BAIXAR e o nome do arquivo.
3. O servidor verifica se o arquivo existe.
4. Se existir, envia o tamanho e o conteúdo.
5. O cliente recebe os dados e salva o arquivo na pasta downloads.
6. Se não existir, o servidor retorna uma mensagem de erro.

## 7. Como executar o projeto

### Pré-requisitos

É necessário ter o JDK instalado e configurado.

O projeto pode ser executado pelo terminal do VS Code ou pelo PowerShell do Windows.

### Passo 1 - Abrir a pasta

Abra a pasta principal do projeto SiCA no terminal.

### Passo 2 - Compilar

Execute:

```powershell
javac -d . src/Servidor.java src/Cliente.java
```

Esse comando compila os dois arquivos Java.

### Passo 3 - Iniciar o servidor

No primeiro terminal, execute:

```powershell
java Servidor
```

O servidor ficará aguardando conexões na porta 5000.

### Passo 4 - Iniciar o cliente

Abra um segundo terminal na pasta principal do projeto e execute:

```powershell
java Cliente
```

O menu principal será apresentado.

### Passo 5 - Utilizar o menu

```text
========================
       CLIENTE SiCA
========================
1 - Enviar arquivo
2 - Listar arquivos
3 - Baixar arquivo
0 - Sair
========================
Escolha uma opcao:
```

Selecione a operação desejada.

Para enviar um arquivo, informe seu caminho.

Para baixar, informe o nome de um arquivo armazenado no servidor.

Para encerrar o cliente, escolha a opção 0.

Para encerrar o servidor, pressione Ctrl + C no terminal correspondente.

## 8. Testes realizados

Foram realizados testes para verificar o funcionamento das operações implementadas.

Os seguintes cenários foram testados com sucesso:

- Conexão TCP entre cliente e servidor.
- Envio de arquivo de texto.
- Listagem dos arquivos disponíveis.
- Download de arquivo de texto.
- Envio e download de uma imagem.
- Comparação dos hashes SHA256 dos arquivos original e baixado.
- Solicitação de download de um arquivo inexistente.

Os hashes SHA256 da imagem original e da imagem baixada apresentaram resultados iguais, confirmando a integridade do arquivo no teste realizado.

## 9. Considerações e limitações

O sistema foi desenvolvido para fins acadêmicos.

A implementação atual atende um cliente por vez, processando as conexões sequencialmente.

Arquivos enviados com nomes já existentes podem substituir os arquivos armazenados anteriormente.

O sistema não possui autenticação de usuários nem criptografia adicional da comunicação.

## 10. Conclusão

O desenvolvimento do SiCA permitiu aplicar conceitos de programação Java, comunicação cliente-servidor, sockets TCP e manipulação de arquivos.

A aplicação implementa as três funcionalidades principais propostas no exercício: envio, listagem e download de arquivos.

Os testes realizados demonstraram o funcionamento dessas operações no ambiente utilizado durante o desenvolvimento.