package backend.academy.bot.client;

import backend.academy.bot.config.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelegramClient {
    private final TelegramBot bot;
    private final Logger logger = LoggerFactory.getLogger(TelegramClient.class);

    public TelegramClient(BotConfig botConfig) {
        this.bot = new TelegramBot(botConfig.telegramToken());
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        SendResponse response = bot.execute(request);

        if (response.isOk()) {
            logger.info("Сообщение успешно отправлено в чат {}: {}", chatId, text);
        } else {
            logger.error("Ошибка при отправке сообщения: {}", response.description());
        }
    }
}
