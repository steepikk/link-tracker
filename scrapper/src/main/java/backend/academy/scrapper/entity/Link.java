package backend.academy.scrapper.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, unique = true)
    private String url;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @ElementCollection
    @CollectionTable(name = "link_tags", joinColumns = @JoinColumn(name = "link_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "link_filters", joinColumns = @JoinColumn(name = "link_id"))
    @Column(name = "filter")
    private List<String> filters = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "link_chat",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id")
    )
    private Set<Chat> chats = new HashSet<>();

    public Link(String url, List<String> tags, List<String> filters) {
        this.url = url;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.filters = filters != null ? filters : new ArrayList<>();
    }

    public void addChat(Chat chat) {
        this.chats.add(chat);
    }

    public void removeChat(Chat chat) {
        this.chats.remove(chat);
        for (Chat c : this.chats) {
            System.out.println("После удаления проходимся" + c.chatId());
        }
    }
}
