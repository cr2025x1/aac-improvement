package cwnuchrome.aac_cwnu_it_2015_1;

/**
 * Created by Chrome on 7/14/15.
 *
 * 쿼리를 구성하는 각 단어들의 정보 구조체
 */
public class QueryWordInfo {
    long count;
    long ref_count;

    public QueryWordInfo(long count, long ref_count) {
        this.count = count;
        this.ref_count = ref_count;
    }
}
