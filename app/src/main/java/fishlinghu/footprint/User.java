package fishlinghu.footprint;

/**
 * Created by fishlinghu on 10/13/17.
 */

public class User {
    private String name;
    private String selfIntro;
    private String photoUrl;
    private Boolean unfinishedTripFlag;

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

    public Boolean getUnfinishedTripFlag() { return unfinishedTripFlag; }
}
