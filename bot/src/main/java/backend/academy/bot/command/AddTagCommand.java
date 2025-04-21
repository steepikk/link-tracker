package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.common.dto.LinkResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AddTagCommand implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public AddTagCommand(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/addtag";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        String[] parts = messageText.trim().split("\\s+", 3);
        if (parts.length < 3) {
            telegramClient.sendMessage(chatId, "Использование: /addtag <ссылка> <тег>");
            return;
        }

        String url = parts[1];
        String tag = parts[2];

        Mono<LinkResponse> result = scrapperClient
                .addTag(chatId, url, tag)
                .doOnSuccess(response -> telegramClient.sendMessage(chatId, "Тег '" + tag + "' добавлен к ссылке: " + response.getUrl()))
                .doOnError(error -> telegramClient.sendMessage(chatId, "Ошибка добавления тэга '" + tag + "' к ссылке '" + url + "'. Проверьте корректность введённых данных."));
        result.subscribe();
    }
}