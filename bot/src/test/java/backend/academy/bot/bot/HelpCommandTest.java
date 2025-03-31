package backend.academy.bot.bot;

import static org.mockito.Mockito.verify;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.command.HelpCommand;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HelpCommandTest {
    @Test
    public void testHelpCommand() {
        TelegramClient telegramClient = Mockito.mock(TelegramClient.class);
        HelpCommand helpCommand = new HelpCommand(telegramClient);

        Long chatId = 12345L;
        helpCommand.handle(chatId, "/help");

        String expectedMessage = "Доступные команды:\n" + "/help - список команд\n"
                + "/track - начать отслеживание ссылки\n"
                + "/untrack - прекратить отслеживание ссылки\n"
                + "/list - показать список отслеживаемых ссылок";

        verify(telegramClient).sendMessage(chatId, expectedMessage);
    }
}
