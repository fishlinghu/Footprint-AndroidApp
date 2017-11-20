package fishlinghu.footprint;

/**
 * Created by fishlinghu on 11/20/17.
 */

public class SimpleUser {
    private String name;
    private String accountEmail;

    public SimpleUser(String name, String accountEmail){
        this.name = name;
        this.accountEmail = accountEmail;
    }

    public String getName() {
        return name;
    }

    public String getAccountEmail() {
        return accountEmail;
    }
}
