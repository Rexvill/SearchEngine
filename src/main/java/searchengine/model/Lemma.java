package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "lemma")
@NoArgsConstructor
@Getter
@Setter
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "site_id", nullable = false)
    int siteId;

    @Column(columnDefinition =  "VARCHAR(255) NOT NULL")
    String lemma;

    @Column(nullable = false)
    int frequency;




}
