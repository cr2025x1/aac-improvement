package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
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
    }

    Random rand;
    ActionItem itemChain[];
    private ActionDBHelper actionDBHelper;
    private SQLiteDatabase db;
    AACGroupContainer containerRef;
    private InterActivityReferrer<AACGroupContainer> referrer;

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

        if (prefix == null) System.out.println(className.substring(className.lastIndexOf('.') + 1) + "." + e.getMethodName() + ": " + text);
        else System.out.println(prefix + className.substring(className.lastIndexOf('.') + 1) + "." + e.getMethodName() + ": " + text);
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

        @NonNull public Vector<HashMap<Long, Double>> evaluate_by_query_map(@NonNull HashMap<String, Long> queryMap) {
            Vector<HashMap<Long, Double>> eval_map_vector = alloc_eval_map_vector();
            HashMap<Long, QueryWordInfo> id_ref_map =
                    ((ActionWord)itemChain[item.ID_Word]).convert_to_id_ref_map(db, queryMap);
            Vector<HashMap<Long, Double>> rank_vector = new Vector<>();
            int i = 0;
            for (ActionItem item : itemChain) {
                rank_vector.add(item.evaluate_by_query_map(
                        db,
                        id_ref_map,
                        eval_map_vector.get(i),
                        entire_collection_count,
                        average_document_length
                ));
                i++;
            }

            return rank_vector;
        }
    }

    public static double ranking_function(
            long word_count_in_query,
            long word_count_in_document,
            long document_length,
            long average_document_length,
            long collection_count,
            long document_with_word_count_in_collection
    ) {
        return word_count_in_query * Math.log1p(Math.log1p(word_count_in_document)) / (1 - AACGroupContainerPreferences.RANKING_FUNCTION_CONSTANT_B + AACGroupContainerPreferences.RANKING_FUNCTION_CONSTANT_B * document_length / average_document_length) * Math.log(collection_count + 1 / document_with_word_count_in_collection);
    }

}
