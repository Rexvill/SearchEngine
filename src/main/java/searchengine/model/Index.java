package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity (name = "`index`")
@NoArgsConstructor
@Getter
@Setter
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "page_id", nullable = false)
    int pageId;

    @Column(name = "lemma_id", nullable = false)
    int lemmaId;

    @Column(name = "`rank`", nullable = false)
    float rank;

}
