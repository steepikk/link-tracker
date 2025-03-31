package backend.academy.bot.session;

import java.util.List;

public class UserSession {
    private UserSessionState state;
    private String link;
    private List<String> tags;
    private List<String> filters;

    public UserSession() {
        this.state = UserSessionState.WAITING_FOR_START;
    }

    public UserSessionState getState() {
        return state;
    }

    public void setState(UserSessionState state) {
        this.state = state;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public void reset() {
        this.state = UserSessionState.WAITING_FOR_COMMAND;
        this.link = null;
        this.tags = null;
        this.filters = null;
    }
}
