package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Chrome on 5/5/15.
 */
public class ActionWord extends ActionItem {

    public ActionWord () {
        TABLE_NAME = "LocalWord";
        SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY," +
//                        SQL.COLUMN_NAME_ENTRY_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE + SQL.TEXT_TYPE +
                        " )";
    }

    public int init (ContentValues values) {
        return 0;
    }
    public int execute () {
        return 0;
    }

    interface SQL extends ActionItem.SQL {
        // 필요한 고정 스트링 추가
    }

    public long add(SQLiteDatabase db, ContentValues values) {
        String word = values.getAsString(ActionWord.SQL.COLUMN_NAME_WORD);
        long result = exists(db, word);
        if (result != -1) return result;

        ContentValues record = new ContentValues();
//        record.put(SQL.COLUMN_NAME_ENTRY_ID, 999); // 임시! 아마도 삭제될 것 같음.
        record.put(SQL.COLUMN_NAME_PARENT_ID, values.getAsString(SQL.COLUMN_NAME_PARENT_ID));
        record.put(SQL.COLUMN_NAME_PRIORITY, ActionMain.getInstance().rand.nextInt(100)); // 이것도 임시
        record.put(SQL.COLUMN_NAME_WORD, word);
        record.put(SQL.COLUMN_NAME_STEM, word);
        record.put(SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        result = db.insert(TABLE_NAME, null, record);
        record.clear();

        return result;
    }

//    public boolean remove(SQLiteDatabase db, String word) {
//        if (!exists(db, word)) return false;
//
//        db.delete(
//                TABLE_NAME,
//                SQL.COLUMN_NAME_WORD + " = '" + word + "'",
//                null
//                );
//        return true;
//    }

//    protected boolean exists(SQLiteDatabase db, String word) {
//        // 워드 쿼리
//        Cursor c = db.query(
//                TABLE_NAME, // The table to query
//                new String[] {ActionWord.SQL._ID}, // The columns to return
//                ActionWord.SQL.COLUMN_NAME_WORD + " = '" + word + "'", // The columns for the WHERE clause
//                null, // The values for the WHERE clause
//                null, // don't group the rows
//                null, // don't filter by row groups
//                null // The sort order
//        );
//        c.moveToFirst();
//        long cursorCount = c.getCount();
//
//        if (cursorCount > 0) return true;
//        return false;
//    }

    /**
     * Created by Chrome on 5/8/15.
     */
    public static class Button extends ActionItem.Button {

        public Button(Context context, onClickClass onClickObj, AACGroupContainer container) {
            super(context, onClickObj, container);
        }

        public static class onClickClass extends ActionItem.Button.onClickClass {
            String message;
            String phonetic;
            public onClickClass(Context context) {super(context); }

            public void onClick(View v) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                container.getTTS().speak(phonetic, TextToSpeech.QUEUE_FLUSH, null, null);
            }
            public void init(ContentValues values) {
                message = values.get(SQL.COLUMN_NAME_WORD) + "," + values.get(SQL.COLUMN_NAME_PRIORITY);
                phonetic = values.get(SQL.COLUMN_NAME_WORD).toString();
            }
        }

        public void init(ContentValues values) {
            super.init(values);
            this.setText("워드 " + values.getAsString(SQL.COLUMN_NAME_WORD));
            this.onClickObj.init(values);
        }

    }
}
