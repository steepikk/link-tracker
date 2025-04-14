package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Link;

import java.util.Collection;
import java.util.List;

public interface LinkService {
    Link addOrUpdateLink(String url, List<String> tags, List<String> filters, Long tgChatId)
            throws IllegalArgumentException;

    Link removeChatFromLink(String url, Long tgChatId) throws Exception;

    Collection<Link> getAllLinks();

    void updateLink(Link link);

    boolean isLinkAlive(String url);
}