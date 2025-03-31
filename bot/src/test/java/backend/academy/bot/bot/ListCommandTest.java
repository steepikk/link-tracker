package backend.academy.bot.bot;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.command.ListCommand;
import backend.academy.common.dto.LinkResponse;
import backend.academy.common.dto.ListLinksResponse;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

public class ListCommandTest {
    @Test
    public void testListCommand() {
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        TelegramClient telegramClient = Mockito.mock(TelegramClient.class);
        ListCommand listCommand = new ListCommand(scrapperClient, telegramClient);

        Long chatId = 12345L;
        List<LinkResponse> links =
                List.of(new LinkResponse(1L, "https://example.com", Collections.emptyList(), Collections.emptyList()));

        when(scrapperClient.getLinks(chatId)).thenReturn(Mono.just(new ListLinksResponse(links, links.size())));

        listCommand.handle(chatId, "/list");

        verify(telegramClient).sendMessage(chatId, "Отслеживаемые ссылки:\nhttps://example.com\n");
    }

    @Test
    public void testListCommandFormat() {
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        TelegramClient telegramClient = Mockito.mock(TelegramClient.class);
        ListCommand listCommand = new ListCommand(scrapperClient, telegramClient);

        Long chatId = 12345L;

        Mockito.when(scrapperClient.getLinks(chatId))
                .thenReturn(Mono.just(new ListLinksResponse(
                        List.of(
                                new LinkResponse(
                                        1L, "https://example.com", Collections.emptyList(), Collections.emptyList()),
                                new LinkResponse(
                                        12L, "https://another.com", Collections.emptyList(), Collections.emptyList())),
                        2)));

        listCommand.handle(chatId, "/list");

        verify(telegramClient).sendMessage(chatId, "Отслеживаемые ссылки:\nhttps://example.com\nhttps://another.com\n");
    }
}
