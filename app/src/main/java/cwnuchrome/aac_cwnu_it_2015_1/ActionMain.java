package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Chrome on 5/5/15.
 *
 * 각 아이템에 대한 핵심 정보가 담긴 싱글턴 객체.
 * 많은 클래스가 이 클래스에 대한 의존성을 가지므로 조심히 다룰 것.
 *
 */
public final class ActionMain {
    private static ActionMain ourInstance = new ActionMain();
    public static ActionMain getInstance() {
        return ourInstance;
    }
    private ActionMain() {
        rand = new Random();
        referrer = new InterActivityReferrer<>();

        itemChain = new ActionItem[item.ITEM_COUNT];
        itemChain[item.ID_Group] = new ActionGroup();
        itemChain[item.ID_Word] = new ActionWord();
        itemChain[item.ID_Macro] = new ActionMacro();

        kryo = new Kryo();
        buffer = new byte[8192];
        kryo.register(HashMap.class);
    }

    Random rand;
    ActionItem itemChain[];
    private ActionDBHelper actionDBHelper;
    private SQLiteDatabase db;
    AACGroupContainer containerRef;
    private InterActivityReferrer<AACGroupContainer> referrer;
    Kryo kryo;
    byte[] buffer;
    Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public interface item {
        int ITEM_COUNT = 3;

        int ID_Group = 0;
        int ID_Word = 1;
        int ID_Macro = 2;
    }

    public void initDBHelper (Context context) {
        actionDBHelper = new ActionDBHelper(context);
        db = actionDBHelper.getWritableDatabase(); // TODO: 언젠가는 멀티스레딩 형식으로 바꾸기.
    }

    public void initTables() {
        actionDBHelper.onCreate(db);
        actionDBHelper.initTable(db);
    }

    public void resetTables() {
        actionDBHelper.deleteTable(db);
        initTables();
    }

    public SQLiteDatabase getDB() { return db; }

    public void update_db_collection_count(long diff, long doc_length) {
        log("***", "starts, diff=" + diff + ", doc_length=" + doc_length + " ***");
        long collection_count = get_db_collection_count();
        long updated_collection_count = collection_count + diff;
        db.execSQL("UPDATE " + SQL.TABLE_NAME +
                " SET " + SQL.COLUMN_NAME_COLLECTION_COUNT + "=" + updated_collection_count);
        db.execSQL("UPDATE " + SQL.TABLE_NAME +
                        " SET " + SQL.COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH +
                        "=(" + SQL.COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH + "*" + collection_count + "+" + doc_length + ")" +
                        "/" + updated_collection_count
        );
        log("***", "ends ***");
    }

    public static void update_db_collection_count(@NonNull SQLiteDatabase db, long diff, long doc_length) {
        log("***", "starts, diff=" + diff + ", doc_length=" + doc_length + " (static) ***");
        long collection_count = get_db_collection_count(db);
        long updated_collection_count = collection_count + diff;
        db.execSQL("UPDATE " + SQL.TABLE_NAME +
                " SET " + SQL.COLUMN_NAME_COLLECTION_COUNT + "=" + updated_collection_count);
        db.execSQL("UPDATE " + SQL.TABLE_NAME +
                        " SET " + SQL.COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH +
                        "=(" + SQL.COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH + "*" + collection_count + "+" + doc_length + ")" +
                        "/" + updated_collection_count
        );
        log("***", "ends ***");
    }

    public long get_db_collection_count() {
        Cursor c = db.query(
                SQL.TABLE_NAME,
                new String[]{SQL.COLUMN_NAME_COLLECTION_COUNT},
                null,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();

        Long count = c.getLong(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_COLLECTION_COUNT));
        c.close();
        return count;
    }

    public static long get_db_collection_count(@NonNull SQLiteDatabase db) {
        Cursor c = db.query(
                SQL.TABLE_NAME,
                new String[] {SQL.COLUMN_NAME_COLLECTION_COUNT},
                null,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();

        Long count = c.getLong(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_COLLECTION_COUNT));
        c.close();
        return count;
    }

    public long get_db_avg_doc_length() {
        Cursor c = db.query(
                SQL.TABLE_NAME,
                new String[]{SQL.COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH},
                null,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();

        Long avg_doc_length = c.getLong(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH));
        c.close();
        return avg_doc_length;
    }

    public static class SearchFeedbackInfo {
        final public long id;
        final public int cat_id;
        final boolean relevance;

