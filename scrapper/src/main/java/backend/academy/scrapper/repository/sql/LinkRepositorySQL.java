package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class LinkRepositorySQL implements LinkRepository {

    private final JdbcTemplate jdbcTemplate;

    public LinkRepositorySQL(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        String sql = "SELECT l.id, l.url, l.last_updated, " +
                "STRING_AGG(DISTINCT lt.tag, ',') as tags, " +
                "STRING_AGG(DISTINCT lf.filter, ',') as filters, " +
                "STRING_AGG(CAST(lc.chat_id AS TEXT), ',') as chat_ids " +
                "FROM links l " +
                "LEFT JOIN link_tags lt ON l.id = lt.link_id " +
                "LEFT JOIN link_filters lf ON l.id = lf.link_id " +
                "LEFT JOIN link_chat lc ON l.id = lc.link_id " +
                "WHERE l.url = ? " +
                "GROUP BY l.id, l.url, l.last_updated";
        List<Link> links = jdbcTemplate.query(sql, this::mapRowToLink, url);
        return links.isEmpty() ? Optional.empty() : Optional.of(links.get(0));
    }

    @Override
    public Link addOrUpdateLink(String url, List<String> tags, List<String> filters, Long tgChatId)
            throws IllegalArgumentException {
        if (!isLinkAlive(url)) {
            throw new IllegalArgumentException("Link is not alive");
        }

        Optional<Link> existingLink = findByUrl(url);
        Long linkId;
        Instant now = Instant.now();

        if (existingLink.isPresent()) {
            linkId = existingLink.get().id();
            jdbcTemplate.update("DELETE FROM link_tags WHERE link_id = ?", linkId);
            jdbcTemplate.update("DELETE FROM link_filters WHERE link_id = ?", linkId);
            jdbcTemplate.update(
                    "UPDATE links SET url = ?, last_updated = ? WHERE id = ?",
                    url, Timestamp.from(now), linkId
            );
        } else {
            String sql = "INSERT INTO links (url, last_updated) VALUES (?, ?) RETURNING id";
            linkId = jdbcTemplate.queryForObject(sql, Long.class, url, Timestamp.from(now));
        }

        if (tags != null && !tags.isEmpty()) {
            String tagSql = "INSERT INTO link_tags (link_id, tag) VALUES (?, ?)";
            for (String tag : tags) {
                jdbcTemplate.update(tagSql, linkId, tag);
            }
        }

        if (filters != null && !filters.isEmpty()) {
            String filterSql = "INSERT INTO link_filters (link_id, filter) VALUES (?, ?)";
            for (String filter : filters) {
                jdbcTemplate.update(filterSql, linkId, filter);
            }
        }

        String chatSql = "INSERT INTO chats (chat_id) VALUES (?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(chatSql, tgChatId);

        String linkChatSql = "INSERT INTO link_chat (link_id, chat_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(linkChatSql, linkId, tgChatId);

        return findByUrl(url).orElseThrow(() -> new IllegalStateException("Link not found after insertion"));
    }

    @Override
    public Link removeChatFromLink(String url, Long tgChatId) throws Exception {
        Optional<Link> optionalLink = findByUrl(url);
        if (optionalLink.isEmpty()) {
            return null;
        }

        Link linkEntity = optionalLink.get();
        String sql = "DELETE FROM link_chat WHERE link_id = ? AND chat_id = ?";
        jdbcTemplate.update(sql, linkEntity.id(), tgChatId);

        Instant now = Instant.now();
        jdbcTemplate.update(
                "UPDATE links SET last_updated = ? WHERE id = ?",
                Timestamp.from(now), linkEntity.id()
        );

        String countSql = "SELECT COUNT(*) FROM link_chat WHERE link_id = ?";
        Integer chatCount = jdbcTemplate.queryForObject(countSql, Integer.class, linkEntity.id());

        if (chatCount == null || chatCount == 0) {
            jdbcTemplate.update("DELETE FROM link_tags WHERE link_id = ?", linkEntity.id());
            jdbcTemplate.update("DELETE FROM link_filters WHERE link_id = ?", linkEntity.id());
            jdbcTemplate.update("DELETE FROM links WHERE id = ?", linkEntity.id());
            return null;
        }

        return findByUrl(url).orElse(null);
    }

    @Override
    public Collection<Link> getAllLinks() {
        String sql = "SELECT l.id, l.url, l.last_updated, " +
                "STRING_AGG(DISTINCT lt.tag, ',') as tags, " +
                "STRING_AGG(DISTINCT lf.filter, ',') as filters, " +
                "STRING_AGG(CAST(lc.chat_id AS TEXT), ',') as chat_ids " +
                "FROM links l " +
                "LEFT JOIN link_tags lt ON l.id = lt.link_id " +
                "LEFT JOIN link_filters lf ON l.id = lf.link_id " +
                "LEFT JOIN link_chat lc ON l.id = lc.link_id " +
                "GROUP BY l.id, l.url, l.last_updated";
        return jdbcTemplate.query(sql, this::mapRowToLink);
    }

    @Override
    public void updateLink(Link link) {
        Instant now = Instant.now();
        String sql = "UPDATE links SET url = ?, last_updated = ? WHERE id = ?";
        jdbcTemplate.update(sql, link.url(), Timestamp.from(now), link.id());

        jdbcTemplate.update("DELETE FROM link_tags WHERE link_id = ?", link.id());
        jdbcTemplate.update("DELETE FROM link_filters WHERE link_id = ?", link.id());

        if (link.tags() != null && !link.tags().isEmpty()) {
            String tagSql = "INSERT INTO link_tags (link_id, tag) VALUES (?, ?)";
            for (String tag : link.tags()) {
                jdbcTemplate.update(tagSql, link.id(), tag);
            }
        }

        if (link.filters() != null && !link.filters().isEmpty()) {
            String filterSql = "INSERT INTO link_filters (link_id, filter) VALUES (?, ?)";
            for (String filter : link.filters()) {
                jdbcTemplate.update(filterSql, link.id(), filter);
            }
        }
    }

    @Override
    public boolean isLinkAlive(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            return 200 <= responseCode && responseCode < 400;
        } catch (IOException e) {
            return false;
        }
    }

    private Link mapRowToLink(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String url = rs.getString("url");
        Timestamp lastUpdatedTs = rs.getTimestamp("last_updated");
        Instant lastUpdated = lastUpdatedTs != null ? lastUpdatedTs.toInstant() : null;

        String tagsStr = rs.getString("tags");
        List<String> tags = tagsStr == null ? new ArrayList<>() :
                Arrays.stream(tagsStr.split(","))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

        String filtersStr = rs.getString("filters");
        List<String> filters = filtersStr == null ? new ArrayList<>() :
                Arrays.stream(filtersStr.split(","))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

        String chatIdsStr = rs.getString("chat_ids");
        Set<Chat> chats = chatIdsStr == null ? Collections.emptySet() :
                Arrays.stream(chatIdsStr.split(","))
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .map(Chat::new)
                        .collect(Collectors.toSet());

        return new Link(id, url, lastUpdated, tags, filters, chats);
    }
}