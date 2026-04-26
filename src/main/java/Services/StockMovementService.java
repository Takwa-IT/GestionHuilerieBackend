package Services;

import Config.ReferenceUtils;
import Mapper.StockMovementMapper;
import Models.LotOlives;
import Models.Stock;
import Models.StockMovement;
import Models.TypeMouvement;
import Models.Utilisateur;
import Repositories.LotOlivesRepository;
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
    private final LotOlivesRepository lotOlivesRepository;
    private final StockMovementMapper stockMovementMapper;
    private final CurrentUserService currentUserService;

    public StockMovementDTO create(StockMovementCreateDTO dto) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        Long effectiveHuilerieId = currentUserService.isAdmin(utilisateur)
                ? dto.getHuilerieId()
                : currentUserService.getCurrentHuilerieIdOrThrow();
        currentUserService.ensureCanAccessHuilerie(effectiveHuilerieId);

        if (dto.getTypeMouvement() == TypeMouvement.ENTREE) {
            throw new RuntimeException("Le type ENTREE est reserve a la creation d'un nouveau lot");
        }

        // Récupérer le lot et son stock via la variété normalisée
        LotOlives lot = lotOlivesRepository.findById(dto.getLotId())
                .orElseThrow(() -> new RuntimeException("Lot non trouve"));

        String varieteNormalisee = lot.getVariete() != null
            ? lot.getVariete().trim().toLowerCase() : "";
        Stock stock = stockRepository.findByLotOlives_Huilerie_IdHuilerieAndVariete(
            effectiveHuilerieId,
            varieteNormalisee)
            .orElseThrow(() -> new RuntimeException("Stock non trouve pour cette variété et huilerie"));

        applyWholeLotMovement(stock, lot, dto.getTypeMouvement());
        // Sauvegarder le lot après mise à jour
        lotOlivesRepository.save(lot);
        // Sauvegarder le stock avec la quantité mise à jour
        stockRepository.save(stock);

        StockMovement movement = stockMovementMapper.toEntity(dto);
        movement.setStock(stock);
        movement.setLotOlives(lot);
        StockMovement saved = stockMovementRepository.save(movement);
        saved.setReference(ReferenceUtils.format("MS", saved.getIdStockMovement()));
        return stockMovementMapper.toDTO(stockMovementRepository.save(saved));
    }

    public StockMovementDTO update(Long idStockMovement, StockMovementUpdateDTO dto) {
        StockMovement movement = stockMovementRepository.findById(idStockMovement)
                .orElseThrow(() -> new RuntimeException("Mouvement de stock non trouve"));

        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        boolean isAdmin = currentUserService.isAdmin(utilisateur);
        Long currentHuilerieId = isAdmin ? null : currentUserService.getCurrentHuilerieIdOrThrow();

        Long movementHuilerieId = movement.getStock() != null
            && movement.getStock().getLotOlives() != null
            && movement.getStock().getLotOlives().getHuilerie() != null
            ? movement.getStock().getLotOlives().getHuilerie().getIdHuilerie()
            : null;

        if (!isAdmin && (movementHuilerieId == null || !movementHuilerieId.equals(currentHuilerieId))) {
            throw new AccessDeniedException("Acces refuse a un mouvement d'une autre huilerie");
        }

        if (movement.getTypeMouvement() == TypeMouvement.ENTREE) {
            throw new RuntimeException("Le mouvement ENTREE genere a l'arrivage n'est pas modifiable");
        }

        movement.setCommentaire(dto.getCommentaire());
        movement.setDateMouvement(dto.getDateMouvement());

        StockMovement saved = stockMovementRepository.save(movement);
        return stockMovementMapper.toDTO(saved);
    }

    public List<StockMovementDTO> findAll(String huilerieNom) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<StockMovement> movements = hasText(huilerieNom)
                    ? stockMovementRepository.findByStock_LotOlives_Huilerie_NomIgnoreCaseOrderByDateMouvementDesc(huilerieNom)
                    : stockMovementRepository.findAll().stream()
                            .sorted((a, b) -> nullSafe(b.getDateMouvement()).compareTo(nullSafe(a.getDateMouvement())))
                            .toList();
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();

            return movements.stream()
                    .sorted((a, b) -> nullSafe(b.getDateMouvement()).compareTo(nullSafe(a.getDateMouvement())))
                    .filter(movement -> movement.getStock() != null
                        && movement.getStock().getLotOlives() != null
                        && movement.getStock().getLotOlives().getHuilerie() != null
                        && accessibleHuilerieIds.contains(movement.getStock().getLotOlives().getHuilerie().getIdHuilerie()))
                    .map(stockMovementMapper::toDTO)
                    .toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return stockMovementRepository.findByStock_LotOlives_Huilerie_IdHuilerieOrderByDateMouvementDesc(huilerieId)
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

        return stockMovementRepository.findByStock_LotOlives_Huilerie_IdHuilerieOrderByDateMouvementDesc(effectiveHuilerieId)
                .stream()
                .map(stockMovementMapper::toDTO)
                .toList();
    }

    public StockMovement createArrivalForStock(Stock stock, LotOlives lot, Double quantiteEntree, String dateMouvement,
            String commentaire) {
        stock.setLotOlives(lot);
        double current = safe(stock.getQuantiteDisponible());
        stock.setQuantiteDisponible(current + safe(quantiteEntree));
        stockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setStock(stock);
        movement.setLotOlives(lot);
        movement.setCommentaire(commentaire);
        movement.setDateMouvement(dateMouvement);
        movement.setTypeMouvement(TypeMouvement.ENTREE);
        StockMovement saved = stockMovementRepository.save(movement);
        saved.setReference(ReferenceUtils.format("MS", saved.getIdStockMovement()));
        return stockMovementRepository.save(saved);
    }

    private void applyWholeLotMovement(Stock stock, LotOlives lot, TypeMouvement typeMouvement) {
        // Vérifier que le lot a une quantité disponible
        double lotQuantite = safe(lot.getQuantiteRestante());
        if (lotQuantite <= 0) {
            throw new RuntimeException("Aucune quantite disponible sur ce lot pour appliquer ce mouvement");
        }

        switch (typeMouvement) {
            case TRANSFERT, AJUSTEMENT -> {
                // Mettre à jour la quantité du lot à 0
                lot.setQuantiteRestante(0d);
                // Mettre à jour la quantité du stock en soustrayant la quantité du lot
                double currentStock = safe(stock.getQuantiteDisponible());
                stock.setQuantiteDisponible(currentStock - lotQuantite);
            }
            case ENTREE -> {
                // ENTREE est reservee a la creation du lot et n'est pas traitee ici.
            }
        }
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
