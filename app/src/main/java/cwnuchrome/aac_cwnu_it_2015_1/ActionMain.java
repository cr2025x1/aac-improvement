package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Random;

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

    public void update_db_collection_count(long diff) {
        db.execSQL("UPDATE " + SQL.TABLE_NAME +
                " SET " + SQL.COLUMN_NAME_COLLECTION_COUNT + "=" + SQL.COLUMN_NAME_COLLECTION_COUNT + "+(" + diff + ")");
    }

    public static void update_db_collection_count(SQLiteDatabase db, long diff) {
        db.execSQL("UPDATE " + SQL.TABLE_NAME +
                " SET " + SQL.COLUMN_NAME_COLLECTION_COUNT + "=" + SQL.COLUMN_NAME_COLLECTION_COUNT + "+(" + diff + ")");
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
        String COMMA_SEP = ",";
        String COLUMN_NAME_COLLECTION_COUNT = "collection_count";

        String TABLE_NAME = "Central";
        String QUERY_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        String QUERY_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, "
                        + COLUMN_NAME_COLLECTION_COUNT + INTEGER_TYPE +
                        ");";
        String QUERY_INIT_ENTRIES =
                "INSERT INTO "
                + TABLE_NAME
                + " ("
                + _ID + COMMA_SEP
                + COLUMN_NAME_COLLECTION_COUNT
                + ") SELECT "
                + 1 + COMMA_SEP
                + 0
                + " WHERE NOT EXISTS (SELECT "
                + _ID
                + " FROM "
                + TABLE_NAME
                + ");";
        String QUERY_RESET_ENTRIES =
                "UPDATE "
                + TABLE_NAME
                + " SET "
                + COLUMN_NAME_COLLECTION_COUNT + "=" + 0 + ";";
    }

    // 참조: http://stackoverflow.com/questions/4065518/java-how-to-get-the-caller-function-name
    public static void log(@Nullable String prefix, @NonNull String text) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3];
        String className = e.getClassName();

        if (prefix == null) System.out.println(className.substring(className.lastIndexOf('.') + 1) + "." + e.getMethodName() + ": " + text);
        else System.out.println(prefix + className.substring(className.lastIndexOf('.') + 1) + "." + e.getMethodName() + ": " + text);
    }
}
