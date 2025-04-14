package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.error.ApiError;
import backend.academy.common.dto.ListLinksResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ListByTagCommand implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public ListByTagCommand(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/listbytag";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        String[] parts = messageText.trim().split("\\s+");
        if (parts.length < 2) {
            telegramClient.sendMessage(chatId, "Использование: /listbytag <тег>");
            return;
        }

        String tag = parts[1];
        Mono<ListLinksResponse> result = scrapperClient.getLinksByTag(chatId, tag);
        result.subscribe(
                response -> {
                    if (response.getLinks().isEmpty()) {
                        telegramClient.sendMessage(chatId, "Ссылок с тегом '" + tag + "' не найдено.");
                    } else {
                        StringBuilder message = new StringBuilder("Ссылки с тегом '" + tag + "':\n");
                        response.getLinks().forEach(link -> message.append("- ").append(link.getUrl()).append("\n"));
                        telegramClient.sendMessage(chatId, message.toString());
                    }
                },
                error -> sendErrorInfo(chatId, error, "Ошибка получения ссылок по тегу")
        );
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