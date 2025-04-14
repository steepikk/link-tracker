package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.error.ApiError;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DeleteTagCommand implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public DeleteTagCommand(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/deletetag";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        String[] parts = messageText.trim().split("\\s+");
        if (parts.length < 2) {
            telegramClient.sendMessage(chatId, "Использование: /deletetag <тег>");
            return;
        }

        String tag = parts[1];
        Mono<Void> result = scrapperClient
                .deleteTag(tag)
                .doOnSuccess(unused -> telegramClient.sendMessage(chatId, "Тег '" + tag + "' успешно удален."))
                .doOnError(error -> sendErrorInfo(chatId, error, "Ошибка удаления тега"))
                .then();
        result.subscribe();
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