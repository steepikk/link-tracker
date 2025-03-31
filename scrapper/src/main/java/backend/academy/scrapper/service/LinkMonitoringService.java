package backend.academy.scrapper.service;

import backend.academy.common.dto.LinkUpdate;
import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.client.TelegramClient;
import backend.academy.scrapper.model.LinkEntry;
import backend.academy.scrapper.repository.LinkRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LinkMonitoringService {
    private final LinkRepository linkRepository;
    private final TelegramClient telegramClient;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;

    public LinkMonitoringService(
            LinkRepository linkRepository,
            TelegramClient telegramClient,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient) {
        this.linkRepository = linkRepository;
        this.telegramClient = telegramClient;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
    }

    @Scheduled(fixedRate = 60000)
    public void monitorLinks() {
        for (LinkEntry linkEntry : linkRepository.getAllLinks()) {
            if (linkEntry.getUrl().contains("github.com")) {
                checkGitHubLink(linkEntry);
            } else if (linkEntry.getUrl().contains("stackoverflow.com")) {
                checkStackOverflowLink(linkEntry);
            }
        }
    }

    private void checkGitHubLink(LinkEntry linkEntry) {
        Instant lastUpdated = gitHubClient.getLastUpdated(linkEntry.getUrl());
        if (linkEntry.getLastUpdated() == null && lastUpdated != null) {
            linkEntry.setLastUpdated(lastUpdated);
            return;
        }
        if (lastUpdated != null && !lastUpdated.equals(linkEntry.getLastUpdated())) {
            linkEntry.setLastUpdated(lastUpdated);
            linkRepository.updateLink(linkEntry);

            LinkUpdate linkUpdate = new LinkUpdate(
                    linkEntry.getId(),
                    linkEntry.getUrl(),
                    "GitHub repository updated",
                    List.copyOf(linkEntry.getTgChatIds()));

            telegramClient.sendUpdate(linkUpdate).subscribe();
        }
    }

    private void checkStackOverflowLink(LinkEntry linkEntry) {
        Instant lastUpdated = stackOverflowClient.getLastUpdated(linkEntry.getUrl());
        if (lastUpdated != null && !lastUpdated.equals(linkEntry.getLastUpdated())) {
            linkEntry.setLastUpdated(lastUpdated);
            linkRepository.updateLink(linkEntry);

            LinkUpdate linkUpdate = new LinkUpdate(
                    linkEntry.getId(),
                    linkEntry.getUrl(),
                    "StackOverflow question updated",
                    List.copyOf(linkEntry.getTgChatIds()));

            telegramClient.sendUpdate(linkUpdate).subscribe();
        }
    }
}
