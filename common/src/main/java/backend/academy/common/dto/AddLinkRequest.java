package backend.academy.common.dto;

import java.util.List;

public class AddLinkRequest {
    private String link;
    private List<String> tags;
    private List<String> filters;

    public AddLinkRequest(String link, List<String> tags, List<String> filters) {
        this.link = link;
        this.tags = tags;
        this.filters = filters;
    }

    public AddLinkRequest() {}

    public void setLink(String link) {
        this.link = link;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public String getLink() {
        return link;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getFilters() {
        return filters;
    }
}
