package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.error.ApiError;
import backend.academy.common.dto.LinkResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UntrackCommand implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public UntrackCommand(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/untrack";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length < 2) {
            telegramClient.sendMessage(chatId, "Использование: /untrack <ссылка>");
            return;
        }
        String link = parts[1];
        Mono<LinkResponse> result = scrapperClient.removeLink(chatId, link);
        result.subscribe(
                response -> telegramClient.sendMessage(chatId, "Ссылка успешно удалена: " + response.getUrl()),
                error -> sendErrorInfo(chatId, error, "Ошибка удаления ссылки"));
    }

    private void sendErrorInfo(Long chatId, Throwable error, String msg) {
        if (error instanceof ApiError) {
            ApiError apiError = (ApiError) error;
            telegramClient.sendMessage(chatId, msg + ":\n " + apiError.getDescription());
        } else {
            telegramClient.sendMessage(chatId, msg + ":\n " + error.getMessage());
        }
    }
}
