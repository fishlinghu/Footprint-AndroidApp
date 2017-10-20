package fishlinghu.footprint;

import android.location.Location;

import java.util.Date;

/**
 * Created by fishlinghu on 10/16/17.
 */

public class CheckIn {
    private Location location;
    private String photoUrl;
    private Date date_time;

    public CheckIn(Location location, String photo_url, Date date_time) {
        this.location = location;
        this.photoUrl = photo_url;
        this.date_time = date_time;
    }

    public Location getLocation() {
        return location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
