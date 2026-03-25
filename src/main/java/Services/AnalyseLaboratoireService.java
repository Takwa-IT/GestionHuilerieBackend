package Services;

import Mapper.AnalyseLaboratoireMapper;
import Models.AnalyseLaboratoire;
import Models.LotOlives;
import Repositories.AnalyseLaboratoireRepository;
import dto.AnalyseLaboratoireCreateDTO;
import dto.AnalyseLaboratoireDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnalyseLaboratoireService {

    private final AnalyseLaboratoireRepository analyseLaboratoireRepository;
    private final AnalyseLaboratoireMapper analyseLaboratoireMapper;
    private final LotOlivesService lotOlivesService;

    public List<AnalyseLaboratoireDTO> findByLot(Long lotId) {
        return analyseLaboratoireRepository.findByLot_IdLotOrderByDateAnalyseAsc(lotId)
                .stream()
                .map(analyseLaboratoireMapper::toDTO)
                .toList();
    }

    public AnalyseLaboratoireDTO create(AnalyseLaboratoireCreateDTO dto) {
        LotOlives lot = lotOlivesService.findLot(dto.getLotId());

        AnalyseLaboratoire entity = analyseLaboratoireMapper.toEntity(dto);
        entity.setLot(lot);
        if (entity.getDateAnalyse() == null || entity.getDateAnalyse().isBlank()) {
            entity.setDateAnalyse(LocalDate.now().toString());
        }
        if (entity.getClasseQualiteFinale() == null || entity.getClasseQualiteFinale().isBlank()) {
            entity.setClasseQualiteFinale(computeClasseQualite(dto));
        }

        return analyseLaboratoireMapper.toDTO(analyseLaboratoireRepository.save(entity));
    }

    private String computeClasseQualite(AnalyseLaboratoireCreateDTO dto) {
        if (dto.getAcidite() <= 0.8 && dto.getIndicePeroxyde() <= 20 && dto.getK232() <= 2.5 && dto.getK270() <= 0.22) {
            return "EXTRA_VIERGE";
        }
        if (dto.getAcidite() <= 2.0) {
            return "VIERGE";
        }
        return "COURANTE";
    }
}
