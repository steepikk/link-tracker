package backend.academy.scrapper.repository.orm;

import backend.academy.scrapper.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Logger logger = Logger.getLogger(ChatRepository.class.getName());

    default void registerChat(Long chatId) {
        if (existsById(chatId)) {
            logger.log(Level.INFO, "This chat id is already registered");
        } else {
            save(new Chat(chatId));
        }
    }

    default boolean removeChat(Long chatId) {
        if (existsById(chatId)) {
            deleteById(chatId);
            return true;
        }
        return false;
    }

    boolean existsById(Long chatId);
}
