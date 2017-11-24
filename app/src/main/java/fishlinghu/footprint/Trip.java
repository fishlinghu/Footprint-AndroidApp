package fishlinghu.footprint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fishlinghu on 10/16/17.
 */

public class Trip implements Serializable{
    private String authorEmail;
    private String tripName;
    private ArrayList<CheckIn> checkInList = new ArrayList<>();
    // maping of email of voter to an empty string
    private Map<String, String> voterMap = new HashMap<>();

    public Trip() {
        this.tripName = "NoName";
    }

    public void addCheckIn(Double latitude, Double longitude, String photo_url, Date date_time, String description, String locationName, String stayingTime) {
        checkInList.add(new CheckIn(latitude, longitude, photo_url, date_time, description, locationName, stayingTime));
    }

    public ArrayList<CheckIn> getCheckInList() {
        return checkInList;
    }

    public String getTripName() {
        return tripName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
        return;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
        return;
    }

    // methods for votes related functions
    public Map<String, String> getVoterMap() {
        return voterMap;
    }

    public int getVoteCount() {
        return voterMap.size();
    }

    public boolean checkVoter(String voter_email) {
        return voterMap.containsKey(voter_email);
    }
}
