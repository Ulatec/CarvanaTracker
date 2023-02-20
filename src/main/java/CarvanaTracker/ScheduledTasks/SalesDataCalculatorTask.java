package CarvanaTracker.ScheduledTasks;

import CarvanaTracker.Model.DailyDataPoint;
import CarvanaTracker.Model.VINEntry;
import CarvanaTracker.Repository.VinRepository;
import CarvanaTracker.Service.DailyDataPointService;
import CarvanaTracker.Service.VinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class SalesDataCalculatorTask {
    @Autowired
    private VinService vinService;
    @Autowired
    private VinRepository vinRepository;
    @Autowired
    private DailyDataPointService dailyDataPointService;
    //Schedule to run every 10 minutes
    @Scheduled(fixedDelay = 600000)
    public void calculateDailyData(){
        List<VINEntry> allVins = vinService.findAll();
        LocalDate now = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        //avoid using now.method() as it modifies the original now object.
        LocalDate oneYearPrior = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusYears(1);
        LocalDate movingDate = now;
        while (movingDate.isAfter(oneYearPrior)){
            int foundSold = 0;
            int firstFound = 0;
            int mostRecentFound = 0;
            double dollarTotal = 0;
            //Run through all VINs and check for all conditions to avoid having to iterate over a long list multiple times.
            for (VINEntry vinEntry : allVins) {
                if (vinEntry.getDateFoundSold() != null) {
                    LocalDate foundSoldDate = vinEntry.getDateFoundSold().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (foundSoldDate.equals(movingDate)) {
                        foundSold++;
                        dollarTotal = dollarTotal + vinEntry.getMostRecentPrice();
                    }
                }
                if (vinEntry.getFirstfound() != null) {
                    LocalDate firstFoundDate = vinEntry.getFirstfound().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (firstFoundDate.equals(movingDate)) {
                        firstFound++;
                    }
                }
                if (vinEntry.getLastfound() != null) {
                    LocalDate lastFoundDate = vinEntry.getLastfound().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (lastFoundDate.equals(movingDate)) {
                        mostRecentFound++;
                    }
                }
            }

            //convert to Java.Util.Date AT START OF DAY. All lookups will be set to start of day.
            Date referenceDate = Date.from(movingDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Optional<DailyDataPoint> optionalDailyDataPoint = dailyDataPointService.findByDate(referenceDate);
            if(mostRecentFound != 0 && firstFound != 0) {
                DailyDataPoint dailyDataPoint;
                if (optionalDailyDataPoint.isPresent()) {
                    //update existing data point with any new information.
                    dailyDataPoint = optionalDailyDataPoint.get();
                } else {
                    //make new data point with new date.
                    dailyDataPoint = new DailyDataPoint();
                    dailyDataPoint.setDate(referenceDate);
                }
                dailyDataPoint.setCountOfLastSeen(mostRecentFound);
                dailyDataPoint.setCountOfNewListings(firstFound);
                dailyDataPoint.setCountOfSold(foundSold);
                dailyDataPoint.setCountOfSalesDollars(dollarTotal);
                dailyDataPointService.save(dailyDataPoint);
            }
            movingDate = movingDate.minusDays(1);
        }

    }


    // Function to calculate write down for any entries that were written in manually.
    @Scheduled(initialDelay = 1000, fixedDelay = 600000)
    public void calculatewriteDowns(){
        List<VINEntry> allVins = vinService.findAll();
        for(VINEntry vinEntry : allVins){
            vinEntry.setWriteDown(vinEntry.getInitialPrice() - vinEntry.getMostRecentPrice());

            System.out.println(allVins.indexOf(vinEntry) + "/ " + allVins.size());
        }
        vinRepository.saveAll(allVins);
        System.out.println("DONE SAVING.");
    }

}
