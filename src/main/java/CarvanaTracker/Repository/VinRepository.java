package CarvanaTracker.Repository;

import CarvanaTracker.Model.VINEntry;
//import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin("http://localhost:*")
@RepositoryRestResource(collectionResourceRel = "vinentry", itemResourceRel = "vinentry", path="vinentry")
public interface VinRepository extends MongoRepository<VINEntry, String> {

    List<VINEntry> findAllBySold(Boolean sold);
    List<VINEntry> findAllBySoldAndLastfoundBefore(Boolean sold, Date date);

    Optional<VINEntry> findByVin(String Vin);
    List<VINEntry> findAllByVin(String Vin);
    VINEntry save(VINEntry vinEntry);

    @Query(value = "{sold : false, imageUrl:  {$ne:null}}", sort = "{writeDown : -1}")
    Page<VINEntry> findAllSoldFalseAndOrderByWriteDown(Pageable pageable);

}
