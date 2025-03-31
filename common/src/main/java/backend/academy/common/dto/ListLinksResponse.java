package backend.academy.common.dto;

import java.util.List;

public class ListLinksResponse {
    private List<LinkResponse> links;
    private int size;

    public ListLinksResponse(List<LinkResponse> links, int size) {
        this.links = links;
        this.size = size;
    }

    public ListLinksResponse() {}

    public List<LinkResponse> getLinks() {
        return links;
    }

    public void setLinks(List<LinkResponse> links) {
        this.links = links;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
