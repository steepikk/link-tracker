package backend.academy.scrapper.client;

import backend.academy.common.dto.ContentUpdate;
import backend.academy.common.dto.UpdateType;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.exception.RepositoryNotFoundException;
import backend.academy.scrapper.util.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
public class GitHubClient {
    private final ScrapperConfig scrapperConfig;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final LinkToApiRequestConverter linkToApiRequestConverter;

    public GitHubClient(ScrapperConfig scrapperConfig, @Qualifier("default") RestClient restClient, ObjectMapper objectMapper, LinkToApiRequestConverter linkToApiRequestConverter) {
        this.scrapperConfig = scrapperConfig;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.linkToApiRequestConverter = linkToApiRequestConverter;
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

    public List<ContentUpdate> getGitHubContent(String link) throws RepositoryNotFoundException, HttpMessageNotReadableException {
        String response = "";
        try {
            response = restClient
                    .get()
                    .uri(linkToApiRequestConverter.convertGithubLinkToIssueApi(link))
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer " + scrapperConfig.githubToken())
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            log.atError()
                    .addKeyValue("link", link)
                    .setMessage("Не удалось найти репозиторий")
                    .log();
            throw new RepositoryNotFoundException("Репозиторий не найден");
        }

        try {
            JsonNode jsonResponse = objectMapper.readTree(response);

            List<ContentUpdate> contents = new ArrayList<>();

            jsonResponse.forEach(content -> {
                String title = content.get("title").asText();
                String userName = content.get("user").get("login").asText();
                String createdAt = content.get("created_at").asText();
                Instant createdInstant = Instant.parse(createdAt);
                String body = content.get("body").asText();
                body = body.equals("null") ? "" : body;
                if (content.has("pull_request")) {
                    contents.add(new ContentUpdate(UpdateType.PR, title, userName, createdInstant, body));
                } else {
                    contents.add(new ContentUpdate(UpdateType.ISSUE, title, userName, createdInstant, body));
                }
            });
            return contents;
        } catch (HttpMessageNotReadableException | JsonProcessingException e) {
            log.atError()
                    .addKeyValue("link", link)
                    .setMessage("Ошибка при получении github контента")
                    .log();
            throw new HttpMessageNotReadableException("Не удаётся прочитать поле 'updated_at'");
        }

    }
}
