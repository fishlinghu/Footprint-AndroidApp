package fishlinghu.footprint;

/**
 * Created by fishlinghu on 10/13/17.
 */

public class User {
    private String name;
    private String photo_url;

    public User() {}

    public User(String name, String photo_url) {
        this.name = name;
        this.photo_url = photo_url;
    }

    public String getName() {
        return name;
    }

    public String getPhoto_url() {
        return photo_url;
    }
}
