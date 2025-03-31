package backend.academy.bot.session;

import backend.academy.bot.client.TelegramClient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserIO {
    private final TelegramClient telegramClient;
    private final TrackingService trackingService;

    public UserIO(TelegramClient telegramClient, TrackingService trackingService) {
        this.telegramClient = telegramClient;
        this.trackingService = trackingService;
    }

    public void addLink(Long chatId, String messageText, UserSession userSession) {
        userSession.setLink(messageText.trim());
        userSession.setState(UserSessionState.ASKING_FOR_TAG);
        telegramClient.sendMessage(chatId, "Хотите настроить теги и фильтры? (да/нет)");
    }

    public void askForTag(Long chatId, String messageText, UserSession userSession) {
        if (messageText.trim().equalsIgnoreCase("нет")) {
            trackingService.completeTracking(chatId, userSession);
        } else if (messageText.trim().equalsIgnoreCase("да")) {
            userSession.setState(UserSessionState.WAITING_FOR_TAG_INPUT);
            telegramClient.sendMessage(chatId, "Введите теги (через пробел, опционально):");
        } else {
            telegramClient.sendMessage(chatId, "Пожалуйста, ответьте 'да' или 'нет'.");
        }
    }

    public void waitingForTagInput(Long chatId, String messageText, UserSession userSession) {
        userSession.setTags(parseInput(messageText));
        userSession.setState(UserSessionState.WAITING_FOR_FILTER_INPUT);
        telegramClient.sendMessage(chatId, "Введите фильтры (через пробел, опционально):");
    }

    public void waitingForFilterInput(Long chatId, String messageText, UserSession userSession) {
        userSession.setFilters(parseInput(messageText));
        trackingService.completeTracking(chatId, userSession);
    }

    public List<String> parseInput(String input) {
        return Arrays.stream(input.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
