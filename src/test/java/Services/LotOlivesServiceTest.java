// Teste : Services.LotOlivesService — Gestion des lots d'olives
package Services;

import Config.ReferenceUtils;
import Models.CampagneOlives;
import Models.Huilerie;
import Models.LotOlives;
import Models.MatierePremiere;
import Models.Stock;
import Models.Utilisateur;
import Repositories.CampagneOlivesRepository;
import Repositories.FournisseurRepository;
import Repositories.HuilerieRepository;
import Repositories.LotOlivesRepository;
import Repositories.StockRepository;
import Repositories.AnalyseLaboratoireRepository;
import Mapper.LotOlivesMapper;
import dto.LotArrivageCreateDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de LotOlivesService")
class LotOlivesServiceTest {

    @Mock
    private LotOlivesRepository lotOlivesRepository;

    @Mock
    private CampagneOlivesRepository campagneOlivesRepository;

    @Mock
    private FournisseurRepository fournisseurRepository;

    @Mock
    private HuilerieRepository huilerieRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private AnalyseLaboratoireRepository analyseLaboratoireRepository;

    @Mock
    private LotOlivesMapper lotOlivesMapper;

    @Mock
    private MatierePremiereService matierePremiereService;

    @Mock
    private StockMovementService stockMovementService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private LotOlivesService lotOlivesService;

    private Huilerie buildHuilerie(Long id) {
        Huilerie huilerie = new Huilerie();
        huilerie.setIdHuilerie(id);
        huilerie.setNom("Huilerie Test");
        return huilerie;
    }

    private Models.Fournisseur buildFournisseur(Long id) {
        Models.Fournisseur fournisseur = new Models.Fournisseur();
        fournisseur.setIdFournisseur(id);
        fournisseur.setNom("Fournisseur Test");
        return fournisseur;
    }

    private MatierePremiere buildMatierePremiere(Long id, String reference) {
        MatierePremiere mp = new MatierePremiere();
        mp.setId(id);
        mp.setReference(reference);
        mp.setType("OLIVE");
        mp.setHuilerie(buildHuilerie(1L));
        return mp;
    }

    private LotOlives buildLotOlives(Long id, String variete) {
        LotOlives lot = new LotOlives();
        lot.setIdLot(id);
        lot.setReference("LO-" + id);
        lot.setVariete(variete);
        lot.setHuilerie(buildHuilerie(1L));
        lot.setMatierePremiere(buildMatierePremiere(1L, "MAT-001"));
        lot.setFournisseur(buildFournisseur(1L));
        return lot;
    }

    private CampagneOlives buildCampagne(Long id, String reference) {
        CampagneOlives campagne = new CampagneOlives();
        campagne.setIdCampagne(id);
        campagne.setReference(reference);
        campagne.setHuilerie(buildHuilerie(1L));
        return campagne;
    }

    private LotArrivageCreateDTO buildLotDTO(String variete, String matiereRef, String campagneRef, Long huilerieId) {
        LotArrivageCreateDTO dto = new LotArrivageCreateDTO();
        dto.setVariete(variete);
        dto.setMatierePremiereReference(matiereRef);
        dto.setCampagneReference(campagneRef);
        dto.setHuilerieId(huilerieId);
        dto.setFournisseurId(1L);
        dto.setPesee(100.0);
        dto.setDateReception("2025-01-01");
        return dto;
    }

    @Nested
    @DisplayName("Tests sur createArrivage()")
    class CreateArrivageTests {

        @Test
        @DisplayName("createArrivage reutilise stock existant quand meme variete")
        void createArrivage_reutiliseStockExistant_quandMemeVariete() {
            LotArrivageCreateDTO dto = buildLotDTO("Arbequina", "MAT-001", "CAMP-2025", 1L);

            LotOlives lot = buildLotOlives(1L, "arbequina");

            Stock existingStock = new Stock();
            existingStock.setIdStock(10L);
            existingStock.setVariete("arbequina");
            existingStock.setLotOlives(lot);

            Utilisateur user = new Utilisateur();
            user.setIdUtilisateur(1L);

            when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(user);
            when(currentUserService.isAdmin(any())).thenReturn(true);
            when(fournisseurRepository.findById(1L)).thenReturn(Optional.of(buildFournisseur(1L)));
            when(matierePremiereService.findMatiere("MAT-001")).thenReturn(buildMatierePremiere(1L, "MAT-001"));
            when(campagneOlivesRepository.findByNormalizedReference("CAMP-2025")).thenReturn(Optional.of(buildCampagne(1L, "CAMP-2025")));
            when(huilerieRepository.findById(1L)).thenReturn(Optional.of(buildHuilerie(1L)));
            when(lotOlivesRepository.save(any())).thenAnswer(inv -> {
                LotOlives l = inv.getArgument(0);
                if (l.getIdLot() == null) l.setIdLot(1L);
                return l;
            });
            when(stockRepository.findByLotOlives_Huilerie_IdHuilerieAndVariete(1L, "arbequina")).thenReturn(Optional.of(existingStock));
            when(lotOlivesMapper.toDTO(any())).thenAnswer(inv -> {
                LotOlives l = inv.getArgument(0);
                dto.LotOlivesDTO d = new dto.LotOlivesDTO();
                d.setIdLot(l.getIdLot());
                d.setReference(l.getReference());
                return d;
            });

            var result = lotOlivesService.createArrivage(dto);

            verify(stockRepository, never()).save(any(Stock.class));
        }

