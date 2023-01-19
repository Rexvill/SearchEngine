package searchengine.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteModel;

public interface SiteRepository extends JpaRepository<SiteModel, Integer> {

    @Override
    void deleteAllInBatch();

    SiteModel findByUrl(String url);


    default void truncateTableWithFK() {
        FKCheckOff();
        truncateTable();
        FKCheckOn();
    }

    @Modifying
    @Transactional
    @Query(value = "SET FOREIGN_KEY_CHECKS=0", nativeQuery = true)
    void FKCheckOff();

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE site", nativeQuery = true)
    void truncateTable();

    @Modifying
    @Transactional
    @Query(value = "SET FOREIGN_KEY_CHECKS=1", nativeQuery = true)
    void FKCheckOn();


}
