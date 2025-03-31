package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.StackOverflowCredentials;
import backend.academy.scrapper.exception.QuestionNotFoundException;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

public class StackOverFlowClientTest {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(new WireMockConfiguration().dynamicPort())
            .build();

    @Test
    public void testHttp404Error() {
        StackOverflowCredentials stackOverflowCredentials = Mockito.mock(StackOverflowCredentials.class);
        Mockito.when(stackOverflowCredentials.key()).thenReturn("api-key");
        Mockito.when(stackOverflowCredentials.accessToken()).thenReturn("access-token");

        ScrapperConfig scrapperConfig = Mockito.mock(ScrapperConfig.class);
        Mockito.when(scrapperConfig.stackOverflow()).thenReturn(stackOverflowCredentials);

        StackOverflowClient client = new StackOverflowClient(scrapperConfig);
        String url = wireMockServer.baseUrl() + "/questions/12345";

        wireMockServer.stubFor(
                get(urlEqualTo("/questions/12345")).willReturn(aResponse().withStatus(404)));

        assertThrows(QuestionNotFoundException.class, () -> {
            client.getLastUpdated(url);
        });
    }

    @Test
    public void testGetLastUpdatedInvalidResponse() throws Exception {
        StackOverflowCredentials stackOverflowCredentials = Mockito.mock(StackOverflowCredentials.class);
        Mockito.when(stackOverflowCredentials.key()).thenReturn("api-key");
        Mockito.when(stackOverflowCredentials.accessToken()).thenReturn("access-token");

        ScrapperConfig scrapperConfig = Mockito.mock(ScrapperConfig.class);
        Mockito.when(scrapperConfig.stackOverflow()).thenReturn(stackOverflowCredentials);

        StackOverflowClient client = new StackOverflowClient(scrapperConfig);
        String questionUrl = wireMockServer.baseUrl() + "/questions/12345";

        wireMockServer.stubFor(get(urlEqualTo("/questions/12345"))
                .willReturn(aResponse().withStatus(200).withBody("{\"items\": [{\"invalid_field\": \"value\"}]}")));

        assertThrows(QuestionNotFoundException.class, () -> {
            client.getLastUpdated(questionUrl);
        });
    }
}
