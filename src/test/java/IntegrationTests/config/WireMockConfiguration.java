package IntegrationTests.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class WireMockConfiguration {

    static final WireMockServer wireMockServer = new WireMockServer(8089);

    static {
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    public static void reset() {
        wireMockServer.resetAll();
    }

    public static void stop() {
        wireMockServer.stop();
    }
}
