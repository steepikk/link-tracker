package backend.academy.scrapper.service.chat;

import backend.academy.scrapper.repository.chat.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);
    private final ChatRepository chatRepository;

    public ChatServiceImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        logger.info("ChatServiceImpl initialized with repository: {}", chatRepository.getClass().getName());
    }

    @Override
    public void registerChat(Long chatId) {
        if (chatRepository.existsById(chatId)) {
            logger.info("This chat id is already registered: {}", chatId);
        } else {
            chatRepository.addChatById(chatId);
            logger.info("Registered chat id: {}", chatId);
        }
    }

    @Override
    public boolean removeChat(Long chatId) {
        if (chatRepository.existsById(chatId)) {
            chatRepository.deleteChatById(chatId);
            logger.info("Removed chat id: {}", chatId);
            return true;
        } else {
            logger.info("Not found chat with id: {}", chatId);
            return false;
        }
    }
}
