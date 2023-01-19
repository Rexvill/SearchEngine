package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;

public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query(value = "SELECT COUNT(path) FROM page  WHERE site_id = :siteId")
    int getPagesCountPerSite(@Param(value = "siteId")int siteId);


    boolean existsBySite_IdAndPathAllIgnoreCase(int id, String path);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE page", nativeQuery = true)
    void truncateTable();



}
