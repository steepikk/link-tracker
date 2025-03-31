package backend.academy.bot.session;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.error.ApiError;
import backend.academy.common.dto.LinkResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TrackingService {
    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public TrackingService(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    public void completeTracking(Long chatId, UserSession userSession) {
        String link = userSession.getLink();
        List<String> tags = userSession.getTags();
        List<String> filters = userSession.getFilters();

        Mono<LinkResponse> result = scrapperClient.addLink(chatId, link, tags, filters);
        result.subscribe(
                response -> telegramClient.sendMessage(chatId, "Ссылка успешно добавлена: " + response.getUrl()),
                error -> sendErrorInfo(chatId, error));

        userSession.reset();
    }

    private void sendErrorInfo(Long chatId, Throwable error) {
        if (error instanceof ApiError) {
            ApiError apiError = (ApiError) error;
            telegramClient.sendMessage(chatId, apiError.getDescription());
        } else {
            telegramClient.sendMessage(chatId, error.getMessage());
        }
    }
}
