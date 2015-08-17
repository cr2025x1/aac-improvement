package cwnuchrome.aac_cwnu_it_2015_1;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Chrome on 7/14/15.
 *
 * 쿼리를 구성하는 각 단어들의 정보 구조체
 *
 */
public class QueryWordInfoRaw {
    long count;
    double weight;

    public QueryWordInfoRaw(long count, double weight) {
        this.count = count;
        this.weight = weight;
    }

    // 참조 출처 링크:
    // http://stackoverflow.com/questions/27581/what-issues-should-be-considered-when-overriding-equals-and-hashcode-in-java
    @Override
    public int hashCode() {
        return hashCode_seed().toHashCode();
    }

    protected HashCodeBuilder hashCode_seed() {
        return new HashCodeBuilder(3119, 149). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(count).
                append(weight);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof QueryWordInfoRaw))
            return false;
        QueryWordInfoRaw qwi = (QueryWordInfoRaw)o;
        return qwi.count == count
                && qwi.weight == weight;
    }
}
