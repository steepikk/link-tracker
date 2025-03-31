package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.error.ApiError;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StartCommand implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public StartCommand(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        Mono<Void> result = scrapperClient
                .registerChat(chatId)
                .doOnSuccess(unused -> telegramClient.sendMessage(chatId, getWelcomeMessage()))
                .doOnError(error -> {
                    if (error instanceof ApiError) {
                        telegramClient.sendMessage(
                                chatId, "Ошибка регистрации: " + ((ApiError) error).getDescription());
                    } else {
                        telegramClient.sendMessage(chatId, "Ошибка регистрации: " + error.getMessage());
                    }
                })
                .then();
        result.subscribe();
    }

    public String getWelcomeMessage() {
        return """
                👋 Привет! Я бот для отслеживания изменений на интересующих вас страницах.

                📌 Доступные команды:
                /track <ссылка> – добавить ссылку в отслеживание
                /untrack <ссылка> – удалить ссылку из списка
                /list – показать все отслеживаемые ссылки

                🔍 Просто отправь команду, и я начну следить за изменениями!
                """;
    }
}
