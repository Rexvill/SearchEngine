package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity (name = "page")
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

    @Column(columnDefinition = "TEXT NOT NULL, UNIQUE KEY site_path_Index(site_id, path(512))")
    String path;

    @Column(columnDefinition = " INT NOT NULL")
    int code;

    @Column(columnDefinition = "MEDIUMTEXT NOT NULL")
    String content;




    public Page(SiteModel site, String path, int code, String content) {
        this.site = site;
        this.path = path;
        this.code = code;
        this.content = content;
    }
}
