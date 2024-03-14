import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private static ArrayList<ClientHandler> usersList = new ArrayList<>();
    private static ArrayList<Product> data = new ArrayList<>();
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;
    private String surname;
    private String email;
    private String password;

    public ClientHandler(Socket socket) {
        try{
            this.clientSocket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public ClientHandler(String name, String surname, String email, String password){
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
    }
    @Override
    public String toString() {
        return "ClientHandler{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
    //----------------------------------------------------------------------------------------------------------------------
    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            UserRegister();
            while(true) {
                out.println("Menù abbigliamento, inserisci il numero della categoria che desideri:");
                out.println("1-Shopping");
                out.println("2-Carrello");
                out.println("3-Exit");
                out.println("Opzione Numero: ");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Messaggio dal client: " + message);
                    // Invia una risposta di conferma al client
                    out.println("Ricevuto");
                    if (message.equals("1")) {
                        shoppingMenu();
                        if (in.readLine().equals("!")) {
                            break;
                        }
                    } else {
                        //carrello
                    }
                }
            }
        } catch (IOException e) {
            // Chiudi la connessione
            System.out.println("Connessione chiusa con il client: " + clientSocket.getInetAddress());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    public void UserRegister() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        boolean correct = false;
        String opz = in.readLine();
        switch (opz){
            case "1":
                String emailVerify = in.readLine();
                String passwordVerify = in.readLine();
                try {
                    //System.out.println(usersList.toString());
                    for (ClientHandler user : usersList) {
                        if (user.email.equals(emailVerify) && user.password.equals(passwordVerify)) {
                            correct = true;
                            out.println("accesso");
                            break;
                        }
                    }
                    if (!correct){
                        out.println("ERRORE. E-mail o Password sbagliati.");
                    }
                }catch (IllegalArgumentException e){
                    System.out.println(e.getMessage());
                }
                break;
            case "2":
                String name = in.readLine();
                String surname = in.readLine();
                String email = in.readLine();
                String password = in.readLine();
                ClientHandler newUser = new ClientHandler(name, surname, email, password);
                usersList.add(newUser);
                out.println("Registrazione avvenuta con successo");
                break;
        }
    }
    public void shoppingMenu() throws IOException {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new FileReader("src/Magazzino.txt"));
            String[] words;
            String line;
            Product product;
            while ((line = reader.readLine()) != null) {
                words = line.split("/");
                String category = words[0];
                String brand = words[1];
                double price = Double.parseDouble(words[2]);
                String code = words[3];
                int quantity = Integer.parseInt(words[4]);
                product = new Product(category, brand, price, code, quantity);
                data.add(product);
                for (String word : words) {
                    //System.out.print(padString(word, 20));
                    out.print(padString(word)); // Separate words with spaces

                }
                //System.out.println();
                out.println();  // Move to the next line
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
    private static String padString(String s) {
        int spacesToAdd = 20 - s.length();  //Calculation to add spaces
        StringBuilder paddedString = new StringBuilder(s);
        for (int i = 0; i < spacesToAdd; i++) {
            paddedString.append(" ");
        }
        return paddedString.toString();
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}