        @Test
        @DisplayName("createArrivage normalise variete en minuscules")
        void createArrivage_normaliseVariete_enMinuscules() {
            LotArrivageCreateDTO dto = buildLotDTO("KORONEIKI", "MAT-001", "CAMP-2025", 1L);

            Utilisateur user = new Utilisateur();
            user.setIdUtilisateur(1L);

            when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(user);
            when(currentUserService.isAdmin(any())).thenReturn(true);
            when(fournisseurRepository.findById(1L)).thenReturn(Optional.of(buildFournisseur(1L)));
            when(matierePremiereService.findMatiere("MAT-001")).thenReturn(buildMatierePremiere(1L, "MAT-001"));
            when(campagneOlivesRepository.findByNormalizedReference("CAMP-2025")).thenReturn(Optional.of(buildCampagne(1L, "CAMP-2025")));
            when(huilerieRepository.findById(1L)).thenReturn(Optional.of(buildHuilerie(1L)));
            when(lotOlivesRepository.save(any())).thenAnswer(inv -> {
                LotOlives l = inv.getArgument(0);
                if (l.getIdLot() == null) l.setIdLot(1L);
                return l;
            });
            when(stockRepository.findByLotOlives_Huilerie_IdHuilerieAndVariete(1L, "koroneiki")).thenReturn(Optional.empty());
            when(stockRepository.save(any())).thenAnswer(inv -> {
                Stock s = inv.getArgument(0);
                if (s.getIdStock() == null) s.setIdStock(10L);
                return s;
            });
            when(lotOlivesMapper.toDTO(any())).thenAnswer(inv -> {
                LotOlives l = inv.getArgument(0);
                dto.LotOlivesDTO d = new dto.LotOlivesDTO();
                d.setIdLot(l.getIdLot());
                d.setReference(l.getReference());
                return d;
            });

            lotOlivesService.createArrivage(dto);

            ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
            verify(stockRepository, atLeastOnce()).save(captor.capture());
            assertThat(captor.getValue().getVariete()).isEqualTo("koroneiki");
        }

        @Test
        @DisplayName("createArrivage ne remplace pas lot original du stock")
        void createArrivage_neRemplacePasLotOriginal_duStock() {
            LotArrivageCreateDTO dto = buildLotDTO("Manzanilla", "MAT-001", "CAMP-2025", 1L);

            LotOlives originalLot = buildLotOlives(1L, "manzanilla");
            originalLot.setIdLot(1L);
            originalLot.setReference("LO-1");

            Stock existingStock = new Stock();
            existingStock.setIdStock(10L);
            existingStock.setVariete("manzanilla");
            existingStock.setLotOlives(originalLot);

            Utilisateur user = new Utilisateur();
            user.setIdUtilisateur(1L);

            when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(user);
            when(currentUserService.isAdmin(any())).thenReturn(true);
            when(fournisseurRepository.findById(1L)).thenReturn(Optional.of(buildFournisseur(1L)));
            when(matierePremiereService.findMatiere("MAT-001")).thenReturn(buildMatierePremiere(1L, "MAT-001"));
            when(campagneOlivesRepository.findByNormalizedReference("CAMP-2025")).thenReturn(Optional.of(buildCampagne(1L, "CAMP-2025")));
            when(huilerieRepository.findById(1L)).thenReturn(Optional.of(buildHuilerie(1L)));
            when(lotOlivesRepository.save(any())).thenAnswer(inv -> {
                LotOlives l = inv.getArgument(0);
                if (l.getIdLot() == null) l.setIdLot(2L);
                return l;
            });
            when(stockRepository.findByLotOlives_Huilerie_IdHuilerieAndVariete(1L, "manzanilla")).thenReturn(Optional.of(existingStock));
            when(lotOlivesMapper.toDTO(any())).thenAnswer(inv -> {
                LotOlives l = inv.getArgument(0);
                dto.LotOlivesDTO d = new dto.LotOlivesDTO();
                d.setIdLot(l.getIdLot());
                d.setReference(l.getReference());
                return d;
            });

            lotOlivesService.createArrivage(dto);

            assertThat(existingStock.getLotOlives().getIdLot()).isEqualTo(2L);
        }
    }
}
