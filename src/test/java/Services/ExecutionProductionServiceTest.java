// Teste : Services.ExecutionProductionService — Gestion des exécutions de production
package Services;

import Models.EtapeProduction;
import Models.ExecutionProduction;
import Models.GuideProduction;
import Models.Huilerie;
import Models.LotOlives;
import Models.ParametreEtape;
import Models.ValeurReelleParametre;
import Repositories.ExecutionProductionRepository;
import Repositories.GuideProductionRepository;
import Repositories.LotOlivesRepository;
import Repositories.ParametreEtapeRepository;
import dto.ExecutionProductionCreateDTO;
import dto.ExecutionProductionUpdateDTO;
import dto.ValeurReelleParametreDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ExecutionProductionService")
class ExecutionProductionServiceTest {

    @Mock
    private ExecutionProductionRepository executionProductionRepository;

    @Mock
    private GuideProductionRepository guideProductionRepository;

    @Mock
    private LotOlivesRepository lotOlivesRepository;

    @Mock
    private StockMovementService stockMovementService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ParametreEtapeRepository parametreEtapeRepository;

    @InjectMocks
    private ExecutionProductionService executionProductionService;

    private Huilerie buildHuilerie(Long id) {
        Huilerie huilerie = new Huilerie();
        huilerie.setIdHuilerie(id);
        huilerie.setNom("Huilerie Test");
        return huilerie;
    }

    private GuideProduction buildGuide(Long id, Long huilerieId) {
        GuideProduction guide = new GuideProduction();
        guide.setIdGuideProduction(id);
        guide.setHuilerie(buildHuilerie(huilerieId));
        return guide;
    }

    private LotOlives buildLot(Long id, Long huilerieId) {
        LotOlives lot = new LotOlives();
        lot.setIdLot(id);
        lot.setHuilerie(buildHuilerie(huilerieId));
        lot.setDateReception("2024-01-01");
        return lot;
    }

    private ExecutionProduction buildExecution(Long id) {
        ExecutionProduction execution = new ExecutionProduction();
        execution.setIdExecutionProduction(id);
        execution.setReference("LOT-01");
        execution.setStatut("EN_COURS");
        return execution;
    }

    @Nested
    @DisplayName("Tests sur create()")
    class CreateTests {

