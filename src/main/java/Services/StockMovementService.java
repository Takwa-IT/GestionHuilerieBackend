package Services;

import Config.ReferenceUtils;
import Mapper.StockMovementMapper;
import Models.Stock;
import Models.StockMovement;
import Models.TypeMouvement;
import Models.Utilisateur;
import Repositories.StockMovementRepository;
import Repositories.StockRepository;
import dto.StockMovementCreateDTO;
import dto.StockMovementDTO;
import dto.StockMovementUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final StockRepository stockRepository;
    private final StockMovementMapper stockMovementMapper;
    private final CurrentUserService currentUserService;

    public StockMovementDTO create(StockMovementCreateDTO dto) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        Long effectiveHuilerieId = currentUserService.isAdmin(utilisateur)
                ? dto.getHuilerieId()
                : currentUserService.getCurrentHuilerieIdOrThrow();
        currentUserService.ensureCanAccessHuilerie(effectiveHuilerieId);

        Stock stock = stockRepository.findByHuilerie_IdHuilerieAndLotOlives_IdLot(effectiveHuilerieId, dto.getReferenceId())
                .orElseThrow(() -> new RuntimeException("Stock non trouve pour ce lot et cette huilerie"));

        updateStockQuantity(stock, dto.getTypeMouvement(), dto.getQuantite());

        StockMovement movement = stockMovementMapper.toEntity(dto);
        movement.setStock(stock);

        StockMovement saved = stockMovementRepository.save(movement);
        saved.setReference(ReferenceUtils.format("MS", saved.getIdStockMovement()));
        saved = stockMovementRepository.save(saved);
        stockRepository.save(stock);
        return stockMovementMapper.toDTO(saved);
    }

    public StockMovementDTO update(Long idStockMovement, StockMovementUpdateDTO dto) {
        StockMovement movement = stockMovementRepository.findById(idStockMovement)
                .orElseThrow(() -> new RuntimeException("Mouvement de stock non trouve"));

        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        boolean isAdmin = currentUserService.isAdmin(utilisateur);
        Long currentHuilerieId = isAdmin ? null : currentUserService.getCurrentHuilerieIdOrThrow();

        Long movementHuilerieId = movement.getStock() != null
            && movement.getStock().getHuilerie() != null
            ? movement.getStock().getHuilerie().getIdHuilerie()
            : null;

        if (!isAdmin && (movementHuilerieId == null || !movementHuilerieId.equals(currentHuilerieId))) {
            throw new AccessDeniedException("Acces refuse a un mouvement d'une autre huilerie");
        }

        Long effectiveHuilerieId = isAdmin ? dto.getHuilerieId() : currentHuilerieId;
        currentUserService.ensureCanAccessHuilerie(effectiveHuilerieId);

        Stock ancienStock = movement.getStock();
        Stock nouveauStock = stockRepository.findByHuilerie_IdHuilerieAndLotOlives_IdLot(effectiveHuilerieId, dto.getReferenceId())
                .orElseThrow(() -> new RuntimeException("Stock non trouve pour ce lot et cette huilerie"));

        double quantiteAncienne = safe(movement.getQuantite());
        double quantiteNouvelle = safe(dto.getQuantite());
        double ancienStockBase = safe(ancienStock.getQuantiteDisponible()) - deltaFor(movement.getTypeMouvement(), quantiteAncienne);

        if (ancienStockBase < 0) {
            throw new RuntimeException("Stock incoherent pour annuler l'ancien mouvement");
        }

        if (ancienStock.getIdStock().equals(nouveauStock.getIdStock())) {
            double stockFinal = ancienStockBase + deltaFor(dto.getTypeMouvement(), quantiteNouvelle);
            if (stockFinal < 0) {
                throw new RuntimeException("Quantite disponible insuffisante pour le stock");
            }
            ancienStock.setQuantiteDisponible(stockFinal);
            stockRepository.save(ancienStock);
        } else {
            ancienStock.setQuantiteDisponible(ancienStockBase);
            stockRepository.save(ancienStock);

            double nouveauStockFinal = safe(nouveauStock.getQuantiteDisponible()) + deltaFor(dto.getTypeMouvement(), quantiteNouvelle);
            if (nouveauStockFinal < 0) {
                throw new RuntimeException("Quantite disponible insuffisante pour le stock cible");
            }
            nouveauStock.setQuantiteDisponible(nouveauStockFinal);
            stockRepository.save(nouveauStock);
        }

        movement.setStock(nouveauStock);
        movement.setTypeMouvement(dto.getTypeMouvement());
        movement.setQuantite(quantiteNouvelle);
        movement.setCommentaire(dto.getCommentaire());
        movement.setDateMouvement(dto.getDateMouvement());

        StockMovement saved = stockMovementRepository.save(movement);
        return stockMovementMapper.toDTO(saved);
    }

    public List<StockMovementDTO> findAll(String huilerieNom) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<StockMovement> movements = hasText(huilerieNom)
                    ? stockMovementRepository.findByStock_Huilerie_NomIgnoreCaseOrderByDateMouvementDesc(huilerieNom)
                    : stockMovementRepository.findAll().stream()
                        .sorted((a, b) -> nullSafe(b.getDateMouvement()).compareTo(nullSafe(a.getDateMouvement())))
                        .toList();
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();

            return movements.stream()
                    .sorted((a, b) -> nullSafe(b.getDateMouvement()).compareTo(nullSafe(a.getDateMouvement())))
                    .filter(movement -> movement.getStock() != null
                            && movement.getStock().getHuilerie() != null
                            && accessibleHuilerieIds.contains(movement.getStock().getHuilerie().getIdHuilerie()))
                    .map(stockMovementMapper::toDTO)
                    .toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return stockMovementRepository.findByStock_Huilerie_IdHuilerieOrderByDateMouvementDesc(huilerieId)
            .stream()
                .map(stockMovementMapper::toDTO)
                .toList();
    }

    public List<StockMovementDTO> findByHuilerie(Long huilerieId) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        Long effectiveHuilerieId = huilerieId;

        if (!currentUserService.isAdmin(utilisateur)) {
            Long currentHuilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
            if (!currentHuilerieId.equals(huilerieId)) {
                throw new AccessDeniedException("Acces refuse a une autre huilerie");
            }
            effectiveHuilerieId = currentHuilerieId;
        } else {
            currentUserService.ensureCanAccessHuilerie(huilerieId);
        }

        return stockMovementRepository.findByStock_Huilerie_IdHuilerieOrderByDateMouvementDesc(effectiveHuilerieId)
                .stream()
                .map(stockMovementMapper::toDTO)
                .toList();
    }

    public StockMovement createArrivalForStock(Stock stock, Double quantite, String dateMouvement, String commentaire) {
        stock.setQuantiteDisponible(safe(stock.getQuantiteDisponible()) + quantite);
        stockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setStock(stock);
        movement.setQuantite(quantite);
        movement.setCommentaire(commentaire);
        movement.setDateMouvement(dateMouvement);
        movement.setTypeMouvement(TypeMouvement.ARRIVAL);
        StockMovement saved = stockMovementRepository.save(movement);
        saved.setReference(ReferenceUtils.format("MS", saved.getIdStockMovement()));
        return stockMovementRepository.save(saved);
    }

    private void updateStockQuantity(Stock stock, TypeMouvement typeMouvement, Double quantite) {
        double current = safe(stock.getQuantiteDisponible());
        double next = current + deltaFor(typeMouvement, quantite);

        if (next < 0) {
            throw new RuntimeException("Quantite disponible insuffisante pour le stock");
        }

        stock.setQuantiteDisponible(next);
    }

    private double deltaFor(TypeMouvement typeMouvement, Double quantite) {
        return switch (typeMouvement) {
            case ARRIVAL, ADJUSTMENT -> quantite;
            case DEPARTURE, TRANSFER -> -quantite;
        };
    }

    private double safe(Double value) {
        return value == null ? 0d : value;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
