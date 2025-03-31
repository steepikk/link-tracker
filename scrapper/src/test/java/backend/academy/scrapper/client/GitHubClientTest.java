package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.exception.RepositoryNotFoundException;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

public class GitHubClientTest {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(new WireMockConfiguration().dynamicPort())
            .build();

    @Test
    public void testHttp401Error() {
        ScrapperConfig scrapperConfig = Mockito.mock(ScrapperConfig.class);
        Mockito.when(scrapperConfig.githubToken()).thenReturn("test-token");

        GitHubClient client = new GitHubClient(scrapperConfig);

        String url = wireMockServer.baseUrl() + "/notfound";

        wireMockServer.stubFor(
                get(urlEqualTo("/notfound")).willReturn(aResponse().withStatus(401)));

        assertThrows(RepositoryNotFoundException.class, () -> {
            client.getLastUpdated(url);
        });
    }
}
