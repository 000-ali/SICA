
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.InvalidPathException;

import java.io.OutputStream;

import java.util.Scanner;

public class Cliente {

    // Endereco IP do servidor.
    private static final String HOST = "127.0.0.1";

    // Porta utilizada para estabelecer a conexao TCP.
    private static final int PORTA = 5000;

    /**
     * Metodo principal do cliente SiCA.
     *
     * Apresenta um menu interativo para que o usuario
     * escolha entre enviar um arquivo, listar os arquivos
     * disponiveis no servidor ou encerrar a aplicacao.
     *
     * O menu permanece ativo ate que a opcao 0 seja escolhida.
     */
    public static void main(String[] args) {

        // Scanner utilizado para ler as entradas do usuario.
        try (Scanner scanner = new Scanner(System.in)) {

            String opcao;

            do {

                // Exibe o menu principal.
                System.out.println("\n========================");
                System.out.println("       CLIENTE SiCA");
                System.out.println("========================");
                System.out.println("1 - Enviar arquivo");
                System.out.println("2 - Listar arquivos");
                System.out.println("3 - Baixar arquivo");
                System.out.println("0 - Sair");
                System.out.println("========================");

                System.out.print("Escolha uma opcao: ");

                opcao = scanner.nextLine();

                // Executa a operacao escolhida pelo usuario.
                switch (opcao) {

                    case "1":

                        System.out.print(
                                "Digite o caminho do arquivo: ");

                        String caminho = scanner.nextLine();

                        try {

                            Path arquivo = Path.of(caminho);

                            // Verifica se o arquivo existe.
                            if (Files.isRegularFile(arquivo)) {

                                enviarArquivo(arquivo);

                            } else {

                                System.out.println(
                                        "Arquivo nao encontrado!");
                            }

                        } catch (InvalidPathException e) {

                            System.out.println(
                                    "Caminho do arquivo invalido!");
                        }

                        break;

                    case "2":

                        // Solicita a lista de arquivos ao servidor.
                        listarArquivos();

                        break;

                    case "3":

                        System.out.print(
                                "Digite o nome do arquivo que deseja baixar: ");

                        String nomeArquivo = scanner.nextLine();

                        baixarArquivo(nomeArquivo);

                        break;

                    case "0":

                        System.out.println(
                                "Encerrando cliente SiCA...");

                        break;

                    default:

                        System.out.println(
                                "Opcao invalida! Tente novamente.");
                }

            } while (!opcao.equals("0"));
        }
    }

