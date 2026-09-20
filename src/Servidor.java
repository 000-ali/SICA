import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.InputStream;

public class Servidor {

    private static final int PORTA = 5000;

    private static final Path PASTA_ARQUIVOS = Path.of("arquivos_servidor").toAbsolutePath().normalize();

    /**
     * Inicia o servidor TCP na porta 5000.
     *
     * Cria a pasta de armazenamento e aguarda conexoes.
     * Cada cliente conectado e atendido individualmente.
     */
    public static void main(String[] args) {

        try {

            // Cria a pasta caso ela ainda nao exista.
            Files.createDirectories(PASTA_ARQUIVOS);

            try (ServerSocket servidor = new ServerSocket(PORTA)) {

                System.out.println("Servidor SiCA iniciado!");
                System.out.println("Aguardando conexoes na porta " + PORTA);

                // Mantem o servidor funcionando apos cada conexao.
                while (true) {

                    try (Socket cliente = servidor.accept()) {

                        System.out.println("Cliente conectado: "
                                + cliente.getInetAddress());

                        processarCliente(cliente);

                    } catch (IOException e) {

                        System.out.println("Erro ao atender cliente: "
                                + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {

            System.out.println("Erro no servidor: "
                    + e.getMessage());
        }
    }

    /**
     * Processa as solicitacoes recebidas do cliente.
     *
     * Identifica o comando enviado pela conexao TCP e
     * executa a operacao correspondente.
     *
     * ENVIAR: recebe e armazena um arquivo.
     * LISTAR: retorna os nomes dos arquivos disponiveis.
     *
     * @param cliente socket do cliente conectado.
     * @throws IOException caso ocorra um erro de comunicacao.
     */
    private static void processarCliente(Socket cliente)
            throws IOException {

        DataInputStream entrada = new DataInputStream(cliente.getInputStream());

        DataOutputStream saida = new DataOutputStream(cliente.getOutputStream());

        // Recebe o comando solicitado pelo cliente.
        String comando = entrada.readUTF();

        if (comando.equals("ENVIAR")) {

            receberArquivo(entrada, saida);

        } else if (comando.equals("LISTAR")) {

            listarArquivos(saida);

        } else if (comando.equals("BAIXAR")) {

            baixarArquivo(entrada, saida);

        } else {

            saida.writeUTF("ERRO: Comando desconhecido.");
            saida.flush();
        }
    }

    /**
     * Recebe um arquivo enviado pelo cliente.
     *
     * Primeiro recebe o nome e o tamanho do arquivo.
     * Em seguida, recebe seus dados em blocos de bytes
     * e grava o conteudo na pasta arquivos_servidor.
     *
     * Apos finalizar, envia uma confirmacao ao cliente.
     */
    private static void receberArquivo(
            DataInputStream entrada,
            DataOutputStream saida) throws IOException {

        // Recebe o nome e o tamanho do arquivo.
        String nomeArquivo = entrada.readUTF();

        long tamanho = entrada.readLong();

        // Impede que o nome recebido indique outro diretorio.
        Path destino = PASTA_ARQUIVOS.resolve(nomeArquivo).normalize();

        if (nomeArquivo.isBlank()
                || !destino.getParent().equals(PASTA_ARQUIVOS)
                || tamanho < 0) {

            saida.writeUTF("ERRO: Dados do arquivo invalidos.");
            saida.flush();
            return;
        }

        System.out.println("Recebendo arquivo: " + nomeArquivo);

        // Abre o arquivo que sera salvo no servidor.
        try (OutputStream arquivo = Files.newOutputStream(destino)) {

            byte[] buffer = new byte[4096];

            long restantes = tamanho;

            // Recebe somente a quantidade de bytes informada.
            while (restantes > 0) {

                int quantidade = entrada.read(
                        buffer,
                        0,
                        (int) Math.min(buffer.length, restantes));

                if (quantidade == -1) {

                    throw new IOException(
                            "Transferencia interrompida.");
                }

                arquivo.write(buffer, 0, quantidade);

                restantes -= quantidade;
            }
        }

        System.out.println("Arquivo recebido: " + nomeArquivo);

        // Confirma ao cliente que o arquivo foi salvo.
        saida.writeUTF("Arquivo enviado com sucesso!");
        saida.flush();
    }

    /**
     * Consulta os arquivos armazenados no servidor.
     *
     * Percorre a pasta arquivos_servidor, identifica
     * os arquivos regulares e organiza seus nomes.
     *
     * Primeiro envia ao cliente a quantidade de arquivos.
     * Depois envia o nome de cada arquivo pela conexao TCP.
     *
     * @param saida canal utilizado para enviar dados ao cliente.
     * @throws IOException caso ocorra um erro de leitura ou envio.
     */
    private static void listarArquivos(DataOutputStream saida)
            throws IOException {

        // Lista que armazenara os nomes dos arquivos.
        List<String> arquivos = new ArrayList<>();

        // Abre a pasta onde os arquivos estao armazenados.
        try (DirectoryStream<Path> diretorio = Files.newDirectoryStream(PASTA_ARQUIVOS)) {

            // Percorre todos os itens encontrados.
            for (Path arquivo : diretorio) {

                // Adiciona apenas arquivos, ignorando pastas.
                if (Files.isRegularFile(arquivo)) {

                    arquivos.add(
                            arquivo.getFileName().toString());
                }
            }
        }

        // Organiza os nomes em ordem alfabetica.
        Collections.sort(arquivos);

        // Envia a quantidade de arquivos encontrados.
        saida.writeInt(arquivos.size());

        // Envia cada nome individualmente.
        for (String nome : arquivos) {

            saida.writeUTF(nome);
        }

        // Garante que os dados sejam enviados.
        saida.flush();

        System.out.println("Listagem enviada ao cliente.");

    }

    /**
     * Envia um arquivo armazenado no servidor para o cliente.
     *
     * Primeiro recebe o nome do arquivo solicitado.
     * Depois verifica se o arquivo existe na pasta do servidor.
     *
     * Se o arquivo existir, envia uma confirmacao, seu tamanho
     * e o conteudo em blocos de bytes pela conexao TCP.
     *
     * Se nao existir, envia uma mensagem de erro ao cliente.
     *
     * @param entrada canal utilizado para receber dados do cliente.
     * @param saida   canal utilizado para enviar dados ao cliente.
     * @throws IOException caso ocorra um erro de comunicacao.
     */
    private static void baixarArquivo(
            DataInputStream entrada,
            DataOutputStream saida) throws IOException {

        // Recebe o nome do arquivo solicitado pelo cliente.
        String nomeArquivo = entrada.readUTF();

        // Monta o caminho completo do arquivo no servidor.
        Path arquivo = PASTA_ARQUIVOS.resolve(nomeArquivo).normalize();

        // Verifica se o nome indica um arquivo da pasta permitida.
        if (nomeArquivo.isBlank()
                || !arquivo.getParent().equals(PASTA_ARQUIVOS)
                || !Files.isRegularFile(arquivo)) {

            // Informa ao cliente que o arquivo nao foi encontrado.
            saida.writeBoolean(false);

            saida.writeUTF("Arquivo nao encontrado no servidor.");

            saida.flush();

            return;
        }

        // Abre o arquivo para realizar a leitura.
        try (InputStream arquivoEntrada = Files.newInputStream(arquivo)) {

            // Obtem o tamanho do arquivo em bytes.
            long tamanho = Files.size(arquivo);

            // Confirma que o arquivo esta disponivel.
            saida.writeBoolean(true);

            // Envia o tamanho do arquivo.
            saida.writeLong(tamanho);

            System.out.println("Enviando arquivo: " + nomeArquivo);

            // Buffer utilizado para transferir os dados em blocos.
            byte[] buffer = new byte[4096];

            long restantes = tamanho;

            // Continua enviando ate completar o tamanho informado.
            while (restantes > 0) {

                int quantidade = arquivoEntrada.read(
                        buffer,
                        0,
                        (int) Math.min(buffer.length, restantes));

                // Verifica se ocorreu uma interrupcao na leitura.
                if (quantidade == -1) {

                    throw new IOException(
                            "Arquivo interrompido durante a leitura.");
                }

                // Envia os bytes lidos ao cliente.
                saida.write(buffer, 0, quantidade);

                // Atualiza a quantidade de bytes restantes.
                restantes -= quantidade;
            }

            // Garante que os dados sejam enviados.
            saida.flush();

            System.out.println(
                    "Arquivo enviado ao cliente: " + nomeArquivo);
        }
    }
}
