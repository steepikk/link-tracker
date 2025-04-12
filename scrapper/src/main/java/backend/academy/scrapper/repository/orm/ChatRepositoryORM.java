package backend.academy.scrapper.repository.orm;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.repository.ChatRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM", matchIfMissing = true)
public interface ChatRepositoryORM extends JpaRepository<Chat, Long>, ChatRepository {
    Logger logger = Logger.getLogger(ChatRepositoryORM.class.getName());

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
