package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.common.dto.LinkUpdate;
import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.model.LinkEntry;

import java.time.Instant;
import java.util.List;

import backend.academy.scrapper.sender.NotificationSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class LinkMonitoringServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private NotificationSenderService notificationSenderService;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private StackOverflowClient stackOverflowClient;

    @InjectMocks
    private LinkMonitoringService linkMonitoringService;

    private LinkEntry linkEntry;

    @BeforeEach
    public void setUp() {
        linkEntry = new LinkEntry(1L, "https://github.com/example", List.of("tag1"), List.of("filter1"));
        linkEntry.addChat(12345L);
        linkEntry.setLastUpdated(Instant.now());
    }

    @Test
    public void shouldSendUpdateForGitHubLinkWhenUpdated() {
        Instant newLastUpdated = Instant.now().plusSeconds(60);
        when(gitHubClient.getLastUpdated(linkEntry.getUrl())).thenReturn(newLastUpdated);
        when(linkRepository.getAllLinks()).thenReturn(List.of(linkEntry));
        when(notificationSenderService.sendNotification(any(LinkUpdate.class))).thenReturn(Mono.empty());

        linkMonitoringService.monitorLinks();

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(notificationSenderService, times(1)).sendNotification(captor.capture());

        LinkUpdate capturedUpdate = captor.getValue();
        assertNotNull(capturedUpdate);
        assertEquals(linkEntry.getId(), capturedUpdate.getId());
        assertEquals(linkEntry.getUrl(), capturedUpdate.getUrl());
        assertEquals("GitHub repository updated", capturedUpdate.getDescription());
        assertTrue(capturedUpdate.getTgChatIds().contains(12345L));
    }

    @Test
    public void shouldNotSendUpdateIfNoChangeOnGitHub() {
        Instant sameLastUpdated = linkEntry.getLastUpdated();
        when(gitHubClient.getLastUpdated(linkEntry.getUrl())).thenReturn(sameLastUpdated);
        when(linkRepository.getAllLinks()).thenReturn(List.of(linkEntry));

        linkMonitoringService.monitorLinks();

        verify(notificationSenderService, times(0)).sendNotification(any(LinkUpdate.class));
    }
}
