package backend.academy.bot.bot;

import static org.mockito.Mockito.verify;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.command.CommandHandler;
import backend.academy.bot.session.UserSessionManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UnknownCommandTest {

    @Test
    public void testUnknownCommand() {
        TelegramClient telegramClient = Mockito.mock(TelegramClient.class);

        CommandHandler startCommand = Mockito.mock(CommandHandler.class);
        Mockito.when(startCommand.getCommand()).thenReturn("/start");

        UserSessionManager userSessionManager = new UserSessionManager(telegramClient, null, List.of(startCommand));

        Long chatId = 12345L;

        userSessionManager.start(chatId, "/start");

        verify(startCommand).handle(chatId, "/start");

        userSessionManager.start(chatId, "просто какой-то текст");

        verify(telegramClient).sendMessage(chatId, "Неизвестная команда. Введите /help для списка команд.");
    }
}
