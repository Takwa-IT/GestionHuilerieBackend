package Controllers;

import Config.PermissionAction;
import Config.RequirePermission;
import Services.ProductionDashboardService;
import dto.ProductionDashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard/production")
@RequiredArgsConstructor
public class ProductionDashboardController {

    private final ProductionDashboardService productionDashboardService;

    @GetMapping("/summary")
    @RequirePermission(module = "DASHBOARD", action = PermissionAction.READ)
    public ResponseEntity<ProductionDashboardDTO> getSummary(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Long huilerieId
    ) {
        LocalDate from = parseDate(dateFrom);
        LocalDate to = parseDate(dateTo);
        return ResponseEntity.ok(productionDashboardService.buildDashboard(from, to, huilerieId));
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
