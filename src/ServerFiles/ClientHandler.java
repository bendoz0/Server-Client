package ServerFiles;
import StorageFiles.Product;
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
        return "ServerFiles.ClientHandler{" +
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
                var = UserRegister();   //function to manage the access or register of users
                //control if the clientSocket is already connected
                if(clientSocket.isClosed()){
                    var= true;
                }
            }while(!var);
            String found;
            while(true) {
                String message = in.readLine(); //reading the option menù
                System.out.println("Messaggio dal client: " + message);
                if (message.equals("1")) {
                    shoppingMenu(); //function to send the storage
                    found = in.readLine();  //reading a variable of client
                    if (found.equals("x")) {    //means that the client want to return to the menù
                    }else{
                        String el = in.readLine();      //reading at which email add the product
                        String code = in.readLine();    //reading the code of the article
                        int quantity = Integer.parseInt(in.readLine());     //reading number of this article
                        findArticle(code, quantity, el);    //function to find the article from storage
                        letturaRiscrittura(code, quantity); //update the storage reducing the quantity of the article selected
                    }
                } else {
                    //cart
                    String el = in.readLine();
                    boolean value = printCart(el);  //function that print the cart of the client
                    if(value){
                        String opzioneCarrello = in.readLine(); //reading if the client want to buy or not the cart
                        if(opzioneCarrello.equals("buy")){
                            buyArticle(el); //function to reset the cart
                        }
                    }
                }
            }
        } catch (IOException e) {
            try {
                //Connection closed
                System.out.println("Connessione chiusa con il client: " + clientSocket.getInetAddress());
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
                    for (ClientHandler user : usersList) {
                        if (user.email.equals(emailVerify) && user.password.equals(passwordVerify)) {
                            out.println("Accesso");
                            return true;
                        }
                    }
                    out.println("ERRORE. E-mail o Password sbagliati.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Connessione chiusa");
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
            try {
                //Connection closed
                clientSocket.close();
            } catch (IOException ex) {
                System.out.println("Client sconnesso.");
            }
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
            BufferedReader reader = new BufferedReader(new FileReader("src/StorageFiles/Magazzino.txt"));
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
            // read the file and save the lines in a list
            List<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader("src/StorageFiles/Magazzino.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            // find the index of the line
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains(itemCode)) {
                    // extract the value of quantity from line
                    String[] value = lines.get(i).split("/");
                    value[4] = String.valueOf(Integer.parseInt(value[4]) - numberOfArticle);

                    // update the line with the new quantity
                    lines.set(i, String.join("/", value));
                    break;
                }
            }
            // rewrote the file with they modify
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/StorageFiles/Magazzino.txt"));
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