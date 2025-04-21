package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.entity.Chat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM", matchIfMissing = true)
public interface ChatRepositoryORM extends JpaRepository<Chat, Long>, ChatRepository {
    Logger logger = Logger.getLogger(ChatRepositoryORM.class.getName());

    default void addChatById(Long chatId) {
        save(new Chat(chatId));
        logger.log(Level.INFO, "Registered chat id: {0}", chatId);
    }

    default void deleteChatById(Long chatId) {
        deleteById(chatId);
        logger.log(Level.INFO, "Removed chat id: {0}", chatId);
    }

    boolean existsById(Long chatId);
}
