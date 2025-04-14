package backend.academy.scrapper.repository.orm;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM", matchIfMissing = true)
public interface LinkRepositoryORM extends JpaRepository<Link, Long>, LinkRepository {
    Optional<Link> findByUrl(String url);

    default Link addOrUpdateLink(String url, List<String> tags, List<String> filters, Long tgChatId)
            throws IllegalArgumentException {
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
}
