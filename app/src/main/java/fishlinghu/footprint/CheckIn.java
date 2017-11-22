package fishlinghu.footprint;

import android.location.Location;
import android.util.Log;

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
    private String locationName;
    private String stayingTime;

    public CheckIn() {}

    public CheckIn(Double latitude, Double longitude, String photo_url, Date dateTime, String description, String locationName, String stayingTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photo_url;
        this.dateTime = dateTime;
        this.description = description;
        this.locationName = locationName;
        this.stayingTime = stayingTime;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Date getDateTime() { return dateTime; }

    public String getDescription() { return description; }

    public String getLocationName() { return locationName; }

    public String getStayingTime() {
        return stayingTime;
    }
}
