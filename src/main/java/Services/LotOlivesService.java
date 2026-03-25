package Services;

import Mapper.LotOlivesMapper;
import Models.LotOlives;
import Repositories.LotOlivesRepository;
import dto.LotOlivesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotOlivesService {

    private final LotOlivesRepository lotOlivesRepository;
    private final LotOlivesMapper lotOlivesMapper;

    public List<LotOlivesDTO> findAll() {
        return lotOlivesRepository.findAll().stream().map(lotOlivesMapper::toDTO).toList();
    }

    public LotOlivesDTO findById(Long idLot) {
        return lotOlivesMapper.toDTO(findLot(idLot));
    }

    public LotOlives findLot(Long idLot) {
        return lotOlivesRepository.findById(idLot)
                .orElseThrow(() -> new RuntimeException("Lot non trouve"));
    }
}
