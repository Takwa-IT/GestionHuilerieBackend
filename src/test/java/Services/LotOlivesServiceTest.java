package Services;

import Config.ReferenceUtils;
import Models.CampagneOlives;
import Models.Huilerie;
import Models.LotOlives;
import Models.MatierePremiere;
import Models.Stock;
import Models.Utilisateur;
import Repositories.CampagneOlivesRepository;
import Repositories.HuilerieRepository;
import Repositories.LotOlivesRepository;
import Repositories.StockRepository;
import Repositories.AnalyseLaboratoireRepository;
import Mapper.LotOlivesMapper;
import dto.LotArrivageCreateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests pour vérifier que LotOlivesService gère correctement
 * la création de stock et l'évitement des doublons via normalisation.
 */
@ExtendWith(MockitoExtension.class)
class LotOlivesServiceTest {

    @Mock
    private LotOlivesRepository lotOlivesRepository;

    @Mock
    private CampagneOlivesRepository campagneOlivesRepository;

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

    private Huilerie huilerie;
    private MatierePremiere matierePremiere;
    private CampagneOlives campagne;

    @BeforeEach
    void setUp() {
        huilerie = new Huilerie();
        huilerie.setIdHuilerie(1L);
        huilerie.setNom("Huilerie Test");

        matierePremiere = new MatierePremiere();
        matierePremiere.setId(1L);
        matierePremiere.setReference("MAT-001");
        matierePremiere.setType("OLIVE");
        matierePremiere.setHuilerie(huilerie);

        campagne = new CampagneOlives();
        campagne.setIdCampagne(1L);
        campagne.setReference("CAMP-2025");
        campagne.setHuilerie(huilerie);
    }

    /**
     * Test 1 : Vérifier qu'une deuxième arrivée de la même variété
     * (même après normalisation) réutilise le stock existant.
     */
    @Test
    void testSecondLotArrivageReuseExistingStock() {
        // Setup : First arrival with "Arbequina"
        LotArrivageCreateDTO dtoFirst = new LotArrivageCreateDTO();
        dtoFirst.setVariete("Arbequina");
        dtoFirst.setMatierePremiereReference("MAT-001");
        dtoFirst.setCampagneReference("CAMP-2025");
        dtoFirst.setHuilerieId(1L);
        dtoFirst.setPesee(100.0);
        dtoFirst.setDateReception("2025-01-01");

        LotOlives firstLot = new LotOlives();
        firstLot.setIdLot(1L);
        firstLot.setReference("LO-1");
        firstLot.setVariete("arbequina");
        firstLot.setHuilerie(huilerie);
        firstLot.setMatierePremiere(matierePremiere);
        firstLot.setCampagne(campagne);
        firstLot.setPesee(100.0);
        firstLot.setQuantiteInitiale(100.0);
        firstLot.setQuantiteRestante(100.0);

        Stock existingStock = new Stock();
        existingStock.setIdStock(10L);
        existingStock.setVariete("arbequina");
        existingStock.setTypeStock("OLIVE");
        existingStock.setQuantiteDisponible(100.0);
        existingStock.setLotOlives(firstLot);

        Utilisateur mockUser = new Utilisateur();
        mockUser.setIdUtilisateur(1L);

        when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(mockUser);
        when(currentUserService.isAdmin(any())).thenReturn(true);
        when(currentUserService.getAccessibleHuilerieIds()).thenReturn(java.util.List.of(1L));
        when(huilerieRepository.findById(1L)).thenReturn(Optional.of(huilerie));
        when(matierePremiereService.findMatiere("MAT-001")).thenReturn(matierePremiere);
        when(campagneOlivesRepository.findByReference("CAMP-2025")).thenReturn(Optional.of(campagne));
        
        // Mock save to return the lot with ID
        when(lotOlivesRepository.save(any(LotOlives.class)))
                .thenAnswer(invocation -> {
                    LotOlives lot = invocation.getArgument(0);
                    if (lot.getIdLot() == null) {
                        lot.setIdLot(1L);
                    }
                    return lot;
                });

        // Stock already exists for normalized variete "arbequina"
        when(stockRepository.findByLotOlives_Huilerie_IdHuilerieAndVariete(1L, "arbequina"))
                .thenReturn(Optional.of(existingStock));

        when(lotOlivesMapper.toDTO(any(LotOlives.class))).thenAnswer(invocation -> {
            LotOlives lot = invocation.getArgument(0);
            dto.LotOlivesDTO dto = new dto.LotOlivesDTO();
            dto.setIdLot(lot.getIdLot());
            dto.setReference(lot.getReference());
            return dto;
        });

        // Execute
        var result = lotOlivesService.createArrivage(dtoFirst);

        // Assert
        verify(stockRepository, never()).save(any(Stock.class));
        assertEquals("LO-1", result.getReference());
    }

