package backend.academy.common.dto;

import java.time.Instant;

public record GitHubContent(UpdateType type, String title, String username, Instant createdAt, String preview) {
}
