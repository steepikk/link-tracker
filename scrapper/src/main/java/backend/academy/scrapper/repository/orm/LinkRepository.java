package backend.academy.scrapper.repository.orm;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByUrl(String url);

    default Link addOrUpdateLink(String url, List<String> tags, List<String> filters, Long tgChatId)
            throws IllegalArgumentException {
        if (!isLinkAlive(url)) {
            throw new IllegalArgumentException("Link is not alive");
        }

        Link linkEntity = findByUrl(url).orElseGet(() -> {
            Link newEntry = new Link(url, tags, filters);
            return save(newEntry);
        });

        linkEntity.addChat(new Chat(tgChatId));
        return save(linkEntity);
    }

    default Link removeChatFromLink(String url, Long tgChatId) throws Exception {
        Optional<Link> optionalLink = findByUrl(url);
        if (optionalLink.isEmpty()) {
            return null;
        }

        Link linkEntity = optionalLink.get();
        linkEntity.removeChat(new Chat(tgChatId));
        System.out.println(linkEntity.chats().isEmpty());
        System.out.println(linkEntity.chats());

        if (linkEntity.chats().isEmpty()) {
            delete(linkEntity);
            return null;
        } else {
            return save(linkEntity);
        }
    }

    default Collection<Link> getAllLinks() {
        return findAll();
    }

    default void updateLink(Link link) {
        save(link);
    }

    default boolean isLinkAlive(String url) {
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