    /**
     * Test 2 : Vérifier que les variétés avec cases différentes
     * créent un seul stock (normalisation).
     */
    @Test
    void testVarieteNormalizationDifferentCases() {
        // Setup : First arrival
        LotArrivageCreateDTO dtoFirst = new LotArrivageCreateDTO();
        dtoFirst.setVariete("KORONEIKI");
        dtoFirst.setMatierePremiereReference("MAT-001");
        dtoFirst.setCampagneReference("CAMP-2025");
        dtoFirst.setHuilerieId(1L);
        dtoFirst.setPesee(100.0);
        dtoFirst.setDateReception("2025-01-01");

        LotOlives firstLot = new LotOlives();
        firstLot.setIdLot(1L);
        firstLot.setReference("LO-1");
        firstLot.setVariete("koroneiki");
        firstLot.setHuilerie(huilerie);
        firstLot.setMatierePremiere(matierePremiere);
        firstLot.setCampagne(campagne);
        firstLot.setPesee(100.0);
        firstLot.setQuantiteInitiale(100.0);
        firstLot.setQuantiteRestante(100.0);

        Utilisateur mockUser = new Utilisateur();
        mockUser.setIdUtilisateur(1L);

        when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(mockUser);
        when(currentUserService.isAdmin(any())).thenReturn(true);
        when(currentUserService.getAccessibleHuilerieIds()).thenReturn(java.util.List.of(1L));
        when(huilerieRepository.findById(1L)).thenReturn(Optional.of(huilerie));
        when(matierePremiereService.findMatiere("MAT-001")).thenReturn(matierePremiere);
        when(campagneOlivesRepository.findByReference("CAMP-2025")).thenReturn(Optional.of(campagne));

        when(lotOlivesRepository.save(any(LotOlives.class)))
                .thenAnswer(invocation -> {
                    LotOlives lot = invocation.getArgument(0);
                    if (lot.getIdLot() == null) {
                        lot.setIdLot(1L);
                    }
                    return lot;
                });

        // No existing stock yet
        when(stockRepository.findByLotOlives_Huilerie_IdHuilerieAndVariete(1L, "koroneiki"))
                .thenReturn(Optional.empty());

        Stock newStock = new Stock();
        newStock.setIdStock(10L);
        newStock.setVariete("koroneiki");
        newStock.setTypeStock("OLIVE");
        newStock.setQuantiteDisponible(0.0);

        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(invocation -> {
                    Stock stock = invocation.getArgument(0);
                    if (stock.getIdStock() == null) {
                        stock.setIdStock(10L);
                    }
                    return stock;
                });

        when(lotOlivesMapper.toDTO(any(LotOlives.class))).thenAnswer(invocation -> {
            LotOlives lot = invocation.getArgument(0);
            dto.LotOlivesDTO dto = new dto.LotOlivesDTO();
            dto.setIdLot(lot.getIdLot());
            dto.setReference(lot.getReference());
            return dto;
        });

        // Execute
        var result = lotOlivesService.createArrivage(dtoFirst);

        // Assert
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository, atLeastOnce()).save(stockCaptor.capture());
        
