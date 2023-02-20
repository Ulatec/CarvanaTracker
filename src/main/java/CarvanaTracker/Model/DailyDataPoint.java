package CarvanaTracker.Model;


import jakarta.persistence.Entity;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;


import java.util.Date;

@Entity
public class DailyDataPoint {


    @jakarta.persistence.Id
    @Id
    public ObjectId id;

    //Use beginning of day times to stay consistent.
    public Date date;

    public int countOfSold;

    public int countOfNewListings;

    public int countOfLastSeen;

    public double countOfSalesDollars;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCountOfSold() {
        return countOfSold;
    }

    public void setCountOfSold(int countOfSold) {
        this.countOfSold = countOfSold;
    }

    public int getCountOfNewListings() {
        return countOfNewListings;
    }

    public void setCountOfNewListings(int countOfNewListings) {
        this.countOfNewListings = countOfNewListings;
    }

    public int getCountOfLastSeen() {
        return countOfLastSeen;
    }

    public void setCountOfLastSeen(int countOfLastSeen) {
        this.countOfLastSeen = countOfLastSeen;
    }

    public double getCountOfSalesDollars() {
        return countOfSalesDollars;
    }

    public void setCountOfSalesDollars(double countOfSalesDollars) {
        this.countOfSalesDollars = countOfSalesDollars;
    }

}
