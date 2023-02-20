package CarvanaTracker.Controller;


import CarvanaTracker.Service.DailyDataPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomDailyDataController {

    @Autowired
    private DailyDataPointService dailyDataPointService;


}
