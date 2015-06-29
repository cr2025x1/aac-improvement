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
 *
 * 단어 클래스. ActionItem에 대해 의존성을 가짐.
 */
public class ActionWord extends ActionItem {

    public ActionWord () {
        TABLE_NAME = "LocalWord";
        SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY," +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE_IS_PRESET + SQL.INTEGER_TYPE +
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
        record.put(SQL.COLUMN_NAME_PARENT_ID, values.getAsString(SQL.COLUMN_NAME_PARENT_ID));
        record.put(SQL.COLUMN_NAME_PRIORITY, ActionMain.getInstance().rand.nextInt(100)); // 이것도 임시
        record.put(SQL.COLUMN_NAME_WORD, word);
        record.put(SQL.COLUMN_NAME_STEM, word);
        record.put(SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        record.put(SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        result = db.insert(TABLE_NAME, null, record);
        record.clear();

        return result;
    }

    protected void addToRemovalList(Context context, AACGroupContainer.RemovalListBundle list, int id) {
        list.add(ActionMain.item.ID_Word, id);
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
            public onClickClass(Context context) {
                super(context);
                itemCategoryID = ActionMain.item.ID_Word;
            }

            public void onClick(View v) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                container.getTTS().speak(phonetic, TextToSpeech.QUEUE_FLUSH, null, null);
            }
            public void init(ContentValues values) {
                super.init(values);
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

    protected boolean checkDependencyRemoval(Context context, AACGroupContainer.RemovalListBundle list) { return true; }

    protected void printRemovalList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Words -");
        for (int i : listBundle.itemVector.get(ActionMain.item.ID_Word)) System.out.println(i);
    }

    protected void printMissingDependencyList(AACGroupContainer.RemovalListBundle listBundle) {}
}
