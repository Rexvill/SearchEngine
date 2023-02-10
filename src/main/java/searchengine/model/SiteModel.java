package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "site")
@NoArgsConstructor
@Getter
@Setter
public class SiteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    Status status;

    @Column(name = "status_time", columnDefinition = "DATETIME NOT NULL")
    Date statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    String lastError = "";

    @Column(columnDefinition = "VARCHAR(255) NOT NULL", updatable = false)
    String url;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL", updatable = false)
    String name;

    @Transient
    @OneToMany(cascade = CascadeType.ALL)
    List<Page> pages = new ArrayList<>();
}
