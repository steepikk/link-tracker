package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class LinkRepositorySQL implements LinkRepository {
    @Override
    public Optional<Link> findByUrl(String url) {
        return Optional.empty();
    }

    @Override
    public Link addOrUpdateLink(String url, List<String> tags, List<String> filters, Long tgChatId) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Link removeChatFromLink(String url, Long tgChatId) throws Exception {
        return null;
    }

    @Override
    public Collection<Link> getAllLinks() {
        return List.of();
    }

    @Override
    public void updateLink(Link link) {

    }

    @Override
    public boolean isLinkAlive(String url) {
        return false;
    }
}
