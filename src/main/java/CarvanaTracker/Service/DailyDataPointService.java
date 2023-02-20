package CarvanaTracker.Service;

import CarvanaTracker.Model.DailyDataPoint;
import CarvanaTracker.Repository.DailyDataPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class DailyDataPointService {

    @Autowired
    private DailyDataPointRepository dailyDataPointRepository;

    public List<DailyDataPoint> findAll(){
        return dailyDataPointRepository.findAll();
    }
    public Optional<DailyDataPoint> findByDate(Date date){
        return dailyDataPointRepository.findByDate(date);
    }
    public void save(DailyDataPoint dailyDataPoint){
        dailyDataPointRepository.save(dailyDataPoint);
    }
}
