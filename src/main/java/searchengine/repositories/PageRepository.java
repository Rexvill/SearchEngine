package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;

public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query(value = "SELECT COUNT(path) FROM page  WHERE site_id = :siteId", nativeQuery = true)
    int getPagesCountPerSite(@Param(value = "siteId")int siteId);


    boolean existsBySite_IdAndPathAllIgnoreCase(int id, String path);

    @Transactional(propagation = Propagation.REQUIRED)
    default void truncateTableAndResetSequenceTable(){
        truncateTable();
        resetSequenceTable();
    }

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE page", nativeQuery = true)
    void truncateTable();

    @Modifying
    @Transactional
    @Query(value = "UPDATE page_seq SET next_val = 1", nativeQuery = true)
    void resetSequenceTable();

//    boolean findByUrl(String url);

}
