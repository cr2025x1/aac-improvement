package cwnuchrome.aac_cwnu_it_2015_1;

/**
 * Created by Chrome on 7/28/15.
 */ // TODO: 축소조치
public class SearchFeedbackInfo {
    final public long id;
    final public int cat_id;
    final boolean relevance;
    long call_count;

    public SearchFeedbackInfo(int cat_id, long id, boolean relevance) {
        this.id = id;
        this.cat_id = cat_id;
        this.relevance = relevance;
        call_count = 0l;
    }
}
