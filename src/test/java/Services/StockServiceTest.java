package Services;

import Models.Huilerie;
import Models.LotOlives;
import Models.MatierePremiere;
import Models.Stock;
import Repositories.StockRepository;
import Repositories.StockMovementRepository;
import Repositories.LotOlivesRepository;
import dto.StockDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests pour vérifier que la normalisation de variété fonctionne correctement
 * et que les doublons de stock sont évités pour une même (huilerie, variété).
 */
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private LotOlivesRepository lotOlivesRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private Mapper.StockMapper stockMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private StockService stockService;

    private Huilerie huilerie;
    private MatierePremiere matierePremiere;

    @BeforeEach
    void setUp() {
        huilerie = new Huilerie();
        huilerie.setIdHuilerie(1L);
        huilerie.setNom("Huilerie Test");

        matierePremiere = new MatierePremiere();
        matierePremiere.setId(1L);
    }

    /**
     * Test 1 : Vérifier qu'une même variété normalisée avec casses différentes
     * crée un seul stock dans la base et n'en retourne qu'un.
     */
    @Test
    void testStockNormalizationVarieteWithDifferentCases() {
        // Setup
        Stock stock1 = createStock(1L, "Arbequina");
        Stock stock2 = createStock(2L, "arbequina");
        Stock stock3 = createStock(3L, "ARBEQUINA");

        List<Stock> stocks = Arrays.asList(stock1, stock2, stock3);

        when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(createMockUtilisateur());
        when(currentUserService.isAdmin(any())).thenReturn(false);
        when(currentUserService.getCurrentHuilerieIdOrThrow()).thenReturn(1L);
        when(stockRepository.findByLotOlives_Huilerie_IdHuilerie(1L)).thenReturn(stocks);
        when(stockMovementRepository.findByStock_IdStockOrderByDateMouvementAsc(anyLong()))
                .thenReturn(new java.util.ArrayList<>());

        // Execute
        List<StockDTO> result = stockService.findAllByHuilerieId(1L);

        // Assert : on devrait avoir un seul stock (les 3 sont fusionnés)
        assertEquals(1, result.size(), "Devrait avoir 1 seul stock après normalisation et fusion");
        assertEquals("arbequina", result.get(0).getVariete(),
                "La variété devrait être normalisée en minuscules");
    }

    /**
     * Test 2 : Vérifier qu'une même variété avec espaces différents
     * crée un seul stock après normalisation.
     */
    @Test
    void testStockNormalizationVarieteWithSpaces() {
        // Setup
        Stock stock1 = createStock(1L, "Arbequina Extra");
        Stock stock2 = createStock(2L, "arbequina  extra"); // Deux espaces
        Stock stock3 = createStock(3L, " Arbequina   Extra "); // Espaces au début/fin et multiples

        List<Stock> stocks = Arrays.asList(stock1, stock2, stock3);

        when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(createMockUtilisateur());
        when(currentUserService.isAdmin(any())).thenReturn(false);
        when(currentUserService.getCurrentHuilerieIdOrThrow()).thenReturn(1L);
        when(stockRepository.findByLotOlives_Huilerie_IdHuilerie(1L)).thenReturn(stocks);
        when(stockMovementRepository.findByStock_IdStockOrderByDateMouvementAsc(anyLong()))
                .thenReturn(new java.util.ArrayList<>());

        // Execute
        List<StockDTO> result = stockService.findAllByHuilerieId(1L);

        // Assert
        assertEquals(1, result.size(), "Devrait avoir 1 seul stock après normalisation");
        assertEquals("arbequina extra", result.get(0).getVariete(),
                "La variété devrait être normalisée avec espaces simples");
    }

    /**
     * Test 3 : Vérifier que les quantités sont sommées correctement
     * quand plusieurs stocks ont la même variété normalisée.
     */
    @Test
    void testStockQuantitiesAreSummed() {
        // Setup
        Stock stock1 = createStock(1L, "Koroneiki");
        stock1.setQuantiteDisponible(100.0);

        Stock stock2 = createStock(2L, "KORONEIKI"); // Même variété normalisée
        stock2.setQuantiteDisponible(50.0);

        Stock stock3 = createStock(3L, "Manzanilla");
        stock3.setQuantiteDisponible(75.0);

        List<Stock> stocks = Arrays.asList(stock1, stock2, stock3);

        when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(createMockUtilisateur());
        when(currentUserService.isAdmin(any())).thenReturn(false);
        when(currentUserService.getCurrentHuilerieIdOrThrow()).thenReturn(1L);
        when(stockRepository.findByLotOlives_Huilerie_IdHuilerie(1L)).thenReturn(stocks);
        when(stockMovementRepository.findByStock_IdStockOrderByDateMouvementAsc(anyLong()))
                .thenReturn(new java.util.ArrayList<>());

        // Execute
        List<StockDTO> result = stockService.findAllByHuilerieId(1L);

        // Assert
        assertEquals(2, result.size(), "Devrait avoir 2 stocks (Koroneiki + Manzanilla)");

        StockDTO koroneiki = result.stream()
                .filter(s -> "koroneiki".equals(s.getVariete()))
                .findFirst()
                .orElseThrow();
        assertEquals(150.0, koroneiki.getQuantiteDisponible(),
                "La quantité pour Koroneiki devrait être 100 + 50 = 150");

        StockDTO manzanilla = result.stream()
                .filter(s -> "manzanilla".equals(s.getVariete()))
                .findFirst()
                .orElseThrow();
        assertEquals(75.0, manzanilla.getQuantiteDisponible(),
                "La quantité pour Manzanilla devrait rester 75");
    }

    /**
     * Test 4 : Vérifier que différentes huileries ont leurs stocks séparés
     * même si les variétés sont identiques.
     */
    @Test
    void testStocksSeparatedByHuilerie() {
        // Setup
        Stock stockHuilerie1 = createStock(1L, "Arbequina");
        stockHuilerie1.setQuantiteDisponible(100.0);

        Stock stockHuilerie2 = createStock(2L, "Arbequina");
        stockHuilerie2.setQuantiteDisponible(50.0);
        stockHuilerie2.getLotOlives().getHuilerie().setIdHuilerie(2L); // Huilerie différente

        List<Stock> stocks = Arrays.asList(stockHuilerie1);

        when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(createMockUtilisateur());
        when(currentUserService.isAdmin(any())).thenReturn(false);
        when(currentUserService.getCurrentHuilerieIdOrThrow()).thenReturn(1L);
        when(stockRepository.findByLotOlives_Huilerie_IdHuilerie(1L)).thenReturn(stocks);
        when(stockMovementRepository.findByStock_IdStockOrderByDateMouvementAsc(anyLong()))
                .thenReturn(new java.util.ArrayList<>());

        // Execute
        List<StockDTO> result = stockService.findAllByHuilerieId(1L);

        // Assert
        assertEquals(1, result.size(), "Devrait avoir 1 stock pour huilerie 1");
        assertEquals(100.0, result.get(0).getQuantiteDisponible(),
                "La quantité ne devrait pas inclure celle d'une autre huilerie");
    }

    /**
     * Test 5 : Vérifier qu'une variété vide ou null n'est pas traitée.
     */
    @Test
    void testEmptyOrNullVarieteIsFiltered() {
        // Setup
        Stock stock1 = createStock(1L, "Arbequina");
        Stock stock2 = createStock(2L, null);
        Stock stock3 = createStock(3L, "");

        List<Stock> stocks = Arrays.asList(stock1, stock2, stock3);

        when(currentUserService.getAuthenticatedUtilisateur()).thenReturn(createMockUtilisateur());
        when(currentUserService.isAdmin(any())).thenReturn(false);
        when(currentUserService.getCurrentHuilerieIdOrThrow()).thenReturn(1L);
        when(stockRepository.findByLotOlives_Huilerie_IdHuilerie(1L)).thenReturn(stocks);
        when(stockMovementRepository.findByStock_IdStockOrderByDateMouvementAsc(anyLong()))
                .thenReturn(new java.util.ArrayList<>());

        // Execute
        List<StockDTO> result = stockService.findAllByHuilerieId(1L);

        // Assert
        assertEquals(1, result.size(), "Devrait avoir 1 seul stock (les stocks sans variété sont filtrés)");
        assertEquals("arbequina", result.get(0).getVariete());
    }

    // ===== Helper Methods =====

    private Stock createStock(Long id, String variete) {
        Stock stock = new Stock();
        stock.setIdStock(id);
        stock.setVariete(variete);
        stock.setTypeStock("MATIERE_PREMIERE");
        stock.setQuantiteDisponible(0.0);

        LotOlives lot = new LotOlives();
        lot.setIdLot(id);
        lot.setReference("LO-" + id);
        lot.setVariete(variete);
        lot.setHuilerie(huilerie);
        lot.setMatierePremiere(matierePremiere);

        stock.setLotOlives(lot);
        return stock;
    }

    private Models.Utilisateur createMockUtilisateur() {
        Models.Utilisateur user = new Models.Utilisateur();
                user.setIdUtilisateur(1L);
        user.setEmail("test@example.com");
        return user;
    }
}
