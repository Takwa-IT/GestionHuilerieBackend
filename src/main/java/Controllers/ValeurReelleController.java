package Controllers;

import Services.ValeurReelleService;
import dto.SaveValeursReellesRequest;
import dto.ValeurReelleParametreDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/execution-productions")
public class ValeurReelleController {

    @Autowired
    private ValeurReelleService valeurReelleService;

    /**
     * Sauvegarde structurée des valeurs réelles d'une exécution (payload objet)
     * POST /api/execution-productions/{executionId}/valeurs-reelles/structured
     */
    @PostMapping("/{executionId}/valeurs-reelles/structured")
    public ResponseEntity<?> saveValeursReelles(
            @PathVariable Long executionId,
            @Valid @RequestBody SaveValeursReellesRequest request) {
        try {
            List<ValeurReelleParametreDTO> result = valeurReelleService.saveValeursReelles(
                    executionId,
                    request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "✅ Valeurs réelles enregistrées avec succès");
            response.put("count", result.size());
            response.put("data", result);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "❌ Erreur: " + e.getMessage());
            error.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Récupère les valeurs réelles via le module structuré
     * GET /api/execution-productions/{executionId}/valeurs-reelles/structured
     */
    @GetMapping("/{executionId}/valeurs-reelles/structured")
    public ResponseEntity<?> getValeursReelles(@PathVariable Long executionId) {
        try {
            List<ValeurReelleParametreDTO> result = valeurReelleService.getValeursReelles(executionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", result.size());
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "❌ Erreur: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Exporte les valeurs réelles pour réentraînement
     * GET
     * /api/execution-productions/valeurs-reelles/export-retrain?depuis=...&jusqu=...
     */
    @GetMapping("/valeurs-reelles/export-retrain")
    public ResponseEntity<?> exportForRetraining(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime depuis,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime jusqu) {
        try {
            if (depuis == null) {
                depuis = LocalDateTime.now().minusMonths(1);
            }
            if (jusqu == null) {
                jusqu = LocalDateTime.now();
            }

            List<ValeurReelleParametreDTO> result = valeurReelleService.getValeursForRetraining(depuis, jusqu);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("depuis", depuis);
            response.put("jusqu", jusqu);
            response.put("count", result.size());
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "❌ Erreur export: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Récupère les paramètres principaux
     * GET /api/execution-productions/valeurs-reelles/main-parameters?depuis=...
     */
    @GetMapping("/valeurs-reelles/main-parameters")
    public ResponseEntity<?> getMainParameters(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime depuis) {
        try {
            if (depuis == null) {
                depuis = LocalDateTime.now().minusMonths(3);
            }

            List<ValeurReelleParametreDTO> result = valeurReelleService.getMainParameters(depuis);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("depuis", depuis);
            response.put("parametres_principaux", result.size());
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "❌ Erreur: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
