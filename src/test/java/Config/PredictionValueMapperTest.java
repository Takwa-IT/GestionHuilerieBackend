// Teste : Config.PredictionValueMapper — Mapping des valeurs frontend vers backend
package Config;

import dto.PredictionInputDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests de PredictionValueMapper")
class PredictionValueMapperTest {

    @Test
    @DisplayName("mapValue region sfax mapped to Nord")
    void mapValue_region_sfaxMappedToNord() {
        String result = PredictionValueMapper.mapValue("region", "Sfax");
        assertThat(result).isEqualTo("Nord");
    }

    @Test
    @DisplayName("mapValue region centre mapped to Centre")
    void mapValue_region_centreMappedToCentre() {
        String result = PredictionValueMapper.mapValue("region", "centre");
        assertThat(result).isEqualTo("Centre");
    }

    @Test
    @DisplayName("mapValue typeMachine 2phases mapped to 2phase")
    void mapValue_typeMachine_2phasesMappedTo2phase() {
        String result = PredictionValueMapper.mapValue("typeMachine", "2_phases");
        assertThat(result).isEqualTo("2_phase");
    }

    @Test
    @DisplayName("mapValue typeMachine presse hydraulique mapped to presse")
    void mapValue_typeMachine_presseHydrauliqueMappedToPresse() {
        String result = PredictionValueMapper.mapValue("typeMachine", "presse_hydraulique");
        assertThat(result).isEqualTo("presse");
    }

    @Test
    @DisplayName("mapValue typeSol argile mapped to argileux")
    void mapValue_typeSol_argileMappedToArgileux() {
        String result = PredictionValueMapper.mapValue("typeSol", "argile");
        assertThat(result).isEqualTo("argileux");
    }

    @Test
    @DisplayName("mapValue champ inconnu retourne valeur originale")
    void mapValue_champInconnu_retourneValeurOriginale() {
        String result = PredictionValueMapper.mapValue("unknown", "unknown");
        assertThat(result).isEqualTo("unknown");
    }

    @Test
    @DisplayName("mapValue valeur null retourne null")
    void mapValue_valeurNull_retourneNull() {
        String result = PredictionValueMapper.mapValue("region", null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("normalizeEnumFields mappe tous les champs")
    void normalizeEnumFields_mappeTousLesChamps() {
        PredictionInputDTO dto = new PredictionInputDTO();
        dto.setTypeSol("argile");
        dto.setRegion("Sfax");
        dto.setTypeMachine("3_phases");

        PredictionValueMapper.normalizeEnumFields(dto);

        assertThat(dto.getTypeSol()).isEqualTo("argileux");
        assertThat(dto.getRegion()).isEqualTo("Nord");
        assertThat(dto.getTypeMachine()).isEqualTo("3_phase");
    }
}
