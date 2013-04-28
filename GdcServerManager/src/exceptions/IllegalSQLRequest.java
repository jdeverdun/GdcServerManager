package exceptions;

/**
 * Exception pour gerer les requetes illegales dans
 * le request panel (ex : usage d'autre chose que select dans la
 * commande executeSelect etc)
 * @author Mobilette
 *
 */
public class IllegalSQLRequest extends Exception {
    public IllegalSQLRequest(String message) {
        super(message);
    }
}
