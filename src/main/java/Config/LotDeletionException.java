package Config;

/**
 * Exception levée quand un lot ne peut pas être supprimé à cause de dépendances
 */
public class LotDeletionException extends RuntimeException {

    private final String reason;

    public LotDeletionException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
