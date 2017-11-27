package fishlinghu.footprint;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by fishlinghu on 11/25/17.
 */

public class Comment implements Serializable {
    private String userName;
    private String userEmail;
    private String content;
    private Date dateTime;

    public Comment(String userName, String userEmail, String content, Date dateTime) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.content = content;
        this.dateTime = dateTime;
    }

    public Comment() {}

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getContent() {
        return content;
    }

    public Date getDateTime() {
        return dateTime;
    }
}
