package CarvanaTracker.Component;

import CarvanaTracker.Model.DailyDataPoint;
import CarvanaTracker.Model.VINEntry;
import CarvanaTracker.Service.DailyDataPointService;
import CarvanaTracker.Service.VinService;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


public class ForecastCalculator {

    private DailyDataPointService dailyDataPointService;
    public SalesTotal calculate(){
        LocalDate today = LocalDate.now();
        //find most recent quarterly boundary to star counting from
        LocalDate  savedBoundaryDate = getPastBoundary(today);
        LocalDate endingBoundaryDate = getFutureBoundary(today);
        // now that we have our boundary date, count the cumulative sales to date.
        SalesTotal salesTotal = calculateSalesBetweenDates(savedBoundaryDate,today);
        //calculate number of days (or % of time) remaining the current quarter.
        int numberOfDaysRemainingInQuarter = (int) ChronoUnit.DAYS.between(today,endingBoundaryDate );

        //find % of sales that happened in the remaining X days of the prior quarter
        LocalDate priorBoundary = getPastBoundary(savedBoundaryDate);
        SalesTotal salesTotalPriorQuarter = calculateSalesBetweenDates(priorBoundary,savedBoundaryDate);

        SalesTotal salesTotalPartial = calculateSalesBetweenDates(savedBoundaryDate.minusDays(numberOfDaysRemainingInQuarter), savedBoundaryDate);

        double salesPercent = (double)salesTotalPartial.getSales()/salesTotalPriorQuarter.getSales();
        double dollarsPercent = (double)salesTotalPartial.getDollars()/salesTotalPriorQuarter.getDollars();
        System.out.println(numberOfDaysRemainingInQuarter);
        return new SalesTotal((int)(salesTotal.getSales()*(1+salesPercent)), (int)(salesTotal.getDollars()*(1+dollarsPercent)));
    }



    public void setDailyDataPointService(DailyDataPointService dailyDataPointService) {
        this.dailyDataPointService = dailyDataPointService;
    }

    public List<LocalDate> getQuarterlyDates(){
        List<LocalDate> dates = new ArrayList<>();

        dates.add(LocalDate.of(LocalDate.now().getYear(),1,1));
        dates.add(LocalDate.of(LocalDate.now().getYear(),4,1));
        dates.add(LocalDate.of(LocalDate.now().getYear(),9,1));
        return dates;
    }
    public LocalDate getPastBoundary(LocalDate today){
        LocalDate savedBoundaryDate = null;
        List<LocalDate> boundaryDates = getQuarterlyDates();
        for(LocalDate localDate : boundaryDates){
            //if date is in the future, decrement by 1 year.
            if(localDate.isAfter(today) || localDate.equals(today)){
                localDate = localDate.minusYears(1);
            }
            if(savedBoundaryDate != null) {
                if (localDate.isAfter(savedBoundaryDate)) {
                    savedBoundaryDate = localDate;
                }
            }else{
                savedBoundaryDate = localDate;
            }
        }
        return savedBoundaryDate;
    }
    public LocalDate getFutureBoundary(LocalDate today){
        LocalDate savedBoundaryDate = null;
        List<LocalDate> boundaryDates = getQuarterlyDates();
        for(LocalDate localDate : boundaryDates){
            //if date is in the past, decrement by 1 year.
            if(localDate.isBefore(today) || localDate.equals(today)){
                localDate = localDate.plusYears(1);
            }
            if(savedBoundaryDate != null) {
                if (localDate.isBefore(savedBoundaryDate)) {
                    savedBoundaryDate = localDate;
                }
            }else{
                savedBoundaryDate = localDate;
            }
        }
        return savedBoundaryDate;
    }

    public SalesTotal calculateSalesBetweenDates(LocalDate start, LocalDate end){
        int foundSold = 0;
        int dollarTotal = 0;
        LocalDate movingDate = start;
        while (movingDate.isBefore(end)){
            Date referenceDate = Date.from(movingDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Optional<DailyDataPoint> dataPointOptional = dailyDataPointService.findByDate(referenceDate);
            if(dataPointOptional.isPresent()){
                foundSold += dataPointOptional.get().countOfSold;
                dollarTotal += dataPointOptional.get().countOfSalesDollars;
            }
            System.out.println(movingDate + "\t" + foundSold + "\t" + dollarTotal + "\t");
            movingDate = movingDate.plusDays(1);
        }
        return new SalesTotal(foundSold, dollarTotal);
    }

    public static class SalesTotal{
        private int sales;
        private int dollars;

        public SalesTotal(int sales, int dollars) {
            this.sales = sales;
            this.dollars = dollars;
        }

        public int getSales() {
            return sales;
        }

        public void setSales(int sales) {
            this.sales = sales;
        }

        public int getDollars() {
            return dollars;
        }

        public void setDollars(int dollars) {
            this.dollars = dollars;
        }
    }
}


