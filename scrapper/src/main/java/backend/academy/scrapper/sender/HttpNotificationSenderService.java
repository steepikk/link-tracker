package backend.academy.scrapper.sender;

import backend.academy.common.dto.LinkUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class HttpNotificationSenderService implements NotificationSenderService {
    private final WebClient webClient;

    public HttpNotificationSenderService(WebClient.Builder webClientBuilder, @Value("${bot.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<?> sendNotification(LinkUpdate linkUpdate) {
        return webClient.post().uri("/updates").bodyValue(linkUpdate).retrieve().bodyToMono(Void.class);
    }
}
