package backend.academy.scrapper.repository;

public interface ChatRepository {
    void registerChat(Long chatId);

    boolean removeChat(Long chatId);

    boolean existsById(Long chatId);
}