    /**
     * Envia um arquivo para o servidor utilizando sockets TCP.
     *
     * Primeiro estabelece uma conexao com o servidor.
     * Depois envia o comando ENVIAR, o nome do arquivo,
     * o tamanho e o conteudo em blocos de bytes.
     *
     * Ao finalizar, aguarda uma resposta do servidor.
     *
     * @param arquivo caminho do arquivo que sera enviado.
     */
    private static void enviarArquivo(Path arquivo) {

        try (Socket socket = new Socket(HOST, PORTA)) {

            // Canal utilizado para enviar dados.
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());

            // Canal utilizado para receber a resposta.
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // Obtem o nome do arquivo.
            String nomeArquivo = arquivo.getFileName().toString();

            // Obtem o tamanho do arquivo em bytes.
            long tamanho = Files.size(arquivo);

            // Informa ao servidor a operacao desejada.
            saida.writeUTF("ENVIAR");

            // Envia o nome do arquivo.
            saida.writeUTF(nomeArquivo);

            // Envia o tamanho do arquivo.
            saida.writeLong(tamanho);

            System.out.println(
                    "\nEnviando arquivo: " + nomeArquivo);

            // Abre o arquivo para realizar a leitura.
            try (InputStream arquivoEntrada = Files.newInputStream(arquivo)) {

                byte[] buffer = new byte[4096];

                int quantidade;

                // Le o arquivo em blocos e envia os bytes.
                while ((quantidade = arquivoEntrada.read(buffer)) != -1) {

                    saida.write(buffer, 0, quantidade);
                }
            }

            // Garante o envio dos dados pendentes.
            saida.flush();

            // Recebe a confirmacao do servidor.
            String resposta = entrada.readUTF();

            System.out.println("Servidor: " + resposta);

        } catch (IOException e) {

            System.out.println(
                    "Erro ao enviar arquivo: " + e.getMessage());
        }
    }

    /**
     * Solicita a listagem dos arquivos armazenados no servidor.
     *
     * Estabelece uma conexao TCP e envia o comando LISTAR.
     *
     * O servidor responde inicialmente com a quantidade
     * de arquivos e depois envia o nome de cada um.
     *
     * Os nomes recebidos sao apresentados no terminal.
     */
    private static void listarArquivos() {

        // Estabelece a conexao com o servidor.
        try (Socket socket = new Socket(HOST, PORTA)) {

            // Canal de envio de dados.
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());

            // Canal de recebimento de dados.
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // Solicita a listagem ao servidor.
            saida.writeUTF("LISTAR");

            saida.flush();

            // Recebe a quantidade de arquivos.
            int quantidade = entrada.readInt();

            System.out.println(
                    "\n===== ARQUIVOS DISPONIVEIS =====");

            // Verifica se existem arquivos no servidor.
            if (quantidade == 0) {

                System.out.println(
                        "Nenhum arquivo encontrado no servidor.");

            } else {

                // Recebe e apresenta os nomes dos arquivos.
                for (int i = 0; i < quantidade; i++) {

                    String nome = entrada.readUTF();

                    System.out.println(
                            (i + 1) + " - " + nome);
                }
            }

            System.out.println(
                    "===============================");

        } catch (IOException e) {

            System.out.println(
                    "Erro ao listar arquivos: " + e.getMessage());
        }
    }

    /**
     * Realiza o download de um arquivo armazenado no servidor.
     *
     * Primeiro estabelece uma conexao TCP e envia o comando
     * BAIXAR, seguido pelo nome do arquivo solicitado.
     *
     * O servidor informa se o arquivo esta disponivel.
     *
     * Se estiver disponivel, o cliente recebe seu tamanho
     * e os dados em blocos de bytes, salvando o conteudo
     * na pasta downloads.
     *
     * @param nomeArquivo nome do arquivo solicitado.
     */
    private static void baixarArquivo(String nomeArquivo) {

        // Pasta onde os arquivos recebidos serao armazenados.
        Path pastaDownloads = Path.of("downloads")
                .toAbsolutePath()
                .normalize();

        // Monta o caminho completo do arquivo de destino.
        Path destino = pastaDownloads.resolve(nomeArquivo).normalize();

        // Verifica se o nome informado e valido.
        if (nomeArquivo.isBlank()
                || !destino.getParent().equals(pastaDownloads)) {

            System.out.println("Nome de arquivo invalido!");
            return;
        }

        // Estabelece uma conexao TCP com o servidor.
        try (Socket socket = new Socket(HOST, PORTA)) {

            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());

            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // Informa ao servidor que deseja baixar um arquivo.
            saida.writeUTF("BAIXAR");

            // Envia o nome do arquivo solicitado.
            saida.writeUTF(nomeArquivo);

            saida.flush();

            // Recebe a informacao sobre a existencia do arquivo.
            boolean encontrado = entrada.readBoolean();

            // Se nao existir, recebe a mensagem de erro.
            if (!encontrado) {

                String mensagem = entrada.readUTF();

                System.out.println("Servidor: " + mensagem);

                return;
            }

            // Recebe o tamanho do arquivo em bytes.
            long tamanho = entrada.readLong();

            if (tamanho < 0) {
                throw new IOException("Tamanho de arquivo invalido.");
            }

            // Cria a pasta downloads caso nao exista.
            Files.createDirectories(pastaDownloads);

            System.out.println(
                    "Iniciando download: " + nomeArquivo);

            // Cria o arquivo e prepara a gravacao dos dados.
            try (OutputStream arquivoSaida = Files.newOutputStream(destino)) {

                byte[] buffer = new byte[4096];

                long restantes = tamanho;

                // Recebe os dados ate completar o tamanho informado.
                while (restantes > 0) {

                    int quantidade = entrada.read(
                            buffer,
                            0,
                            (int) Math.min(buffer.length, restantes));

                    // Verifica se a conexao foi interrompida.
                    if (quantidade == -1) {

                        throw new IOException(
                                "Download interrompido.");
                    }

                    // Grava os bytes recebidos no arquivo.
                    arquivoSaida.write(buffer, 0, quantidade);

                    // Atualiza a quantidade de bytes restantes.
                    restantes -= quantidade;
                }
            }

            System.out.println(
                    "Download concluido com sucesso!");

            System.out.println(
                    "Arquivo salvo em: " + destino);

        } catch (IOException e) {

            System.out.println(
                    "Erro ao baixar arquivo: " + e.getMessage());
        }
    }
}