package CarvanaTracker.Component;

import CarvanaTracker.Model.VINEntry;
import CarvanaTracker.Service.VinService;
import CarvanaTracker.Model.ScrapingProxy;
//import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
//import org.springframework.data.jpa.repository.Lock;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class CarvanaAPIVehicleDetailsTask {

    private VinService vinService;
    private List<ScrapingProxy> scrapingProxy;
    private List<VINEntry> vinEntryList = new ArrayList<>();
    public boolean complete = false;
    private static Logger logger = LogManager.getLogger(CarvanaAPIVehicleDetailsTask.class);
    Random random = new Random();
    private List<VINEntry> pendingVINsToPush = new ArrayList<>();

    @Async
    public void run(){
        if(vinEntryList.size() > 0){
            //keeping running if there are VINs to run in the list
            checkVin();
        }else{
            //Flag to the scheduler that we are done and want to restart.
            //TODO: Replace with Spring Events API
            boolean complete = true;
        }
    }

    public void checkVin(){
        //remove VIN immediate to avoid another thread from running the same VIN.
        //remove VINs until list is  empty.
        VINEntry vinToRun = vinEntryList.get(random.nextInt(vinEntryList.size()));
        vinEntryList.remove(vinToRun);
        ScrapingProxy proxy = getScrapingProxyFromPool();

        //wrap request in a for loop. Retry until successful request takes place.
        while(true) {
            try {

                logger.debug("https://apim.carvana.io/vehicle-details-api/api/v1/availability?vehicleId=" + vinToRun.getVehicleId() + "&zipCode=67543");
                Connection.Response res = Jsoup.connect("https://apim.carvana.io/vehicle-details-api/api/v1/availability?vehicleId=" + vinToRun.getVehicleId() + "&zipCode=67543")
                        .method(org.jsoup.Connection.Method.GET)
                        .ignoreContentType(true)
                        .proxy(proxy.getIP(), proxy.getPort())
                        .execute();
                logger.debug(res.body());

                JSONObject jsonObject = new JSONObject(res.body());
                if (jsonObject.getString("error").equals("IsSold")) {
                    List<VINEntry> results = vinService.findAllWithVin(vinToRun.getVin());
                    logger.debug("found sold: " + vinToRun.getVin());
                    if (results.size() > 0) {
                        VINEntry tempEntry = results.get(0);
                        tempEntry.setSold(true);
                        tempEntry.setDateFoundSold(new Date());
                        vinService.save(tempEntry);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void loadVins(List<VINEntry> vinEntryList){
        //Only import new VINs if list is depleted
        if(this.vinEntryList.size() == 0){
            this.vinEntryList.addAll(vinEntryList);
        }
    }

    public ScrapingProxy getScrapingProxyFromPool(){
        return scrapingProxy.get(random.nextInt(scrapingProxy.size()));
    }
    public void setScrapingProxies(List<ScrapingProxy> scrapingProxyList){
        this.scrapingProxy = scrapingProxyList;
    }
    public void setVinService(VinService vinService) {
        this.vinService = vinService;
    }

    public List<VINEntry> getPendingVINsToPush(){
        return pendingVINsToPush;
    }
    public void emptyPendingVINs(){
        pendingVINsToPush = new ArrayList<>();
    }
    public List<VINEntry> getRemainingVins(){
        return vinEntryList;
    }
}
