package fishlinghu.footprint;

import java.util.Comparator;

/**
 * Created by fishlinghu on 11/28/17.
 */

public class TripVoteComparator implements Comparator<Trip> {
    @Override
    public int compare(Trip o1, Trip o2) {
        return o1.getVoteCount() - o2.getVoteCount();
    }
}