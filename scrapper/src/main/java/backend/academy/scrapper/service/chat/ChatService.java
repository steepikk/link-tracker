package backend.academy.scrapper.service.chat;

public interface ChatService {
    void registerChat(Long chatId);

    boolean removeChat(Long chatId);
}
