package fishlinghu.footprint;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fishlinghu on 10/13/17.
 */

public class User {
    private String name;
    private String selfIntro;
    private String photoUrl;
    private Boolean unfinishedTripFlag;
    // mapping of account email to name of followers
    private Map<String, String> followerMap = new HashMap<>();
    // mapping of account email to name of people followed
    private Map<String, String> followingMap = new HashMap<>();
    // mapping of trip key to author name
    private Map<String, String> newTripMap = new HashMap<>();

    public User() {}

    public User(String name, String self_intro, String photo_url) {
        this.name = name;
        this.selfIntro = self_intro;
        this.photoUrl = photo_url;
        this.unfinishedTripFlag = false;
    }

    public String getName() {
        return name;
    }

    public String getSelfIntro() {
        return selfIntro;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Boolean getUnfinishedTripFlag() {
        return unfinishedTripFlag;
    }

    public Map<String, String> getFollowerMap() {
        return followerMap;
    }

    public Map<String, String> getFollowingMap() {
        return followingMap;
    }

    public Map<String, String> getNewTripMap() {
        return newTripMap;
    }
}
