package cwnuchrome.aac_cwnu_it_2015_1;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Chrome on 7/14/15.
 *
 * 쿼리를 구성하는 각 단어들의 정보 구조체
 */
public class QueryWordInfo extends QueryWordInfoRaw {
    long ref_count;
    double feedback_weight;

    public QueryWordInfo(long count, long ref_count, double weight, double feedback_weight) {
        super(count, weight);
        this.ref_count = ref_count;
        this.feedback_weight = feedback_weight;
    }

    @Override
    protected HashCodeBuilder hashCode_seed() {
        return super.hashCode_seed()
                .append(ref_count)
                .append(feedback_weight);
    }

    @Override
    public boolean equals(Object o) {
        boolean result = super.equals(o);
        if (!result) return false;
        if (!(o instanceof QueryWordInfo))
            return false;
        QueryWordInfo qwi = (QueryWordInfo)o;
        return qwi.ref_count  == ref_count
                && qwi.feedback_weight == feedback_weight;
    }
}
