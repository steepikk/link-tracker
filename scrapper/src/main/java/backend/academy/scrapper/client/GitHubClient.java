package backend.academy.scrapper.client;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.exception.RepositoryNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
public class GitHubClient {
    private final ScrapperConfig scrapperConfig;

    public GitHubClient(ScrapperConfig scrapperConfig) {
        this.scrapperConfig = scrapperConfig;
    }

    public Instant getLastUpdated(String repoUrl) {
        String apiUrl = repoUrl.replace("https://github.com/", "https://api.github.com/repos/");

        RestClient restClient = RestClient.create();
        String response = "";
        try {
            response = restClient
                    .get()
                    .uri(apiUrl)
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer " + scrapperConfig.githubToken())
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new RepositoryNotFoundException("Репозиторий не найден");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response);
            return Instant.parse(jsonResponse.get("updated_at").asText());
        } catch (HttpMessageNotReadableException | JsonProcessingException e) {
            throw new HttpMessageNotReadableException("Не удаётся прочитать поле 'updated_at'");
        }
    }
}
