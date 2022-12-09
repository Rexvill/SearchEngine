package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "site")
@NoArgsConstructor
@Getter
@Setter
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(columnDefinition = " ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL")
    Status status;

    @Column(name = "status_time", columnDefinition = "DATETIME NOT NULL")
    LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    String lastError;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    String url;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    String name;

}
