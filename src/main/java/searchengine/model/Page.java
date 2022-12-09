package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "page")
@Table(indexes = @Index(name = "path_index", columnList = "path", unique = true))
@NoArgsConstructor
@Getter
@Setter
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name= "site_id")
    Site site;

    @Column(columnDefinition = "varchar(255) NOT NULL")
    String path;

    @Column(columnDefinition = " INT NOT NULL")
    int code;

    @Column(columnDefinition = "MEDIUMTEXT NOT NULL")
    String content;
}