        Stock capturedStock = stockCaptor.getValue();
        assertEquals("koroneiki", capturedStock.getVariete(),
                "La variété du stock devrait être normalisée en minuscules");
    }

    /**
     * Test 3 : Vérifier que le stock existant n'est pas écrasé
     * par le nouveau lot (lot ne doit pas être remplacé).
     */
    @Test
    void testExistingStockLotIsNotOverwritten() {
        // Setup
        LotArrivageCreateDTO dto = new LotArrivageCreateDTO();
        dto.setVariete("Manzanilla");
        dto.setMatierePremiereReference("MAT-001");
        dto.setCampagneReference("CAMP-2025");
        dto.setHuilerieId(1L);
        dto.setPesee(50.0);
        dto.setDateReception("2025-01-02");

        LotOlives newLot = new LotOlives();
        newLot.setIdLot(2L);
        newLot.setReference("LO-2");
        newLot.setVariete("manzanilla");
        newLot.setHuilerie(huilerie);
        newLot.setMatierePremiere(matierePremiere);
        newLot.setCampagne(campagne);
        newLot.setPesee(50.0);
        newLot.setQuantiteInitiale(50.0);
        newLot.setQuantiteRestante(50.0);

        // Lot initial qui a créé le stock
        LotOlives originalLot = new LotOlives();
        originalLot.setIdLot(1L);
        originalLot.setReference("LO-1");
        originalLot.setVariete("manzanilla");

        Stock existingStock = new Stock();
        existingStock.setIdStock(10L);
        existingStock.setVariete("manzanilla");
        existingStock.setTypeStock("OLIVE");
        existingStock.setQuantiteDisponible(100.0);
        existingStock.setLotOlives(originalLot); // Ce lot ne doit pas être changé

        Utilisateur mockUser = new Utilisateur();
        mockUser.setIdUtilisateur(1L);

        when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(mockUser);
        when(currentUserService.isAdmin(any())).thenReturn(true);
        when(currentUserService.getAccessibleHuilerieIds()).thenReturn(java.util.List.of(1L));
        when(huilerieRepository.findById(1L)).thenReturn(Optional.of(huilerie));
        when(matierePremiereService.findMatiere("MAT-001")).thenReturn(matierePremiere);
        when(campagneOlivesRepository.findByReference("CAMP-2025")).thenReturn(Optional.of(campagne));

        when(lotOlivesRepository.save(any(LotOlives.class)))
                .thenAnswer(invocation -> {
                    LotOlives lot = invocation.getArgument(0);
                    if (lot.getIdLot() == null) {
                        lot.setIdLot(2L);
                    }
                    return lot;
                });

        when(stockRepository.findByLotOlives_Huilerie_IdHuilerieAndVariete(1L, "manzanilla"))
                .thenReturn(Optional.of(existingStock));

        when(lotOlivesMapper.toDTO(any(LotOlives.class))).thenAnswer(invocation -> {
            LotOlives lot = invocation.getArgument(0);
            dto.LotOlivesDTO dtoResult = new dto.LotOlivesDTO();
            dtoResult.setIdLot(lot.getIdLot());
            dtoResult.setReference(lot.getReference());
            return dtoResult;
        });

        // Execute
        lotOlivesService.createArrivage(dto);

        // Assert
        verify(stockRepository).findByLotOlives_Huilerie_IdHuilerieAndVariete(1L, "manzanilla");
        
        // Verify stock's lot is not overwritten (should remain the original lot LO-1)
        assertEquals(1L, existingStock.getLotOlives().getIdLot(),
                "Le lot original du stock ne doit pas être écrasé");
        assertEquals("LO-1", existingStock.getLotOlives().getReference());
    }
}
