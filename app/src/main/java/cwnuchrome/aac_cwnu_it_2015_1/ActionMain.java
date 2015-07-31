package cwnuchrome.aac_cwnu_it_2015_1;

import android.annotation.SuppressLint;
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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
        id_referrer = new InterActivityReferrer<>();

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
    private InterActivityReferrer<ArrayList<Long>> id_referrer;
    Kryo kryo;
    byte[] buffer;
    Context context;
    ReadWriteLock lock = new ReentrantReadWriteLock();
    Lock read_lock = lock.readLock();
    Lock write_lock = lock.writeLock();

    public void setContext(Context context) {
        this.context = context;
    }
    public Context getContext() {
        return context;
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

    public double get_db_avg_doc_length() {
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

        double avg_doc_length = c.getDouble(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_AVERAGE_DOCUMENT_LENGTH));
        c.close();
        return avg_doc_length;
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
    public InterActivityReferrer<ArrayList<Long>> getIDReferrer() {
        return id_referrer;
    }

    // 주어진 문장을 받아 해쉬맵으로 만들어 반환함.
    // TODO: 형태소 분석기 도입시 반드시 업데이트되어야 할 부분.
    @NonNull public static HashMap<String, Long> reduce_to_map(@NonNull String text) {
        HashMap<String, Long> map = new HashMap<>();

        String[] textTokens = tokenize(text);
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
        double denominator = lhs.length() > rhs.length() ? lhs.length() : rhs.length();

        double cs_sum = 0.0d;
        String[] lhs_split = lhs.split("(?<=.)");
        String[] rhs_split = rhs.split("(?<=.)");

        int i = 0;
        while (i < lhs_split.length && i < rhs_split.length) {
            cs_sum += cos_sim_frag(
                    Normalizer.normalize(lhs_split[i], Normalizer.Form.NFD),
                    Normalizer.normalize(rhs_split[i], Normalizer.Form.NFD)
            );
            i++;
        }

        return cs_sum / denominator;
    }

    // 주어진 두 문자(NFD 형태)의 코사인 유사도를 반환
//    public static double cos_sim(@NonNull String lhs, @NonNull String rhs) {
    public static double cos_sim_frag(@NonNull String lhs, @NonNull String rhs) {
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

    @NonNull public Evaluation allocEvaluation() { return new Evaluation(); }

    // 쿼리에 따른 아이템들의 관련성 평가를 위한 내부 클래스
    public class Evaluation {
        /*
         * 쿼리 해시맵 정보를 받아 주어진 모든 자료들에 대해 쿼리와의 관계도를 평가하는 메소드의 집합 클래스
         */

        // 각 메소드들을 위한 공통 인수들
        long entire_collection_count; // 전체 문서 집합의 문서의 총 수
        double average_document_length; // 평균 문서의 길이 (여기서의 길이는 철자의 숫자가 아니라, 각 문서를 구성하고 있는 단어 아이템의 수를 일컫는다.)

        public Evaluation() {
            entire_collection_count = get_db_collection_count();
            average_document_length = get_db_avg_doc_length();
        }

        @SuppressLint("UseSparseArrays")
        @NonNull private Vector<HashMap<Long, Double>> alloc_eval_map_vector(@Nullable String selection, @Nullable String[] selectionArgs) {
            /*
                모든 자료에 대한 평가값을 기록할 해쉬맵의 벡터를 생성한다.
                모든 자료는 카테고리 별로 분류된다.
                모든 자료는 각각 대응하는 숫자로 표현된다.
                그 자료의 평가값 수치는 0으로 초기화된다.
             */

            Vector<HashMap<Long, Double>> eval_map_vector = new Vector<>();
            for (ActionItem item : itemChain) {
                eval_map_vector.add(item.alloc_evaluation_map(db, selection, selectionArgs));
            }
            return eval_map_vector;
        }

        @SuppressLint("UseSparseArrays")
        @NonNull public Vector<HashMap<Long, Double>> evaluate_by_query_map(
                @NonNull HashMap<Long, QueryWordInfo> query_id_map,
                @Nullable String selection,
                @Nullable String[] selectionArgs
        ) {

            /*
                피드백 파트 : 주어진 쿼리의 해시맵을 피드백 정보를 더해 확장한다.
             */
            HashMap<Long, QueryWordInfo> query_id_map_feedbacked = new HashMap<>();
            for (Map.Entry<Long, QueryWordInfo> e : query_id_map.entrySet()) {
                long key = e.getKey();
                QueryWordInfo e_qwi = e.getValue();
                // 만일 엔트리 e의 ID값에 대한 정보가 이미 query_id_map_feedbacked 정보에 있다면?
                if (query_id_map_feedbacked.containsKey(key)) {
                    /*
                        실제로 원 쿼리 해시맵 query_id_map에서 가져온 정보를 적용할 때는 이 루프를 그 정보의 엔트리 e에 대해서 돌 뿐이다.
                        그런데 이미 이 루프의 결과물인 query_id_map_feedbacked에서 이 엔트리 e의 key값이 이미 존재한다는 말은,
                        이 for 루프의 현재 이터레이션의 이전 이터레이션이 이 엔트리 e가 가진 ID에 대한 피드백 정보가 더해졌음을 의미한다.
                        그런데 아래쪽에서 보면 알겠지만, 피드백 맵에서는 QueryWordInfo는 특이한 구성을 가진다.

                        "
                        QueryWordInfo 내에는 쿼리 내 해당 ID의 개수, 참조 횟수, 쿼리 내 가중치, 그리고 마지막으로 피드백 내 가중치를 가진다.
                        그런데 여기서 **쿼리 내에 원래 존재하지 않는** ID 정보를 쿼리에 추가할 때는, 두 가지 정보가 없다는 상황에 처한다.
                        쿼리 내 해당 ID의 개수, 쿼리 내의 가중치이다.
                        쿼리 내에 해당 ID가 없는데 이 값들을 구할 수 있을 리가 없다.
                        따라서 이 때에는 Ranking Function 내에서 해당 값들이 관계되는 연산의 **항등원**으로 대체 입력해서, 이 문제를 해결한다.
                        "

                        이렇게 대체입력된 값들로 매꾸어진 QueryWordInfo 객체라는 뜻이다.
                        이 객체에서 중요한 값은 피드백 가중치 뿐으로, 나머지는 모두 현재 엔트리 e에 담긴 정보를 이용하면 된다.
                        대체 입력된 값은 없는 정보를 메우기 위한 대체 값에 불과하기 때문에, 버린다.
                        참조 횟수는 어차피 두 QueryWordInfo 객체의 값이 같다.
                     */
                    QueryWordInfo old_qwi = query_id_map_feedbacked.get(key);
                    // 이미 query_id_map_feedbacked에 있는 QueryWordInfo 객체에서 해당 값만을 가져와서 이번 이터레이션의 엔트리 e의 QueryWordInfo 객체에 넣는다.
                    e_qwi.feedback_weight += old_qwi.feedback_weight;
                    query_id_map_feedbacked.put(key, e_qwi); // 그리고 그 QueryWordInfo 객체로 저장된 매핑을 대체한다.
                }
                else query_id_map_feedbacked.put(key, e_qwi); // 저장된 값이 없었다면 그냥 이번 엔트리 e의 QWI 객체를 그대로 가져와서 넣는다.

                // 해당 워드 ID의 피드백 맵을 데이터베이스에서 쿼리한다.
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
                    // 해당 워드 ID에 부속된 피드맥 맵 정보가 있다. 그렇다면 이 맵 정보를 해동해 쿼리에 적용한다.
                    HashMap<Long, Double> feedback_map = thaw_map(frozen_map);
                    for (Map.Entry<Long, Double> fb_e : feedback_map.entrySet()) {
                        long fb_key = fb_e.getKey();
                        long ref_count = 0;

                        // 먼저 해당 워드 ID에 대한 매핑이 query_id_map_feedbacked에 있는지 확인한다.
                        if (query_id_map_feedbacked.containsKey(fb_key)) {
                            // 있다. 그렇다면 피드백 정보만 그 매핑된 QWI 객체에 더해주기만 하면 된다.
                            QueryWordInfo fb_qwi = query_id_map_feedbacked.get(fb_key);
                            fb_qwi.feedback_weight += fb_e.getValue();
                        }
                        else {
                            // 해당 매핑이 없다. 그렇다면 해당 워드 ID에 대한 QWI 정보를 만들어서 넣어주어야 한다.
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

                            /*
                                QueryWordInfo 내에는 쿼리 내 해당 ID의 개수, 참조 횟수, 쿼리 내 가중치, 그리고 마지막으로 피드백 내 가중치를 가진다.
                                그런데 여기서 **쿼리 내에 원래 존재하지 않는** ID 정보를 쿼리에 추가할 때는, 두 가지 정보가 없다는 상황에 처한다.
                                쿼리 내 해당 ID의 개수, 쿼리 내의 가중치이다.
                                쿼리 내에 해당 ID가 없는데 이 값들을 구할 수 있을 리가 없다.
                                따라서 이 때에는 Ranking Function 내에서 해당 값들이 관계되는 연산의 **항등원**으로 대체 입력해서, 이 문제를 해결한다.
                             */
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

            // 모든 문서에 대한 평가값 해시맵을 생성하고, 그 해쉬맵들을 묶을 벡터를 생성한다.
            Vector<HashMap<Long, Double>> eval_map_vector = alloc_eval_map_vector(selection, selectionArgs);
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

            return rank_vector;
        }

    }

    static final int NO_FILTER = 0;
    static final int FILTER_BY_THRESHOLD = 1;

    @NonNull public Vector<ArrayList<Map.Entry<Long, Double>>> filter_rank_vector(@NonNull Vector<HashMap<Long, Double>> rank_vector, int mode) {
        Vector<ArrayList<Map.Entry<Long, Double>>> filtered_rank_vector = new Vector<>(item.ITEM_COUNT);
        for (int i = 0; i < item.ITEM_COUNT; i++) {
            filtered_rank_vector.add(new ArrayList<Map.Entry<Long, Double>>(rank_vector.get(i).size())); // 왜인지는 모르나 <> 형식을 쓰면 에러가 난다.
        }

        // 기준값 이하의 엔트리들은 모두 소거
        int i = 0;
        for (HashMap<Long, Double> map : rank_vector) {
            ArrayList<Map.Entry<Long, Double>> filtered_list = filtered_rank_vector.get(i);
            for (Map.Entry<Long, Double> e : map.entrySet()) {
                if (mode == NO_FILTER || e.getValue() > AACGroupContainerPreferences.RANKING_FUNCTION_CUTOFF_THRESHOLD) {
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

        if (mode == NO_FILTER) return filtered_rank_vector;

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
            double average_document_length,
            long collection_count,
            long document_with_word_count_in_collection
    ) {
        return (word_count_in_query + word_feedback_weight) * Math.log1p(Math.log1p(word_count_in_document)) / (1 - AACGroupContainerPreferences.RANKING_FUNCTION_CONSTANT_B + AACGroupContainerPreferences.RANKING_FUNCTION_CONSTANT_B * document_length / average_document_length) * Math.log((collection_count + 1) / document_with_word_count_in_collection);
    }

//    public void apply_feedback(HashMap<Long, QueryWordInfo> query_id_map, SearchImplicitFeedback feedback) {
    public void apply_feedback(HashMap<Long, ? extends QueryWordInfoRaw> query_id_map, SearchImplicitFeedback feedback) {
        long query_size = 0;
        for (Map.Entry<Long, ? extends QueryWordInfoRaw> qwi : query_id_map.entrySet()) {
            query_size += qwi.getValue().count;
        }

        long relevant_doc_count = feedback.rel_doc_count;
        long irrelevant_doc_count = feedback.pos_max + 1 - relevant_doc_count;

        HashMap<Long, Double> feedback_combined = new HashMap<>();
        for (int i = 0; i < item.ITEM_COUNT; i++) {
            HashMap<Long, SearchFeedbackInfo> feedback_info_map = feedback.fb_info_v.get(i);
            for (Map.Entry<Long, SearchFeedbackInfo> e : feedback_info_map.entrySet()) {
                SearchFeedbackInfo info = e.getValue();
                long id = e.getKey();
                double coefficient;
                if (info.relevance) coefficient = AACGroupContainerPreferences.FEEDBACK_ROCCHIO_COEFFICIENT_RELEVANT_DOC / relevant_doc_count;
                else coefficient = (-1) * AACGroupContainerPreferences.FEEDBACK_ROCCHIO_COEFFICIENT_IRRELEVANT_DOC / irrelevant_doc_count;

                HashMap<Long, Long> itemMap = itemChain[i].get_id_count_map(id);
                for (Map.Entry<Long, Long> item_e : itemMap.entrySet()) {
                    long key = item_e.getKey();
                    double mod = (double)item_e.getValue() * coefficient * (double)info.call_count;

                    if (feedback_combined.containsKey(key)) feedback_combined.put(key, feedback_combined.get(key) + mod);
                    else feedback_combined.put(key, mod);
                }
            }
        }

        ActionWord actionWord = (ActionWord)itemChain[item.ID_Word];
        for (Map.Entry<Long, ? extends QueryWordInfoRaw> e : query_id_map.entrySet()) {
            long query_word_key = e.getKey();
            QueryWordInfoRaw qwi = e.getValue();
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

            for (Map.Entry<Long, Double> fb_e : feedback_combined.entrySet()) {
                Long key = fb_e.getKey();
                Double mod = fb_e.getValue() * qwi.count / query_size * qwi.weight;
                if (map.containsKey(key)) map.put(key, map.get(key) + mod);
                else map.put(key, mod);
            }

            actionWord.update_feedback(query_word_key, map);
        }
    }

//    public void apply_feedback(HashMap<Long, QueryWordInfo> query_id_map, SearchImplicitFeedback.SearchFeedbackInfo[] infos) {
//        long query_size = 0;
//        for (Map.Entry<Long, QueryWordInfo> qwi : query_id_map.entrySet()) {
//            query_size += qwi.getValue().count;
//        }
//
//        long irrelevant_doc_count = 0;
//        long relevant_doc_count = 0;
//        for (SearchImplicitFeedback.SearchFeedbackInfo info : infos) {
//            if (info.relevance) relevant_doc_count++;
//            else irrelevant_doc_count++;
//        }
//
//        HashMap<Long, Double> feedback = new HashMap<>();
//        for (SearchImplicitFeedback.SearchFeedbackInfo info : infos) {
//            double coefficient;
//            if (info.relevance) coefficient = AACGroupContainerPreferences.FEEDBACK_ROCCHIO_COEFFICIENT_RELEVANT_DOC / relevant_doc_count;
//            else coefficient = (-1) * AACGroupContainerPreferences.FEEDBACK_ROCCHIO_COEFFICIENT_IRRELEVANT_DOC / irrelevant_doc_count;
//
//            HashMap<Long, Long> itemMap = itemChain[info.cat_id].get_id_count_map(info.id);
//            for (Map.Entry<Long, Long> e : itemMap.entrySet()) {
//                long key = e.getKey();
//                double mod = (double)e.getValue() * coefficient;
//
//                if (feedback.containsKey(key)) feedback.put(key, feedback.get(key) + mod);
//                else feedback.put(key, mod);
//            }
//        }
//
//        ActionWord actionWord = (ActionWord)itemChain[item.ID_Word];
//        for (Map.Entry<Long, QueryWordInfo> e : query_id_map.entrySet()) {
//            long query_word_key = e.getKey();
//            QueryWordInfo qwi = e.getValue();
//            Cursor c = db.query(
//                    actionWord.MAP_TABLE_NAME,
//                    new String[]{ActionWord.SQL.COLUMN_NAME_FEEDBACK_MAP},
//                    ActionWord.SQL.COLUMN_NAME_OWNER_ID + "=" + query_word_key,
//                    null,
//                    null,
//                    null,
//                    null
//            );
//            c.moveToFirst();
//
//            byte[] frozen_map = null;
//            HashMap<Long, Double> map;
//            if (c.getCount() > 0) {
//                frozen_map = c.getBlob(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_FEEDBACK_MAP));
//            }
//            c.close();
//            if (frozen_map == null) map = new HashMap<>();
//            else map = thaw_map(frozen_map);
//
//            for (Map.Entry<Long, Double> fb_e : feedback.entrySet()) {
//                Long key = fb_e.getKey();
//                Double mod = fb_e.getValue() * qwi.count / query_size * qwi.weight;
//                if (map.containsKey(key)) map.put(key, map.get(key) + mod);
//                else map.put(key, mod);
//            }
//
//            actionWord.update_feedback(query_word_key, map);
//
//        }
//
//
//    }

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

    // 주어진 문장을 어떻게 토큰화할 것인지 정의하는 메소드이다.
    @NonNull public static String[] tokenize(@NonNull String text) {
        String trimmed = text.trim();
        if (trimmed.length() == 0 || trimmed.equals("")) return new String[0];
        else {
            return trimmed.split("\\s");
        }
    }
}