        public SearchFeedbackInfo(int cat_id, long id, boolean relevance) {
            this.id = id;
            this.cat_id = cat_id;
            this.relevance = relevance;
        }
    }

    private class ActionDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Action.db";

        private ActionDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            create_central_table(db);
            init_central_table(db);
            createTable(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and fetchSuggestion over
            // TODO: 수정 필요
            delete_central_table(db);
            for (ActionItem i : itemChain) i.clearTable(db);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public void createTable(SQLiteDatabase db) {
            for (ActionItem i : itemChain) i.createTable(db);
        }

        public void initTable(SQLiteDatabase db) {
            for (ActionItem i : itemChain) i.initTable(db);
        }

        public void deleteTable(SQLiteDatabase db) {
            reset_central_table(db);
            for (ActionItem i : itemChain) i.deleteTable(db);
        }

        private void create_central_table(SQLiteDatabase db) {
            db.execSQL(SQL.QUERY_CREATE_ENTRIES);
        }

        private void init_central_table(SQLiteDatabase db) {
            db.execSQL(SQL.QUERY_INIT_ENTRIES);
        }

        private void reset_central_table(SQLiteDatabase db) {
            db.execSQL(SQL.QUERY_RESET_ENTRIES);
        }

        private void delete_central_table(SQLiteDatabase db) {
            db.execSQL(SQL.QUERY_DELETE_ENTRIES);
        }
    }

    private interface SQL extends BaseColumns {
        String INTEGER_TYPE = " INTEGER";
        String REAL_TYPE = " REAL";
        String COMMA_SEP = ",";
        String COLUMN_NAME_COLLECTION_COUNT = "collection_count";
        String COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH = "avg_doc_length";

        String TABLE_NAME = "Central";
        String QUERY_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        String QUERY_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NAME_COLLECTION_COUNT + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH + REAL_TYPE +
                        ");";
        String QUERY_INIT_ENTRIES =
                "INSERT INTO " +
                        TABLE_NAME +
                        " (" +
                        _ID + COMMA_SEP +
                        COLUMN_NAME_COLLECTION_COUNT + COMMA_SEP +
                        COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH +
                        ") SELECT " +
                        1 + COMMA_SEP +
                        0 + COMMA_SEP +
                        0 +
                        " WHERE NOT EXISTS (SELECT " +
                        _ID + " FROM " +
                        TABLE_NAME +
                        ");";
        String QUERY_RESET_ENTRIES =
                "UPDATE " +
                        TABLE_NAME +
                        " SET " +
                        COLUMN_NAME_COLLECTION_COUNT + "=" + 0 + COMMA_SEP +
                        COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH + "=" + 0 +
                        ";";
    }

