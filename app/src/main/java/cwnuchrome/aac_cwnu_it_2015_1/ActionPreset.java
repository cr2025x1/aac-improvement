package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Chrome on 5/9/15.
 */
public class ActionPreset {
    private static ActionPreset ourInstance = new ActionPreset();

    public static ActionPreset getInstance() {
        return ourInstance;
    }

    private ActionPreset() {
    }

    private SecureRandom random = new SecureRandom();
    public String randomString() {
        return new BigInteger(130, random).toString(32);
    }
    private Random random_weak = new Random();
    public int random() { return random_weak.nextInt(100); }

    public void insertDefaultRecords(SQLiteDatabase db, Context context) {
        File dir = new File(context.getFilesDir() + "/pictures");

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }

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

        ActionMain actionMain = ActionMain.getInstance();

        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, 1);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, 2);
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "테스트");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "테스트");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.number);
        record.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        record.put(ActionGroup.SQL.COLUMN_NAME_PARENT_ID, 1); // parent is root
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, 1);
        record.put(ActionGroup.SQL.COLUMN_NAME_WORD, "테스트");
        record.put(ActionGroup.SQL.COLUMN_NAME_STEM, "테스트");
        record.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, "|:2:|");
        record.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, R.drawable.color);
        record.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, null, record);
        record.clear();

        long groupID;
        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME,  // The table to query
                new String[] {ActionWord.SQL._ID},                               // The columns to return
                ActionWord.SQL.COLUMN_NAME_WORD + " = '테스트'", // queryClause,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null // sortOrder                                 // The sort order
        );
        c.moveToFirst();
        groupID = c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL._ID));
        c.close();

        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, 2);
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "나는");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "나는");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.play);
        record.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, 3);
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "당신을");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "당신을");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.family);
        record.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, 4);
        record.put(ActionWord.SQL.COLUMN_NAME_WORD, "사랑합니다");
        record.put(ActionWord.SQL.COLUMN_NAME_STEM, "사랑합니다");
        record.put(ActionWord.SQL.COLUMN_NAME_PICTURE, R.drawable.feeling);
        record.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, null, record);
        record.clear();

        record.put(ActionMacro.SQL.COLUMN_NAME_PARENT_ID, groupID);
        record.put(ActionMacro.SQL.COLUMN_NAME_PRIORITY, random());
        record.put(ActionMacro.SQL.COLUMN_NAME_WORD, "나는 당신을 사랑합니다");
        record.put(ActionMacro.SQL.COLUMN_NAME_STEM, "나는 당신을 사랑합니다");
        record.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, "|:3::4::5:|");
        record.put(ActionMacro.SQL.COLUMN_NAME_PICTURE, R.drawable.bookmark);
        record.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        db.insert(actionMain.itemChain[ActionMain.item.ID_Macro].TABLE_NAME, null, record);
        record.clear();
    }

    public static final class SQL implements BaseColumns {
        public static final String TABLE_NAME = "LocalPreset";
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
