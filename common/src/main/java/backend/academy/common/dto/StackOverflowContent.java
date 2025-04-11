package backend.academy.common.dto;

import java.time.Instant;

public record StackOverflowContent(String questionTitle, String username, Instant createdAt, String preview) {
}
