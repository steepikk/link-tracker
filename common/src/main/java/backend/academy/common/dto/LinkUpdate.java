package backend.academy.common.dto;

import java.util.List;

public class LinkUpdate {
    private Long id;
    private String url;
    private ContentUpdate contentUpdate;
    private List<Long> tgChatIds;

    public LinkUpdate(long id, String url, ContentUpdate contentUpdate, List<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.contentUpdate = contentUpdate;
        this.tgChatIds = tgChatIds;
    }

    public long getId() {
        return id;
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

    public void setTgChatIds(List<Long> tgChatIds) {
        this.tgChatIds = tgChatIds;
    }

    public ContentUpdate getContentUpdate() {
        return contentUpdate;
    }

    public void setContentUpdate(ContentUpdate contentUpdate) {
        this.contentUpdate = contentUpdate;
    }

    public boolean isEmpty() {
        return id == null || url == null || contentUpdate == null || tgChatIds == null;
    }
}
