package backend.academy.scrapper.repository;

public interface ChatRepository {
    void addChatById(Long chatId);

    void deleteChatById(Long chatId);

    boolean existsById(Long chatId);
}
