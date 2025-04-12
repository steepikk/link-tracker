package backend.academy.scrapper.service;

import backend.academy.common.dto.LinkUpdate;
import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.entity.Link;
import java.time.Instant;
import java.util.List;

import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.sender.NotificationSenderService;
import backend.academy.scrapper.entity.Chat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LinkMonitoringService {
    private final LinkRepository linkRepository;
    private final NotificationSenderService notificationSenderService;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;

    public LinkMonitoringService(
            LinkRepository linkRepository,
            NotificationSenderService notificationSenderService,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient) {
        this.linkRepository = linkRepository;
        this.notificationSenderService = notificationSenderService;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
    }

    @Scheduled(fixedRate = 60000)
    public void monitorLinks() {
        for (Link linkEntry : linkRepository.getAllLinks()) {
            if (linkEntry.url().contains("github.com")) {
                checkGitHubLink(linkEntry);
            } /*else if (linkEntry.getUrl().contains("stackoverflow.com")) {
                checkStackOverflowLink(linkEntry);
            }*/
        }
    }

    private void checkGitHubLink(Link linkEntry) {
        Instant lastUpdated = gitHubClient.getLastUpdated(linkEntry.url());
        if (linkEntry.lastUpdated() == null && lastUpdated != null) {
            linkEntry.lastUpdated(lastUpdated);
            return;
        }
        if (lastUpdated != null && !lastUpdated.equals(linkEntry.lastUpdated())) {
            linkEntry.lastUpdated(lastUpdated);
            linkRepository.updateLink(linkEntry);

            List<Long> chatIds = linkEntry.chats()
                    .stream()
                    .map(Chat::chatId)
                    .toList();

            LinkUpdate linkUpdate = new LinkUpdate(
                    linkEntry.id(),
                    linkEntry.url(),
                    "GitHub repository updated",
                    chatIds
            );

            notificationSenderService.sendNotification(linkUpdate).subscribe();
        }
    }

    /*private void checkStackOverflowLink(Link linkEntry) {
        Instant lastUpdated = stackOverflowClient.getLastUpdated(linkEntry.getUrl());
        if (lastUpdated != null && !lastUpdated.equals(linkEntry.getLastUpdated())) {
            linkEntry.setLastUpdated(lastUpdated);
            linkRepository.updateLink(linkEntry);

            LinkUpdate linkUpdate = new LinkUpdate(
                    linkEntry.getId(),
                    linkEntry.getUrl(),
                    "StackOverflow question updated",
                    List.copyOf(linkEntry.getTgChatIds()));

            notificationSenderService.sendNotification(linkUpdate).subscribe();
        }
    }*/
}
