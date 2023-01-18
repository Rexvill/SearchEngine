package searchengine.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.SiteModel;

public interface SiteRepository extends JpaRepository<SiteModel, Integer> {

    @Override
    void deleteAllInBatch();

//    @Modifying
//    @Transactional
//    @Query(value = "SET FOREIGN_KEY_CHECKS=0; TRUNCATE site; SET FOREIGN_KEY_CHECKS=1", nativeQuery = true)
//    void clearTable();


}
