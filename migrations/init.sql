-- Таблица чатов
CREATE TABLE chats
(
    chat_id BIGINT PRIMARY KEY
);

-- Таблица ссылок
CREATE TABLE links
(
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL UNIQUE,
    last_updated TIMESTAMP
);

-- Таблица связей many-to-many между link и chat
CREATE TABLE link_chat
(
    link_id BIGINT NOT NULL,
    chat_id BIGINT NOT NULL,
    PRIMARY KEY (link_id, chat_id),
    FOREIGN KEY (link_id) REFERENCES links (id) ON DELETE CASCADE,
    FOREIGN KEY (chat_id) REFERENCES chats (chat_id) ON DELETE CASCADE
);

-- Таблица тегов ссылки (ElementCollection)
CREATE TABLE link_tags
(
    link_id BIGINT NOT NULL,
    tag TEXT,
    FOREIGN KEY (link_id) REFERENCES links (id) ON DELETE CASCADE
);

-- Таблица фильтров ссылки (ElementCollection)
CREATE TABLE link_filters
(
    link_id BIGINT NOT NULL,
    filter TEXT,
    FOREIGN KEY (link_id) REFERENCES links (id) ON DELETE CASCADE
);


-- Убедимся, что last_updated не будет отрицательным и не null (опционально)
ALTER TABLE links
    ALTER COLUMN last_updated SET NOT NULL;

-- Индекс на tag для быстрого поиска
CREATE INDEX idx_link_tags_tag ON link_tags(tag);

-- Тестовые инсёрты
-- Чаты
INSERT INTO chats (chat_id) VALUES (1001), (1002);

-- Ссылки
INSERT INTO links (url, last_updated)
VALUES
    ('https://github.com/example/repo1', NOW()),
    ('https://stackoverflow.com/questions/123456', NOW());

-- Связи link-chat (many-to-many)
INSERT INTO link_chat (link_id, chat_id)
VALUES
    (1, 1001),
    (1, 1002),
    (2, 1002);

-- Теги
INSERT INTO link_tags (link_id, tag)
VALUES
    (1, 'github'),
    (1, 'java'),
    (2, 'stackoverflow');