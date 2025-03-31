package backend.academy.bot.session;

import backend.academy.bot.client.TelegramClient;
import backend.academy.bot.command.CommandHandler;
import backend.academy.common.dto.LinkUpdate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserSessionManager {
    private final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;
    private final UserIO userIO;
    private final Logger logger = LoggerFactory.getLogger(UserSessionManager.class);
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    public UserSessionManager(TelegramClient telegramClient, UserIO userIO, List<CommandHandler> handlers) {
        this.telegramClient = telegramClient;
        this.userIO = userIO;
        for (CommandHandler handler : handlers) {
            commandHandlers.put(handler.getCommand().toLowerCase(), handler);
        }
    }

    public void start(Long chatId, String messageText) {
        UserSession session = getSession(chatId);
        UserSessionState state = session.getState();

        if (state == UserSessionState.WAITING_FOR_START) {
            startSession(chatId, messageText, session);
            return;
        }

        if (state == UserSessionState.WAITING_FOR_COMMAND) {
            processCommand(chatId, messageText, session);
            return;
        }

        switch (state) {
            case WAITING_FOR_LINK:
                userIO.addLink(chatId, messageText, session);
                break;
            case ASKING_FOR_TAG:
                userIO.askForTag(chatId, messageText, session);
                break;
            case WAITING_FOR_TAG_INPUT:
                userIO.waitingForTagInput(chatId, messageText, session);
                break;
            case WAITING_FOR_FILTER_INPUT:
                userIO.waitingForFilterInput(chatId, messageText, session);
                break;
            default:
                telegramClient.sendMessage(chatId, "Неверное состояние. Введите /help для получения списка команд.");
                session.reset();
                logger.error("Недопустимое состояние для chatId={}", chatId);
        }
    }

    private void startSession(Long chatId, String messageText, UserSession session) {
        if (messageText.trim().equalsIgnoreCase("/start")) {
            session.setState(UserSessionState.WAITING_FOR_COMMAND);
            commandHandlers.get("/start").handle(chatId, messageText);
        } else {
            telegramClient.sendMessage(chatId, "Введите /start для начала работы с ботом.");
        }
    }

    private void processCommand(Long chatId, String messageText, UserSession session) {
        String lower = messageText.trim().toLowerCase();
        if (lower.startsWith("/track")) {
            String[] parts = messageText.split(" ", 2);
            if (parts.length >= 2 && !parts[1].isBlank()) {
                userIO.addLink(chatId, parts[1], session);
            } else {
                session.setState(UserSessionState.WAITING_FOR_LINK);
                telegramClient.sendMessage(chatId, "Введите ссылку для отслеживания:");
            }
        } else if (lower.startsWith("/help") || lower.startsWith("/list") || lower.startsWith("/untrack")) {
            commandHandlers.get(lower.split(" ")[0]).handle(chatId, messageText);
            session.reset();
        } else if (lower.startsWith("/start")) {
            telegramClient.sendMessage(chatId, "Вы уже зарегистрировали чат. Введите /help для списка команд.");
        } else {
            telegramClient.sendMessage(chatId, "Неизвестная команда. Введите /help для списка команд.");
            logger.info("Неизвестная команда: message={}, chatId={}", messageText, chatId);
        }
    }

    public void notifyUser(Long chatId, LinkUpdate update) {
        String notificationMessage = "Обновление по ссылке: " + update.getUrl() + "\n " + update.getDescription();
        sessions.get(chatId).reset();
        telegramClient.sendMessage(chatId, "Уведомление:\n" + notificationMessage);
        logger.info("Notify user: chatId={}, notificationMessage={}", chatId, notificationMessage);
    }

    public UserSession getSession(Long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new UserSession());
    }
}
