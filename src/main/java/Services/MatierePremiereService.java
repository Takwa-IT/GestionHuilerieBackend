package Services;

import Config.ReferenceUtils;
import Mapper.MatierePremiereMapper;
import Models.MatierePremiere;
import Repositories.MatierePremiereRepository;
import dto.MatierePremiereCreateDTO;
import dto.MatierePremiereDTO;
import dto.MatierePremiereUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MatierePremiereService {

    private final MatierePremiereRepository matierePremiereRepository;
    private final MatierePremiereMapper matierePremiereMapper;

    public MatierePremiereDTO create(MatierePremiereCreateDTO dto) {
        MatierePremiere entity = matierePremiereMapper.toEntity(dto);
        entity.setReference("TMP-MP-" + UUID.randomUUID());
        MatierePremiere saved = matierePremiereRepository.save(entity);
        saved.setReference(ReferenceUtils.format("MP", saved.getId()));
        return matierePremiereMapper.toDTO(matierePremiereRepository.save(saved));
    }

    public MatierePremiereDTO update(String reference, MatierePremiereUpdateDTO dto) {
        MatierePremiere entity = findMatiere(reference);
        matierePremiereMapper.updateFromDTO(dto, entity);
        return matierePremiereMapper.toDTO(matierePremiereRepository.save(entity));
    }

    public void delete(String reference) {
        matierePremiereRepository.delete(findMatiere(reference));
    }

    public MatierePremiereDTO findByReference(String reference) {
        return matierePremiereMapper.toDTO(findMatiere(reference));
    }

    public List<MatierePremiereDTO> findAll() {
        return matierePremiereRepository.findAll().stream().map(matierePremiereMapper::toDTO).toList();
    }

    //recupere une matiere premiere par reference pour le module CRUD
    public MatierePremiere findMatiere(String reference) {
        return matierePremiereRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Matiere premiere non trouvee"));
    }

    //recupere une matiere premiere par ID + utilisable dans les autres methodes existantes
    public MatierePremiere findMatiere(Long id) {
        return matierePremiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matiere premiere non trouvee"));
    }

}
