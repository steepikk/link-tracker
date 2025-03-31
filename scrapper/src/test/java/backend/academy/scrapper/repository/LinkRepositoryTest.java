package backend.academy.scrapper.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

import backend.academy.scrapper.model.LinkEntry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LinkRepositoryTest {
    private LinkRepository linkRepository;

    @BeforeEach
    public void setUp() {
        linkRepository = new LinkRepository();
    }

    @Test
    public void testAddOrUpdateLink() throws Exception {
        String url = "https://example.com";
        List<String> tags = List.of("tag1", "tag2");
        List<String> filters = List.of("filter1", "filter2");
        Long tgChatId = 12345L;

        LinkEntry linkEntry = linkRepository.addOrUpdateLink(url, tags, filters, tgChatId);

        assertNotNull(linkEntry);
        assertEquals(url, linkEntry.getUrl());
        assertTrue(linkEntry.getTags().contains("tag1"));
        assertTrue(linkEntry.getFilters().contains("filter1"));
        assertTrue(linkEntry.getTgChatIds().contains(tgChatId));
    }

    @Test
    public void testAddLink() throws Exception {
        String url = "https://example.com";
        List<String> tags = List.of("tag1");
        List<String> filters = List.of("filter1");
        Long tgChatId = 12345L;

        LinkEntry linkEntry = linkRepository.addOrUpdateLink(url, tags, filters, tgChatId);

        assertNotNull(linkEntry);
        assertEquals(url, linkEntry.getUrl());
    }

    @Test
    public void testRemoveLink() throws Exception {
        String url = "https://example.com";
        List<String> tags = List.of("tag1");
        List<String> filters = List.of("filter1");
        Long tgChatId = 12345L;

        linkRepository.addOrUpdateLink(url, tags, filters, tgChatId);

        LinkEntry removedLink = linkRepository.removeChatFromLink(url, tgChatId);

        assertNotNull(removedLink);
        assertEquals(url, removedLink.getUrl());
    }

    @Test
    public void testAddDuplicateLink() throws Exception {
        String url = "https://example.com";
        List<String> tags = List.of("tag1");
        List<String> filters = List.of("filter1");
        Long tgChatId = 12345L;

        LinkEntry firstEntry = linkRepository.addOrUpdateLink(url, tags, filters, tgChatId);

        LinkEntry secondEntry = linkRepository.addOrUpdateLink(url, tags, filters, tgChatId);

        assertSame(firstEntry, secondEntry, "Должен быть один объект для данной ссылки");
    }
}
