package fishlinghu.footprint;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by fishlinghu on 10/16/17.
 */

public class Trip {
    private String trip_name;
    private ArrayList<CheckIn> check_in_list = new ArrayList<>();

    public void addCheckIn(Location current_location, String photo_url) {
        check_in_list.add(new CheckIn(current_location, photo_url));
    }

    public ArrayList getCheckInList() {
        return check_in_list;
    }

    public String getTripName() {
        return trip_name;
    }
}
