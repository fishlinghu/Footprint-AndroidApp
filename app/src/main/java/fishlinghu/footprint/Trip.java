package fishlinghu.footprint;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by fishlinghu on 10/16/17.
 */

public class Trip {
    private String tripName;
    private ArrayList<CheckIn> checkInList = new ArrayList<>();

    public Trip() {
        this.tripName = "NoName";
    }

    public void addCheckIn(Location current_location, String photo_url, Date date_time) {
        checkInList.add(new CheckIn(current_location, photo_url, date_time));
    }

    public ArrayList getCheckInList() {
        return checkInList;
    }

    public String getTripName() {
        return tripName;
    }
}
