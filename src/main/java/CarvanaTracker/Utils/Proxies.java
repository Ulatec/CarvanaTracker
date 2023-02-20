package CarvanaTracker.Utils;

import CarvanaTracker.Model.ScrapingProxy;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Proxies {

    public static List<ScrapingProxy> getProxyListFromAPI(String URL, String username, String password, boolean authenticationRequired){

        // Modified for specific proxy vendor.
        String base64login = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        List<ScrapingProxy> list = new ArrayList<>();
        try{
            Connection connection = Jsoup.connect(URL).method(Connection.Method.GET)
                    .method(Connection.Method.GET);
            if(authenticationRequired){
                connection.header("Authorization", "Basic " + base64login);
            }
            String[] lines = connection.execute().body().split("\n");
            for(String line : lines){
                String[] params = line.split(":");
                list.add(new ScrapingProxy(params[0], Integer.parseInt(params[1])));
            }
        }catch(Exception e){
            return list;
        }
        return list;
    }


}