package backend.academy.scrapper.util;

import org.springframework.stereotype.Component;

@Component
public class LinkToApiRequestConverter {
    public String convertGithubLinkToIssueApi(String link) {
        String[] linkParts = link.split("/");

        String owner = linkParts[3];
        String repoName = linkParts[4];

        return String.format("https://api.github.com/repos/%s/%s/issues", owner, repoName);
    }
}
