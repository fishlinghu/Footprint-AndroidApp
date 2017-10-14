package fishlinghu.footprint;

/**
 * Created by fishlinghu on 10/13/17.
 */

public class User {
    private String name;
    private String self_intro;
    private String photo_url;

    public User() {}

    public User(String name, String self_intro, String photo_url) {
        this.name = name;
        this.self_intro = self_intro;
        this.photo_url = photo_url;
    }

    public String getName() {
        return name;
    }

    public String getSelfIntro() {
        return self_intro;
    }

    public String getPhotoUrl() {
        return photo_url;
    }
}
