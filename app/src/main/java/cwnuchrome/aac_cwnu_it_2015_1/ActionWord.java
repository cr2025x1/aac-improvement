package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chrome on 5/5/15.
 *
 * 단어 클래스. ActionItem에 대해 의존성을 가짐.
 */
public class ActionWord extends ActionItem {

    public ActionWord () {
        super(ActionMain.item.ID_Word, "Word");

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
                        SQL.COLUMN_NAME_PICTURE_IS_PRESET + SQL.INTEGER_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_REFERENCE_COUNT + SQL.INTEGER_TYPE +
                        " )";
    }

    public int init (ContentValues values) {
        return 0;
    }
    public int execute () {
        return 0;
    }

    interface SQL extends ActionItem.SQL {
         String COLUMN_NAME_REFERENCE_COUNT = "ref_count";
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
                        SQL.COLUMN_NAME_PICTURE_IS_PRESET + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_REFERENCE_COUNT +
                        ") " +
                        "SELECT " +
                        "1" + SQL.COMMA_SEP +
                        "0" + SQL.COMMA_SEP +
                        "0" + SQL.COMMA_SEP +
                        "'" + AACGroupContainerPreferences.ROOT_GROUP_NAME + "'" + SQL.COMMA_SEP +
                        "'" + AACGroupContainerPreferences.ROOT_GROUP_NAME + "'" + SQL.COMMA_SEP +
                        R.drawable.btn_default + SQL.COMMA_SEP +
                        "1" + SQL.COMMA_SEP +
                        "1" + // 루트 그룹이 이 단어를 쓰기 때문에 참조 카운트값에 0 대신 1이 들어감. 만일 이 조건이 달라진다면 이 또한 바뀌어야 한다.
                        " WHERE NOT EXISTS (SELECT 1 FROM " +
                        TABLE_NAME + " WHERE " +
                        SQL._ID + " = 1" +
                        ");"
        );
        // ActionMain.update_db_collection_count(db, 1); // 기본 키워드의 parent_id가 0이 아니게 세팅될 경우에 주석처리를 지울 것
    }

    // 주어진 단어가 이미 DB 상에 존재하면 그 단어의 ID를 반환, 없으면 추가 후 추가된 단어의 ID를 반환.
    // TODO: ContentValues 리패킹 제거
    public long raw_add(ContentValues values) {
        String word = values.getAsString(ActionWord.SQL.COLUMN_NAME_WORD);
        long result = exists(word);
        if (result != -1) {
            ActionMain actionMain = ActionMain.getInstance();
            SQLiteDatabase db = actionMain.getDB();

            Cursor c = db.query(
                    actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME,
                    new String[] {ActionWord.SQL._ID, ActionWord.SQL.COLUMN_NAME_PARENT_ID},
                    ActionWord.SQL.COLUMN_NAME_WORD + "='" + word + "'",
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();
            int parentID = c.getInt(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PARENT_ID));
            int id = c.getInt(c.getColumnIndexOrThrow(ActionWord.SQL._ID));
            c.close();

//                if (parentID == 0 && actionMain.containerRef.rootGroupElement.ids.contains(id)) {
            if (parentID == 0) {
                ContentValues record = new ContentValues();
                record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, values.getAsLong(SQL.COLUMN_NAME_PARENT_ID));
                if (actionMain.itemChain[ActionMain.item.ID_Word].updateWithIDs(actionMain.containerRef.context, record, new long[] {id}) > 0) {
                    update_reference_count(id, 1);
                    actionMain.update_db_collection_count(1, 1);
                }
            }

            return result;
        }

        ContentValues record = new ContentValues();
        record.put(SQL.COLUMN_NAME_PARENT_ID, values.getAsString(SQL.COLUMN_NAME_PARENT_ID));
        record.put(SQL.COLUMN_NAME_PRIORITY, ActionMain.getInstance().rand.nextInt(100)); // 이것도 임시
        record.put(SQL.COLUMN_NAME_WORD, word);
        record.put(SQL.COLUMN_NAME_STEM, word);
        record.put(SQL.COLUMN_NAME_PICTURE, values.getAsString(SQL.COLUMN_NAME_PICTURE));
        record.put(SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        record.put(SQL.COLUMN_NAME_REFERENCE_COUNT, 1);

        return super.raw_add(record);
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
        values.put(SQL.COLUMN_NAME_REFERENCE_COUNT, 1);

        return raw_add(values);
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
    public boolean removeWithID(Context context, long id) {
        ActionMain actionMain = ActionMain.getInstance();

        boolean isReserved = false;
        for (int i : reservedID) if (i == id) {
            isReserved = true;
            break;
        }

        if (actionMain.containerRef.rootGroupElement.ids.contains(id) || isReserved) {
            ContentValues values = new ContentValues();
            values.put(SQL.COLUMN_NAME_PARENT_ID, 0);
            boolean effected = updateWithIDs(context, values, new long[]{id}) > 0;

            update_reference_count(id, -1);
            actionMain.update_db_collection_count(-1, -1);

            return effected;
        }
        else return super.removeWithID(context, id);
    }

    // 단어의 참조 카운트 업데이트 메소드
    public void update_reference_count(long id, long diff) {
        ActionMain actionMain = ActionMain.getInstance();
        actionMain.getDB().execSQL("UPDATE " + TABLE_NAME +
                " SET " + SQL.COLUMN_NAME_REFERENCE_COUNT + "=" + SQL.COLUMN_NAME_REFERENCE_COUNT + "+(" + diff + ")"
                + " WHERE " + SQL._ID + "=" + id
        );
    }

    public boolean is_hidden_word(String s) {
        ActionMain actionMain = ActionMain.getInstance();
        long existCheck = exists(s);
        if (existCheck == -1) return false;
        else {
            Cursor c = actionMain.getDB().query(
                    actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME,
                    new String[] {ActionWord.SQL._ID, ActionWord.SQL.COLUMN_NAME_PARENT_ID},
                    ActionWord.SQL._ID + "=" + existCheck,
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();
            int parentID = c.getInt(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PARENT_ID));
            c.close();

            if (parentID == 0) return true;
        }
        return false;
    }

    public onClickClass allocOCC(Context context, AACGroupContainer container) {
        return new onClickClass(context, container);
    }

    public static class onClickClass extends ActionItem.onClickClass {
        public onClickClass(Context context, AACGroupContainer container) {
            super(context, container);
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



    // 주어진 문자열-문자열 수 쿼리 해시맵에 대응되는 워드의 ID와 그 워드의 참조 횟수의 해시맵을 제공한다.
    @NonNull
    public HashMap<Long, QueryWordInfo> convert_to_id_ref_map(@NonNull SQLiteDatabase db, @NonNull HashMap<String, Long> queryMap) {
        HashMap<Long, QueryWordInfo> id_ref_map = new HashMap<>();

        for (Map.Entry<String, Long> entry : queryMap.entrySet()) {
            String entry_word = entry.getKey();
            Cursor entry_word_cursor = db.query(
                    TABLE_NAME,
                    new String[]{SQL._ID, SQL.COLUMN_NAME_REFERENCE_COUNT},
                    ActionItem.SQL.COLUMN_NAME_STEM + "='" + entry_word + "'",
                    null,
                    null,
                    null,
                    null
            );
            entry_word_cursor.moveToFirst();

            // DB 상에 존재하지 않는 쿼리 워드는 버림.
            if (entry_word_cursor.getCount() > 0) {
                long id = entry_word_cursor.getLong(entry_word_cursor.getColumnIndexOrThrow(SQL._ID));
                if (!id_ref_map.containsKey(id)) {
                    QueryWordInfo info = new QueryWordInfo(
                            entry.getValue(),
                            entry_word_cursor.getLong(entry_word_cursor.getColumnIndexOrThrow(SQL.COLUMN_NAME_REFERENCE_COUNT))
                    );

                    id_ref_map.put(
                            id,
                            info
                    );
                }
            }
            entry_word_cursor.close();
        }
        return id_ref_map;
    }


    @NonNull
    public HashMap<Long, Double> evaluate_by_query_map(
            @NonNull SQLiteDatabase db,
            @NonNull HashMap<Long, QueryWordInfo> queryMap,
            @NonNull HashMap<Long, Double> eval_map,
            long entire_collection_count,
            long average_document_length) {

        for (Map.Entry<Long, QueryWordInfo> entry : queryMap.entrySet()) {
            QueryWordInfo info = entry.getValue();

            double eval = ActionMain.ranking_function(
                    info.count,
                    1,
                    1,
                    average_document_length,
                    entire_collection_count,
                    info.ref_count
            );

            long key = entry.getKey();
            eval_map.put(key, eval_map.get(key) + eval);

        }

        return eval_map;
    }

}
