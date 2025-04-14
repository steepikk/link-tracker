package backend.academy.scrapper.controller;

import backend.academy.common.dto.ApiErrorResponse;
import java.util.ArrayList;
import java.util.List;

import backend.academy.scrapper.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
public class ChatController {
    private final ChatService chatService;

    private static final String BAD_REQUEST_DESCRIPTION = "Некорректные параметры запроса";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String BAD_REQUEST_EXCEPTION = "IllegalArgumentException";
    private static final String BAD_REQUEST_MESSAGE = "Chat ID не должен быть null";

    private static final String NOT_FOUND_DESCRIPTION = "Ссылка не найдена";
    private static final String NOT_FOUND_CODE = "404";
    private static final String NOT_FOUND_EXCEPTION = "NotFoundException";
    private static final String NOT_FOUND_MESSAGE = "Запрашиваемый ресурс не найден";

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> registerChat(@PathVariable("id") Long chatId) {
        if (chatId == null) {
            return ResponseEntity.badRequest()
                    .body(createBadRequestError("Не указан идентификатор чата.", new ArrayList<>()));
        }
        chatService.registerChat(chatId);
        return ResponseEntity.ok("Чат зарегистрирован");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable("id") Long chatId) {
        if (chatId == null) {
            return ResponseEntity.badRequest().body(createBadRequestError(BAD_REQUEST_DESCRIPTION, new ArrayList<>()));
        }
        boolean removed = chatService.removeChat(chatId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createNotFoundError(NOT_FOUND_DESCRIPTION, new ArrayList<>()));
        }
        return ResponseEntity.ok("Чат успешно удалён");
    }

    private ApiErrorResponse createBadRequestError(String description, List<String> stacktrace) {
        return new ApiErrorResponse(
                description, BAD_REQUEST_CODE, BAD_REQUEST_EXCEPTION, BAD_REQUEST_MESSAGE, stacktrace);
    }

    private ApiErrorResponse createNotFoundError(String description, List<String> stacktrace) {
        return new ApiErrorResponse(description, NOT_FOUND_CODE, NOT_FOUND_EXCEPTION, NOT_FOUND_MESSAGE, stacktrace);
    }
}
