package backend.academy.scrapper.service;

import backend.academy.common.dto.ContentUpdate;
import backend.academy.common.dto.LinkUpdate;
import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.entity.Link;
import java.time.Instant;
import java.util.List;

import backend.academy.scrapper.sender.NotificationSenderService;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.service.link.LinkService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LinkMonitoringService {
    private final LinkService linkService;
    private final NotificationSenderService notificationSenderService;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;

    public LinkMonitoringService(
            LinkService linkService,
            NotificationSenderService notificationSenderService,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient) {
        this.linkService = linkService;
        this.notificationSenderService = notificationSenderService;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
    }

    @Scheduled(fixedRate = 60000)
    public void monitorLinks() {
        for (Link linkEntry : linkService.getAllLinks()) {
            if (linkEntry.url().contains("github.com")) {
                checkGitHubLink(linkEntry);
            } else if (linkEntry.url().contains("stackoverflow.com")) {
                checkStackOverflowLink(linkEntry);
            }
        }
    }

    private void checkGitHubLink(Link linkEntry) {
        Instant lastUpdated = gitHubClient.getLastUpdated(linkEntry.url());
        if (lastUpdated == null || (linkEntry.lastUpdated() != null && !lastUpdated.isAfter(linkEntry.lastUpdated()))) {
            return;
        }

        List<ContentUpdate> contents = gitHubClient.getGitHubContent(linkEntry.url());
        if (contents.isEmpty()) {
            return;
        }

        List<ContentUpdate> newUpdates = contents.stream()
                .filter(content -> linkEntry.lastUpdated() == null || content.createdAt().isAfter(linkEntry.lastUpdated()))
                .toList();

        if (newUpdates.isEmpty()) {
            return;
        }

        linkEntry.lastUpdated(lastUpdated);
        linkService.updateLink(linkEntry);

        List<Long> chatIds = linkEntry.chats()
                .stream()
                .map(Chat::chatId)
                .toList();

        for (ContentUpdate update : newUpdates) {
            LinkUpdate linkUpdate = new LinkUpdate(
                    linkEntry.id(),
                    linkEntry.url(),
                    update,
                    chatIds
            );
            notificationSenderService.sendNotification(linkUpdate).subscribe();
        }
    }

    private void checkStackOverflowLink(Link linkEntry) {
        List<ContentUpdate> contents = stackOverflowClient.getSOContent(linkEntry.url());
        if (contents.isEmpty()) {
            return;
        }

        Instant lastContentUpdate = contents.stream()
                .map(ContentUpdate::createdAt)
                .max(Instant::compareTo)
                .orElse(null);

        if (lastContentUpdate == null || (linkEntry.lastUpdated() != null && !lastContentUpdate.isAfter(linkEntry.lastUpdated()))) {
            return;
        }

        List<ContentUpdate> newUpdates = contents.stream()
                .filter(content -> linkEntry.lastUpdated() == null || content.createdAt().isAfter(linkEntry.lastUpdated()))
                .toList();

        if (newUpdates.isEmpty()) {
            return;
        }

        linkEntry.lastUpdated(lastContentUpdate);
        linkService.updateLink(linkEntry);

        List<Long> chatIds = linkEntry.chats()
                .stream()
                .map(Chat::chatId)
                .toList();

        for (ContentUpdate update : newUpdates) {
            LinkUpdate linkUpdate = new LinkUpdate(
                    linkEntry.id(),
                    linkEntry.url(),
                    update,
                    chatIds
            );
            notificationSenderService.sendNotification(linkUpdate).subscribe();
        }
    }
}
