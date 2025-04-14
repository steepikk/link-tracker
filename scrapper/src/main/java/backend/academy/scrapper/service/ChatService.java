package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Chat;

public interface ChatService {
    void registerChat(Long chatId);

    boolean removeChat(Long chatId);
}
