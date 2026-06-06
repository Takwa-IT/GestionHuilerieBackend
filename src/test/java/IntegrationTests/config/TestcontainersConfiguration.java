package IntegrationTests.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration
public class TestcontainersConfiguration {

    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    static {
        mysql.start();
    }

    public static String getJdbcUrl() {
        return mysql.getJdbcUrl() + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static String getUsername() {
        return mysql.getUsername();
    }

    public static String getPassword() {
        return mysql.getPassword();
    }
}
