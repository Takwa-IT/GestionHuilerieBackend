// Teste : Config.ReferenceUtils — Formatage des références
package Config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests de ReferenceUtils")
class ReferenceUtilsTest {

    @Test
    @DisplayName("format id1 retourne LO01")
    void format_id1_retourneLO01() {
        String result = ReferenceUtils.format("LO", 1L);
        assertThat(result).isEqualTo("LO01");
    }

    @Test
    @DisplayName("format id10 retourne LO10")
    void format_id10_retourneLO10() {
        String result = ReferenceUtils.format("LO", 10L);
        assertThat(result).isEqualTo("LO10");
    }

    @Test
    @DisplayName("format id100 retourne LO100")
    void format_id100_retourneLO100() {
        String result = ReferenceUtils.format("LO", 100L);
        assertThat(result).isEqualTo("LO100");
    }

    @Test
    @DisplayName("format idNull retourne null")
    void format_idNull_retourneNull() {
        String result = ReferenceUtils.format("LO", null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("format prefixMP retourne MP01")
    void format_prefixMP_retourneMP01() {
        String result = ReferenceUtils.format("MP", 1L);
        assertThat(result).isEqualTo("MP01");
    }
}
