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
        Stock stock = findStock(dto.getStockId());
        if (!stock.getHuilerie().getIdHuilerie().equals(dto.getHuilerieId())) {
            throw new RuntimeException("Le stock ne correspond pas a l'huilerie fournie");
        }
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
        return stockMovementRepository.findAll().stream().map(stockMovementMapper::toDTO).toList();
    }

    public List<StockMovementDTO> findByStock(Long stockId) {
        return stockMovementRepository.findByStock_IdStockOrderByDateMouvementAsc(stockId)
                .stream()
                .map(stockMovementMapper::toDTO)
                .toList();
    }

    //lors de reception d'une nouvelle reception elle augmente le stock, cree un mouvement ARRIVAL et le sauvegarde
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

    //recalculer la quantité disponible du stock selon le type de mouvement.
    private void updateStockQuantity(Stock stock, TypeMouvement typeMouvement, Double quantite) {
        double current = safe(stock.getQuantiteDisponible());
        double next = current + deltaFor(typeMouvement, quantite);

        if (next < 0) {
            throw new RuntimeException("Quantite disponible insuffisante pour le stock");
        }

        stock.setQuantiteDisponible(next);
    }

    //recupere un stock par ID + utilisable dans des autres methodes
    private Stock findStock(Long stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock non trouve"));
    }

    //transformer un type de mouvement en effet mathématique sur le stock
    private double deltaFor(TypeMouvement typeMouvement, Double quantite) {
        return switch (typeMouvement) {
            case ARRIVAL, ADJUSTMENT -> quantite;
            case DEPARTURE, TRANSFER -> -quantite;
        };
    }

    //evite les NULL sur les quantites
    private double safe(Double value) {
        return value == null ? 0d : value;
    }
}
