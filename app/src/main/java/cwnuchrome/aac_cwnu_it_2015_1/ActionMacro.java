package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by Chrome on 5/5/15.
 *
 * 매크로 클래스. 단어 클래스에 대한 의존성을 가진다.
 */
public class ActionMacro extends ActionMultiWord {

    public ActionMacro() {
        super(ActionMain.item.ID_Macro);

        TABLE_NAME = "LocalMacro";
        SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY," +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORDCHAIN + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE_IS_PRESET + SQL.INTEGER_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_ELEMENT_ID_TAG + SQL.TEXT_TYPE +
                        " )";
    }

    public int init (ContentValues values) {
        return 0;
    }
    public int execute () {
        return 0;
    }

    @Override
    // TODO: 객체 리패킹 제거
    public long raw_add(ContentValues values) {
        String word = values.getAsString(ActionWord.SQL.COLUMN_NAME_WORD);
        long result = exists(word);
        if (result != -1) return result;

        ContentValues record = new ContentValues();
        record.put(SQL.COLUMN_NAME_PARENT_ID, values.getAsString(SQL.COLUMN_NAME_PARENT_ID));
        record.put(SQL.COLUMN_NAME_PRIORITY, ActionMain.getInstance().rand.nextInt(100)); // TODO: 임시. 언젠가 지워야 할 라인.
        record.put(SQL.COLUMN_NAME_WORD, word);
        record.put(SQL.COLUMN_NAME_STEM, word);
        record.put(SQL.COLUMN_NAME_WORDCHAIN, values.getAsString(SQL.COLUMN_NAME_WORDCHAIN));
        record.put(SQL.COLUMN_NAME_PICTURE, values.getAsString(SQL.COLUMN_NAME_PICTURE));
        record.put(SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);

        record.put(SQL.COLUMN_NAME_ELEMENT_ID_TAG, values.getAsString(SQL.COLUMN_NAME_ELEMENT_ID_TAG));

        result = super.raw_add(record);
        record.clear();

        return result;
    }

    protected void addToRemovalList(Context context, AACGroupContainer.RemovalListBundle list, int id) {
        list.add(ActionMain.item.ID_Macro, id);
    }

    public static class Button extends ActionItem.Button {

        public Button(Context context, onClickClass onClickObj, AACGroupContainer container) {
            super(context, onClickObj, container);
        }

        public static class onClickClass extends ActionItem.Button.onClickClass {
            String message;
            ActionMain actionMain;

            public onClickClass(Context context) {
                super(context);
                itemCategoryID = ActionMain.item.ID_Macro;
                actionMain = ActionMain.getInstance();
            }

            public void onClick(View v) {
                if (!isOnline) return;

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                container.getTTS().speak(phonetic, TextToSpeech.QUEUE_FLUSH, null);
//                container.getTTS().speak(phonetic, TextToSpeech.QUEUE_FLUSH, null, null); // API 21 이상 필요
            }

            public void init(ContentValues values) {
                super.init(values);
                message = values.get(SQL.COLUMN_NAME_WORD) + "," + values.get(SQL.COLUMN_NAME_PRIORITY);
                phonetic = values.getAsString(SQL.COLUMN_NAME_WORD);

                // wordchain 문자열 파싱
                ((ActionMultiWord)actionMain.itemChain[itemCategoryID]).parseWordChain(
                        values.getAsString(SQL.COLUMN_NAME_WORDCHAIN),
                        new onParseCommand() {
                            @Override
                            public void onParse(int itemID) {
                                // 쿼리 옵션 설정
                                Cursor c;
                                SQLiteDatabase db = actionMain.getDB();
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

                                c.close();
                            }
                        });
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

    @Override
    protected void printRemovalList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Macros -");
        super.printRemovalList(listBundle);
    }

    @Override
    protected void printMissingDependencyList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Macros -");
        super.printMissingDependencyList(listBundle);
    }

    long add(
            long parentID,
            int priority,
            String word,
            String stem,
            long[] wordIDs,
            String picture,
            Boolean is_picture_preset
    ) {
        HashMap<Long, Long> map = create_element_id_count_map(wordIDs);

        ContentValues values = new ContentValues();
        values.put(ActionMacro.SQL.COLUMN_NAME_PARENT_ID, parentID);
        values.put(ActionMacro.SQL.COLUMN_NAME_PRIORITY, priority);
        values.put(ActionMacro.SQL.COLUMN_NAME_WORD, word);
        values.put(ActionMacro.SQL.COLUMN_NAME_STEM, stem);
        values.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, create_wordchain(wordIDs));
        values.put(ActionMacro.SQL.COLUMN_NAME_PICTURE, picture);
        values.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, is_picture_preset ? 1 : 0);

        values.put(SQL.COLUMN_NAME_ELEMENT_ID_TAG, create_element_id_count_tag(map));

//        ActionMain actionMain = ActionMain.getInstance();
//        long id = actionMain.getDB().insert(actionMain.itemChain[itemClassID].TABLE_NAME, null, values);
//        if (id != -1) actionMain.update_db_collection_count(1);
        return raw_add(values);

//        return id;
    }

    long add(
            long parentID,
            int priority,
            String word,
            String stem,
            long[] wordIDs,
            int picture,
            Boolean is_picture_preset
    ) {
        return add(parentID, priority, word, stem, wordIDs, Integer.toString(picture), is_picture_preset);
    }

}
