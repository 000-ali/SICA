import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    // Porta utilizada para receber as conexões dos clientes.
    private static final int PORTA = 5000;

    /**
     * Método principal do servidor.
     *
     * Cria um ServerSocket na porta 5000 e permanece aguardando
     * a conexão de um cliente.
     *
     * Quando um cliente se conecta, o servidor apresenta uma
     * mensagem no terminal e encerra a conexão.
     *
     * @param args argumentos recebidos pela linha de comando.
     */
    public static void main(String[] args) {

        System.out.println("Iniciando servidor SiCA...");

        // Cria o servidor e abre a porta TCP.
        try (ServerSocket servidor = new ServerSocket(PORTA)) {

            System.out.println("Servidor iniciado com sucesso!");
            System.out.println("Aguardando conexao na porta " + PORTA + "...");

            // Aguarda ate que um cliente tente se conectar.
            try (Socket cliente = servidor.accept()) {

                // Exibe o endereco IP do cliente conectado.
                System.out.println("Cliente conectado: "
                        + cliente.getInetAddress().getHostAddress());

                System.out.println("Conexao encerrada.");
            }

        } catch (IOException e) {

            // Exibe erros relacionados a rede ou a conexao.
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }
}