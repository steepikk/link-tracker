package backend.academy.scrapper.client;

import backend.academy.common.dto.LinkUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TelegramClient {
    private final WebClient webClient;

    public TelegramClient(WebClient.Builder webClientBuilder, @Value("${bot.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<?> sendUpdate(LinkUpdate update) {
        return webClient.post().uri("/updates").bodyValue(update).retrieve().bodyToMono(Void.class);
    }
}
