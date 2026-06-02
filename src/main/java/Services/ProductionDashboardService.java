package Services;

import Models.AnalyseLaboratoire;
import Models.ExecutionProduction;
import Models.Prediction;
import Models.LotOlives;
import Models.Machine;
import Models.StockMovement;
import Models.TypeMouvement;
import Models.Utilisateur;
import Repositories.AnalyseLaboratoireRepository;
import Repositories.ExecutionProductionRepository;
import Repositories.LotOlivesRepository;
import Repositories.MachineRepository;
import Repositories.StockMovementRepository;
import dto.ProductionDashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionDashboardService {

    private static final String MODULE_DASHBOARD = "DASHBOARD";
    private static final String MODULE_GUIDE_PRODUCTION = "GUIDE_PRODUCTION";
    private static final String MODULE_RECEPTION = "RECEPTION";
    private static final String MODULE_LOTS = "LOTS_TRAÇABILITE";
    private static final String MODULE_MACHINES = "MACHINES";
    private static final String MODULE_STOCK = "STOCK";
    private static final String MODULE_STOCK_MOUVEMENT = "STOCK_MOUVEMENT";

    private final CurrentUserService currentUserService;
    private final PermissionService permissionService;
    private final ExecutionProductionRepository executionProductionRepository;
    private final LotOlivesRepository lotOlivesRepository;
    private final StockMovementRepository stockMovementRepository;
    private final MachineRepository machineRepository;
    private final AnalyseLaboratoireRepository analyseLaboratoireRepository;

    public ProductionDashboardDTO buildDashboard(LocalDate dateFrom, LocalDate dateTo) {
        return buildDashboard(dateFrom, dateTo, null);
    }

    public ProductionDashboardDTO buildDashboard(LocalDate dateFrom, LocalDate dateTo, Long huilerieId) {
        LocalDate effectiveTo = dateTo != null ? dateTo : LocalDate.now();
        LocalDate effectiveFrom = dateFrom != null ? dateFrom : effectiveTo.minusDays(30);

        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        Long utilisateurId = utilisateur.getIdUtilisateur();
        List<Long> huilerieIds = resolveHuilerieIds(huilerieId);

        if (huilerieIds.isEmpty()) {
            throw new IllegalStateException("Aucune huilerie accessible pour l'utilisateur courant");
        }

        List<ExecutionProduction> executions = loadExecutions(huilerieIds);
        List<LotOlives> lots = loadLots(huilerieIds);
        List<StockMovement> movements = loadMovements(huilerieIds);
        List<Machine> machines = loadMachines(huilerieIds);
        List<AnalyseLaboratoire> analyses = loadAnalyses(huilerieIds);

        ProductionDashboardDTO dto = new ProductionDashboardDTO();

        if (canRead(utilisateur, utilisateurId, Set.of(MODULE_DASHBOARD))
                && canRead(utilisateur, utilisateurId, Set.of(MODULE_GUIDE_PRODUCTION))) {
            dto.setGlobalIndicators(buildGlobalIndicators(executions, effectiveFrom, effectiveTo));
        }

        if (canRead(utilisateur, utilisateurId, Set.of(MODULE_RECEPTION, MODULE_LOTS))) {
            dto.setReceptionLots(buildReceptionLots(lots, effectiveFrom, effectiveTo));
        }

        if (canRead(utilisateur, utilisateurId, Set.of(MODULE_GUIDE_PRODUCTION))) {
            dto.setProductionProcess(buildProductionProcess(executions, effectiveFrom, effectiveTo));
        }

        if (canRead(utilisateur, utilisateurId, Set.of(MODULE_MACHINES))) {
            dto.setMachines(buildMachines(executions, machines, effectiveFrom, effectiveTo));
        }

        if (canRead(utilisateur, utilisateurId, Set.of(MODULE_GUIDE_PRODUCTION))) {
            dto.setQuality(buildQuality(executions, analyses));
        }

        if (canRead(utilisateur, utilisateurId, Set.of(MODULE_STOCK, MODULE_STOCK_MOUVEMENT))) {
            dto.setStockMovements(buildStockMovements(movements, effectiveFrom, effectiveTo));
        }

        return dto;
    }

    private List<Long> resolveHuilerieIds(Long huilerieId) {
        List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();
        if (huilerieId == null) {
            return accessibleHuilerieIds;
        }

        currentUserService.ensureCanAccessHuilerie(huilerieId);
        if (!accessibleHuilerieIds.contains(huilerieId)) {
            throw new IllegalStateException("Aucune huilerie accessible pour l'utilisateur courant");
        }

        return List.of(huilerieId);
    }

    private boolean canRead(Utilisateur utilisateur, Long utilisateurId, Set<String> modules) {
        if (currentUserService.isAdmin(utilisateur)) {
            return true;
        }

        return modules.stream().anyMatch(module -> permissionService.hasPermission(utilisateurId, module, "READ"));
    }

    private List<ExecutionProduction> loadExecutions(List<Long> huilerieIds) {
        return huilerieIds.stream()
                .flatMap(huilerieId -> executionProductionRepository.findAllByHuilerieId(huilerieId).stream())
                .toList();
    }

    private List<LotOlives> loadLots(List<Long> huilerieIds) {
        return huilerieIds.stream()
                .flatMap(huilerieId -> lotOlivesRepository.findAllByHuilerieId(huilerieId).stream())
                .toList();
    }

    private List<StockMovement> loadMovements(List<Long> huilerieIds) {
        return huilerieIds.stream()
                .flatMap(huilerieId -> stockMovementRepository
                        .findByStock_LotOlives_Huilerie_IdHuilerieOrderByDateMouvementDesc(huilerieId)
                        .stream())
                .toList();
    }

    private List<Machine> loadMachines(List<Long> huilerieIds) {
        return huilerieIds.stream()
                .flatMap(huilerieId -> machineRepository.findByHuilerie_IdHuilerie(huilerieId).stream())
                .toList();
    }

    private List<AnalyseLaboratoire> loadAnalyses(List<Long> huilerieIds) {
        return huilerieIds.stream()
                .flatMap(huilerieId -> analyseLaboratoireRepository.findByLot_Huilerie_IdHuilerie(huilerieId).stream())
                .toList();
    }

    private ProductionDashboardDTO.GlobalIndicatorsDTO buildGlobalIndicators(
            List<ExecutionProduction> executions,
            LocalDate from,
            LocalDate to) {
        ProductionDashboardDTO.GlobalIndicatorsDTO dto = new ProductionDashboardDTO.GlobalIndicatorsDTO();

        LocalDate today = LocalDate.now();

        long enCours = executions.stream()
                .filter(execution -> isOngoingStatus(execution.getStatut()))
                .count();

        long termineesAujourdhui = executions.stream()
                .filter(execution -> isFinishedStatus(execution.getStatut()))
                .filter(execution -> isSameDay(execution.getDateFinPrevue(), today))
                .count();

        List<Double> rendements = executions.stream()
                .map(ExecutionProduction::getProduitFinal)
                .filter(Objects::nonNull)
                .map(produit -> produit.getRendement())
                .filter(Objects::nonNull)
                .toList();

        double rendementMoyen = rendements.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double quantiteProduitePeriode = executions.stream()
                .filter(execution -> isWithinPeriod(execution.getDateFinPrevue(), execution.getDateDebut(), from, to))
                .map(ExecutionProduction::getProduitFinal)
                .filter(Objects::nonNull)
                .map(produit -> produit.getQuantiteProduite())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        double quantiteProduiteAujourdhui = executions.stream()
                .filter(execution -> isSameDay(execution.getDateFinPrevue(), today))
                .map(ExecutionProduction::getProduitFinal)
                .filter(Objects::nonNull)
                .map(produit -> produit.getQuantiteProduite())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        double rendementMoyenAujourdhui = executions.stream()
                .filter(execution -> isSameDay(execution.getDateFinPrevue(), today))
                .map(ExecutionProduction::getProduitFinal)
                .filter(Objects::nonNull)
                .map(produit -> produit.getRendement())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(rendementMoyen);

        dto.setExecutionsEnCours(enCours);
        dto.setExecutionsTermineesAujourdhui(termineesAujourdhui);
        dto.setRendementMoyenReel(round2(rendementMoyen));
        dto.setRendementMoyenAujourdhui(round2(rendementMoyenAujourdhui));
        dto.setQuantiteProduitePeriode(round2(quantiteProduitePeriode));
        dto.setQuantiteProduiteAujourdhui(round2(quantiteProduiteAujourdhui));

        // Calcul des rendements moyens par jour pour la période demandée
        Map<LocalDate, List<Double>> perDay = new HashMap<>();
        for (ExecutionProduction execution : executions) {
            Double r = Optional.ofNullable(execution.getProduitFinal()).map(p -> p.getRendement()).orElse(null);
            if (r == null)
                continue;

            Optional<LocalDateTime> dt = parseDateTime(execution.getDateFinPrevue());
            if (dt.isEmpty())
                continue;
            LocalDate d = dt.get().toLocalDate();
            if (d.isBefore(from) || d.isAfter(to))
                continue;

            perDay.computeIfAbsent(d, k -> new ArrayList<>()).add(r);
        }

        List<ProductionDashboardDTO.DailyYieldDTO> daily = new ArrayList<>();
        LocalDate cur = from;
        while (!cur.isAfter(to)) {
            List<Double> vals = perDay.getOrDefault(cur, List.of());
            double avg = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            ProductionDashboardDTO.DailyYieldDTO dy = new ProductionDashboardDTO.DailyYieldDTO();
            dy.setDate(cur.toString());
            dy.setRendement(round2(avg));
            daily.add(dy);

            cur = cur.plusDays(1);
        }

        dto.setDailyRendements(daily);
        return dto;
    }

    private ProductionDashboardDTO.ReceptionLotsDTO buildReceptionLots(List<LotOlives> lots, LocalDate from, LocalDate to) {
        ProductionDashboardDTO.ReceptionLotsDTO dto = new ProductionDashboardDTO.ReceptionLotsDTO();
        // NOTE (FR): Par demande — changer le KPI pour n'afficher que les lots
        // dont la date d'enregistrement (`dateReception`) est la date du jour.
        // Les paramètres `from` / `to` sont ignorés pour ces indicateurs.

        LocalDate today = LocalDate.now();

        double matiereRecueAujourdhui = lots.stream()
                .filter(lot -> isSameDay(lot.getDateReception(), today))
                .map(LotOlives::getQuantiteInitiale)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        long lotsRecusAujourdhui = lots.stream()
                .filter(lot -> isSameDay(lot.getDateReception(), today))
                .count();

        double stockUtilisable = matiereRecueAujourdhui;

        dto.setMatiereRecueAujourdhui(round2(matiereRecueAujourdhui));
        dto.setLotsRecusAujourdhui(lotsRecusAujourdhui);
        dto.setStockUtilisable(round2(stockUtilisable));
        return dto;
    }

    private ProductionDashboardDTO.ProductionProcessDTO buildProductionProcess(List<ExecutionProduction> executions,
                                                                               LocalDate from,
                                                                               LocalDate to) {
        ProductionDashboardDTO.ProductionProcessDTO dto = new ProductionDashboardDTO.ProductionProcessDTO();

        List<Double> ecarts = new ArrayList<>();
        for (ExecutionProduction execution : executions) {
            if (execution.getProduitFinal() == null || execution.getProduitFinal().getQuantiteProduite() == null) {
                continue;
            }

            Double predicted = getLatestPredictedQuantity(execution);
            if (predicted == null || predicted <= 0) {
                continue;
            }

            double diffPercent = ((execution.getProduitFinal().getQuantiteProduite() - predicted) / predicted) * 100.0;
            ecarts.add(diffPercent);
        }

        double ecartMoyen = ecarts.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        dto.setEcartReelVsPreditMoyen(round2(ecartMoyen));

        dto.setExtractionHoraire(buildExtractionHoraire(executions, from, to));
        dto.setTopOperations(buildTopOperations(executions));
        return dto;
    }

    private List<ProductionDashboardDTO.HourlyExtractionDTO> buildExtractionHoraire(
            List<ExecutionProduction> executions, LocalDate from, LocalDate to) {
        Map<Integer, Double> hourToQuantity = new HashMap<>();

        // Aggregate produced quantity (litres) by hour over the requested range.
        executions.stream()
                .filter(execution -> isWithinPeriod(execution.getDateFinPrevue(), execution.getDateDebut(), from, to))
                .forEach(execution -> {
                    Optional<LocalDateTime> date = parseDateTime(execution.getDateFinPrevue());
                    if (date.isEmpty()) {
                        date = parseDateTime(execution.getDateDebut());
                    }
                    if (date.isEmpty()) {
                        return;
                    }

                    double quantity = Optional.ofNullable(execution.getProduitFinal())
                            .map(produit -> produit.getQuantiteProduite())
                            .orElse(0.0);

                    int hour = date.get().getHour();
                    hourToQuantity.merge(hour, quantity, Double::sum);
                });

        List<ProductionDashboardDTO.HourlyExtractionDTO> result = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            ProductionDashboardDTO.HourlyExtractionDTO point = new ProductionDashboardDTO.HourlyExtractionDTO();
            point.setHeure(String.format(Locale.ROOT, "%02dh", hour));
            // Per-hour sum: each bar is the total produced during that hour only.
            point.setQuantite(round2(hourToQuantity.getOrDefault(hour, 0.0)));
            result.add(point);
        }

        return result;
    }

    private List<ProductionDashboardDTO.OperationStatusDTO> buildTopOperations(List<ExecutionProduction> executions) {
        return executions.stream()
                .filter(execution -> isOngoingStatus(execution.getStatut()))
                .sorted(Comparator.comparing((ExecutionProduction execution) -> parseDateTime(execution.getDateDebut())
                        .orElse(LocalDateTime.MIN)).reversed())
                .limit(5)
                .map(execution -> {
                    ProductionDashboardDTO.OperationStatusDTO item = new ProductionDashboardDTO.OperationStatusDTO();
                    item.setReference(execution.getReference());
                    item.setStatut(execution.getStatut());
                    item.setMachine(resolveExecutionMachineName(execution));
                    return item;
                })
                .toList();
    }

    private ProductionDashboardDTO.MachinesDTO buildMachines(
            List<ExecutionProduction> executions,
            List<Machine> machines,
            LocalDate from,
            LocalDate to) {
        ProductionDashboardDTO.MachinesDTO dto = new ProductionDashboardDTO.MachinesDTO();

        long active = machines.stream().filter(machine -> isServiceMachine(machine.getEtatMachine())).count();
        dto.setMachinesActives(active);
        dto.setMachinesInactives(Math.max(0, machines.size() - active));

        Map<String, Double> categoryToQuantity = new LinkedHashMap<>();
        for (String category : machineCategoryOrder()) {
            categoryToQuantity.put(category, 0.0);
        }

        for (ExecutionProduction execution : executions) {
            if (!isWithinPeriod(execution.getDateFinPrevue(), execution.getDateDebut(), from, to)) {
                continue;
            }

            Double quantity = resolveExecutionQuantity(execution);
            if (quantity == null) {
                continue;
            }

            Set<String> categories = resolveExecutionMachineCategories(execution);
            for (String category : categories) {
                categoryToQuantity.merge(category, quantity, Double::sum);
            }
        }

        Map<String, List<Machine>> categoryToMachines = new LinkedHashMap<>();
        for (String category : machineCategoryOrder()) {
            categoryToMachines.put(category, new ArrayList<>());
        }

        for (Machine machine : machines) {
            String category = resolveMachineCategory(machine);
            if (!categoryToMachines.containsKey(category)) {
                continue;
            }
            categoryToMachines.get(category).add(machine);
        }

        List<ProductionDashboardDTO.MachineLoadDTO> loads = machineCategoryOrder().stream()
                .map(category -> {
                    ProductionDashboardDTO.MachineLoadDTO item = new ProductionDashboardDTO.MachineLoadDTO();
                    item.setMachine(displayMachineCategory(category));
                    item.setQuantite((double) categoryToMachines.getOrDefault(category, List.of()).size());
                    item.setUnite("machine(s)");
                    item.setActive(categoryToMachines.getOrDefault(category, List.of()).stream()
                            .anyMatch(machine -> isServiceMachine(machine.getEtatMachine())));
                    return item;
                })
                .sorted(Comparator.comparing(ProductionDashboardDTO.MachineLoadDTO::getQuantite).reversed())
                .toList();

        dto.setChargeParMachine(loads);
        return dto;
    }

    private Double resolveExecutionQuantity(ExecutionProduction execution) {
        Double finalQuantity = Optional.ofNullable(execution.getProduitFinal())
                .map(produit -> produit.getQuantiteProduite())
                .orElse(null);
        if (finalQuantity != null) {
            return finalQuantity;
        }

        return getLatestPredictedQuantity(execution);
    }

    private ProductionDashboardDTO.QualityDTO buildQuality(List<ExecutionProduction> executions,
                                                           List<AnalyseLaboratoire> analyses) {
        ProductionDashboardDTO.QualityDTO dto = new ProductionDashboardDTO.QualityDTO();

        double aciditeMoyenne = analyses.stream()
                .map(AnalyseLaboratoire::getAcidite_huile_pourcent)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double indicePeroxydeMoyen = analyses.stream()
                .map(AnalyseLaboratoire::getIndice_peroxyde_meq_o2_kg)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double humiditeMoyenne = executions.stream()
                .map(ExecutionProduction::getLot)
                .filter(Objects::nonNull)
                .map(LotOlives::getHumiditePourcent)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double polyphenolsMoyen = analyses.stream()
                .map(AnalyseLaboratoire::getPolyphenols_mg_kg)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        long extraVierge = 0L;
        long vierge = 0L;
        long lampante = 0L;

        for (ExecutionProduction execution : executions) {
            String normalized = normalizeQuality(
                    Optional.ofNullable(execution.getProduitFinal()).map(produit -> produit.getQualite()).orElse(null));
            if ("EXTRA_VIERGE".equals(normalized)) {
                extraVierge++;
            } else if ("VIERGE".equals(normalized)) {
                vierge++;
            } else if ("LAMPANTE".equals(normalized)) {
                lampante++;
            }
        }

        ProductionDashboardDTO.QualityDistributionDTO distribution = new ProductionDashboardDTO.QualityDistributionDTO();
        distribution.setExtraVierge(extraVierge);
        distribution.setVierge(vierge);
        distribution.setLampante(lampante);

        dto.setAciditeMoyenne(round2(aciditeMoyenne));
        dto.setIndicePeroxydeMoyen(round2(indicePeroxydeMoyen));
        dto.setHumiditeMoyennePate(round2(humiditeMoyenne));
        dto.setPolyphenolsMoyen(round2(polyphenolsMoyen));
        dto.setRepartitionQualiteFinale(distribution);

        return dto;
    }

    private ProductionDashboardDTO.StockMovementsDTO buildStockMovements(List<StockMovement> movements, LocalDate from, LocalDate to) {
        ProductionDashboardDTO.StockMovementsDTO dto = new ProductionDashboardDTO.StockMovementsDTO();

        // NOTE (FR): Par demande — compter uniquement les mouvements dont
        // la date d'enregistrement (`dateMouvement`) est la date du jour.
        // Les paramètres `from` / `to` sont ignorés pour ces indicateurs.
        LocalDate today = LocalDate.now();

        long entrees = movements.stream()
                .filter(movement -> isSameDay(movement.getDateMouvement(), today))
                .filter(movement -> movement.getTypeMouvement() == TypeMouvement.ENTREE)
                .count();

        long transferts = movements.stream()
                .filter(movement -> isSameDay(movement.getDateMouvement(), today))
                .filter(movement -> movement.getTypeMouvement() == TypeMouvement.TRANSFERT)
                .count();

        long sorties = movements.stream()
                .filter(movement -> isSameDay(movement.getDateMouvement(), today))
                .filter(movement -> movement.getTypeMouvement() == TypeMouvement.AJUSTEMENT)
                .count();

        dto.setEntreesAujourdhui(entrees);
        dto.setSortiesAujourdhui(sorties);
        dto.setTransfertsAujourdhui(transferts);
        return dto;
    }

    private Optional<LocalDateTime> parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String trimmed = value.trim();
        String normalized = trimmed.replace(' ', 'T');
        try {
            if (trimmed.length() == 10) {
                return Optional.of(LocalDate.parse(trimmed).atStartOfDay());
            }
            return Optional.of(LocalDateTime.parse(normalized));
        } catch (DateTimeParseException ignored) {
            try {
                return Optional.of(OffsetDateTime.parse(normalized)
                        .atZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime());
            } catch (Exception ignoredOffset) {
                try {
                    return Optional.of(ZonedDateTime.parse(normalized)
                            .withZoneSameInstant(ZoneId.systemDefault())
                            .toLocalDateTime());
                } catch (Exception ex) {
                    try {
                        return Optional.of(LocalDate.parse(trimmed.substring(0, 10)).atStartOfDay());
                    } catch (Exception ex2) {
                        return Optional.empty();
                    }
                }
            }
        }
    }

    private boolean isSameDay(String value, LocalDate day) {
        return parseDateTime(value)
                .map(date -> date.toLocalDate().isEqual(day))
                .orElse(false);
    }

    private boolean isWithinPeriod(String value, LocalDate from, LocalDate to) {
        return parseDateTime(value)
                .map(date -> {
                    LocalDate localDate = date.toLocalDate();
                    return (localDate.isEqual(from) || localDate.isAfter(from))
                            && (localDate.isEqual(to) || localDate.isBefore(to));
                })
                .orElse(false);
    }

    private boolean isWithinPeriod(String primaryDate, String fallbackDate, LocalDate from, LocalDate to) {
        Optional<LocalDateTime> parsed = parseDateTime(primaryDate);
        if (parsed.isEmpty()) {
            parsed = parseDateTime(fallbackDate);
        }

        return parsed
                .map(date -> {
                    LocalDate localDate = date.toLocalDate();
                    return (localDate.isEqual(from) || localDate.isAfter(from))
                            && (localDate.isEqual(to) || localDate.isBefore(to));
                })
                .orElse(false);
    }

    private boolean isOngoingStatus(String status) {
        String normalized = normalize(status);
        return normalized.contains("EN_COURS") || normalized.contains("ONGOING") || normalized.equals("EN COURS");
    }

    private boolean isFinishedStatus(String status) {
        String normalized = normalize(status);
        return normalized.contains("TERMINE") || normalized.contains("COMPLETED") || normalized.contains("FINI");
    }

    private boolean isServiceMachine(String etat) {
        String normalized = normalize(etat);
        if (normalized.isBlank()) {
            return false;
        }
        return normalized.equals("EN_SERVICE") || normalized.equals("EN SERVICE");
    }

    private String resolveExecutionMachineName(ExecutionProduction execution) {
        if (execution.getGuideProduction() == null || execution.getGuideProduction().getEtapes() == null) {
            return "-";
        }

        return execution.getGuideProduction().getEtapes().stream()
                .map(etape -> etape.getMachine())
                .filter(Objects::nonNull)
                .map(machine -> machine.getNomMachine())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("-");
    }

    private Set<String> resolveExecutionMachineCategories(ExecutionProduction execution) {
        if (execution.getGuideProduction() == null || execution.getGuideProduction().getEtapes() == null) {
            return Set.of();
        }

        return execution.getGuideProduction().getEtapes().stream()
                .map(etape -> etape.getMachine())
                .filter(Objects::nonNull)
                .map(this::resolveMachineCategory)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private String resolveMachineCategory(Machine machine) {
        if (machine == null) {
            return "";
        }

        String category = normalizeMachineCategory(machine.getCategorieMachine());
        if (!category.isBlank()) {
            return category;
        }

        return normalizeMachineCategory(machine.getTypeMachine());
    }

    private String normalizeMachineCategory(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return "";
        }

        if (normalized.contains("BROYEUR") || normalized.contains("BROYAGE")) {
            return "broyeur";
        }
        if (normalized.contains("MALAXEUR") || normalized.contains("MALAXAGE")) {
            return "malaxeur";
        }
        if (normalized.contains("PRESSE") || normalized.contains("PRESS")) {
            return "presse";
        }
        if (normalized.contains("SEPARATION") || normalized.contains("SEPARATEUR") || normalized.contains("DECANT")) {
            return "separation";
        }
        if (normalized.contains("NETTOYAGE") || normalized.contains("LAVAGE") || normalized.contains("NETTOYER")) {
            return "nettoyage";
        }

        return normalized.toLowerCase(Locale.ROOT);
    }

    private String displayMachineCategory(String category) {
        return switch (category) {
            case "broyeur" -> "Broyeur";
            case "malaxeur" -> "Malaxeur";
            case "presse" -> "Presse";
            case "separation" -> "Séparation";
            case "nettoyage" -> "Nettoyage";
            default -> category;
        };
    }

    private List<String> machineCategoryOrder() {
        return List.of("broyeur", "malaxeur", "presse", "separation", "nettoyage");
    }

    private Double getLatestPredictedQuantity(ExecutionProduction execution) {
        if (execution.getPredictions() == null || execution.getPredictions().isEmpty()) {
            return null;
        }

        return execution.getPredictions().stream()
                .sorted(Comparator
                        .comparing((Prediction prediction) -> parseDateTime(prediction.getDateCreation())
                                .orElse(LocalDateTime.MIN))
                        .reversed())
                .map(prediction -> prediction.getQuantiteHuileRecalculeeLitres())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private String normalizeQuality(String value) {
        String normalized = normalize(value);
        if (normalized.contains("EXTRA") || normalized.contains("EXCELLENTE")) {
            return "EXTRA_VIERGE";
        }
        if (normalized.contains("LAMPANTE") || normalized.contains("MOYENNE")) {
            return "LAMPANTE";
        }
        if (normalized.contains("VIERGE") || normalized.contains("BONNE")) {
            return "VIERGE";
        }
        return "";
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('É', 'E')
                .replace('È', 'E')
                .replace('Ê', 'E')
                .replace('À', 'A')
                .replace('Â', 'A')
                .replace('Ù', 'U')
                .replace('Û', 'U')
                .replace('Î', 'I')
                .replace('Ï', 'I')
                .replace('Ô', 'O')
                .replace('-', '_');
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
