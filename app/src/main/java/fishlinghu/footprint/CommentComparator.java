package fishlinghu.footprint;

import java.util.Comparator;

/**
 * Created by fishlinghu on 11/26/17.
 */

public class CommentComparator implements Comparator<Comment> {
    @Override
    public int compare(Comment o1, Comment o2) {
        return o1.getDateTime().compareTo(o2.getDateTime());
    }
}
