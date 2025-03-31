package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.error.ApiError;
import backend.academy.common.dto.ListLinksResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ListCommand implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public ListCommand(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/list";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        Mono<ListLinksResponse> monoResponse = scrapperClient.getLinks(chatId);
        monoResponse.subscribe(
                response -> {
                    if (response.getLinks() == null || response.getLinks().isEmpty()) {
                        telegramClient.sendMessage(chatId, "Список отслеживаемых ссылок пуст.");
                    } else {
                        StringBuilder sb = new StringBuilder("Отслеживаемые ссылки:\n");
                        response.getLinks()
                                .forEach(link -> sb.append(link.getUrl()).append("\n"));
                        telegramClient.sendMessage(chatId, sb.toString());
                    }
                },
                error -> sendErrorInfo(chatId, error, "Ошибка получения списка ссылок"));
    }

    private void sendErrorInfo(Long chatId, Throwable error, String msg) {
        if (error instanceof ApiError apiError) {
            // Если ошибка является экземпляром ApiError, выводим описание
            telegramClient.sendMessage(chatId, msg + ":\n " + apiError.getDescription());
        } else {
            // В случае других ошибок выводим стандартное сообщение
            telegramClient.sendMessage(chatId, msg + ":\n " + error.getMessage());
        }
    }
}
