package CarvanaTracker.Model;

import jakarta.persistence.Entity;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.util.Date;

//@Entity
public class VINEntry {

    @Version
    private Long version;
    @jakarta.persistence.Id
    @Id
    //@Field("_id")

    private ObjectId id;
    private String vin;
    private double initialPrice;
    private Date lastfound;
    private Date firstfound;
    private Date inventoryDate;

    private int vehicleId;

    private int locationId;

    private int inventoryType;

    private double writeDown;

    private double mostRecentPrice;
    private boolean sold;
    private Date dateFoundSold;

    private String imageUrl;

    private int vehicleYear;

    private String vehicleMake;

    private String vehicleModel;

    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public Date getDateFoundSold() {
        return dateFoundSold;
    }

    public void setDateFoundSold(Date dateFoundSold) {
        this.dateFoundSold = dateFoundSold;
    }

    public VINEntry(){

    }
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }


    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(double price) {
        this.initialPrice = price;
    }

    public Date getLastfound() {
        return lastfound;
    }

    public void setLastfound(Date lastfound) {
        this.lastfound = lastfound;
    }

    public Date getFirstfound() {
        return firstfound;
    }

    public void setFirstfound(Date firstfound) {
        this.firstfound = firstfound;
    }

    public Date getInventoryDate() {
        return inventoryDate;
    }

    public void setInventoryDate(Date inventoryDate) {
        this.inventoryDate = inventoryDate;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(int inventoryType) {
        this.inventoryType = inventoryType;
    }

    public double getWriteDown() {
        return writeDown;
    }

    public void setWriteDown(double writeDown) {
        this.writeDown = writeDown;
    }

    public double getMostRecentPrice() {
        return mostRecentPrice;
    }

    public void setMostRecentPrice(double mostRecentPrice) {
        this.mostRecentPrice = mostRecentPrice;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getVehicleYear() {
        return vehicleYear;
    }

    public void setVehicleYear(int vehicleYear) {
        this.vehicleYear = vehicleYear;
    }

    public String getVehicleMake() {
        return vehicleMake;
    }

    public void setVehicleMake(String vehicleMake) {
        this.vehicleMake = vehicleMake;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    @Override
    public String toString() {
        return "VINEntry{" +
                "version=" + version +
                ", id=" + id +
                ", vin='" + vin + '\'' +
                ", initialPrice=" + initialPrice +
                ", lastfound=" + lastfound +
                ", firstfound=" + firstfound +
                ", inventoryDate=" + inventoryDate +
                ", vehicleId=" + vehicleId +
                ", locationId=" + locationId +
                ", inventoryType=" + inventoryType +
                ", writeDown=" + writeDown +
                ", mostRecentPrice=" + mostRecentPrice +
                ", sold=" + sold +
                ", dateFoundSold=" + dateFoundSold +
                ", imageUrl='" + imageUrl + '\'' +
                ", vehicleYear=" + vehicleYear +
                ", vehicleMake='" + vehicleMake + '\'' +
                ", vehicleModel='" + vehicleModel + '\'' +
                '}';
    }
}
