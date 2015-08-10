package cwnuchrome.aac_cwnu_it_2015_1;

/**
 * Created by Chrome on 7/28/15.
 *
 * 각 아이템의 피드백 정보 클래스
 */
public class SearchFeedbackInfo {
    final boolean relevance; // 상관있는 문서인가? 상관없는 문서인가?
    long call_count; // 호출(클릭) 횟수

    public SearchFeedbackInfo(boolean relevance) {
        this.relevance = relevance;
        call_count = 0l;
    }
}
