package CarvanaTracker.Component;

import CarvanaTracker.Model.VINEntry;
import CarvanaTracker.Service.VinService;
import CarvanaTracker.Utils.Proxies;
import CarvanaTracker.Model.ScrapingProxy;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;

public class ExecutorHandlerComponent {
    @Value("${CarvanaTracker.Configuration.NumberOfAPIExecutors}")
    private int numberOfAPIExecutors;

    @Value("${CarvanaTracker.Collector.ProxyInformation.URL}")
    private String proxyURL;
    @Value("${CarvanaTracker.Collector.ProxyInformation.username}")
    private String proxyUsername;
    @Value("${CarvanaTracker.Collector.ProxyInformation.password}")
    private String proxyPassword;
    private VinService vinService;

    Random random = new Random();

    List<CarvanaAPIResultsBrowserTask> listOfCarvanaAPIExecutors ;
    List<List<VINEntry>> vinPool = new ArrayList<>();


    public ExecutorHandlerComponent(VinService vinService){
        this.vinService = vinService;
         listOfCarvanaAPIExecutors = new ArrayList<>();
    }

    public void run(){
        if(listOfCarvanaAPIExecutors.size()>0){
            List<CarvanaAPIResultsBrowserTask> finishedDetailExecutors = listOfCarvanaAPIExecutors.stream().
                            filter(p -> p.complete).
                            collect(Collectors.toList());
                    for(CarvanaAPIResultsBrowserTask carvanaAPIResultsBrowserTask : finishedDetailExecutors ){
                        //Send Vins to push to VinService. Avoid doing in executor Do not want locking failure.
                        if(carvanaAPIResultsBrowserTask.getPendingVINsToPush().size() > 0) {
                            //vinService.batchSave(carvanaAPIResultsBrowserTask.getPendingVINsToPush());
                        }
                        carvanaAPIResultsBrowserTask.emptyPendingVINs();
                        //Finished executor needs to acquire a new list of VINs from the VIN pool.
                        prepareOneExecutor(listOfCarvanaAPIExecutors,carvanaAPIResultsBrowserTask);
                    }
            //Randomly select an executor and run the next VIN in the pool of the executor.
             listOfCarvanaAPIExecutors.get(random.nextInt(listOfCarvanaAPIExecutors.size())).run();
        }

    }

    public void init(){
        while(listOfCarvanaAPIExecutors.size() <numberOfAPIExecutors){
            //add new Executors until given number
            listOfCarvanaAPIExecutors.add(getNewExecutor());
        }

        prepareAllExecutors(listOfCarvanaAPIExecutors);
    }

    public void prepareAllExecutors(List<CarvanaAPIResultsBrowserTask> listOfCarvanaAPIExecutors){
        List<ScrapingProxy> proxyList = Proxies.getProxyListFromAPI(proxyURL,proxyUsername,proxyPassword,true);
        //Set list of proxies for all executors
        for(CarvanaAPIResultsBrowserTask carvanaAPIResultsBrowserTask : listOfCarvanaAPIExecutors){
            carvanaAPIResultsBrowserTask.setProxyList(proxyList);
        }
        //Set instance numbers for each executor. Not critical. Nice for debugging.
        int i = 0;
        for(CarvanaAPIResultsBrowserTask carvanaAPIResultsBrowserTask : listOfCarvanaAPIExecutors){
            carvanaAPIResultsBrowserTask.setThreadNumber(i);
            i++;
        }
        //Store unique payload zip codes.
        HashSet<String> uniqueZipCodeStrings = new HashSet<>();

        //Check to make sure that no two executors use the same zip code
        for(CarvanaAPIResultsBrowserTask carvanaAPIResultsBrowserTask : listOfCarvanaAPIExecutors) {
            if (uniqueZipCodeStrings.contains(carvanaAPIResultsBrowserTask.getPayloadZipCode())) {
                while (uniqueZipCodeStrings.contains(carvanaAPIResultsBrowserTask.getPayloadZipCode())) {
                    carvanaAPIResultsBrowserTask.randomNewPayload();
                }
            } else {
                uniqueZipCodeStrings.add(carvanaAPIResultsBrowserTask.getPayloadZipCode());
                //Run initial search request now that all requirements are met.
                carvanaAPIResultsBrowserTask.runInitialSearchRequest();
            }
        }
    }

    public void prepareOneExecutor(List<CarvanaAPIResultsBrowserTask> listOfCarvanaAPIExecutors, CarvanaAPIResultsBrowserTask executorToPrepare){
        //Build unique zip set before checking against list.
        HashSet<String> uniqueZipCodeStrings = new HashSet<>();
        for(CarvanaAPIResultsBrowserTask carvanaAPIResultsBrowserTask : listOfCarvanaAPIExecutors){
            uniqueZipCodeStrings.add(carvanaAPIResultsBrowserTask.getPayloadZipCode());
        }

        //Remove old zip from the list. This leaves only the currently active zip codes to check against.
        uniqueZipCodeStrings.remove(executorToPrepare.getPayloadZipCode());
        //Get a new payload/ZipCode to check against.
        executorToPrepare.randomNewPayload();

        String zipToCheck = executorToPrepare.getPayloadZipCode();
        //Store unique payload zip codes.
        while(uniqueZipCodeStrings.contains(zipToCheck)) {
            executorToPrepare.randomNewPayload();
        }
        executorToPrepare.complete = false;
        //Run initial search request now that all requirements are met.
       // executorToPrepare.runInitialSearchRequest();
    }


    public CarvanaAPIResultsBrowserTask getNewExecutor() {
        CarvanaAPIResultsBrowserTask carvanaAPIResultsBrowserTask = new CarvanaAPIResultsBrowserTask();
        carvanaAPIResultsBrowserTask.setVinService(vinService);
        return carvanaAPIResultsBrowserTask;
    }



}



