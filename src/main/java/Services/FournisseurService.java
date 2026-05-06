package Services;

import Models.Fournisseur;
import Repositories.FournisseurRepository;
import dto.FournisseurCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;

    public Fournisseur create(FournisseurCreateDTO dto) {
        String cin = dto.getCin() != null ? dto.getCin().trim() : null;
        if (cin == null || cin.isEmpty()) {
            throw new IllegalArgumentException("CIN requis");
        }
        if (fournisseurRepository.existsByCin(cin)) {
            throw new DataIntegrityViolationException("CIN déjà utilisé");
        }
        Fournisseur f = new Fournisseur();
        f.setNom(dto.getNom());
        f.setCin(cin);
        return fournisseurRepository.save(f);
    }
}
