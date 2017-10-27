package fishlinghu.footprint;

import android.location.Location;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by fishlinghu on 10/16/17.
 */

public class CheckIn implements Serializable {
    private Double latitude;
    private Double longitude;
    private String photoUrl;
    private Date dateTime;
    private String description;

    public CheckIn() {}

    public CheckIn(Double latitude, Double longitude, String photo_url, Date dateTime, String description) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photo_url;
        this.dateTime = dateTime;
        this.description = description;
    }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Date getDateTime() { return dateTime; }

    public String getDescription() { return description; }
}
