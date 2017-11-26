package fishlinghu.footprint;

/**
 * Created by fishlinghu on 11/25/17.
 */

public class Comment {
    private String userName;
    private String userEmail;
    private String content;

    public Comment(String userName, String userEmail, String content) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getContent() {
        return content;
    }
}
