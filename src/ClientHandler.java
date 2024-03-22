import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientHandler implements Runnable {
    private static ArrayList<ClientHandler> usersList = new ArrayList<>();
    private static HashMap<String, ArrayList<Product>> usersCart = new HashMap<>();
    private static ArrayList<Product> data = new ArrayList<>();
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;
    private String surname;
    private String email;
    private String password;
    private static String emailDiAccesso;

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
            String trovato = "";
            while(true) {
                String message = in.readLine();
                System.out.println("Messaggio dal client: " + message);
                if (message.equals("1")) {
                    shoppingMenu();
                    trovato = in.readLine();
                    if (trovato.equals("x")) {
                    }else{
                        String code = in.readLine();
                        int quantity = Integer.parseInt(in.readLine());
                        findArticle(code, quantity);
                        letturaRiscrittura(code, quantity);
                    }
                } else {
                    //carrello
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
                            emailDiAccesso = emailVerify;
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
                emailDiAccesso = email;
                ClientHandler newUser = new ClientHandler(name, surname, email, password);
                usersList.add(newUser);
                System.out.println(usersList);
                fillMap(emailDiAccesso);
                //out.println("Registrazione avvenuta con successo");
                break;
        }
    }
    public static void fillMap (String mail){
        usersCart.putIfAbsent(mail, new ArrayList<Product>());
    }
    public static void findArticle(String itemCode, int numberOfArticle){
        for(Product p : data){
            if (p.getCode().equals(itemCode)){
                ClientHandler.cartManagement(p, numberOfArticle);
                break;
            }
        }
    }
    public static void cartManagement(Product p, int numberOfArticle){
        p.setQuantity(numberOfArticle);
        if (usersCart.containsKey(emailDiAccesso)) {
            usersCart.get(emailDiAccesso).add(p);
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
                    out.print(padString(word)); // Separate words with spaces
                }
                out.println();  // Move to the next line
            }
            out.println("end");
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
    private static String padString(String s) {
        int spacesToAdd = 20 - s.length();  //Calculation to add spaces
        for (int i = 0; i < spacesToAdd; i++) {
            s += " ";
        }
        return s;
    }
    public static void letturaRiscrittura(String itemCode, int numberOfArticle){
        try {
            // Legge il file e memorizza le lines in una lista
            List<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader("src/Magazzino.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            // Trova l'indice della line
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains(itemCode)) {
                    // Estrai il valore quantita dalla line
                    String[] value = lines.get(i).split("/");
                    value[4] = String.valueOf(Integer.parseInt(value[4]) - numberOfArticle);

                    // Aggiorna la line con la nuova quantita
                    lines.set(i, String.join("/", value));
                    break;
                }
            }
            // Riscrivi il file con la modifica
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/Magazzino.txt"));
            for (String changedLine : lines) {
                writer.write(changedLine);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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