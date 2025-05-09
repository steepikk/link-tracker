package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM", matchIfMissing = true)
public interface LinkRepositoryORM extends JpaRepository<Link, Long>, LinkRepository {

    Optional<Link> findByUrl(String url);

    @Override
    default Long insertLink(String url, Instant lastUpdated) {
        Link link = new Link(url, List.of(), List.of());
        link.lastUpdated(lastUpdated);
        return save(link).id();
    }

    @Override
    default void updateLinkData(Long id, String url, Instant lastUpdated) {
        findById(id).ifPresent(link -> {
            link.url(url);
            link.lastUpdated(lastUpdated);
            save(link);
        });
    }

    @Override
    default void deleteLink(Long id) {
        deleteById(id);
    }

    @Override
    default void insertTags(Long linkId, List<String> tags) {
        findById(linkId).ifPresent(link -> {
            List<String> newTags = new ArrayList<>(link.tags());
            newTags.addAll(tags);
            link.tags(newTags);
            save(link);
        });
    }

    @Override
    default Link update(Link link) {
        return findById(link.id()).map(existingLink -> {
            existingLink.url(link.url());
            existingLink.lastUpdated(link.lastUpdated());

            existingLink.tags().clear();
            existingLink.tags().addAll(link.tags());

            existingLink.filters().clear();
            existingLink.filters().addAll(link.filters());

            existingLink.chats().clear();
            existingLink.chats().addAll(link.chats());

            return save(existingLink);
        }).orElseThrow(() -> new IllegalArgumentException("Link not found with id: " + link.id()));
    }

    @Override
    default void deleteTags(Long linkId) {
        findById(linkId).ifPresent(link -> {
            link.tags(new ArrayList<>());
            save(link);
        });
    }

    @Override
    default void insertFilters(Long linkId, List<String> filters) {
        findById(linkId).ifPresent(link -> {
            List<String> newFilters = new ArrayList<>(link.filters());
            newFilters.addAll(filters);
            link.filters(newFilters);
            save(link);
        });
    }

    @Override
    default void deleteFilters(Long linkId) {
        findById(linkId).ifPresent(link -> {
            link.filters(new ArrayList<>());
            save(link);
        });
    }

    @Override
    default void insertChat(Long chatId) {
        Link link = findById(1L).orElse(null);
        if (link != null) {
            link.addChat(new Chat(chatId));
            save(link);
        }
    }

    @Override
    default void insertLinkChat(Long linkId, Long chatId) {
        findById(linkId).ifPresent(link -> {
            link.addChat(new Chat(chatId));
            save(link);
        });
    }

    @Override
    default void deleteLinkChat(Long linkId, Long chatId) {
        findById(linkId).ifPresent(link -> {
            link.removeChat(new Chat(chatId));
            save(link);
        });
    }

    @Override
    default int countChatsByLinkId(Long linkId) {
        return findById(linkId)
                .map(link -> link.chats().size())
                .orElse(0);
    }

    @Override
    default Collection<Link> getAllLinks() {
        return findAll();
    }

    @Override
    default Collection<Link> findByTag(String tag) {
        return findAll().stream()
                .filter(link -> link.tags().contains(tag))
                .collect(Collectors.toList());
    }

    @Override
    default List<String> getAllTags() {
        return findAll().stream()
                .flatMap(link -> link.tags().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    default void deleteTag(String tag) {
        findAll().forEach(link -> {
            List<String> updatedTags = new ArrayList<>(link.tags());
            updatedTags.remove(tag);
            link.tags(updatedTags);
            save(link);
        });
    }
}