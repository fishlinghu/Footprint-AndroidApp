package fishlinghu.footprint;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by fishlinghu on 10/16/17.
 */

public class Trip implements Serializable{
    private String tripName;
    private ArrayList<CheckIn> checkInList = new ArrayList<>();

    public Trip() {
        this.tripName = "NoName";
    }

    public void addCheckIn(Double latitude, Double longitude, String photo_url, Date date_time, String description) {
        checkInList.add(new CheckIn(latitude, longitude, photo_url, date_time, description));
    }

    public ArrayList getCheckInList() {
        return checkInList;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
        return;
    }
}
