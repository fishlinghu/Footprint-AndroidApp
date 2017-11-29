package fishlinghu.footprint;

import java.util.Comparator;

/**
 * Created by fishlinghu on 11/26/17.
 */

public class CommentComparator implements Comparator<Comment> {
    @Override
    public int compare(Comment o1, Comment o2) {
        long diff = o1.getDateTime().getTime() - o2.getDateTime().getTime();
        int result;
        if (diff > 0) {
            result = 1;
        } else if (diff == 0) {
            result = 0;
        } else {
            result = -1;
        }
        return result;
    }
}
