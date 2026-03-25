package Services;

import Mapper.StockMovementMapper;
import Models.Stock;
import Models.StockMovement;
import Models.TypeMouvement;
import Repositories.StockMovementRepository;
import Repositories.StockRepository;
import dto.StockMovementCreateDTO;
import dto.StockMovementDTO;
import dto.StockMovementTypeUpdateDTO;
import lombok.RequiredArgsConstructor;
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

    public StockMovementDTO create(StockMovementCreateDTO dto) {
        Stock stock = stockRepository.findByHuilerie_IdHuilerieAndLotOlives_IdLot(dto.getHuilerieId(), dto.getReferenceId())
                .orElseThrow(() -> new RuntimeException("Stock non trouve pour ce lot et cette huilerie"));

        updateStockQuantity(stock, dto.getTypeMouvement(), dto.getQuantite());

        StockMovement movement = stockMovementMapper.toEntity(dto);
        movement.setStock(stock);

        StockMovement saved = stockMovementRepository.save(movement);
        stockRepository.save(stock);
        return stockMovementMapper.toDTO(saved);
    }

    public StockMovementDTO updateTypeMouvement(Long idStockMovement, StockMovementTypeUpdateDTO dto) {
        StockMovement movement = stockMovementRepository.findById(idStockMovement)
                .orElseThrow(() -> new RuntimeException("Mouvement de stock non trouve"));

        if (movement.getTypeMouvement() == dto.getTypeMouvement()
                && safe(movement.getQuantite()) == safe(dto.getQuantite())) {
            return stockMovementMapper.toDTO(movement);
        }

        Stock stock = movement.getStock();
        double current = safe(stock.getQuantiteDisponible());
        double quantiteAncienne = safe(movement.getQuantite());
        double quantiteNouvelle = safe(dto.getQuantite());
        double stockApresAnnulationAncienMouvement = current - deltaFor(movement.getTypeMouvement(), quantiteAncienne);

        if ((dto.getTypeMouvement() == TypeMouvement.DEPARTURE || dto.getTypeMouvement() == TypeMouvement.TRANSFER)
                && quantiteNouvelle > stockApresAnnulationAncienMouvement) {
            throw new RuntimeException(
                    "Quantite insuffisante pour appliquer ce mouvement. Stock disponible apres annulation de l'ancien mouvement = "
                            + stockApresAnnulationAncienMouvement);
        }

        double next = stockApresAnnulationAncienMouvement + deltaFor(dto.getTypeMouvement(), quantiteNouvelle);

        stock.setQuantiteDisponible(next);
        movement.setTypeMouvement(dto.getTypeMouvement());
        movement.setQuantite(quantiteNouvelle);

        stockRepository.save(stock);
        StockMovement saved = stockMovementRepository.save(movement);
        return stockMovementMapper.toDTO(saved);
    }

    public List<StockMovementDTO> findAll() {
        return stockMovementRepository.findAll().stream()
                .sorted((a, b) -> nullSafe(b.getDateMouvement()).compareTo(nullSafe(a.getDateMouvement())))
                .map(stockMovementMapper::toDTO)
                .toList();
    }

    public List<StockMovementDTO> findByHuilerie(Long huilerieId) {
        return stockMovementRepository.findByStock_Huilerie_IdHuilerieOrderByDateMouvementDesc(huilerieId)
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
        return stockMovementRepository.save(movement);
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
}
