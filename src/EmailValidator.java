import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convalida gli indirizzi e-mail utilizzando un modello di espressione regolare.
 * Il modello verifica la presenza di indirizzi e-mail sintatticamente validi.
 * @author Jacopo Bendotti
 */
public class EmailValidator {
    private final Pattern pattern;

    private static final String EMAIL_PATTERN =             //costante che definisce un espressione regolare
            "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * Costruisce un'istanza di Classi.UserManagement.EmailValidator.
     * Il costruttore crea un oggetto pattern da confrontare con gli indirizzi e-mail di input.
     */
    public EmailValidator() {   //costruttore usato per creare un oggetto patter da comparare con la email da input
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    /**
     * Convalida se l'indirizzo e-mail specificato è sintatticamente valido.
     * @param hex L'indirizzo e-mail da convalidare.
     * @return {@code true} se l'indirizzo email è valido, {@code false} in caso contrario.
     */
    public boolean validate(final String hex) {
        Matcher matcher = pattern.matcher(hex);
        return matcher.matches();
    }
}
