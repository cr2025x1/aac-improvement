package cwnuchrome.aac_cwnu_it_2015_1;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Chrome on 7/14/15.
 *
 * 쿼리를 구성하는 각 단어들의 정보 구조체
 */
public class QueryWordInfo {
    long count;
    long ref_count;
    double weight;
    double feedback_weight;

    public QueryWordInfo(long count, long ref_count, double weight, double feedback_weight) {
        this.count = count;
        this.ref_count = ref_count;
        this.weight = weight;
        this.feedback_weight = feedback_weight;
    }

    // 참조 출처 링크:
    // http://stackoverflow.com/questions/27581/what-issues-should-be-considered-when-overriding-equals-and-hashcode-in-java
    @Override
    public int hashCode() {
        return new HashCodeBuilder(3119, 149). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(count).
                append(ref_count).
                append(weight).
                append(feedback_weight).
                toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof QueryWordInfo))
            return false;
        QueryWordInfo qwi = (QueryWordInfo)o;
        return qwi.count == count
                && qwi.ref_count  == ref_count
                && qwi.weight == weight
                && qwi.feedback_weight == feedback_weight;
    }
}
