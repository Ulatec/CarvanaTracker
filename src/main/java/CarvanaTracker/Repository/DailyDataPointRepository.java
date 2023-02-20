package CarvanaTracker.Repository;

import CarvanaTracker.Model.DailyDataPoint;
import org.bson.types.ObjectId;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "dailyData", itemResourceRel = "dailyData", path="dailyData")
public interface DailyDataPointRepository extends MongoRepository<DailyDataPoint, ObjectId> {

    @Override
    Optional<DailyDataPoint> findById(ObjectId objectId);

    Optional<DailyDataPoint> findByDate(Date date);

    //List<DailyDataPoint> findByCountOfSoldNotOrderByDate(int not);
    @Query(value = "{countOfSold : {$ne:0}}", sort = "{ date : -1}")
    Page<DailyDataPoint> findByCountOfSoldNotOrderByDate(int not, Pageable pageable);
}
