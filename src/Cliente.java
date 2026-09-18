import java.io.IOException;
import java.net.Socket;

public class Cliente {

    // Endereco IP ou nome do computador onde o servidor esta.
    private static final String HOST = "127.0.0.1";

    // Porta TCP utilizada pelo servidor.
    private static final int PORTA = 5000;

    /**
     * Metodo principal do cliente.
     *
     * Estabelece uma conexao TCP com o servidor por meio
     * de um Socket, utilizando o endereco IP e a porta.
     *
     * Quando a conexao e estabelecida, apresenta uma
     * mensagem de sucesso e encerra a conexao.
     *
     * @param args argumentos da linha de comando.
     */
    public static void main(String[] args) {

        System.out.println("Iniciando cliente SiCA...");

        // Tenta estabelecer uma conexao TCP com o servidor.
        try (Socket socket = new Socket(HOST, PORTA)) {

            System.out.println("Conectado ao servidor com sucesso!");

            System.out.println("Endereco do servidor: " + HOST);
            System.out.println("Porta utilizada: " + PORTA);

        } catch (IOException e) {

            // Trata erros de conexao, como servidor desligado.
            System.out.println("Erro ao conectar ao servidor: "
                    + e.getMessage());
        }
    }
}