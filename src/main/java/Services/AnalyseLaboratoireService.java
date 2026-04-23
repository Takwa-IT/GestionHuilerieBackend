package Services;

import Config.ReferenceUtils;
import Mapper.AnalyseLaboratoireMapper;
import Models.AnalyseLaboratoire;
import Models.LotOlives;
import Repositories.AnalyseLaboratoireRepository;
import dto.AnalyseLaboratoireCreateDTO;
import dto.AnalyseLaboratoireDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnalyseLaboratoireService {

    private final AnalyseLaboratoireRepository analyseLaboratoireRepository;
    private final AnalyseLaboratoireMapper analyseLaboratoireMapper;
    private final LotOlivesService lotService;

    public List<AnalyseLaboratoireDTO> findByLot(Long lotId) {
        return analyseLaboratoireRepository.findByLot_IdLotOrderByDateAnalyseAsc(lotId)
                .stream()
                .map(analyseLaboratoireMapper::toDTO)
                .toList();
    }

    public AnalyseLaboratoireDTO create(AnalyseLaboratoireCreateDTO dto) {
        LotOlives lot = lotService.findLot(dto.getLotId());
        AnalyseLaboratoire entity = analyseLaboratoireRepository.findFirstByLot_IdLot(lot.getIdLot())
                .orElseGet(AnalyseLaboratoire::new);

        entity.setLot(lot);
        entity.setAcidite_huile_pourcent(dto.getAcidite_huile_pourcent());
        entity.setIndice_peroxyde_meq_o2_kg(dto.getIndice_peroxyde_meq_o2_kg());
        entity.setPolyphenols_mg_kg(dto.getPolyphenols_mg_kg());
        entity.setK232(dto.getK232());
        entity.setK270(dto.getK270());

        if (dto.getDateAnalyse() == null || dto.getDateAnalyse().isBlank()) {
            entity.setDateAnalyse(java.time.LocalDate.now().toString());
        } else {
            entity.setDateAnalyse(dto.getDateAnalyse());
        }

        AnalyseLaboratoire saved = analyseLaboratoireRepository.save(entity);
        saved.setReference(ReferenceUtils.format("AL", saved.getIdAnalyse()));
        return analyseLaboratoireMapper.toDTO(analyseLaboratoireRepository.save(saved));
    }
}
