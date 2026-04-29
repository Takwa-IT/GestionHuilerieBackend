package Services;

import dto.EtapeProductionCreateDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideProductionTemplateFactoryTest {

    @Test
    void buildDefaultEtapes_shouldGenerateThreePhaseExtractionPlan() {
        List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("3_phase");

        assertEquals(7, etapes.size());
        assertEquals("Réception", etapes.get(0).getNom());
        assertEquals("Malaxage", etapes.get(3).getNom());
        assertEquals("Extraction", etapes.get(4).getNom());
        assertTrue(etapes.get(4).getDescription().contains("3 phases"));
        assertTrue(etapes.get(4).getDescription().contains("séparateur"));
        assertFalse(etapes.get(5).getDescription().contains("optionnelle"));
    }

    @Test
    void buildDefaultEtapes_shouldGenerateTwoPhaseExtractionPlan() {
        List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("2_phase");

        assertEquals(7, etapes.size());
        assertTrue(etapes.get(4).getDescription().contains("2 phases"));
        assertTrue(etapes.get(5).getDescription().contains("optionnelle"));
        assertEquals("vitesse_decanteur_tr_min", etapes.get(4).getParametres().get(0).getCodeParametre());
    }

    @Test
    void buildDefaultEtapes_shouldGeneratePressPlan() {
        List<EtapeProductionCreateDTO> etapes = GuideProductionTemplateFactory.buildDefaultEtapes("presse");

        assertEquals(7, etapes.size());
        assertTrue(etapes.get(4).getDescription().contains("presse hydraulique"));
        assertTrue(etapes.get(5).getDescription().contains("décantation naturelle"));
        assertEquals("2.5", etapes.get(4).getParametres().get(1).getValeur());
    }
}
