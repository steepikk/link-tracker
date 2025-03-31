package backend.academy.scrapper.client;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.exception.QuestionNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class StackOverflowClient {
    private final ScrapperConfig scrapperConfig;

    public Instant getLastUpdated(String questionUrl) {
        RestClient restClient = RestClient.create();

        String questionId = questionUrl.split("/")[4];
        String apiUrl = "https://api.stackexchange.com/2.3/questions/" + questionId + "?site=stackoverflow" + "&key="
                + scrapperConfig.stackOverflow().key() + "&access_token="
                + scrapperConfig.stackOverflow().accessToken();

        String response = "";
        try {
            response = restClient.get().uri(apiUrl).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            log.atError()
                    .addKeyValue("questionUrl", questionUrl)
                    .setMessage("Не удалось получить информацию о вопросе")
                    .log();
            throw new QuestionNotFoundException("Не удалось найти вопрос по ссылке");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response);

            JsonNode items = jsonResponse.get("items");
            if (items != null && items.size() > 0) {
                JsonNode firstItem = items.get(0);
                if (firstItem.has("last_activity_date")) {
                    long lastActivityDate = firstItem.get("last_activity_date").asLong();
                    return Instant.ofEpochSecond(lastActivityDate);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке ответа от API StackOverflow", e);
            throw new QuestionNotFoundException("Не удалось обработать данные ответа");
        }

        return null;
    }
}
