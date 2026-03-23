package Services;

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

@Service
@RequiredArgsConstructor
@Transactional
public class MatierePremiereService {

    private final MatierePremiereRepository matierePremiereRepository;
    private final MatierePremiereMapper matierePremiereMapper;

    public MatierePremiereDTO create(MatierePremiereCreateDTO dto) {
        MatierePremiere entity = matierePremiereMapper.toEntity(dto);
        return matierePremiereMapper.toDTO(matierePremiereRepository.save(entity));
    }

    public MatierePremiereDTO update(Long idMatierePremiere, MatierePremiereUpdateDTO dto) {
        MatierePremiere entity = findMatiere(idMatierePremiere);
        matierePremiereMapper.updateFromDTO(dto, entity);
        return matierePremiereMapper.toDTO(matierePremiereRepository.save(entity));
    }

    public void delete(Long idMatierePremiere) {
        matierePremiereRepository.delete(findMatiere(idMatierePremiere));
    }

    public MatierePremiereDTO findById(Long idMatierePremiere) {
        return matierePremiereMapper.toDTO(findMatiere(idMatierePremiere));
    }

    public List<MatierePremiereDTO> findAll() {
        return matierePremiereRepository.findAll().stream().map(matierePremiereMapper::toDTO).toList();
    }

    //recupere une matiere premiere par ID + utilisable dans des autres methodes
    public MatierePremiere findMatiere(Long id) {
        return matierePremiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matiere premiere non trouvee"));
    }

}
