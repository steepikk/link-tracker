package backend.academy.scrapper.repository.chat;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class ChatRepositorySQL implements ChatRepository {

    private static final Logger logger = Logger.getLogger(ChatRepositorySQL.class.getName());
    private final JdbcTemplate jdbcTemplate;

    public ChatRepositorySQL(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        logger.info("Initialized ChatRepositorySQL");
    }

    @Override
    public void addChatById(Long chatId) {
        String sql = "INSERT INTO chats (chat_id) VALUES (?)";
        jdbcTemplate.update(sql, chatId);
        logger.log(Level.INFO, "Registered chat id: {0}", chatId);

    }

    @Override
    public void deleteChatById(Long chatId) {
        String sql = "DELETE FROM chats WHERE chat_id = ?";
        jdbcTemplate.update(sql, chatId);
        logger.log(Level.INFO, "Removed chat id: {0}", chatId);
    }

    @Override
    public boolean existsById(Long chatId) {
        String sql = "SELECT COUNT(*) FROM chats WHERE chat_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        boolean exists = count != null && count > 0;
        logger.log(Level.FINE, "Checked existence of chat id {0}: {1}", new Object[]{chatId, exists});
        return exists;
    }
}