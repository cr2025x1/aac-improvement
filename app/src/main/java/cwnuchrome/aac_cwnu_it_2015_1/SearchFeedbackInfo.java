package cwnuchrome.aac_cwnu_it_2015_1;

/**
 * Created by Chrome on 7/28/15.
 */
public class SearchFeedbackInfo {
    final boolean relevance;
    long call_count;

    public SearchFeedbackInfo(boolean relevance) {
        this.relevance = relevance;
        call_count = 0l;
    }
}