        @Test
        @DisplayName("create leve exception quand guide et lot huileries differentes")
        void create_leveException_quandGuideEtLotHuileriesDifferentes() {
            GuideProduction guide = buildGuide(1L, 1L);
            LotOlives lot = buildLot(1L, 2L);

            ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
            dto.setGuideProductionId(1L);
            dto.setLotId(1L);
            dto.setReference("LOT-1");
            dto.setDateDebut("2024-01-01");
            dto.setDateFinPrevue("2024-01-02");
            dto.setStatut("EN_COURS");

            when(guideProductionRepository.findById(1L)).thenReturn(Optional.of(guide));
            when(lotOlivesRepository.findById(1L)).thenReturn(Optional.of(lot));

            assertThatThrownBy(() -> executionProductionService.create(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("doivent appartenir a la meme huilerie");
        }

        @Test
        @DisplayName("create leve exception quand guide sans huilerie")
        void create_leveException_quandGuideSansHuilerie() {
            GuideProduction guide = new GuideProduction();
            guide.setIdGuideProduction(1L);
            guide.setHuilerie(null);

            LotOlives lot = buildLot(1L, 1L);

            ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
            dto.setGuideProductionId(1L);
            dto.setLotId(1L);
            dto.setReference("LOT-1");
            dto.setDateDebut("2024-01-01");
            dto.setDateFinPrevue("2024-01-02");
            dto.setStatut("EN_COURS");

            when(guideProductionRepository.findById(1L)).thenReturn(Optional.of(guide));
            when(lotOlivesRepository.findById(1L)).thenReturn(Optional.of(lot));

            assertThatThrownBy(() -> executionProductionService.create(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("sans huilerie associee");
        }

        @Test
        @DisplayName("create appelle stockMovementService apres creation")
        void create_appelleStockMovementService_apresCreation() {
            GuideProduction guide = buildGuide(1L, 1L);
            LotOlives lot = buildLot(1L, 1L);

            ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
            dto.setGuideProductionId(1L);
            dto.setLotId(1L);
            dto.setReference("LOT-1");
            dto.setDateDebut("2024-01-01");
            dto.setDateFinPrevue("2024-01-02");
            dto.setStatut("EN_COURS");

            when(guideProductionRepository.findById(1L)).thenReturn(Optional.of(guide));
            when(lotOlivesRepository.findById(1L)).thenReturn(Optional.of(lot));
            when(executionProductionRepository.existsByCodeLot("LOT-1")).thenReturn(false);
            when(executionProductionRepository.save(any())).thenReturn(buildExecution(1L));

            executionProductionService.create(dto);

            verify(stockMovementService, times(1)).create(any());
        }

        @Test
        @DisplayName("create sauvegarde execution avec reference unique")
        void create_sauvegardeExecutionAvecReferenceUnique() {
            GuideProduction guide = buildGuide(1L, 1L);
            LotOlives lot = buildLot(1L, 1L);

            ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
            dto.setGuideProductionId(1L);
            dto.setLotId(1L);
            dto.setReference("LOT-1");
            dto.setDateDebut("2024-01-01");
            dto.setDateFinPrevue("2024-01-02");
            dto.setStatut("EN_COURS");

            when(guideProductionRepository.findById(1L)).thenReturn(Optional.of(guide));
            when(lotOlivesRepository.findById(1L)).thenReturn(Optional.of(lot));
            when(executionProductionRepository.existsByCodeLot("LOT-1")).thenReturn(false);
            when(executionProductionRepository.save(any())).thenReturn(buildExecution(1L));

            executionProductionService.create(dto);

            verify(executionProductionRepository).save(argThat(exec -> "LOT-1".equals(exec.getReference())));
        }

        @Test
        @DisplayName("create genere suffixe quand reference existe deja")
        void create_genereSuffixe_quandReferenceExisteDeja() {
            GuideProduction guide = buildGuide(1L, 1L);
            LotOlives lot = buildLot(1L, 1L);

            ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
            dto.setGuideProductionId(1L);
            dto.setLotId(1L);
            dto.setReference("LOT-1");
            dto.setDateDebut("2024-01-01");
            dto.setDateFinPrevue("2024-01-02");
            dto.setStatut("EN_COURS");

            when(guideProductionRepository.findById(1L)).thenReturn(Optional.of(guide));
            when(lotOlivesRepository.findById(1L)).thenReturn(Optional.of(lot));
            when(executionProductionRepository.existsByCodeLot("LOT-1")).thenReturn(true);
            when(executionProductionRepository.existsByCodeLot("LOT-1-2")).thenReturn(false);
            when(executionProductionRepository.save(any())).thenReturn(buildExecution(1L));

            executionProductionService.create(dto);

            verify(executionProductionRepository).save(argThat(exec -> "LOT-1-2".equals(exec.getReference())));
        }
    }

    @Nested
    @DisplayName("Tests sur computeStorageDurationDays")
    class ComputeStorageDurationDaysTests {

        @Test
        @DisplayName("calcule 3 jours entre date reception et date debut")
        void calcule3Jours_entredateReceptionEtDateDebut() {
            GuideProduction guide = buildGuide(1L, 1L);
            LotOlives lot = buildLot(1L, 1L);
            lot.setDateReception("2024-01-01");

            ExecutionProductionCreateDTO dto = new ExecutionProductionCreateDTO();
            dto.setGuideProductionId(1L);
            dto.setLotId(1L);
            dto.setReference("LOT-1");
            dto.setDateDebut("2024-01-04");
            dto.setDateFinPrevue("2024-01-05");
            dto.setStatut("EN_COURS");

            when(guideProductionRepository.findById(1L)).thenReturn(Optional.of(guide));
            when(lotOlivesRepository.findById(1L)).thenReturn(Optional.of(lot));
            when(executionProductionRepository.existsByCodeLot("LOT-1")).thenReturn(false);
            when(executionProductionRepository.save(any())).thenAnswer(invocation -> {
                ExecutionProduction exec = invocation.getArgument(0);
                return exec;
            });

            executionProductionService.create(dto);

            verify(lotOlivesRepository).save(argThat(l -> l.getDureeStockageAvantBroyage() == 3));
        }


        @Test
        @DisplayName("retourne null quand date reception null")
        void retourneNull_quandDateReceptionNull() {
            Integer result = executionProductionService.computeStorageDurationDays(null, "2024-01-01");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("retourne null quand date debut null")
        void retourneNull_quandDateDebutNull() {
            Integer result = executionProductionService.computeStorageDurationDays("2024-01-01", null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("retourne null quand date reception vide")
        void retourneNull_quandDateReceptionVide() {
            Integer result = executionProductionService.computeStorageDurationDays("", "2024-01-01");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("retourne null quand date debut vide")
        void retourneNull_quandDateDebutVide() {
            Integer result = executionProductionService.computeStorageDurationDays("2024-01-01", "");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("calcule 3 jours entre reception et debut")
        void calcule3Jours_entreReceptionEtDebut() {
            Integer result = executionProductionService.computeStorageDurationDays("2024-01-01", "2024-01-04");
            assertThat(result).isEqualTo(3);
        }

        @Test
        @DisplayName("calcule 0 jours quand meme jour")
        void calcule0Jours_quandMemJour() {
            Integer result = executionProductionService.computeStorageDurationDays("2024-01-01", "2024-01-01");
            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Tests sur saveValeursReelles()")
    class SaveValeursReellesTests {

        @Test
        @DisplayName("saveValeursReelles leve exception quand parametre n appartient pas au guide")
        void saveValeursReelles_leveException_quandParametreNAppartientPasAuGuide() {
            ExecutionProduction execution = buildExecution(1L);
            GuideProduction guide1 = buildGuide(1L, 1L);
            execution.setGuideProduction(guide1);

            GuideProduction guide2 = buildGuide(2L, 2L);
            EtapeProduction etape = new EtapeProduction();
            etape.setGuideProduction(guide2);

            ParametreEtape param = new ParametreEtape();
            param.setIdParametreEtape(1L);
            param.setEtapeProduction(etape);

            ValeurReelleParametreDTO dto = new ValeurReelleParametreDTO();
            dto.setParametreEtapeId(1L);
            dto.setValeurReelle(30.0);

            when(executionProductionRepository.findById(1L)).thenReturn(Optional.of(execution));
            when(parametreEtapeRepository.findById(1L)).thenReturn(Optional.of(param));

            assertThatThrownBy(() -> executionProductionService.saveValeursReelles(1L, List.of(dto)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ne correspond pas au guide");
        }

        @Test
        @DisplayName("saveValeursReelles sauvegarde valeur quand parametre valide")
        void saveValeursReelles_sauvegardeValeur_quandParametreValide() {
            ExecutionProduction execution = buildExecution(1L);
            execution.setValeursReelles(new ArrayList<>());
            GuideProduction guide = buildGuide(1L, 1L);
            execution.setGuideProduction(guide);

            EtapeProduction etape = new EtapeProduction();
            etape.setGuideProduction(guide);

            ParametreEtape param = new ParametreEtape();
            param.setIdParametreEtape(1L);
            param.setEtapeProduction(etape);

            ValeurReelleParametreDTO dto = new ValeurReelleParametreDTO();
            dto.setParametreEtapeId(1L);
            dto.setValeurReelle(30.0);

            when(executionProductionRepository.findById(1L)).thenReturn(Optional.of(execution));
            when(parametreEtapeRepository.findById(1L)).thenReturn(Optional.of(param));
            when(executionProductionRepository.save(any())).thenReturn(execution);

            executionProductionService.saveValeursReelles(1L, List.of(dto));

            verify(executionProductionRepository, times(1)).save(any());
        }
    }

    @Nested
    @DisplayName("Tests sur update()")
    class UpdateTests {

        @Test
        @DisplayName("update met a jour statut quand statut fourni")
        void update_metAJourStatut_quandStatutFourni() {
            ExecutionProduction execution = buildExecution(1L);
            execution.setStatut("EN_COURS");

            ExecutionProductionUpdateDTO dto = new ExecutionProductionUpdateDTO();
            dto.setStatut("TERMINE");

            when(executionProductionRepository.findById(1L)).thenReturn(Optional.of(execution));
            when(executionProductionRepository.save(any())).thenReturn(execution);

            executionProductionService.update(1L, dto);

            assertThat(execution.getStatut()).isEqualTo("TERMINE");
        }

        @Test
        @DisplayName("update met a jour rendement quand rendement fourni")
        void update_metAJourRendement_quandRendementFourni() {
            ExecutionProduction execution = buildExecution(1L);
            execution.setRendement(20.0);

            ExecutionProductionUpdateDTO dto = new ExecutionProductionUpdateDTO();
            dto.setRendement(25.5);

            when(executionProductionRepository.findById(1L)).thenReturn(Optional.of(execution));
            when(executionProductionRepository.save(any())).thenReturn(execution);

            executionProductionService.update(1L, dto);

            assertThat(execution.getRendement()).isEqualTo(25.5);
        }

        @Test
        @DisplayName("update tronque date fin reelle a 10 caracteres")
        void update_tronqueDateFinReelle_à10Caracteres() {
            ExecutionProduction execution = buildExecution(1L);

            ExecutionProductionUpdateDTO dto = new ExecutionProductionUpdateDTO();
            dto.setDateFinReelle("2024-01-15T10:30:00");

            when(executionProductionRepository.findById(1L)).thenReturn(Optional.of(execution));
            when(executionProductionRepository.save(any())).thenReturn(execution);

            executionProductionService.update(1L, dto);

            assertThat(execution.getDateFinReelle()).isEqualTo("2024-01-15");
        }
    }
}
