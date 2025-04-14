package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.error.ApiError;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ListTagsCommand implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public ListTagsCommand(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/listtags";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        Mono<List<String>> result = scrapperClient.getAllTags();
        result.subscribe(
                tags -> {
                    if (tags.isEmpty()) {
                        telegramClient.sendMessage(chatId, "Теги отсутствуют.");
                    } else {
                        StringBuilder message = new StringBuilder("Доступные теги:\n");
                        tags.forEach(tag -> message.append("- ").append(tag).append("\n"));
                        telegramClient.sendMessage(chatId, message.toString());
                    }
                },
                error -> sendErrorInfo(chatId, error, "Ошибка получения списка тегов")
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