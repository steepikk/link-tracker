package backend.academy.bot.listener;

import backend.academy.bot.config.BotConfig;
import backend.academy.bot.session.UserSessionManager;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SetMyCommands;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TelegramBotListener {
    private final UserSessionManager userSessionManager;
    private final TelegramBot bot;

    public TelegramBotListener(BotConfig config, UserSessionManager userSessionManager) {
        this.bot = new TelegramBot(config.telegramToken());
        this.userSessionManager = userSessionManager;

        registerCommands();

        bot.setUpdatesListener(this::handleUpdates);
    }

    private int handleUpdates(List<Update> updates) {
        for (Update update : updates) {
            Message message = update.message();
            if (message != null && message.text() != null) {
                Long chatId = message.chat().id();
                String text = message.text();

                userSessionManager.start(chatId, text);
            }
        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void registerCommands() {
        BotCommand[] commands = {
                new BotCommand("/start", "Регистрация пользователя"),
                new BotCommand("/help", "Вывод списка доступных команд"),
                new BotCommand("/track", "Начать отслеживание ссылки"),
                new BotCommand("/untrack", "Прекратить отслеживание ссылки"),
                new BotCommand("/list", "Показать список отслеживаемых ссылок"),
                new BotCommand("/addtag", "Добавить тэг к существующей ссылке"),
                new BotCommand("/listbytag", "Получение ссылок по тегу"),
                new BotCommand("/listtags", "Получение всех тегов."),
                new BotCommand("/deletetag", "Удаление тега")
        };

        SetMyCommands setMyCommands = new SetMyCommands(commands);
        bot.execute(setMyCommands);
    }
}
