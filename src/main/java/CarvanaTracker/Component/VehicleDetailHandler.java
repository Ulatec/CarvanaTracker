package CarvanaTracker.Component;

import CarvanaTracker.Model.VINEntry;
import CarvanaTracker.Service.VinService;
import CarvanaTracker.Utils.ListSplitter;
import CarvanaTracker.Utils.Proxies;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VehicleDetailHandler {

    @Value("${CarvanaTracker.Configuration.NumberOfAPIExecutors}")
    private int numberOfAPIExecutors;

    private VinService vinService;
    @Value("${CarvanaTracker.Collector.ProxyInformation.URL}")
    private String proxyURL;
    @Value("${CarvanaTracker.Collector.ProxyInformation.username}")
    private String proxyUsername;
    @Value("${CarvanaTracker.Collector.ProxyInformation.password}")
    private String proxyPassword;
    List<List<VINEntry>> vinPool;

    List<CarvanaAPIVehicleDetailsTask> listOfCarvanaAPIDetailExecutors = new ArrayList<>();
    Random random = new Random();

    public VehicleDetailHandler(VinService vinService){
        this.vinService = vinService;
    }

    public void run() {
        if(listOfCarvanaAPIDetailExecutors.size()> 0) {
            List<CarvanaAPIVehicleDetailsTask> finishedDetailExecutors = listOfCarvanaAPIDetailExecutors.stream().
                    filter(p -> p.getRemainingVins().size() == 0 || p.getRemainingVins() == null).
                    collect(Collectors.toList());
            for (CarvanaAPIVehicleDetailsTask carvanaAPIVehicleDetailsTask : finishedDetailExecutors) {
                //Send Vins to push to VinService. Avoid doing in executor Do not want locking failure.
                if (carvanaAPIVehicleDetailsTask.getPendingVINsToPush().size() > 0) {
                    vinService.batchSave(carvanaAPIVehicleDetailsTask.getPendingVINsToPush());
                }
                carvanaAPIVehicleDetailsTask.emptyPendingVINs();
                //Finished executor needs to acquire a new list of VINs from the VIN pool.
                prepareOneDetailExecutor(carvanaAPIVehicleDetailsTask);
            }
            //Randomly select an executor and run the next VIN in the pool of the executor.
            listOfCarvanaAPIDetailExecutors.get(random.nextInt(listOfCarvanaAPIDetailExecutors.size())).run();
        }
    }

    public void init(){
        if(vinService.findAllVinsNeedToBeSearched().size()>0) {
            vinPool = ListSplitter.split(vinService.findAllVinsNeedToBeSearched(), numberOfAPIExecutors);

            while (listOfCarvanaAPIDetailExecutors.size() < numberOfAPIExecutors) {

                listOfCarvanaAPIDetailExecutors.add(getNewExecutor());
            }
        }
    }

    public void prepareOneDetailExecutor(CarvanaAPIVehicleDetailsTask executorToPrepare){
        //Get new list of VINs to load.
        if(vinPool.size() == 0) {
            //no lists to load. Generate new lists.
            vinPool = ListSplitter.split(vinService.findAllVinsNeedToBeSearched(), numberOfAPIExecutors);
        }
        //Grab a sublist to load
        List<VINEntry> temporaryListOfVINs = vinPool.get(new Random().nextInt(vinPool.size()));
        //remove list from the pool.
        vinPool.remove(temporaryListOfVINs);
        //load VINs into executor
        executorToPrepare.loadVins(temporaryListOfVINs);

    }

    public CarvanaAPIVehicleDetailsTask getNewExecutor() {
        CarvanaAPIVehicleDetailsTask carvanaAPIVehicleDetailsTask = new CarvanaAPIVehicleDetailsTask();
        carvanaAPIVehicleDetailsTask.setVinService(vinService);
        carvanaAPIVehicleDetailsTask.setScrapingProxies(Proxies.getProxyListFromAPI(proxyURL,proxyUsername,proxyPassword,true));
        return carvanaAPIVehicleDetailsTask;
    }
}
