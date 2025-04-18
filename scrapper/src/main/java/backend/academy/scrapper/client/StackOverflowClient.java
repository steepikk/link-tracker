package backend.academy.scrapper.client;

import backend.academy.common.dto.ContentUpdate;
import backend.academy.common.dto.UpdateType;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.exception.QuestionNotFoundException;
import backend.academy.scrapper.util.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@Slf4j
public class StackOverflowClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final LinkToApiRequestConverter linkToApiRequestConverter;

    private final String key;
    private final String access_token;
    private final String filter;

    private static final String JSON_ERROR = "Ошибка при разборе JSON-Ответа";
    private static final String REQUEST_ERROR = "Ошибка при запросе";

    public StackOverflowClient(ScrapperConfig scrapperConfig, RestClient restClient, ObjectMapper objectMapper, LinkToApiRequestConverter linkToApiRequestConverter) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.linkToApiRequestConverter = linkToApiRequestConverter;

        this.key = scrapperConfig.stackOverflow().key();
        this.access_token = scrapperConfig.stackOverflow().accessToken();
        this.filter = "withbody";
    }

    private String performRequest(String url) {
        url += "&key=" + key + "&access_token=" + access_token + "&filter=" + filter;

        try {
            return restClient.get().uri(url).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            log.atError().addKeyValue("link", url).setMessage(REQUEST_ERROR).log();
            throw new QuestionNotFoundException("Ошибка при запросе: " + url);
        }
    }

    private List<ContentUpdate> parseContentList(String jsonResponse, UpdateType type, String title, String url) {
        List<ContentUpdate> stackOverflowContentList = new ArrayList<>();
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            jsonNode.get("items").forEach(item -> {
                String ownerName = item.get("owner").get("display_name").asText();
                String body = item.get("body").asText();
                String creationDate = item.get("creation_date").asText();
                Instant createdInstant = Instant.parse(creationDate);
                stackOverflowContentList.add(new ContentUpdate(type, title, ownerName, createdInstant, body));
            });
        } catch (Exception e) {
            log.atError().addKeyValue("link", url).setMessage(JSON_ERROR).log();
            throw new HttpMessageNotReadableException("Ошибка при разборе JSON-ответа по URL " + url);
        }
        return stackOverflowContentList;
    }

    public String getTitle(String link) {
        String url = linkToApiRequestConverter.convertSOtoQuestion(link);
        String jsonResponse = performRequest(url);
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            return jsonNode.get("items").get(0).get("title").asText();
        } catch (HttpMessageNotReadableException | JsonProcessingException e) {
            log.atError().addKeyValue("link", url).setMessage(JSON_ERROR).log();
            throw new HttpMessageNotReadableException("Ошибка при разборе JSON-ответа по URL " + url);
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("link", url)
                    .setMessage("Некорректное тело запроса")
                    .log();
            throw new RuntimeException("Некорректное тело запроса по URL " + url);
        }
    }

    private List<ContentUpdate> getComments(String link, String title) {
        String soUrl = linkToApiRequestConverter.convertSOtoQuestionComments(link);
        return parseContentList(performRequest(soUrl), UpdateType.COMMENT, title, soUrl);
    }

    private List<ContentUpdate> getAnswers(String link, String title) {
        String soUrl = linkToApiRequestConverter.convertSOtoQuestionAnswers(link);
        return parseContentList(performRequest(soUrl), UpdateType.ANSWER, title, soUrl);
    }

    private List<ContentUpdate> getAnswerComments(String link, String title) {
        List<ContentUpdate> stackOverflowContentList = new ArrayList<>();
        String url = linkToApiRequestConverter.convertSOtoQuestionAnswers(link);
        String jsonResponse = performRequest(url);

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            jsonNode.get("items").forEach(item -> {
                String answerId = item.get("answer_id").asText();
                String commentsUrl = linkToApiRequestConverter.convertSOtoAnswerCommentsLink(answerId, link);
                stackOverflowContentList.addAll(
                        parseContentList(performRequest(commentsUrl), UpdateType.COMMENT, title, commentsUrl));
            });
        } catch (HttpMessageNotReadableException | JsonProcessingException e) {
            log.atError().addKeyValue("link", url).setMessage(REQUEST_ERROR).log();
            throw new HttpMessageNotReadableException(e.getMessage());
        } catch (Exception e) {
            log.atError().addKeyValue("link", url).setMessage(REQUEST_ERROR).log();
            throw new QuestionNotFoundException(e.getMessage());
        }

        return stackOverflowContentList;
    }

    public List<ContentUpdate> getSOContent(String link) {
        String title = getTitle(link);

        List<ContentUpdate> stackOverflowContentList = new ArrayList<>();
        stackOverflowContentList.addAll(getComments(link, title));
        stackOverflowContentList.addAll(getAnswers(link, title));
        stackOverflowContentList.addAll(getAnswerComments(link, title));

        return stackOverflowContentList;
    }
}
