package backend.academy.scrapper.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinkEntry {
    private Long id;
    private String url;
    private Instant lastUpdated;
    private List<String> tags;
    private List<String> filters;
    private Set<Long> tgChatIds;

    public LinkEntry(Long id, String url, List<String> tags, List<String> filters) {
        this.id = id;
        this.url = url;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.filters = filters != null ? filters : new ArrayList<>();
        this.tgChatIds = new HashSet<>();
        this.lastUpdated = null;
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getFilters() {
        return filters;
    }

    public Set<Long> getTgChatIds() {
        return tgChatIds;
    }

    public void addChat(Long tgChatId) {
        this.tgChatIds.add(tgChatId);
    }

    public void removeChat(Long tgChatId) {
        this.tgChatIds.remove(tgChatId);
    }
}