    // 참조: http://stackoverflow.com/questions/4065518/java-how-to-get-the-caller-function-name
    public static void log(@Nullable String prefix, @NonNull String text) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3];
        String className = e.getClassName();

        StringBuilder sb = new StringBuilder();
        if (prefix != null) sb.append(prefix);
        sb.append(className.substring(className.lastIndexOf('.') + 1));
        sb.append('.');
        sb.append(e.getMethodName());
        sb.append(": ");
        sb.append(text);

        System.out.println(sb.toString());
    }

    public InterActivityReferrer<AACGroupContainer> getReferrer() {
        return referrer;
    }

    // 주어진 문장을 받아 해쉬맵으로 만들어 반환함.
    // TODO: 형태소 분석기 도입시 반드시 업데이트되어야 할 부분.
    @NonNull public static HashMap<String, Long> reduce_to_map(@NonNull String text) {
        HashMap<String, Long> map = new HashMap<>();

        String wordSequence = text.trim();
        if (wordSequence.length() == 0) return map;
        String[] textTokens = wordSequence.trim().split("\\s");
        if (textTokens.length == 0) return map;

        for (String s : textTokens) {
            if (map.containsKey(s)) map.put(s, map.get(s) + 1l);
            else map.put(s, 1l);
        }

        return map;
    }

    // 코사인 유사도를 측정하기 위해 한 문자별로 조각내어 벡터 공간의 맵으로 만드는 메소드
    @NonNull public static HashMap<String, Long> reduce_to_map_for_cos_sim(@NonNull String text) {
        HashMap<String, Long> map = new HashMap<>();

        String wordSequence = text.trim();
        if (wordSequence.length() == 0) return map;
        // 참고 출처: http://stackoverflow.com/questions/13453075/splitting-a-string-with-no-delimiter (Bohemian의 답변)
        String[] keys = text.split("(?<=.)");

        for (String key : keys) {
            if (map.containsKey(key)) map.put(key, map.get(key) + 1l);
            else map.put(key, 1l);
        }

        return map;
    }

    // 주어진 두 문자열의 코사인 유사도를 반환
    public static double cos_sim(@NonNull String lhs, @NonNull String rhs) {
        HashMap<String, Long> lhs_map = reduce_to_map_for_cos_sim(lhs);
        HashMap<String, Long> rhs_map = reduce_to_map_for_cos_sim(rhs);

        double scalar_product = 0;
        double lhs_vector_size = 0;
        double rhs_vector_size = 0;
        for (Map.Entry<String, Long> e : lhs_map.entrySet()) {
            String key = e.getKey();
            Long value = e.getValue();
            if (rhs_map.containsKey(key)) {
                scalar_product += value * rhs_map.get(key);
            }
            lhs_vector_size += value * value;
        }
        lhs_vector_size = Math.sqrt(lhs_vector_size);

        for (Map.Entry<String, Long> e : rhs_map.entrySet()) {
            Long value = e.getValue();
            rhs_vector_size += value * value;
        }
        rhs_vector_size = Math.sqrt(rhs_vector_size);

        return scalar_product / lhs_vector_size / rhs_vector_size;
    }

    // 쿼리에 따른 아이템들의 관련성 평가를 위한 내부 클래스
    @NonNull public Evaluation allocEvaluation() { return new Evaluation(); }
    class Evaluation {
        long entire_collection_count;
        long average_document_length;

        public Evaluation() {
            entire_collection_count = get_db_collection_count();
            average_document_length = get_db_avg_doc_length();
        }

        @NonNull private Vector<HashMap<Long, Double>> alloc_eval_map_vector() {
            Vector<HashMap<Long, Double>> eval_map_vector = new Vector<>();
            for (ActionItem item : itemChain) eval_map_vector.add(item.alloc_evaluation_map(db));
            return eval_map_vector;
        }

        @NonNull public Vector<ArrayList<Map.Entry<Long, Double>>> evaluate_by_query_map(@NonNull HashMap<Long, QueryWordInfo> query_id_map) {
            // 피드백 파트
            HashMap<Long, QueryWordInfo> query_id_map_feedbacked = new HashMap<>();
            for (Map.Entry<Long, QueryWordInfo> e : query_id_map.entrySet()) {
                long key = e.getKey();
                QueryWordInfo e_qwi = e.getValue();
                if (query_id_map_feedbacked.containsKey(key)) {
                    QueryWordInfo old_qwi = query_id_map_feedbacked.get(key);
                    e_qwi.feedback_weight += old_qwi.feedback_weight;
                    query_id_map_feedbacked.put(key, e_qwi);
                }
                else query_id_map_feedbacked.put(key, e_qwi);

                ActionWord actionWord = (ActionWord)itemChain[item.ID_Word];
                Cursor c = db.query(
                        actionWord.MAP_TABLE_NAME,
                        new String[] {ActionWord.SQL.COLUMN_NAME_FEEDBACK_MAP},
                        ActionWord.SQL.COLUMN_NAME_OWNER_ID + "=" + key,
                        null,
                        null,
                        null,
                        null
                );
                c.moveToFirst();
                byte[] frozen_map = c.getBlob(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_FEEDBACK_MAP));
                if (frozen_map != null) {
                    HashMap<Long, Double> feedback_map = thaw_map(frozen_map);
                    for (Map.Entry<Long, Double> fb_e : feedback_map.entrySet()) {
                        long fb_key = fb_e.getKey();
                        long ref_count = 0;

                        if (query_id_map_feedbacked.containsKey(fb_key)) {
                            QueryWordInfo fb_qwi = query_id_map_feedbacked.get(fb_key);
                            fb_qwi.feedback_weight += fb_e.getValue();
                            query_id_map_feedbacked.put(fb_key, fb_qwi);
                        }
                        else {
                            if (!query_id_map.containsKey(fb_key)) {
                                Cursor key_c = db.query(
                                        actionWord.TABLE_NAME,
                                        new String[] {ActionWord.SQL.COLUMN_NAME_REFERENCE_COUNT},
                                        ActionWord.SQL._ID + "=" + fb_key,
                                        null,
                                        null,
                                        null,
                                        null
                                );
                                key_c.moveToFirst();
                                ref_count = key_c.getLong(key_c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_REFERENCE_COUNT));
                                key_c.close();
                            }

                            QueryWordInfo fb_qwi = new QueryWordInfo(
                                    1l,
                                    ref_count,
                                    1.0d,
                                    fb_e.getValue()
                            );
                            query_id_map_feedbacked.put(fb_key, fb_qwi);
                        }
                    }
                }

                c.close();
            }

            Vector<HashMap<Long, Double>> eval_map_vector = alloc_eval_map_vector();
            Vector<HashMap<Long, Double>> rank_vector = new Vector<>();
            int i = 0;
            for (ActionItem item : itemChain) {
                rank_vector.add(item.evaluate_by_query_map(
                        db,
                        query_id_map_feedbacked,
                        eval_map_vector.get(i),
                        entire_collection_count,
                        average_document_length
                ));
                i++;
            }

            return filter_rank_vector(rank_vector);
        }

    }

    @NonNull public Vector<ArrayList<Map.Entry<Long, Double>>> filter_rank_vector(@NonNull Vector<HashMap<Long, Double>> rank_vector) {
        // 기준값 이하의 엔트리들은 모두 소거
        Vector<ArrayList<Map.Entry<Long, Double>>> filtered_rank_vector = new Vector<>(item.ITEM_COUNT);
        for (int i = 0; i < item.ITEM_COUNT; i++) filtered_rank_vector.add(new ArrayList<Map.Entry<Long, Double>>(rank_vector.get(i).size())); // 왜인지는 모르나 <> 형식을 쓰면 에러가 난다.
        int i = 0;
        for (HashMap<Long, Double> map : rank_vector) {
            ArrayList<Map.Entry<Long, Double>> filtered_list = filtered_rank_vector.get(i);
            for (Map.Entry<Long, Double> e : map.entrySet()) {
                if (e.getValue() > AACGroupContainerPreferences.RANKING_FUNCTION_CUTOFF_THRESHOLD) {
                    filtered_list.add(e);
                }
            }
            i++;
        }

        // 정렬 후 최상위 n개만 남기고 소거
        Vector<ListIterator<Map.Entry<Long, Double>>> iterator_vector = new Vector<>(item.ITEM_COUNT);
        for (ArrayList<Map.Entry<Long, Double>> list : filtered_rank_vector) {
            Collections.sort(list, new Comparator<Map.Entry<Long, Double>>() {
                @Override
                public int compare(Map.Entry<Long, Double> lhs, Map.Entry<Long, Double> rhs) {
                    return lhs.getValue() > rhs.getValue() ? -1 : 1;
                }
            });

            iterator_vector.add(list.listIterator());
        }

        int count = 0;
        int iter_end_count = 0;
        boolean count_full = false;
        while (iter_end_count < item.ITEM_COUNT - 1) {
            double max_value = 0.0d;

            iter_end_count = 0;
            for (ListIterator<Map.Entry<Long, Double>> iter : iterator_vector) {
                if (iter.hasNext()) {
                    Map.Entry<Long, Double> e = iter.next();
                    double e_value = e.getValue();
                    if (e_value > max_value) {
                        max_value = e_value;
                    }
                    iter.previous();
                }
                else iter_end_count++;
            }

            for (ListIterator<Map.Entry<Long, Double>> iter : iterator_vector) {
                if (iter.hasNext()) {
                    Map.Entry<Long, Double> e = iter.next();
                    double e_value = e.getValue();
                    if (e_value < max_value) iter.previous();
                    else count++;
                }
            }

            if (count >= AACGroupContainerPreferences.RANKING_FUNCTION_BEST_MATCH_N) {
                count_full = true;
                break;
            }
        }

        if (!count_full) for (ListIterator<Map.Entry<Long, Double>> iter : iterator_vector) {
            Map.Entry<Long, Double> e;
            while (iter.hasNext()) {
                e = iter.next();
                while (iter.hasNext()) {
                    Map.Entry<Long, Double> ei = iter.next();
                    count++;
                    if (ei.getValue() < e.getValue()) break;
                }

                if (count >= AACGroupContainerPreferences.RANKING_FUNCTION_BEST_MATCH_N) {
                    break;
                }
            }
        }

        for (ListIterator<Map.Entry<Long, Double>> iter : iterator_vector) {
            if (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
        }

        return filtered_rank_vector;
    }

    public static double ranking_function(
            long word_count_in_query,
            double word_feedback_weight,
            long word_count_in_document,
            long document_length,
            long average_document_length,
            long collection_count,
            long document_with_word_count_in_collection
    ) {
        return (word_count_in_query + word_feedback_weight) * Math.log1p(Math.log1p(word_count_in_document)) / (1 - AACGroupContainerPreferences.RANKING_FUNCTION_CONSTANT_B + AACGroupContainerPreferences.RANKING_FUNCTION_CONSTANT_B * document_length / average_document_length) * Math.log(collection_count + 1 / document_with_word_count_in_collection);
    }

    public void applyFeedback(HashMap<Long, QueryWordInfo> query_id_map, SearchFeedbackInfo[] infos) {
        long query_size = 0;
        for (Map.Entry<Long, QueryWordInfo> qwi : query_id_map.entrySet()) {
            query_size += qwi.getValue().count;
        }

        long irrelevant_doc_count = 0;
        long relevant_doc_count = 0;
        for (SearchFeedbackInfo info : infos) {
            if (info.relevance) relevant_doc_count++;
            else irrelevant_doc_count++;
        }

        HashMap<Long, Double> feedback = new HashMap<>();
        for (SearchFeedbackInfo info : infos) {
            double coefficient;
            if (info.relevance) coefficient = AACGroupContainerPreferences.FEEDBACK_ROCCHIO_COEFFICIENT_RELEVANT_DOC / relevant_doc_count;
            else coefficient = (-1) * AACGroupContainerPreferences.FEEDBACK_ROCCHIO_COEFFICIENT_IRRELEVANT_DOC / irrelevant_doc_count;

            HashMap<Long, Long> itemMap = itemChain[info.cat_id].get_id_count_map(info.id);
            for (Map.Entry<Long, Long> e : itemMap.entrySet()) {
                long key = e.getKey();
                double mod = (double)e.getValue() * coefficient;

                if (feedback.containsKey(key)) feedback.put(key, feedback.get(key) + mod);
                else feedback.put(key, mod);
            }
        }

        ActionWord actionWord = (ActionWord)itemChain[item.ID_Word];
        for (Map.Entry<Long, QueryWordInfo> e : query_id_map.entrySet()) {
            long query_word_key = e.getKey();
            QueryWordInfo qwi = e.getValue();
            Cursor c = db.query(
                    actionWord.MAP_TABLE_NAME,
                    new String[]{ActionWord.SQL.COLUMN_NAME_FEEDBACK_MAP},
                    ActionWord.SQL.COLUMN_NAME_OWNER_ID + "=" + query_word_key,
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();

            byte[] frozen_map = null;
            HashMap<Long, Double> map;
            if (c.getCount() > 0) {
                frozen_map = c.getBlob(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_FEEDBACK_MAP));
            }
            c.close();
            if (frozen_map == null) map = new HashMap<>();
            else map = thaw_map(frozen_map);

            for (Map.Entry<Long, Double> fb_e : feedback.entrySet()) {
                Long key = fb_e.getKey();
                Double mod = fb_e.getValue() * qwi.count / query_size * qwi.weight;
                if (map.containsKey(key)) map.put(key, map.get(key) + mod);
                else map.put(key, mod);
            }

            actionWord.update_feedback(query_word_key, map);

        }


    }

    // Kryo 라이브러리를 이용, 피드백 정보가 담긴 해쉬맵을 직렬화한다. *냉동 보관을 위해 꽁꽁 얼린다*
    @NonNull public byte[] freeze_map(@NonNull HashMap<Long, Double> feedbackMap) {
        Output output = new Output(buffer);
        kryo.writeObject(output, feedbackMap);
        output.close();

        return output.toBytes();
    }

    // Kryo 라이브러리를 이용, 피드백 정보가 담긴 해쉬맵을 바이트 배열 상태에서 살아있는 객체로 병렬화한다. *얼렸던 것을 꺼내 쓰기 위해 해동한다*
    @SuppressWarnings("unchecked")
    @NonNull public HashMap<Long, Double> thaw_map(@NonNull byte[] frozen_map) {
        Input input = new Input(frozen_map);

        HashMap<Long, Double> thawed_map = kryo.readObject(input, HashMap.class);
        input.close();
        return thawed_map;
    }

    @NonNull public ContentValues process_external_images(@NonNull ContentValues values) {
        if (values.getAsInteger(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET) == 0) {
            String filepath = values.getAsString(ActionItem.SQL.COLUMN_NAME_PICTURE);
            values.remove(ActionItem.SQL.COLUMN_NAME_PICTURE);
            values.put(ActionItem.SQL.COLUMN_NAME_PICTURE, ExternalImageProcessor.copyAfterHashing(context, filepath));
        }

        return values;
    }
}
