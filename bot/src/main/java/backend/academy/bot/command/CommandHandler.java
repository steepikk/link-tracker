package backend.academy.bot.command;

public interface CommandHandler {
    String getCommand();

    void handle(Long chatId, String messageText);
}
