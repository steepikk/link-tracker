package backend.academy.scrapper.controller;

import backend.academy.common.dto.AddLinkRequest;
import backend.academy.common.dto.ApiErrorResponse;
import backend.academy.common.dto.LinkResponse;
import backend.academy.common.dto.ListLinksResponse;
import backend.academy.common.dto.RemoveLinkRequest;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.service.LinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/links")
public class LinkController {

    private final LinkService linkService;

    private static final String BAD_REQUEST_DESCRIPTION = "Некорректные параметры запроса";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String BAD_REQUEST_EXCEPTION = "IllegalArgumentException";
    private static final String BAD_REQUEST_MESSAGE = "Chat ID не должен быть null";

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping
    public ResponseEntity<?> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {
        if (chatId == null) {
            return ResponseEntity.badRequest()
                    .body(createBadRequestError("Неуказан идентификатор чата", new ArrayList<>()));
        }
        try {
            Link entry = linkService.addOrUpdateLink(request.getLink(), request.getTags(), request.getFilters(), chatId);
            LinkResponse response = toLinkResponse(entry);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createBadRequestError("Ссылка неактивна", new ArrayList<>()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        if (chatId == null) {
            return ResponseEntity.badRequest()
                    .body(createBadRequestError("Неуказан идентификатор чата", new ArrayList<>()));
        }
        try {
            Collection<Link> allEntries = linkService.getAllLinks();
            List<LinkResponse> responses = allEntries.stream()
                    .filter(entry -> entry.chats().stream().anyMatch(chat -> chat.chatId().equals(chatId)))
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
            Link entry = linkService.removeChatFromLink(request.getLink(), chatId);
            return ResponseEntity.ok(toLinkResponse(entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createBadRequestError(BAD_REQUEST_DESCRIPTION, new ArrayList<>()));
        }
    }

    @GetMapping("/by-tag")
    public ResponseEntity<Collection<Link>> findByTag(@RequestParam String tag) {
        try {
            return ResponseEntity.ok(linkService.findByTag(tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        return ResponseEntity.ok(linkService.getAllTags());
    }

    @DeleteMapping("/tags")
    public ResponseEntity<Void> deleteTag(@RequestParam String tag) {
        try {
            linkService.deleteTag(tag);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private LinkResponse toLinkResponse(Link entry) {
        return new LinkResponse(entry.id(), entry.url(), entry.tags(), entry.filters());
    }

    private ApiErrorResponse createBadRequestError(String description, List<String> stacktrace) {
        return new ApiErrorResponse(
                description, BAD_REQUEST_CODE, BAD_REQUEST_EXCEPTION, BAD_REQUEST_MESSAGE, stacktrace);
    }
}