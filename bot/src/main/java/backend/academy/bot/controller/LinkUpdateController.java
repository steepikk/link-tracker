package backend.academy.bot.controller;

import backend.academy.bot.session.UserSessionManager;
import backend.academy.common.dto.ApiErrorResponse;
import backend.academy.common.dto.LinkUpdate;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
@Validated
@CrossOrigin
public class LinkUpdateController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkUpdateController.class);
    private static final String BAD_REQUEST_DESCRIPTION = "Некорректные параметры запроса";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String BAD_REQUEST_EXCEPTION = "IllegalArgumentException";
    private static final String BAD_REQUEST_MESSAGE = "Chat ID не должен быть null";

    private final UserSessionManager userSessionManager;

    public LinkUpdateController(UserSessionManager userSessionManager) {
        this.userSessionManager = userSessionManager;
    }

    @PostMapping
    public ResponseEntity<?> receiveUpdate(@Valid @RequestBody LinkUpdate update) {
        if (!update.isEmpty()) {
            try {
                LOGGER.info("Получено обновление: {} - {} by {} at {}",
                        update.getContentUpdate().type(),
                        update.getContentUpdate().title(),
                        update.getContentUpdate().username(),
                        update.getContentUpdate().createdAt());

                List<Long> chatStorage = update.getTgChatIds();
                for (Long chatId : chatStorage) {
                    userSessionManager.notifyUser(chatId, update);
                }
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(createBadRequestError(BAD_REQUEST_DESCRIPTION, new ArrayList<>()));
            }
        } else {
            return ResponseEntity.badRequest().body(createBadRequestError(BAD_REQUEST_DESCRIPTION, new ArrayList<>()));
        }
    }

    private ApiErrorResponse createBadRequestError(String description, List<String> stacktrace) {
        return new ApiErrorResponse(
                description, BAD_REQUEST_CODE, BAD_REQUEST_EXCEPTION, BAD_REQUEST_MESSAGE, stacktrace);
    }
}
