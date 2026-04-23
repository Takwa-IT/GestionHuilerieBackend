package Services;

import Models.ExecutionProduction;
import Models.Prediction;
import Repositories.ExecutionProductionRepository;
import Repositories.PredictionRepository;
import dto.PredictionCreateDTO;
import dto.PredictionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final ExecutionProductionRepository executionProductionRepository;

    public PredictionDTO create(PredictionCreateDTO dto) {
        ExecutionProduction execution = executionProductionRepository.findById(dto.getExecutionProductionId())
                .orElseThrow(() -> new RuntimeException("Execution de production non trouvée"));

        Prediction prediction = new Prediction();
        prediction.setModePrediction(dto.getModePrediction());
        prediction.setQualitePredite(dto.getQualitePredite());
        prediction.setProbabiliteQualite(dto.getProbabiliteQualite());
        prediction.setRendementPreditPourcent(dto.getRendementPreditPourcent());
        prediction.setQuantiteHuileRecalculeeLitres(dto.getQuantiteHuileRecalculeeLitres());
        prediction.setExecutionProduction(execution);
        prediction.setDateCreation(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Prediction saved = predictionRepository.save(prediction);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public PredictionDTO findById(Long id) {
        return predictionRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Prédiction non trouvée"));
    }

    @Transactional(readOnly = true)
    public List<PredictionDTO> findAll() {
        return predictionRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PredictionDTO> findByExecutionProductionId(Long executionId) {
        return predictionRepository.findByExecutionProductionId(executionId).stream()
                .map(this::toDTO)
                .toList();
    }

    public PredictionDTO update(Long id, PredictionCreateDTO dto) {
        Prediction prediction = predictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prédiction non trouvée"));

        prediction.setModePrediction(dto.getModePrediction());
        prediction.setQualitePredite(dto.getQualitePredite());
        prediction.setProbabiliteQualite(dto.getProbabiliteQualite());
        prediction.setRendementPreditPourcent(dto.getRendementPreditPourcent());
        prediction.setQuantiteHuileRecalculeeLitres(dto.getQuantiteHuileRecalculeeLitres());

        if (dto.getExecutionProductionId() != null
                && !dto.getExecutionProductionId()
                .equals(prediction.getExecutionProduction().getIdExecutionProduction())) {
            ExecutionProduction execution = executionProductionRepository.findById(dto.getExecutionProductionId())
                    .orElseThrow(() -> new RuntimeException("Execution de production non trouvée"));
            prediction.setExecutionProduction(execution);
        }

        return toDTO(predictionRepository.save(prediction));
    }

    public void delete(Long id) {
        predictionRepository.deleteById(id);
    }

    private PredictionDTO toDTO(Prediction prediction) {
        PredictionDTO dto = new PredictionDTO();
        dto.setIdPrediction(prediction.getIdPrediction());
        dto.setModePrediction(prediction.getModePrediction());
        dto.setQualitePredite(prediction.getQualitePredite());
        dto.setProbabiliteQualite(prediction.getProbabiliteQualite());
        dto.setRendementPreditPourcent(prediction.getRendementPreditPourcent());
        dto.setQuantiteHuileRecalculeeLitres(prediction.getQuantiteHuileRecalculeeLitres());
        if (prediction.getExecutionProduction() != null) {
            dto.setExecutionProductionId(prediction.getExecutionProduction().getIdExecutionProduction());
        }
        dto.setDateCreation(prediction.getDateCreation());
        return dto;
    }
}
