// Teste : Services.GuideProductionService — Gestion des guides de production
package Services;

import Models.EtapeProduction;
import Models.GuideProduction;
import Models.Huilerie;
import Models.ParametreEtape;
import Repositories.ExecutionProductionRepository;
import Repositories.GuideProductionRepository;
import Repositories.HuilerieRepository;
import Repositories.MachineRepository;
import Repositories.ValeurReelleParametreRepository;
import dto.EtapeProductionCreateDTO;
import dto.GuideProductionCreateDTO;
import dto.GuideProductionDTO;
import dto.ParametreEtapeCreateDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de GuideProductionService")
class GuideProductionServiceTest {

    @Mock
    private GuideProductionRepository guideProductionRepository;

    @Mock
    private HuilerieRepository huilerieRepository;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private ExecutionProductionRepository executionProductionRepository;

    @Mock
    private ValeurReelleParametreRepository valeurReelleParametreRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private GuideProductionService guideProductionService;

    private Huilerie buildHuilerie(Long id) {
        Huilerie huilerie = new Huilerie();
        huilerie.setIdHuilerie(id);
        huilerie.setNom("Huilerie Test");
        return huilerie;
    }

    private GuideProductionCreateDTO buildGuideDTO(Long huilerieId, String typeMachine, List<EtapeProductionCreateDTO> etapes) {
        GuideProductionCreateDTO dto = new GuideProductionCreateDTO();
        dto.setNom("Guide Test");
        dto.setDescription("Description test");
        dto.setDateCreation("2024-01-01");
        dto.setHuilerieId(huilerieId);
        dto.setTypeMachine(typeMachine);
        dto.setEtapes(etapes);
        return dto;
    }

    @Nested
    @DisplayName("Tests sur create()")
    class CreateTests {

