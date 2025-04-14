package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    public Optional<Link> findByUrl(String url) {
        validateUrl(url);
        logger.debug("Finding link by URL: {}", url);
        return linkRepository.findByUrl(url);
    }

    @Override
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
        return linkRepository.addOrUpdateLink(url, validTags, validFilters, tgChatId);
    }

    @Override
    public Link removeChatFromLink(String url, Long tgChatId) throws Exception {
        validateUrl(url);
        validateChatId(tgChatId);
        logger.info("Removing chat {} from link: {}", tgChatId, url);
        return linkRepository.removeChatFromLink(url, tgChatId);
    }

    @Override
    public Collection<Link> getAllLinks() {
        logger.debug("Retrieving all links");
        return linkRepository.getAllLinks();
    }

    @Override
    public void updateLink(Link link) {
        if (link == null) {
            throw new IllegalArgumentException("Link cannot be null");
        }
        validateUrl(link.url());
        logger.info("Updating link: {}", link.url());
        linkRepository.updateLink(link);
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