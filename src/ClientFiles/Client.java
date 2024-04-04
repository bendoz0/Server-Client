package ClientFiles;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private String name;
    private String surname;
    private String email;
    private String password;
    private static String optionLogIn;
    private static String optionMenu;
    private static String el;
    private static ArrayList<String> codes = new ArrayList<>();
    private static ArrayList<Integer> disponibility = new ArrayList<>();

    // -----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        Client client = new Client();

        client.connectToServer();   //create a connection with the server
        client.createReaderWriter();    //creating the variable to read and write
        client.recognition();   //function of start phases: access or registration
        if(optionLogIn.equals("2")) {
            out.println(optionLogIn);   //send the option and personal data to register the user
            client.sendMsg();
        }
        while(true){
            System.out.print("""
                                \nMenù abbigliamento, inserisci il numero della categoria che desideri:
                                1-Shopping
                                2-Carrello
                                3-Exit
                                Opzione Numero:
                                """);
            client.writeMessage(input);
            if(optionMenu.equals("1")) {
                client.readingStorage();    //used to read the storage
                //block the print for ten seconds, because the print of the article are more slowly
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                readingInputArticle();
            }else{
                cartManaging();
            }
        }
    }
    // -----------------------------------------------------------------------------------------------------------------
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 4444);
        } catch (IOException e) {
            System.err.println("SERVER OFFLINE");
        }
    }
    private void createReaderWriter() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("PROBLEM OF CONNECTION");
        }
    }

    private void recognition(){
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner input = new Scanner(System.in);
            while (true) {
                System.out.println("\nSelezionare l'opzione desiderata:\n1 - Accedi\n2 - Registrati");
                String opz = input.nextLine();
                Client.optionLogIn = opz;
                if (opz.equals("1")) {
                    System.out.print("Inserisci email: ");
                    String emailVerify = input.nextLine();
                    System.out.print("Inserisci password: ");
                    String passwordVerify = input.nextLine();
                    out.println(opz);
                    out.println(emailVerify);
                    out.println(passwordVerify);
                    String stato = in.readLine();
                    if (stato.equals("Accesso")) {
                        System.out.println(stato);
                        el = emailVerify;
                        break;
                    }
                    System.out.println(stato);
                } else if (opz.equals("2")) {
                    setName();
                    setSurname();
                    setEmail();
                    setPassword();
                    break;
                } else {
                    System.out.println("Opzione inesistente");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void setName() {
        Scanner input = new Scanner(System.in);
        boolean allOk = false;
        while (!allOk) {
            System.out.print("Nome: ");
            String n = input.nextLine();
            if (n.matches("[a-zA-Z]+")) {
                this.name = n;
                allOk = true;
            } else {
                System.out.println("Nome inserito non valido. Si prega di rinserire il dato.");
            }
        }
    }
    private void setSurname() {
        Scanner input = new Scanner(System.in);
        boolean allOk = false;
        while (!allOk) {
            System.out.print("Surname: ");
            String s = input.nextLine();
            if (s.matches("[a-zA-Z]+")) {
                this.surname = s;
                allOk = true;
            } else {
                System.out.println("Cognome inserito non valido. Si prega di rinserire il dato.");
            }
        }
    }
    private void setEmail() {
        Scanner input = new Scanner(System.in);
        boolean allOk = false;
        while (!allOk) {
            System.out.print("E-mail: ");
            String e = input.nextLine();
            EmailValidator validator = new EmailValidator();
            if (validator.validate(e)) {
                allOk = true;
                this.email = e;
                el = e;
            } else {
                System.out.println("email errata. Si prega di rinserire il dato.");
            }
        }
    }
    private void setPassword() {
        Scanner input = new Scanner(System.in);
        boolean allOk = false;
        while (!allOk) {
            System.out.print("Password (minimo 8 caratteri) : ");
            String p = input.nextLine();
            if (p.length() >= 8) {
                allOk = true;
                this.password = p;
            } else {
                System.out.println("Password debole (minimo 8 caratteri). Si prega di rinserire il dato.");
            }
        }
    }

    private void sendMsg() {
        out.println(name);
        out.println(surname);
        out.println(email);
        out.println(password);
    }
    private void readingStorage() {
        //when the method is called started a thread, the method read all the message which send the client
        new Thread(() -> {
            String response;
            String[] words;
            codes.clear();
            disponibility.clear();
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //read the response of the server
                try {
                    response = in.readLine();
                } catch (IOException e) {
                    System.out.println("ERRORE in lettura");
                    throw new RuntimeException(e);
                }
                if (response.equals("end")) break;
                words = response.split("\\s+");
                codes.add(words[3]);
                disponibility.add(Integer.parseInt(words[4]));
                System.out.println(response);
            }
        }).start();
    }
    private void writeMessage(Scanner input){
        // send a message to server
        while (true) {
            try {
                optionMenu = input.nextLine();
                if (optionMenu.equals("3")){
                    System.exit(0);
                }
                if (optionMenu.equals("1") || optionMenu.equals("2")) {
                    out.println(optionMenu);
                    break;
                } else {
                    System.out.println("L'opzione: '" + optionMenu + "' è inesistente. Riprova.");
                }
            } catch (Exception e) {
                System.err.println("Errore è: " + e.getMessage());
                // close the connection with the server
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private static void setNumberArticle(int position, String codeArticle){
        Scanner input = new Scanner(System.in);
        while (true) {
            int numberOfArticle;
            System.out.println("Quanti ne vuoi di questo articolo? ");
            String numFormat = input.next();
            try{
                numberOfArticle = Integer.parseInt(numFormat);
            }catch(NumberFormatException e){
                System.out.println("ERRORE: Inserire un numero");
                continue;
            }
            if (numberOfArticle > 0) {
                if (numberOfArticle <= disponibility.get(position)) {
                    out.println("v");   //to know at the server that are entered correct code and quantity article
                    out.println(el);
                    out.println(codeArticle);
                    out.println(numberOfArticle);
                    break;
                } else {
                    System.out.println("ERRORE: Numero articoli insufficenti");
                }
            } else {
                System.out.println("ERRORE: Inserire un numero maggiore di zero");
            }
        }
    }
    private static void readingInputArticle(){
        Scanner input = new Scanner(System.in);
        boolean find = false;
        int position = 0;
        System.out.print("Se desideri aggiungere al carrello qualche articolo inserisci il CODICE dell'articolo.\nAltrimenti inserisci un altro carattere\nRisposta: ");
        String codeArticle = input.nextLine();
        for (String code : codes) {
            if (code.equals(codeArticle)) {
                position = codes.indexOf(codeArticle);
                find = true;
                break;
            }
        }
        if (find) {
            setNumberArticle(position, codeArticle);
        } else {
            out.println("x");
        }
    }
    private static void cartManaging(){
        Scanner input = new Scanner(System.in);
        try {
            out.println(el);
            if(in.readLine().equals("empty")){
                String stato = in.readLine();
                System.out.println(stato);
            }else{
                while(true){
                    String text = in.readLine();
                    if(text.equals("end"))break;
                    System.out.println(text);
                }
                System.out.print("\nOpzioni:\t1 - Compra\t2 - GoBack\nOpzione Numero: ");
                String option = input.nextLine();
                if(option.equals("1")){
                    out.println("buy");
                    System.out.println("Grazie mille per aver acquistato su ZalandoCOPY. \nLa consegna del tuo ordine è prevista tra 5 giorni lavorativi a partire da oggi.");
                }else{
                    out.println("out");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}