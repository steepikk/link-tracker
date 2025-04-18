package backend.academy.common.dto;

import java.time.Instant;

public record ContentUpdate(UpdateType type, String title, String username, Instant createdAt, String preview) {
}
