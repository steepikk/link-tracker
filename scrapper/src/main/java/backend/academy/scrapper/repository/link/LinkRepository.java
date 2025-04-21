package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    Optional<Link> findByUrl(String url);

    Long insertLink(String url, Instant lastUpdated);

    Link update(Link link);

    void updateLinkData(Long id, String url, Instant lastUpdated);

    void deleteLink(Long id);

    void insertTags(Long linkId, List<String> tags);

    void deleteTags(Long linkId);

    void insertFilters(Long linkId, List<String> filters);

    void deleteFilters(Long linkId);

    void insertChat(Long chatId);

    void insertLinkChat(Long linkId, Long chatId);

    void deleteLinkChat(Long linkId, Long chatId);

    int countChatsByLinkId(Long linkId);

    Collection<Link> getAllLinks();

    Collection<Link> findByTag(String tag);

    List<String> getAllTags();

    void deleteTag(String tag);
}