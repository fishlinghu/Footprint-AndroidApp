package fishlinghu.footprint;

import android.location.Location;

/**
 * Created by fishlinghu on 10/16/17.
 */

public class CheckIn {
    private Location location;
    private String photoUrl;

    public CheckIn(Location location, String photo_url) {
        this.location = location;
        this.photoUrl = photo_url;
    }

    public Location getLocation() {
        return location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
