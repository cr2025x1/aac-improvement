package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Chrome on 5/9/15.
 */
public class ActionDebug {
    private static ActionDebug ourInstance = new ActionDebug();

    public static ActionDebug getInstance() {
        return ourInstance;
    }

    private ActionDebug() {
    }

    private SecureRandom random = new SecureRandom();
    public String randomString() {
        return new BigInteger(130, random).toString(32);
    }
    private Random random_weak = new Random();
    public int random() { return random_weak.nextInt(100); }

    public void insertTestRecords(SQLiteDatabase db) {
        Cursor c;
        ContentValues record;

        // 디버그 자료형식 삽입 여부 확인 - 존재시 생성 생략, 없다면 생성 후 마킹
        db.execSQL(SQL.SQL_CREATE_ENTRIES);
        c = db.query(
                SQL.TABLE_NAME,  // The table to query
                new String[] {SQL._ID},                               // The columns to return
                SQL._ID + " = 1", // queryClause,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null // sortOrder                                 // The sort order
        );
        c.moveToFirst();
        if (c.getCount() > 0) return;
        record = new ContentValues();
        record.put(SQL._ID, 1);
        db.insert(SQL.TABLE_NAME, null, record);
        c.close();
        record.clear();

        // 테이블 청소

        ActionMain actionMain = ActionMain.getInstance();

        // 테이블 생성
//        for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) {
//            actionMain.itemChain[i].createTable(db);
//        }

        // root 그룹에 A 그룹 삽입
//        record.put(ActionGroup.SQL.COLUMN_NAME_ENTRY_ID, 2);
        record.put(ActionGroup.SQL.COLUMN_NAME_PARENT_ID, 1); // parent is root
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, 1);
        record.put(ActionGroup.SQL.COLUMN_NAME_WORD, "A");
        record.put(ActionGroup.SQL.COLUMN_NAME_STEM, "A");
        record.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, null, record);
        record.clear();

        // root 그룹에 B 그룹 삽입
//        record.put(ActionGroup.SQL.COLUMN_NAME_ENTRY_ID, 3);
        record.put(ActionGroup.SQL.COLUMN_NAME_PARENT_ID, 1); // parent is root
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionGroup.SQL.COLUMN_NAME_WORD, "B");
        record.put(ActionGroup.SQL.COLUMN_NAME_STEM, "B");
        record.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, null, record);
        record.clear();

        // root 그룹에 a 워드 삽입
//        record.put(ActionWord.SQL.COLUMN_NAME_ENTRY_ID, 1);
        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, 1); // parent is root
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "a");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "a");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        // root 그룹에 b 워드 삽입
//        record.put(ActionWord.SQL.COLUMN_NAME_ENTRY_ID, 2);
        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, 1); // parent is root
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "b");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "b");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        // A 그룹의 _ID 가져오기
        long groupID;
        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME,  // The table to query
                new String[] {ActionWord.SQL._ID},                               // The columns to return
                ActionWord.SQL.COLUMN_NAME_WORD + " = 'A'", // queryClause,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null // sortOrder                                 // The sort order
        );
        c.moveToFirst();
        groupID = c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL._ID));
        c.close();

        // A 그룹에 Aa 워드 삽입
//        record.put(ActionWord.SQL.COLUMN_NAME_ENTRY_ID, 3);
        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "Aa");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "Aa");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        // A 그룹에 Ab 워드 삽입
//        record.put(ActionWord.SQL.COLUMN_NAME_ENTRY_ID, 4);
        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "Ab");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "Ab");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        // B 그룹의 _ID 가져오기
        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME,  // The table to query
                new String[] {ActionWord.SQL._ID},                               // The columns to return
                ActionWord.SQL.COLUMN_NAME_WORD + " = 'B'", // queryClause,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null // sortOrder                                 // The sort order
        );
        c.moveToFirst();
        groupID = c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL._ID));
        c.close();
        long groupID_B = groupID;

        // B 그룹에 Ba 워드 삽입
//        record.put(ActionWord.SQL.COLUMN_NAME_ENTRY_ID, 5);
        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "Ba");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "Ba");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        // B 그룹에 Bb 워드 삽입
//        record.put(ActionWord.SQL.COLUMN_NAME_ENTRY_ID, 6);
        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "Bb");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "Bb");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        // B 그룹에 B_A 그룹 삽입
//        record.put(ActionGroup.SQL.COLUMN_NAME_ENTRY_ID, 4);
        record.put(ActionGroup.SQL.COLUMN_NAME_PARENT_ID, groupID); // parent is root
        record.put(ActionGroup.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionGroup.SQL.COLUMN_NAME_WORD, "B_A");
        record.put(ActionGroup.SQL.COLUMN_NAME_STEM, "B_A");
        record.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        groupID = db.insert(actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, null, record); // groupID에는 B_A 그룹의 PRIMARY KEY값이 반환
        record.clear();

        // B_A 그룹에 BAa 워드 삽입
//        record.put(ActionWord.SQL.COLUMN_NAME_ENTRY_ID, 7);
        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "BAa");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "BAa");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        // B_A 그룹에 BAb 워드 삽입
//        record.put(ActionWord.SQL.COLUMN_NAME_ENTRY_ID, 8);
        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "BAb");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "BAb");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        // B 그룹에 Ba + a + BAa
//        record.put(ActionMacro.SQL.COLUMN_NAME_ENTRY_ID, 1);
        record.put(ActionMacro.SQL.COLUMN_NAME_PARENT_ID, groupID_B);
        record.put(ActionMacro.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionMacro.SQL.COLUMN_NAME_WORD, "Ba a BAa");
        record.put(ActionMacro.SQL.COLUMN_NAME_STEM, "Ba a BAa");
        record.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, "|:5::1::7:|");
        record.put(ActionMacro.SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Macro].TABLE_NAME, null, record);
        record.clear();
    }

    public static final class SQL implements BaseColumns {
        public static final String TABLE_NAME = "LocalDebug";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" +
                        " )";
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    void deleteFlag (SQLiteDatabase db) {
        db.execSQL(SQL.SQL_DELETE_ENTRIES);
    }

}
