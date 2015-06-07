package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Chrome on 5/5/15.
 */
public class ActionMacro extends ActionItem {

    public ActionMacro() {
        TABLE_NAME = "LocalMacro";
        SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY," +
//                        SQL.COLUMN_NAME_ENTRY_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORDCHAIN + SQL.TEXT_TYPE +
                        " )";
    }

    public int init (ContentValues values) {
        return 0;
    }
    public int execute () {
        return 0;
    }

    interface SQL extends ActionItem.SQL {
        String COLUMN_NAME_WORDCHAIN = "wordchain";
    }

//    public void createTable(SQLiteDatabase db) {
//        db.execSQL(SQL_CREATE_ENTRIES);
//    }
//    public void clearTable(SQLiteDatabase db) {
//        db.execSQL(SQL_DELETE_ENTRIES);
//    }
//    public void initTable(SQLiteDatabase db) {
//
//    }
//    public void deleteTable(SQLiteDatabase db) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
//    }

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
        record.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, values.getAsString(SQL.COLUMN_NAME_WORDCHAIN));
        result = db.insert(TABLE_NAME, null, record);
        record.clear();

        return result;
    }

    /**
     * Created by Chrome on 5/8/15.
     */
    public static class Button extends ActionItem.Button {

        public Button(Context context, onClickClass onClickObj, AACGroupContainer container) {
            super(context, onClickObj, container);
        }

        public static class onClickClass extends ActionItem.Button.onClickClass {
            String message;
            // ArrayList<ActionWord.Button.onClickClass> wordChain;
            ArrayList<String> wordMsgChain;
            ActionMain actionMain;

            public onClickClass(Context context) {
                super(context);
                // wordChain = new ArrayList<ActionWord.Button.onClickClass>();
                wordMsgChain = new ArrayList<String>();
                actionMain = ActionMain.getInstance();
            }

            public void onClick(View v) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                // for (ActionWord.Button.onClickClass wordOCC : wordChain) wordOCC.onClick(v);
                container.getTTS().speak(phonetic, TextToSpeech.QUEUE_FLUSH, null, null);
            }

            public void init(ContentValues values) {
                message = values.get(SQL.COLUMN_NAME_WORD) + "," + values.get(SQL.COLUMN_NAME_PRIORITY);
                phonetic = values.getAsString(SQL.COLUMN_NAME_WORD);

                // wordchain 문자열 파싱
                String itemChain = values.getAsString(SQL.COLUMN_NAME_WORDCHAIN);
                StringBuilder buffer = new StringBuilder();
                boolean inChain = false;
                boolean inWord = false;
                int pos = 0;
                if (itemChain.charAt(pos++) == '|') inChain = true;
                for (;inChain;pos++) {
                    if (itemChain.charAt(pos) == '|') {inChain = false; continue;}
                    if (itemChain.charAt(pos) == ':') {
                        inWord = !inWord;
                        if (!inWord) {
                            long itemID = Long.parseLong(buffer.toString(), 10);

                            // 쿼리 옵션 설정
                            Cursor c;
                            SQLiteDatabase db = new ActionDBHelper(context).getWritableDatabase();
                            String[] projection = {
                                    ActionWord.SQL._ID,
                                    ActionWord.SQL.COLUMN_NAME_WORD,
                                    ActionWord.SQL.COLUMN_NAME_PRIORITY
                            };
                            String sortOrder =
                                    ActionWord.SQL.COLUMN_NAME_PRIORITY + " DESC";
                            String queryClause = ActionWord.SQL._ID  + " = " + itemID; // 검색 조건

                            // 워드 쿼리
                            c = db.query(
                                    ActionMain.getInstance().itemChain[ActionMain.item.ID_Word].TABLE_NAME, // The table to query
                                    projection, // The columns to return
                                    queryClause, // The columns for the WHERE clause
                                    null, // The values for the WHERE clause
                                    null, // don't group the rows
                                    null, // don't filter by row groups
                                    sortOrder // The sort order
                            );
                            c.moveToFirst();

                            /*
                            ActionWord.Button.onClickClass wordOCC = new ActionWord.Button.onClickClass(context);
                            values.put(ActionWord.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_WORD)));
                            long priority = c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PRIORITY));
                            values.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, priority);
                            wordOCC.init(values);
                            wordOCC.setContainer(actionMain.containerRef);

                            values.clear();
                            wordChain.add(wordOCC);
                            */
                            c.close();
                            buffer.setLength(0);
                        }
                        continue;
                    }
                    buffer.append(itemChain.charAt(pos));
                }
            }
        }

        public void init(ContentValues values) {
            super.init(values);

            this.setText("매크로 " + values.getAsString(SQL.COLUMN_NAME_WORD));
            this.onClickObj.setContainer(container);
            this.onClickObj.setButton(this);
            this.onClickObj.init(values);
        }

    }
}
