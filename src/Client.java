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
    private static String opz;
    private static String opzione;
    private static String el;
    private static ArrayList<String> codici = new ArrayList<>();
    private static ArrayList<Integer> quantita = new ArrayList<>();

    // -----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        Client client = new Client();
        boolean trovato = false;
        int posizione = 0;
        client.connectToServer();
        client.createReaderWriter();
        try {
            client.recognition();
            if(!opz.equals("1")) {  //if it's different from one
                out.println(opz);
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
                if(opzione.equals("1")) {
                    client.readingStorage();    //serve per leggere il magazzino
                    /*blocco per 10sec questa stampa perchè la stampa del magazzino è più lenta
                    - InterruptedException per gestire eccezioni durante Thread.sleep
                    - le eccezioni aggiunte al main vanno gestite internamente e non lì*/
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.print("Se desideri aggiungere al carrello qualche articolo inserisci il CODICE dell'articolo.\nAltrimenti inserisci un altro carattere\nRisposta: ");
                    String codiceArticolo = input.nextLine();
                    for (String codice : codici) {
                        if (codice.equals(codiceArticolo)) {
                            posizione = codici.indexOf(codiceArticolo);
                            trovato = true;
                            break;
                        }
                    }
                    if (trovato) {
                        trovato = setNumberArticle(posizione, codiceArticolo);
                    } else {
                        out.println("x");
                    }
                }else{
                    out.println(el);
                    if(in.readLine().equals("empty")){
                        String stato = in.readLine();
                        System.out.println(stato+"\n");
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
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // -----------------------------------------------------------------------------------------------------------------
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 4444);
        } catch (IOException e) {
            System.err.println("Errore è: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void createReaderWriter() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void recognition(){
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner input = new Scanner(System.in);
            while (true) {
                System.out.println("\nSelezionare l'opzione desiderata:\n1 - Accedi\n2 - Registrati");
                String opz = input.nextLine();
                Client.opz = opz;
                if (opz.equals("1")) {
                    System.out.print("Inserisci email: ");
                    String emailVerify = input.nextLine();
                    System.out.print("Inserisci password: ");
                    String passwordVerify = input.nextLine();
                    out.println(opz);
                    out.println(emailVerify);
                    out.println(passwordVerify);
                    String message = "ERRORE. E-mail o Password sbagliati.";
                    String stato = in.readLine();
                    if (!stato.equals(message)) {
                        System.out.println(stato);
                        el = emailVerify;
                        break;
                    }
                    System.out.println(message);
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
                System.out.println("Password inserita troppo corta (minimo 8 caratteri). Si prega di rinserire il dato.");
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
        //quando inzia il metodo viene avviato un thread, vengono letti tutti i messaggi che invia il server appena non invia piu nulla brekka esce dal ciclo e quando ternima il metodo il thread viene distrutto
        new Thread(() -> {
            String response;
            String[] words;
            codici.clear();
            quantita.clear();
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // Leggi la risposta del server
                try {
                    response = in.readLine();
                } catch (IOException e) {
                    System.out.println("ERRORE in lettura");
                    throw new RuntimeException(e);
                }
                if (response.equals("end")) break;
                words = response.split("\\s+");
                codici.add(words[3]);
                quantita.add(Integer.parseInt(words[4]));
                System.out.println(response); //"Risposta dal server: " +
            }
        }).start();
    }
    private void writeMessage(Scanner input){
        // Invia un messaggio al server
        while (true) {
            try {
                opzione = input.nextLine();
                if (opzione.equals("3")){
                    System.exit(0);
                }
                if (opzione.equals("1") || opzione.equals("2")) {
                    out.println(opzione);
                    break;
                } else {
                    System.out.println("L'opzione: '" + opzione + "' è inesistente. Riprova.");
                }
            } catch (Exception e) {
                System.err.println("Errore è: " + e.getMessage());
                // Chiudi la connessione con il server
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private static boolean setNumberArticle(int posizione, String codiceArticolo){
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
                if (numberOfArticle <= quantita.get(posizione)) {
                    out.println("v");   //per far capire al server che è stato inserito un codice e num. art. corretto
                    out.println(el);
                    out.println(codiceArticolo);
                    out.println(numberOfArticle);
                    //input.nextLine();
                    return false;
                } else {
                    System.out.println("ERRORE: Numero articoli insufficenti");
                }
            } else {
                System.out.println("ERRORE: Inserire un numero maggiore di zero");
            }
        }
    }
}