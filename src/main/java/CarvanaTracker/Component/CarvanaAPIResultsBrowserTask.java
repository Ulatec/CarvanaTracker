package CarvanaTracker.Component;

import CarvanaTracker.Model.Payload;
import CarvanaTracker.Model.VINEntry;
import CarvanaTracker.Service.VinService;
import CarvanaTracker.Utils.PayloadLibrary;
import CarvanaTracker.Model.ScrapingProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.Async;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CarvanaAPIResultsBrowserTask{
    private VinService vinService;
    private int threadnum;
    public boolean complete = false;
    private int totalPages, purchasesPending, neverUnlock, newVins;
    protected int currentPage;
    private static Logger logger = LogManager.getLogger(CarvanaAPIResultsBrowserTask.class);
    private List<ScrapingProxy> proxyList = new ArrayList<>();

    //Payload from the pre-defined payload library
    private Payload payload;

    private String modifiedPayload;

    List<VINEntry> pendingVINsToPush = new ArrayList<>();



    public CarvanaAPIResultsBrowserTask(){
        //Get random Payload
        payload = PayloadLibrary.getAllPayloads().get(new Random().nextInt(PayloadLibrary.getAllPayloads().size()));
        currentPage = 1;
        purchasesPending = 0;
        neverUnlock = 0;
        newVins = 0;
    }

    @Async
    public void run() {
         if(currentPage <= totalPages){
            //Run requests on each page until there are no more pages to query.
            runNextRequest();
        }else if(currentPage >= totalPages){
            //Flag to the handler that we are done and want to restart.
            //TODO: Replace with Spring Events API
            complete = true;
            purchasesPending = 0;
            neverUnlock = 0;
            totalPages = 0;
            currentPage = 0;
             modifiedPayload = null;
             newVins = 0;
        }

    }


    public void runNextRequest(){
        if(modifiedPayload == null){
            modifiedPayload = payload.getPayload();
        }
        logger.info("[" + threadnum + "] " + "PAGE:" + currentPage + " Pending: " + purchasesPending + " NeverUnlock: " + neverUnlock + " newVins: " + newVins);
        //increment immediately to avoid another thread executor running an identical query while one is still in progress.
        currentPage++;
        //timestamp is required for the body of the API query
        Date timestamp = new Date();
        Random random = new Random();
        ScrapingProxy proxy = proxyList.get(random.nextInt(proxyList.size()));
        try {
            //Modify the payload to request next page.
            modifiedPayload = modifiedPayload.replace("\"page\":" + (currentPage-1), "\"page\":" + currentPage)
                    .replace("\"timestamp\":*,", "\"timestamp\":"+timestamp + ",");
            Connection.Response res2 = Jsoup.connect("https://apik.carvana.io/merch/thekraken/")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .requestBody(modifiedPayload)
                    .proxy(proxy.getIP(), proxy.getPort())
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Content-Type", "application/json")
                    .header("Referer", "https://www.carvana.com/cars")
                    .header("Origin", "https://www.carvana.com")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .execute();
            JSONObject json2 = new JSONObject(res2.body());
            int pages = json2.getJSONObject("data").getJSONObject("inventory").getJSONObject("pagination").getInt("totalMatchedPages");
            totalPages = pages;
            System.out.println("pages: " + pages);
            JSONArray jsonArray  = json2.getJSONObject("data").getJSONObject("inventory").getJSONArray("vehicles");
                try {
                    pendingVINsToPush.addAll(parseJSONIntoVINEntryObjects(jsonArray));
                } catch (Exception e) {
                    e.printStackTrace();
                }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void runInitialSearchRequest(){
        modifiedPayload = payload.getPayload();
        ScrapingProxy scrapingProxy = getProxy();
        try {

            //Run our initial search request to find out how many pages exist. Not all regions/zip codes have the same results.
            Connection.Response res = Jsoup.connect("https://apik.carvana.io/merch/thekraken/")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .requestBody(payload.getPayload())
                    .proxy(scrapingProxy.getIP(), scrapingProxy.getPort())
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Content-Type", "application/json")
                    .header("Referer", "https://www.carvana.com/cars")
                    .header("Origin", "https://www.carvana.com")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .execute();
            JSONObject JsonResponse = new JSONObject(res.body()).getJSONObject("data").getJSONObject("inventory").getJSONObject("pagination");
            totalPages = JsonResponse.getInt("totalMatchedPages");

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<VINEntry> parseJSONIntoVINEntryObjects(JSONArray JsonArray) throws ParseException {
        List<VINEntry> parsedVINs = new ArrayList<>();
        int newOnPage = 0;
        for(Object object : JsonArray) {
            JSONObject vehicleJsonObject = (JSONObject) object;
            VINEntry newEntry = new VINEntry();
            newEntry.setVin(vehicleJsonObject.getString("vin"));
            newEntry.setLastfound(new Date());
            newEntry.setMostRecentPrice(vehicleJsonObject.getJSONObject("price").getInt("total"));
            newEntry.setLocationId(vehicleJsonObject.getInt("locationId"));
            newEntry.setVehicleId(vehicleJsonObject.getInt("vehicleId"));
            newEntry.setInventoryType(vehicleJsonObject.getInt("vehicleInventoryType"));
            newEntry.setImageUrl(vehicleJsonObject.getString("jellyBeanDesktopUrl"));
            newEntry.setVehicleMake(vehicleJsonObject.getString("make"));
            newEntry.setVehicleYear(vehicleJsonObject.getInt("year"));
            newEntry.setVehicleModel(vehicleJsonObject.getString("model"));
            if (vehicleJsonObject.has("addedToCoreInventoryDateTime")) {
                if(vehicleJsonObject.get("addedToCoreInventoryDateTime") != JSONObject.NULL) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
                    Date date = sdf.parse(vehicleJsonObject.getString("addedToCoreInventoryDateTime"));
                    newEntry.setInventoryDate(date);
                }
            }
            List<VINEntry> existingEntriesWithVIN = vinService.findAllWithVin(vehicleJsonObject.getString("vin"));
            if(existingEntriesWithVIN.size() > 0 ){
                newEntry.setId(existingEntriesWithVIN.get(0).getId());
                newEntry.setVersion(existingEntriesWithVIN.get(0).getVersion());
                newEntry.setWriteDown(existingEntriesWithVIN.get(0).getInitialPrice() - newEntry.getMostRecentPrice());
                newEntry.setFirstfound(existingEntriesWithVIN.get(0).getFirstfound());
            }else{
                newOnPage++;
                newEntry.setFirstfound(new Date());
                newEntry.setWriteDown(0);
                newEntry.setInitialPrice(vehicleJsonObject.getJSONObject("price").getInt("total"));
            }
            //double check that initalprice isnt 0
            if(newEntry.getInitialPrice() == 0){
                newEntry.setInitialPrice(vehicleJsonObject.getJSONObject("price").getInt("total"));
            }
            vinService.save(newEntry);
            parsedVINs.add(newEntry);
        }
        newVins = newVins + newOnPage;
        return parsedVINs;
    }

    public void randomNewPayload(){
        payload = PayloadLibrary.getAllPayloads().get(new Random().nextInt(PayloadLibrary.getAllPayloads().size()));
    }

    public ScrapingProxy getProxy() {
        Random random = new Random();
        return proxyList.get(random.nextInt(proxyList.size()));
    }
    public String getPayloadZipCode(){
        return payload.getZip();
    }

    public void setVinService(VinService vinService){
        this.vinService = vinService;
    }
    public void setProxyList(List<ScrapingProxy> proxyList){
        this.proxyList = proxyList;
    }
    public void setThreadNumber(int threadNumber){
        this.threadnum = threadNumber;
    }
    public List<VINEntry> getPendingVINsToPush(){
        return pendingVINsToPush;
    }
    public void emptyPendingVINs(){
        pendingVINsToPush = new ArrayList<>();
    }
}
