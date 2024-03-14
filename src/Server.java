import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(4444);
            System.out.println("Server avviato. In attesa di client...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connesso: " + clientSocket.getInetAddress());
                // Avvia un thread separato per gestire la connessione con il client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}