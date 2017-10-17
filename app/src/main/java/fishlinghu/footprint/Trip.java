package fishlinghu.footprint;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by fishlinghu on 10/16/17.
 */

public class Trip {
    private String tripName;
    private ArrayList<CheckIn> checkInList = new ArrayList<>();

    public void addCheckIn(Location current_location, String photo_url) {
        checkInList.add(new CheckIn(current_location, photo_url));
    }

    public ArrayList getCheckInList() {
        return checkInList;
    }

    public String getTripName() {
        return tripName;
    }
}
