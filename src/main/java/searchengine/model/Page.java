package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "page")
@NoArgsConstructor
@Getter
@Setter
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    @ManyToOne()
    @JoinColumn(name= "site_id")
    SiteModel site;

    @Column(columnDefinition = "TEXT NOT NULL, UNIQUE KEY pathIndex (path(512),site_id)")
    String path;

    @Column(columnDefinition = " INT NOT NULL")
    int code;

    @Column(columnDefinition = "MEDIUMTEXT NOT NULL")
    String content;
}
