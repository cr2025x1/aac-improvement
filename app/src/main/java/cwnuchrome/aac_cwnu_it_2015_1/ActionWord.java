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
        super(ActionMain.item.ID_Word);

        reservedID = new int[] {1};
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

    @Override
    public void initTable(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " +
                        TABLE_NAME + " (" +
                        SQL._ID + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE_IS_PRESET +
                        ") " +
                        "SELECT " +
                        "1" + SQL.COMMA_SEP +
                        "0" + SQL.COMMA_SEP +
                        "0" + SQL.COMMA_SEP +
                        "'" + AACGroupContainerPreferences.ROOT_GROUP_NAME + "'" + SQL.COMMA_SEP +
                        "'" + AACGroupContainerPreferences.ROOT_GROUP_NAME + "'" + SQL.COMMA_SEP +
                        R.drawable.btn_default + SQL.COMMA_SEP +
                        "1" +
                        " WHERE NOT EXISTS (SELECT 1 FROM " +
                        TABLE_NAME + " WHERE " +
                        SQL._ID + " = 1" +
                        ");"
        );
    }

    // 주어진 단어가 이미 DB 상에 존재하면 그 단어의 ID를 반환, 없으면 추가 후 추가된 단어의 ID를 반환.
    public long raw_add(ContentValues values) {
        String word = values.getAsString(ActionWord.SQL.COLUMN_NAME_WORD);
        long result = exists(word);
        if (result != -1) return result;

        ContentValues record = new ContentValues();
        record.put(SQL.COLUMN_NAME_PARENT_ID, values.getAsString(SQL.COLUMN_NAME_PARENT_ID));
        record.put(SQL.COLUMN_NAME_PRIORITY, ActionMain.getInstance().rand.nextInt(100)); // 이것도 임시
        record.put(SQL.COLUMN_NAME_WORD, word);
        record.put(SQL.COLUMN_NAME_STEM, word);
        record.put(SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        record.put(SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        result = ActionMain.getInstance().getDB().insert(TABLE_NAME, null, record);
        record.clear();

        return result;
    }

    // TODO: 오로지 디버깅용. 나중에는 삭제해야 할 메소드임.
    long add(
            long parentID,
            int priority,
            String word,
            String stem,
            String picture,
            boolean is_picture_preset
    ) {
        ContentValues values = new ContentValues();
        values.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, parentID);
        values.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, priority);
        values.put(ActionWord.SQL.COLUMN_NAME_WORD, word);
        values.put(ActionWord.SQL.COLUMN_NAME_STEM, stem);
        values.put(ActionWord.SQL.COLUMN_NAME_PICTURE, picture);
        values.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, is_picture_preset ? 1 : 0);

        ActionMain actionMain = ActionMain.getInstance();
        return actionMain.getDB().insert(actionMain.itemChain[itemClassID].TABLE_NAME, null, values);
    }

    long add(
            long parentID,
            int priority,
            String word,
            String stem,
            int picture,
            boolean is_picture_preset
    ) {
        return add(
                parentID,
                priority,
                word,
                stem,
                Integer.toString(picture),
                is_picture_preset
        );
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
                if (!isOnline) return;

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                container.getTTS().speak(phonetic, TextToSpeech.QUEUE_FLUSH, null);
//                container.getTTS().speak(phonetic, TextToSpeech.QUEUE_FLUSH, null, null); // over API 21
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

    protected boolean verifyAndCorrectDependencyRemoval(Context context, AACGroupContainer.RemovalListBundle list) { return true; }

    @Override
    protected void printRemovalList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Words -");
        super.printRemovalList(listBundle);
    }

    @Override
    protected void printMissingDependencyList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Words -");
        super.printMissingDependencyList(listBundle);
    }

    @Override
    public boolean removeWithID(Context context, int id) {
        ActionMain actionMain = ActionMain.getInstance();

        boolean isReserved = false;
        for (int i : reservedID) if (i == id) {
            isReserved = true;
            break;
        }

        if (actionMain.containerRef.rootGroupElement.ids.contains(id) || isReserved) {
            ContentValues values = new ContentValues();
            values.put(SQL.COLUMN_NAME_PARENT_ID, 0);
            return updateWithIDs(context, values, new int[] {id}) > 0;
        }
        else return super.removeWithID(context, id);
    }

}
