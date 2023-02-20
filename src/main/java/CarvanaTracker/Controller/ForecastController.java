package CarvanaTracker.Controller;


import CarvanaTracker.Component.ForecastCalculator;
import CarvanaTracker.Service.DailyDataPointService;
import CarvanaTracker.Service.VinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("http://localhost:*")
@RestController
public class ForecastController {
    @Autowired
    private DailyDataPointService dailyDataPointService;

    @GetMapping("/quarterlyForecast")
    public ForecastCalculator.SalesTotal getQuarterlyForecast(){
        ForecastCalculator forecastCalculator = new ForecastCalculator();
        forecastCalculator.setDailyDataPointService(dailyDataPointService);

        return forecastCalculator.calculate();
    }
}
