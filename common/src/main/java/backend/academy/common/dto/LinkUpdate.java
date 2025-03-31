package backend.academy.common.dto;

import java.util.List;

public class LinkUpdate {
    private Long id;
    private String url;
    private String description;
    private List<Long> tgChatIds;

    public LinkUpdate(long id, String url, String description, List<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.description = description;
        this.tgChatIds = tgChatIds;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public List<Long> getTgChatIds() {
        return tgChatIds;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTgChatIds(List<Long> tgChatIds) {
        this.tgChatIds = tgChatIds;
    }

    public boolean isEmpty() {
        return id == null || url == null || description == null || tgChatIds == null;
    }
}
