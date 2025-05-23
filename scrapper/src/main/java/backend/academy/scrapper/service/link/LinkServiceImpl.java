package backend.academy.scrapper.service.link;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.link.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LinkServiceImpl implements LinkService {

    private static final Logger logger = LoggerFactory.getLogger(LinkServiceImpl.class);
    private final LinkRepository linkRepository;

    public LinkServiceImpl(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
        logger.info("LinkServiceImpl initialized with repository: {}", linkRepository.getClass().getName());
    }

    @Override
    @Transactional
    public Link addOrUpdateLink(String url, List<String> tags, List<String> filters, Long tgChatId)
            throws IllegalArgumentException {
        validateUrl(url);
        validateChatId(tgChatId);
        if (!isLinkAlive(url)) {
            logger.warn("Attempted to add/update an inactive link: {}", url);
            throw new IllegalArgumentException("Link is not alive");
        }
        List<String> validTags = validateAndCleanTags(tags);
        List<String> validFilters = validateAndCleanFilters(filters);

        logger.info("Adding or updating link: URL={}, tags={}, filters={}, chatId={}", url, validTags, validFilters, tgChatId);

        Optional<Link> existingLink = linkRepository.findByUrl(url);
        Long linkId;
        Instant now = Instant.now();

        if (existingLink.isPresent()) {
            linkId = existingLink.get().id();
            linkRepository.deleteTags(linkId);
            linkRepository.deleteFilters(linkId);
            linkRepository.updateLinkData(linkId, url, now);
        } else {
            linkId = linkRepository.insertLink(url, now);
        }

        linkRepository.insertTags(linkId, validTags);
        linkRepository.insertFilters(linkId, validFilters);
        linkRepository.insertChat(tgChatId);
        linkRepository.insertLinkChat(linkId, tgChatId);

        return linkRepository.findByUrl(url)
                .orElseThrow(() -> new IllegalStateException("Link not found after insertion"));
    }

    @Override
    @Transactional
    public Link removeChatFromLink(String url, Long tgChatId) throws Exception {
        validateUrl(url);
        validateChatId(tgChatId);
        logger.info("Removing chat {} from link: {}", tgChatId, url);

        Optional<Link> optionalLink = linkRepository.findByUrl(url);
        if (optionalLink.isEmpty()) {
            return null;
        }

        Link link = optionalLink.get();
        linkRepository.deleteLinkChat(link.id(), tgChatId);

        Instant now = Instant.now();
        linkRepository.updateLinkData(link.id(), url, now);

        int chatCount = linkRepository.countChatsByLinkId(link.id());
        if (chatCount == 0) {
            linkRepository.deleteTags(link.id());
            linkRepository.deleteFilters(link.id());
            linkRepository.deleteLink(link.id());
            return link;
        }

        return link;
    }

    @Override
    public Collection<Link> getAllLinks() {
        logger.debug("Retrieving all links");
        return linkRepository.getAllLinks();
    }

    @Override
    @Transactional
    public void updateLink(Link link) {
        if (link == null) {
            throw new IllegalArgumentException("Link cannot be null");
        }
        validateUrl(link.url());
        logger.info("Updating link: {}", link.url());

        Instant now = Instant.now();
        linkRepository.updateLinkData(link.id(), link.url(), now);
        linkRepository.deleteTags(link.id());
        linkRepository.deleteFilters(link.id());
        linkRepository.insertTags(link.id(), validateAndCleanTags(link.tags()));
        linkRepository.insertFilters(link.id(), validateAndCleanFilters(link.filters()));
    }

    @Override
    public boolean isLinkAlive(String url) {
        validateUrl(url);
        logger.debug("Checking if link is alive: {}", url);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            boolean isAlive = 200 <= responseCode && responseCode < 400;
            logger.debug("Link {} is alive: {}", url, isAlive);
            return isAlive;
        } catch (IOException e) {
            logger.warn("Failed to check link {}: {}", url, e.getMessage());
            return false;
        }
    }

    @Override
    public Collection<Link> findByTag(String tag) {
        validateTag(tag);
        logger.debug("Finding links by tag: {}", tag);
        return linkRepository.findByTag(tag);
    }

    @Override
    public List<String> getAllTags() {
        logger.debug("Retrieving all tags");
        return linkRepository.getAllTags();
    }

    @Override
    @Transactional
    public void deleteTag(String tag) {
        validateTag(tag);
        logger.info("Deleting tag: {}", tag);
        linkRepository.deleteTag(tag);
    }

    @Override
    public Link addTagToLink(String url, String tag, Long chatId) {
        Optional<Link> optionalLink = linkRepository.findByUrl(url);
        if (optionalLink.isEmpty()) {
            return null;
        }

        Link link = optionalLink.get();

        if (!isLinkAlive(url)) {
            System.out.println("owdjkioweoiwejkdmi");
            return null;
        }

        boolean isSubscribed = link.chats().stream().anyMatch(chat -> chat.chatId().equals(chatId));
        if (!isSubscribed) {
            throw new IllegalArgumentException("Чат не подписан на ссылку: " + url);
        }

        List<String> updatedTags = new ArrayList<>(link.tags());
        if (!updatedTags.contains(tag)) {
            updatedTags.add(tag);
            link = new Link(
                    link.id(),
                    link.url(),
                    link.lastUpdated(),
                    updatedTags,
                    link.filters(),
                    link.chats()
            );
            link = linkRepository.update(link);
        }
        return link;
    }

    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }
    }

    private void validateChatId(Long tgChatId) {
        if (tgChatId == null || tgChatId <= 0) {
            throw new IllegalArgumentException("Chat ID must be a positive number");
        }
    }

    private void validateTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be null or empty");
        }
        if (linkRepository.findByTag(tag).isEmpty()) {
            throw new IllegalArgumentException("Tag " + tag + " already exists");
        }
    }

    private List<String> validateAndCleanTags(List<String> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private List<String> validateAndCleanFilters(List<String> filters) {
        if (filters == null) {
            return new ArrayList<>();
        }
        return filters.stream()
                .filter(filter -> filter != null && !filter.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }
}