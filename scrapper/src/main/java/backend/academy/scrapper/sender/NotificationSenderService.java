package backend.academy.scrapper.sender;

import backend.academy.common.dto.LinkUpdate;
import reactor.core.publisher.Mono;

public interface NotificationSenderService {
    Mono<?> sendNotification(LinkUpdate linkUpdate);
}