        @Test
        @DisplayName("create leve exception quand huilerie inexistante")
        void create_leveException_quandHuilerieInexistante() {
            GuideProductionCreateDTO dto = buildGuideDTO(99L, "3_phase", null);

            when(huilerieRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> guideProductionService.create(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Huilerie non trouvee");
        }

        @Test
        @DisplayName("create genere etapes template quand etapes dto vides")
        void create_genereEtapesTemplate_quandEtapesDtoVides() {
            Huilerie huilerie = buildHuilerie(1L);
            GuideProductionCreateDTO dto = buildGuideDTO(1L, "3_phase", null);

            when(huilerieRepository.findById(1L)).thenReturn(Optional.of(huilerie));
            when(guideProductionRepository.save(any())).thenAnswer(invocation -> {
                GuideProduction guide = invocation.getArgument(0);
                guide.setIdGuideProduction(1L);
                return guide;
            });

            GuideProductionDTO result = guideProductionService.create(dto);

            verify(guideProductionRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("create utilise etapes custom quand fournies")
        void create_utiliseEtapesCustom_quandFournies() {
            Huilerie huilerie = buildHuilerie(1L);

            List<EtapeProductionCreateDTO> etapes = new ArrayList<>();
            EtapeProductionCreateDTO etape1 = new EtapeProductionCreateDTO();
            etape1.setNom("Etape 1");
            etape1.setOrdre(1);
            etape1.setParametres(new ArrayList<>());
            etapes.add(etape1);

            EtapeProductionCreateDTO etape2 = new EtapeProductionCreateDTO();
            etape2.setNom("Etape 2");
            etape2.setOrdre(2);
            etape2.setParametres(new ArrayList<>());
            etapes.add(etape2);

            GuideProductionCreateDTO dto = buildGuideDTO(1L, "3_phase", etapes);

            when(huilerieRepository.findById(1L)).thenReturn(Optional.of(huilerie));
            when(guideProductionRepository.save(any())).thenAnswer(invocation -> {
                GuideProduction guide = invocation.getArgument(0);
                guide.setIdGuideProduction(1L);
                return guide;
            });

            GuideProductionDTO result = guideProductionService.create(dto);

            verify(guideProductionRepository, times(2)).save(argThat(guide -> guide.getEtapes().size() == 2));
        }

        @Test
        @DisplayName("create leve exception quand parametre valeur manquante")
        void create_leveException_quandParametreValeurManquante() {
            Huilerie huilerie = buildHuilerie(1L);

            List<EtapeProductionCreateDTO> etapes = new ArrayList<>();
            EtapeProductionCreateDTO etape1 = new EtapeProductionCreateDTO();
            etape1.setNom("Etape 1");
            etape1.setOrdre(1);

            List<ParametreEtapeCreateDTO> params = new ArrayList<>();
            ParametreEtapeCreateDTO param = new ParametreEtapeCreateDTO();
            param.setCodeParametre("temperature");
            param.setValeur("");
            params.add(param);
            etape1.setParametres(params);
            etapes.add(etape1);

            GuideProductionCreateDTO dto = buildGuideDTO(1L, "3_phase", etapes);

            when(huilerieRepository.findById(1L)).thenReturn(Optional.of(huilerie));

            assertThatThrownBy(() -> guideProductionService.create(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("doit contenir une valeur estimee");
        }

        @Test
        @DisplayName("create leve exception quand type machine vide")
        void create_leveException_quandTypeMachineVide() {
            Huilerie huilerie = buildHuilerie(1L);
            GuideProductionCreateDTO dto = buildGuideDTO(1L, "", null);

            when(huilerieRepository.findById(1L)).thenReturn(Optional.of(huilerie));

            assertThatThrownBy(() -> guideProductionService.create(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("obligatoire");
        }
    }

    @Nested
    @DisplayName("Tests sur delete()")
    class DeleteTests {

        @Test
        @DisplayName("delete supprime quand aucune execution")
        void delete_supprime_quandAucuneExecution() {
            GuideProduction guide = new GuideProduction();
            guide.setIdGuideProduction(1L);

            when(guideProductionRepository.findById(1L)).thenReturn(Optional.of(guide));
            when(executionProductionRepository.existsByGuideProduction_IdGuideProduction(1L)).thenReturn(false);

            guideProductionService.delete(1L);

            verify(guideProductionRepository).deleteById(1L);
        }

        @Test
        @DisplayName("delete leve exception quand executions existantes")
        void delete_leveException_quandExecutionsExistantes() {
            GuideProduction guide = new GuideProduction();
            guide.setIdGuideProduction(1L);

            when(guideProductionRepository.findById(1L)).thenReturn(Optional.of(guide));
            when(executionProductionRepository.existsByGuideProduction_IdGuideProduction(1L)).thenReturn(true);

            assertThatThrownBy(() -> guideProductionService.delete(1L))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    @DisplayName("Tests sur normalizeCodeEtape (via create)")
    class NormalizeCodeEtapeTests {

        @Test
        @DisplayName("normalise nom en snake case")
        void normaliseNomEnSnakeCase() {
            Huilerie huilerie = buildHuilerie(1L);

            List<EtapeProductionCreateDTO> etapes = new ArrayList<>();
            EtapeProductionCreateDTO etape1 = new EtapeProductionCreateDTO();
            etape1.setNom("Malaxage Double Cuve");
            etape1.setOrdre(1);
            etape1.setParametres(new ArrayList<>());
            etapes.add(etape1);

            GuideProductionCreateDTO dto = buildGuideDTO(1L, "3_phase", etapes);

            when(huilerieRepository.findById(1L)).thenReturn(Optional.of(huilerie));
            when(guideProductionRepository.save(any())).thenAnswer(invocation -> {
                GuideProduction guide = invocation.getArgument(0);
                guide.setIdGuideProduction(1L);
                return guide;
            });

            guideProductionService.create(dto);

            verify(guideProductionRepository, times(2)).save(argThat(guide -> 
                guide.getEtapes().get(0).getCodeEtape().equals("malaxage_double_cuve")));
        }

        @Test
        @DisplayName("normalise caracteres speciaux")
        void normaliseCaracteresSpeciaux() {
            Huilerie huilerie = buildHuilerie(1L);

            List<EtapeProductionCreateDTO> etapes = new ArrayList<>();
            EtapeProductionCreateDTO etape1 = new EtapeProductionCreateDTO();
            etape1.setNom("Réception & Contrôle");
            etape1.setOrdre(1);
            etape1.setParametres(new ArrayList<>());
            etapes.add(etape1);

            GuideProductionCreateDTO dto = buildGuideDTO(1L, "3_phase", etapes);

            when(huilerieRepository.findById(1L)).thenReturn(Optional.of(huilerie));
            when(guideProductionRepository.save(any())).thenAnswer(invocation -> {
                GuideProduction guide = invocation.getArgument(0);
                guide.setIdGuideProduction(1L);
                return guide;
            });

            guideProductionService.create(dto);

            verify(guideProductionRepository, times(2)).save(any());
            
            ArgumentCaptor<GuideProduction> captor = ArgumentCaptor.forClass(GuideProduction.class);
            verify(guideProductionRepository, atLeastOnce()).save(captor.capture());
            assertThat(captor.getValue().getEtapes().get(0).getCodeEtape()).isEqualTo("r_ception_contr_le");
        }
    }
}
