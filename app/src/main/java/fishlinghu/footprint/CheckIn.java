package fishlinghu.footprint;

import android.location.Location;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by fishlinghu on 10/16/17.
 */

public class CheckIn implements Serializable {
    private Double latitude;
    private Double longitude;
    private String photoUrl;
    private int photoRotatedDegree;
    private Date dateTime;
    private String description;
    private String locationName;
    private String stayingTime;
    private ArrayList<Comment> commentList = new ArrayList<>();

    public CheckIn() {}

    public CheckIn(Double latitude, Double longitude, String photo_url, int photoRotatedDegree, Date dateTime, String description, String locationName, String stayingTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photo_url;
        this.photoRotatedDegree = photoRotatedDegree;
        this.dateTime = dateTime;
        this.description = description;
        this.locationName = locationName;
        this.stayingTime = stayingTime;
    }

    public void addComment(String userName, String userEmail, String content) {
        commentList.add(new Comment(userName, userEmail, content));
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public int getPhotoRotatedDegree() {
        return photoRotatedDegree;
    }

    public Date getDateTime() { return dateTime; }

    public String getDescription() { return description; }

    public String getLocationName() { return locationName; }

    public String getStayingTime() {
        return stayingTime;
    }

    public ArrayList<Comment> getCommentList() {
        return commentList;
    }
}
