package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.repository.ChatRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class ChatRepositorySQL implements ChatRepository {
    @Override
    public void registerChat(Long chatId) {

    }

    @Override
    public boolean removeChat(Long chatId) {
        return false;
    }

    @Override
    public boolean existsById(Long chatId) {
        return false;
    }
}
