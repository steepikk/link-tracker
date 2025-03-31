package backend.academy.common.dto;

import java.util.List;

public class LinkResponse {
    private long id;
    private String url;
    private List<String> tags;
    private List<String> filters;

    public LinkResponse(long id, String url, List<String> tags, List<String> filters) {
        this.id = id;
        this.url = url;
        this.tags = tags;
        this.filters = filters;
    }

    public String getUrl() {
        return url;
    }

    public long getId() {
        return id;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}
