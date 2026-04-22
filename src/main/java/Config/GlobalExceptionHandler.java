package Config;

import dto.ApiResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.fail(ex.getMessage(), List.of(ex.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.fail(ex.getMessage(), List.of(ex.getMessage())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleConflict(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (message == null || message.isBlank()) {
            message = "Conflit de donnees";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDTO.fail("Conflit de donnees", List.of(message)));
    }

    @ExceptionHandler(LotDeletionException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleLotDeletionException(LotDeletionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDTO.fail(ex.getMessage(), List.of(ex.getReason())));
    }

    @ExceptionHandler(org.hibernate.TransientPropertyValueException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleTransientPropertyValue(org.hibernate.TransientPropertyValueException ex) {
        String reason = "Ce lot est lié à d'autres données et ne peut pas être supprimé.";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDTO.fail("Impossible de supprimer ce lot", List.of(reason)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.fail("Validation echouee", errors));
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<Map<String, Object>> handlePermissionDenied(PermissionDeniedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Permission insuffisante");
        body.put("module", ex.getModule());
        body.put("action", ex.getAction());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Permission insuffisante");
        body.put("module", null);
        body.put("action", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDTO.fail(ex.getMessage(), List.of(ex.getMessage())));
    }
}


