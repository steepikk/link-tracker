package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
    public Long insertLink(String url, Instant lastUpdated) {
        String sql = "INSERT INTO links (url, last_updated) VALUES (?, ?) RETURNING id";
        return jdbcTemplate.queryForObject(sql, Long.class, url, Timestamp.from(lastUpdated));
    }

    @Override
    public void updateLinkData(Long id, String url, Instant lastUpdated) {
        String sql = "UPDATE links SET url = ?, last_updated = ? WHERE id = ?";
        jdbcTemplate.update(sql, url, Timestamp.from(lastUpdated), id);
    }

    @Override
    public Link update(Link link) {
        jdbcTemplate.update(
                "UPDATE links SET url = ?, last_updated = ? WHERE id = ?",
                link.url(), Timestamp.from(link.lastUpdated()), link.id()
        );

        deleteTags(link.id());
        deleteFilters(link.id());
        jdbcTemplate.update("DELETE FROM link_chat WHERE link_id = ?", link.id());

        insertTags(link.id(), link.tags());
        insertFilters(link.id(), link.filters());
        for (Chat chat : link.chats()) {
            insertLinkChat(link.id(), chat.chatId());
        }

        return link;
    }

    @Override
    public void deleteLink(Long id) {
        jdbcTemplate.update("DELETE FROM links WHERE id = ?", id);
    }

    @Override
    public void insertTags(Long linkId, List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            String sql = "INSERT INTO link_tags (link_id, tag) VALUES (?, ?)";
            for (String tag : tags) {
                jdbcTemplate.update(sql, linkId, tag);
            }
        }
    }

    @Override
    public void deleteTags(Long linkId) {
        jdbcTemplate.update("DELETE FROM link_tags WHERE link_id = ?", linkId);
    }

    @Override
    public void insertFilters(Long linkId, List<String> filters) {
        if (filters != null && !filters.isEmpty()) {
            String sql = "INSERT INTO link_filters (link_id, filter) VALUES (?, ?)";
            for (String filter : filters) {
                jdbcTemplate.update(sql, linkId, filter);
            }
        }
    }

    @Override
    public void deleteFilters(Long linkId) {
        jdbcTemplate.update("DELETE FROM link_filters WHERE link_id = ?", linkId);
    }

    @Override
    public void insertChat(Long chatId) {
        String sql = "INSERT INTO chats (chat_id) VALUES (?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    public void insertLinkChat(Long linkId, Long chatId) {
        String sql = "INSERT INTO link_chat (link_id, chat_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, linkId, chatId);
    }

    @Override
    public void deleteLinkChat(Long linkId, Long chatId) {
        String sql = "DELETE FROM link_chat WHERE link_id = ? AND chat_id = ?";
        jdbcTemplate.update(sql, linkId, chatId);
    }

    @Override
    public int countChatsByLinkId(Long linkId) {
        String sql = "SELECT COUNT(*) FROM link_chat WHERE link_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, linkId);
        return count != null ? count : 0;
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
    public Collection<Link> findByTag(String tag) {
        String sql = "SELECT l.id, l.url, l.last_updated, " +
                "STRING_AGG(DISTINCT lt2.tag, ',') as tags, " +
                "STRING_AGG(DISTINCT lf.filter, ',') as filters, " +
                "STRING_AGG(CAST(lc.chat_id AS TEXT), ',') as chat_ids " +
                "FROM links l " +
                "JOIN link_tags lt ON l.id = lt.link_id " +
                "LEFT JOIN link_tags lt2 ON l.id = lt2.link_id " +
                "LEFT JOIN link_filters lf ON l.id = lf.link_id " +
                "LEFT JOIN link_chat lc ON l.id = lc.link_id " +
                "WHERE lt.tag = ? " +
                "GROUP BY l.id, l.url, l.last_updated";
        return jdbcTemplate.query(sql, this::mapRowToLink, tag);
    }

    @Override
    public List<String> getAllTags() {
        String sql = "SELECT DISTINCT tag FROM link_tags ORDER BY tag";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public void deleteTag(String tag) {
        String sql = "DELETE FROM link_tags WHERE tag = ?";
        jdbcTemplate.update(sql, tag);
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