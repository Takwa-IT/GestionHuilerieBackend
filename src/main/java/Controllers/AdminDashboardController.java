package Controllers;

import Config.PermissionAction;
import Config.RequirePermission;
import Services.AdminDashboardService;
import dto.AdminDashboardDTO;
import dto.ApiResponseDTO;
import dto.HuilerieDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/huileries")
    @RequirePermission(module = "DASHBOARD", action = PermissionAction.READ)
    public ResponseEntity<ApiResponseDTO<List<HuilerieDTO>>> listHuileries() {
        return ResponseEntity.ok(ApiResponseDTO.ok(adminDashboardService.findHuileries(), "Liste des huileries"));
    }

    @GetMapping("/summary")
    @RequirePermission(module = "DASHBOARD", action = PermissionAction.READ)
    public ResponseEntity<ApiResponseDTO<AdminDashboardDTO>> summary(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long huilerieId
    ) {
        LocalDate from = parseDate(dateFrom);
        LocalDate to = parseDate(dateTo);
        return ResponseEntity.ok(ApiResponseDTO.ok(
                adminDashboardService.buildDashboard(from, to, huilerieId),
                "Tableau de bord administrateur"
        ));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }
}