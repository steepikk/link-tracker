package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.LinkEntry;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class LinkRepository {

    private final Map<String, LinkEntry> links = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public LinkEntry addOrUpdateLink(String url, List<String> tags, List<String> filters, Long tgChatId)
            throws IllegalArgumentException {

        if (!isLinkAlive(url)) {
            throw new IllegalArgumentException();
        }
        return links.compute(url, (key, existing) -> {
            if (existing == null) {
                LinkEntry newEntry = new LinkEntry(idGenerator.getAndIncrement(), url, tags, filters);
                newEntry.addChat(tgChatId);
                return newEntry;
            } else {
                existing.addChat(tgChatId);
                return existing;
            }
        });
    }

    public LinkEntry removeChatFromLink(String url, Long tgChatId) throws Exception {
        LinkEntry entry = links.get(url);
        if (entry != null) {
            entry.removeChat(tgChatId);
            if (entry.getTgChatIds().isEmpty()) {
                links.remove(url);
            }
        }
        return entry;
    }

    public Collection<LinkEntry> getAllLinks() {
        return links.values();
    }

    public void updateLink(LinkEntry linkEntry) {
        links.put(linkEntry.getUrl(), linkEntry);
    }

    public boolean isLinkAlive(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            return 200 <= responseCode && responseCode < 400;
        } catch (IOException e) {
            return false;
        }
    }
}
