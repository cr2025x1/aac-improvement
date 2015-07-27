package cwnuchrome.aac_cwnu_it_2015_1;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Chrome on 7/27/15.
 *
 * 검색 엔진의 피드백 Helper 클래스
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
            sfi = new SearchFeedbackInfo(category_id, id, true);
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
                        new SearchFeedbackInfo(category_id, id, false)
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
