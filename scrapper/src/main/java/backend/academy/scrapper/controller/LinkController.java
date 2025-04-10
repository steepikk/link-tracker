package backend.academy.scrapper.controller;

import backend.academy.common.dto.*;
import backend.academy.scrapper.model.LinkEntry;
import backend.academy.scrapper.repository.LinkRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
public class LinkController {

    private final LinkRepository repository;

    private static final String BAD_REQUEST_DESCRIPTION = "Некорректные параметры запроса";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String BAD_REQUEST_EXCEPTION = "IllegalArgumentException";
    private static final String BAD_REQUEST_MESSAGE = "Chat ID не должен быть null";

    private static final String NOT_FOUND_DESCRIPTION = "Ссылка не найдена";
    private static final String NOT_FOUND_CODE = "404";
    private static final String NOT_FOUND_EXCEPTION = "NotFoundException";
    private static final String NOT_FOUND_MESSAGE = "Запрашиваемый ресурс не найден";

    public LinkController(LinkRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {
        if (chatId == null) {
            return ResponseEntity.badRequest()
                    .body(createBadRequestError("Неуказан идентификатор чата", new ArrayList<>()));
        }
        try {
            LinkEntry entry =
                    repository.addOrUpdateLink(request.getLink(), request.getTags(), request.getFilters(), chatId);
            LinkResponse response = toLinkResponse(entry);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createBadRequestError("Ссылка неактивна", new ArrayList<>()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        try {
            Collection<LinkEntry> allEntries = repository.getAllLinks();
            List<LinkResponse> responses = allEntries.stream()
                    .filter(entry -> entry.getTgChatIds().contains(chatId))
                    .map(this::toLinkResponse)
                    .collect(Collectors.toList());
            ListLinksResponse response = new ListLinksResponse(responses, responses.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createBadRequestError(BAD_REQUEST_DESCRIPTION, new ArrayList<>()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        if (chatId == null) {
            return ResponseEntity.badRequest().body(createBadRequestError(BAD_REQUEST_DESCRIPTION, new ArrayList<>()));
        }
        try {
            LinkEntry entry = repository.removeChatFromLink(request.getLink(), chatId);
            if (entry == null)
                return ResponseEntity.badRequest().body(createNotFoundError(NOT_FOUND_DESCRIPTION, new ArrayList<>()));
            return ResponseEntity.ok(toLinkResponse(entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createBadRequestError(BAD_REQUEST_DESCRIPTION, new ArrayList<>()));
        }
    }

    private LinkResponse toLinkResponse(LinkEntry entry) {
        return new LinkResponse(entry.getId(), entry.getUrl(), entry.getTags(), entry.getFilters());
    }

    private ApiErrorResponse createBadRequestError(String description, List<String> stacktrace) {
        return new ApiErrorResponse(
                description, BAD_REQUEST_CODE, BAD_REQUEST_EXCEPTION, BAD_REQUEST_MESSAGE, stacktrace);
    }

    private ApiErrorResponse createNotFoundError(String description, List<String> stacktrace) {
        return new ApiErrorResponse(description, NOT_FOUND_CODE, NOT_FOUND_EXCEPTION, NOT_FOUND_MESSAGE, stacktrace);
    }
}
