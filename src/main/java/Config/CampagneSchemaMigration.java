package Config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampagneSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void dropUniqueIndexOnCampagneAnneeIfExists() {
        try {
            List<String> uniqueIndexNames = jdbcTemplate.queryForList(
                    """
                    select distinct s.index_name
                    from information_schema.statistics s
                    where s.table_schema = database()
                      and s.table_name = 'campagne_olives'
                      and s.column_name = 'annee'
                      and s.non_unique = 0
                      and s.index_name <> 'PRIMARY'
                    """,
                    String.class
            );

            for (String indexName : uniqueIndexNames) {
                String sql = "ALTER TABLE campagne_olives DROP INDEX `" + indexName + "`";
                jdbcTemplate.execute(sql);
                log.info("[DB MIGRATION] Dropped unique index '{}' on campagne_olives.annee", indexName);
            }
        } catch (Exception ex) {
            log.warn("[DB MIGRATION] Unable to drop unique index on campagne_olives.annee: {}", ex.getMessage());
        }
    }
}
