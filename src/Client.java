import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client {
    private static Socket socket;
    private static PrintWriter out;
    private BufferedReader in;
    private String name;
    private String surname;
    private String email;
    private String password;
    private static String opz;

    public void recognition() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("Selezionare l'opzione desiderata:\n1 - Accedi\n2 - Registrati");
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
    }
    public void setName() {
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
    public void setSurname() {
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
    public void setEmail() {
        Scanner input = new Scanner(System.in);
        boolean allOk = false;
        while (!allOk) {
            System.out.print("E-mail: ");
            String e = input.nextLine();
            EmailValidator validator = new EmailValidator();
            if (validator.validate(e)) {
                allOk = true;
                this.email = e;
            } else {
                System.out.println("email errata. Si prega di rinserire il dato.");
            }
        }
    }
    public void setPassword() {
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

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 4444);
        } catch (IOException e) {
            System.err.println("Errore è: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void createReaderWriter() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    public void sendMsg() {
        out.println(opz);
        out.println(name);
        out.println(surname);
        out.println(email);
        out.println(password);
    }
    private void serverResponse() {
        //quando inzia il metodo viene avviato un thread, vengono letti tutti i messaggi che invia il server appena non invia piu nulla brekka esce dal ciclo e quando ternima il metodo il thread viene distrutto
        new Thread(() -> {
            String response;
            while (true) {
                try {
                    if ((response = in.readLine()) == null) break;
                } catch (IOException e) {
                    System.out.println("ERRORE in lettura");
                    throw new RuntimeException(e);
                }
                // Leggi la risposta del server
                System.out.println(response); //"Risposta dal server: " +
            }
        }).start();
    }
    private void writeMessage(Scanner input) throws IOException {
        // Invia un messaggio al server
        while (true) {
            try {
                String opzione = input.nextLine();
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
                socket.close();
            }
        }
    }
    // -----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner input = new Scanner(System.in);
        Client client = new Client();
        client.connectToServer();
        client.createReaderWriter();
        client.recognition();
        if(!opz.equals("1")) {  //if it's different from one
            client.sendMsg();
        }
        client.serverResponse();    //serve per leggere il menù
        client.writeMessage(input);
        client.serverResponse();    //serve per leggere il magazzino
        /*blocco per 1sec perchè la stampa del magazzino è troppo lenta e mi esegue subito la stampa
        - InterruptedException per gestire eccezioni durante Thread.sleep*/
        Thread.sleep(1000);
        System.out.println("Se desideri aggiungere al carrello qualche articolo inserisci il CODICE dell'articolo.\nAltrimenti inserisci '!'\nRisposta: ");
        String codiceArticolo = input.nextLine();
        //se viene inserito "!" mettere un ciclo che continua a leggere e rinviare
        out.println(codiceArticolo);
        //se si inserisce '!' per non inserire nulla nel carrello devo mette1re in loop nel server che se legge ! mi richiede il menù
        /*Gestire da client l'inserimento del codice dell'articolo e la quantità e poi inviarlo al server
        - Il server poi deve vedere se il codice inserito e la quantità richiesta sono disponibili.
        - Se si li aggiunge al carrello, in caso negativo ritorna al client un errore e viene richiesto l'inserimento. */
    }
    // -----------------------------------------------------------------------------------------------------------------
}