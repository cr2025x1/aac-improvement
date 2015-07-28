package cwnuchrome.aac_cwnu_it_2015_1;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Chrome on 7/27/15.
 *
 * 검색 엔진의 피드백 Helper 클래스
 *
 * 피드백 방법 : 묵시적 피드백 (Implicit feedback)
 * 주어진 쿼리로 나온 검색 결과를, 그 쿼리를 사용자가 변경하기 이전에 클릭한 정보를 토대로 피드백한다.
 * 여기서 "변경하기 이전"이란 것은, 사용자가 입력한 텍스트 그 자체의 변경을 의미하지 않는다.
 * 텍스트-쿼리 해쉬맵은 1:1 대응관계가 아니기 때문이다.
 * 입력된 텍스트를 처리해서 만들어진 id-count 해쉬맵 결과물이 변경되는 경우에만 변경된 것으로 간주된다.
 *
 * 피드백의 범위
 * 사용자가 클릭한 아이템 중 가장 아래쪽의 아이템이 n번째라면, 가장 위에서부터 그 n번째까지의 아이템만을 피드백한다.
 * 그 이하에 대해서는 사용자가 검색 결과를 읽지 않고 종료한 것으로 간주해, 피드백이 없는 것으로 본다.
 *
 * 관련-비관련성의 결정
 * 1~n번째 아이템 중 사용자가 클릭한 것은 쿼리와 관련있는 문서로 간주, 그렇지 않은 것은 관련없는 문서로 간주한다.
 */

public class SearchImplicitFeedback {
    protected Vector<HashMap<Long, SearchFeedbackInfo>> fb_info_v;
    protected HashMap<Long, ? extends QueryWordInfoRaw> query_id_map;
    protected int pos_max;
    protected DocumentProcessor doc_proc;
    protected int rel_doc_count;

    @SuppressLint("UseSparseArrays")
    public SearchImplicitFeedback(DocumentProcessor doc_proc) {
        fb_info_v = new Vector<>(ActionMain.item.ITEM_COUNT);
        for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) {
            fb_info_v.add(new HashMap<Long, SearchFeedbackInfo>());
        }
        pos_max = -1;
        rel_doc_count = 0;
        this.doc_proc = doc_proc;
        query_id_map = null;
    }

    public void set_query_id_map(HashMap<Long, ? extends QueryWordInfoRaw> query_id_map) {
        clear();
        this.query_id_map = query_id_map;
    }

    public void add_rel(ItemIDInfo idInfo, int pos) {
        int category_id = idInfo.category_id;
        long id = idInfo.id;
        HashMap<Long, SearchFeedbackInfo> map = fb_info_v.get(category_id);
        SearchFeedbackInfo sfi;
        if ((sfi = map.get(id)) == null) {
            sfi = new SearchFeedbackInfo(true);
            sfi.call_count = 1l;
            map.put(id, sfi);
        }
        else {
            sfi.call_count++;
        }

        pos_max = Math.max(pos, pos_max);
        rel_doc_count++;
    }

    protected void add_irrel() {
        for (int i = 0; i <= pos_max; i++) {
            ItemIDInfo idInfo = doc_proc.get_doc_id(i);
            int category_id = idInfo.category_id;
            long id = idInfo.id;
            HashMap<Long, SearchFeedbackInfo> map = fb_info_v.get(category_id);
            if (!map.containsKey(id)) {
                map.put(
                        id,
                        new SearchFeedbackInfo(false)
                );
            }
        }
    }

    public void clear() {
        for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) {
            fb_info_v.get(i).clear();
        }
        pos_max = -1;
        rel_doc_count = 0;
        query_id_map = null;
    }

    public void send_feedback() {
        if (pos_max == -1) return;

        add_irrel();
        ActionMain actionMain = ActionMain.getInstance();
        actionMain.apply_feedback(query_id_map, this);
        clear();
    }

    public interface DocumentProcessor {
        ItemIDInfo get_doc_id(int pos);
    }

    public static class ItemIDInfo {
        protected int category_id;
        protected long id;
        public ItemIDInfo(int category_id, long id) {
            this.category_id = category_id;
            this.id = id;
        }
    }
}
