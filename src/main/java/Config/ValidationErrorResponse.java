package Config;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * Réponse standard pour les erreurs de validation
 * Utilisée par les endpoints pour retourner les détails des erreurs
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {

    private String status;
    private String message;
    private List<String> errors;
    private Map<String, Object> details;
    private long timestamp;
    private String path;

    // ═══════════════════════════════════════════════════════════════
    // CONSTRUCTEURS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Constructeur minimal: juste la liste d'erreurs
     */
    public ValidationErrorResponse(List<String> errors) {
        this.status = "VALIDATION_ERROR";
        this.message = "Erreurs de validation détectées";
        this.errors = errors;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructeur avec message personnalisé
     */
    public ValidationErrorResponse(String message, List<String> errors) {
        this.status = "VALIDATION_ERROR";
        this.message = message;
        this.errors = errors;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructeur complet
     */
    public ValidationErrorResponse(String status, String message, List<String> errors, String path) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.timestamp = System.currentTimeMillis();
        this.path = path;
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS ET SETTERS
    // ═══════════════════════════════════════════════════════════════

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Retourne le nombre d'erreurs
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    /**
     * Ajoute un détail supplémentaire (ex: champ invalide, valeur reçue, etc.)
     */
    public void addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new java.util.HashMap<>();
        }
        this.details.put(key, value);
    }

    @Override
    public String toString() {
        return String.format(
                "ValidationErrorResponse{status='%s', message='%s', errorCount=%d, timestamp=%d}",
                status, message, getErrorCount(), timestamp
        );
    }
}
