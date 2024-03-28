import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            boolean var;
            do {
                var = UserRegister();
            }while(!var);
            String trovato;
            while(true) {
                String message = in.readLine();
                System.out.println("Messaggio dal client: " + message);
                if (message.equals("1")) {
                    shoppingMenu();
                    trovato = in.readLine();
                    if (trovato.equals("x")) {
                    }else{
                        String el = in.readLine();
                        String code = in.readLine();
                        int quantity = Integer.parseInt(in.readLine());
                        findArticle(code, quantity, el);
                        letturaRiscrittura(code, quantity);
                    }
                } else {
                    //carrello
                    String el = in.readLine();
                    boolean value = printCart(el);
                    if(value){
                        String opzioneCarrello = in.readLine();
                        if(opzioneCarrello.equals("buy")){
                            buyArticle(el);
                        }
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
    private boolean UserRegister() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String opz = in.readLine();
            if(opz.equals("1")) {
                String emailVerify = in.readLine();
                String passwordVerify = in.readLine();
                try {
                    //System.out.println(usersList.toString());
                    for (ClientHandler user : usersList) {
                        if (user.email.equals(emailVerify) && user.password.equals(passwordVerify)) {
                            out.println("Accesso");
                            return true;
                        }
                    }
                    out.println("ERRORE. E-mail o Password sbagliati.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }else {
                String name = in.readLine();
                String surname = in.readLine();
                String email = in.readLine();
                String password = in.readLine();
                ClientHandler newUser = new ClientHandler(name, surname, email, password);
                usersList.add(newUser);
                fillMap(newUser.email);
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    private synchronized static void fillMap (String mail){
        usersCart.putIfAbsent(mail, new ArrayList<>());
    }
    private void findArticle(String itemCode, int numberOfArticle, String el){
        for(Product p : data){
            if (p.getCode().equals(itemCode)){
                cartManagement(p, numberOfArticle, el);
                break;
            }
        }
    }
    private synchronized void cartManagement(Product p, int numberOfArticle, String el){
        p.setQuantity(numberOfArticle);
        if (usersCart.containsKey(el)) {
            usersCart.get(el).add(p);
        }
    }
    private synchronized void buyArticle(String el){
        if(usersCart.containsKey(el)){
            usersCart.get(el).clear();
        }
    }
    private void shoppingMenu(){
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
    private synchronized static void letturaRiscrittura(String itemCode, int numberOfArticle){
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
    private synchronized boolean printCart(String el){
        PrintWriter out;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        double sum = 0;
        if (usersCart.get(el).isEmpty()) {
            out.println("empty");
            out.println("La lista del carrello è vuota.");
            return false;
        }else{
            out.println("something");
            for (Map.Entry <String, ArrayList<Product>> entry : usersCart.entrySet()){
                if (entry.getKey().equals(el)) {
                    out.println("\nCARRELLO:");
                    for(Product elem : entry.getValue()){
                        out.println(elem.getCategory()+" "+elem.getBrand()+" - codice articolo: "+elem.getCode()+" - quantità: "+elem.getQuantity()+" - Prezzo Totale dell'articolo: "+(elem.getPrice()*elem.getQuantity()+"€"));
                        sum += elem.getPrice() * elem.getQuantity();
                    }
                }
            }
            String formattedValue = String.format("%.2f", sum);
            out.println("Prezzo TOTALE: " + formattedValue + "€");
            out.println("end");
            return true;
        }
    }
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
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