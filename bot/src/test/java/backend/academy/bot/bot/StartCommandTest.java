package backend.academy.bot.bot;

import static org.mockito.Mockito.verify;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.command.StartCommand;
import backend.academy.bot.error.ApiError;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

public class StartCommandTest {
    @Test
    public void testStartCommandSuccess() {
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        TelegramClient telegramClient = Mockito.mock(TelegramClient.class);
        StartCommand startCommand = new StartCommand(scrapperClient, telegramClient);

        Long chatId = 12345L;

        Mockito.when(scrapperClient.registerChat(chatId)).thenReturn(Mono.empty());

        startCommand.handle(chatId, "/start");

        verify(scrapperClient).registerChat(chatId);

        verify(telegramClient).sendMessage(chatId, startCommand.getWelcomeMessage());
    }

    @Test
    public void testStartCommandFailure() {
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        TelegramClient telegramClient = Mockito.mock(TelegramClient.class);
        StartCommand startCommand = new StartCommand(scrapperClient, telegramClient);

        Long chatId = 12345L;

        String errorMessage = "Ошибка регистрации: Чат уже зарегистрирован";
        Mockito.when(scrapperClient.registerChat(chatId))
                .thenReturn(Mono.error(
                        new ApiError(errorMessage, "400", "BadRequest", "Чат уже зарегистрирован", List.of())));

        startCommand.handle(chatId, "/start");

        verify(telegramClient).sendMessage(chatId, "Ошибка регистрации: " + errorMessage);
    }
}
