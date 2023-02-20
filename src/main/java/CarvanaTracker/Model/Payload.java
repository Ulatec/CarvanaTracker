package CarvanaTracker.Model;

public class Payload {

    private String zip;

    private String payload;

    public Payload(String zip, String payload){
        this.zip = zip;
        this.payload = payload;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
