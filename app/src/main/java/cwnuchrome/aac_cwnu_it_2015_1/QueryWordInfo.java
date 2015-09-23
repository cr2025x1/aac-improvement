package cwnuchrome.aac_cwnu_it_2015_1;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Chrome on 7/14/15.
 *
 * 쿼리를 구성하는 각 단어들의 정보 구조체
 * QWIR에 비해 조금 더 확장되었다. 추가된 두 맴버 변수는 모두 DB 액세스를 필요로 한다.
 */
public class QueryWordInfo extends QueryWordInfoRaw {
    long ref_count; // 이 단어를 사용하는 문서들의 총 개수.
    double feedback_weight; // 피드백으로 조정되는 이 단어에 대한 보정계수.

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
