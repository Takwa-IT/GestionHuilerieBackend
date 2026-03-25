package Services;

import Mapper.StockMapper;
import Repositories.StockRepository;
import dto.StockDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;

    public List<StockDTO> findAll() {
        return stockRepository.findAll().stream().map(stockMapper::toDTO).toList();
    }

    public List<StockDTO> findByLot(Long lotId) {
        return stockRepository.findByLotOlives_IdLot(lotId).stream().map(stockMapper::toDTO).toList();
    }

    public StockDTO findByLotAndHuilerie(Long lotId, Long huilerieId) {
        return stockRepository.findByHuilerie_IdHuilerieAndLotOlives_IdLot(huilerieId, lotId)
                .map(stockMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Stock non trouve"));
    }
}
