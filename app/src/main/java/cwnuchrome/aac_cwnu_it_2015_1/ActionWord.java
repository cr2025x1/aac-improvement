package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    String MAP_TABLE_NAME;
    String MAP_SQL_CREATE_ENTRIES;
    String MAP_SQL_DELETE_ENTRIES;

    public ActionWord () {
        super(ActionMain.item.ID_Word, "Word");

        reservedID = new int[] {1};

        TABLE_NAME = "LocalWord";
        MAP_TABLE_NAME = TABLE_NAME + "_FeedbackMap";
        SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        SQL._ID + SQL.INTEGER_PRIMARY_KEY + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.TEXT_TYPE + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.TEXT_TYPE + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.TEXT_TYPE + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM + SQL.TEXT_TYPE + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE + SQL.TEXT_TYPE + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE_IS_PRESET + SQL.INTEGER_TYPE + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_REFERENCE_COUNT + SQL.INTEGER_TYPE + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_FEEDBACK_MAP_TAG + SQL.TEXT_TYPE + SQL.NOT_NULL +
                        " )";
        MAP_SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + MAP_TABLE_NAME + " (" +
                        SQL._ID + SQL.INTEGER_PRIMARY_KEY + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_OWNER_ID + SQL.INTEGER_TYPE + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_FEEDBACK_MAP + SQL.BLOB_TYPE + // 맵은 null 값을 가질 수 있음!!!
                        ")";
        MAP_SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + MAP_TABLE_NAME;
    }

    public int init (ContentValues values) {
        return 0;
    }
    public int execute () {
        return 0;
    }

    interface SQL extends ActionItem.SQL {
        String COLUMN_NAME_REFERENCE_COUNT = "ref_count";
        String COLUMN_NAME_FEEDBACK_MAP = "feedback_map";
        String COLUMN_NAME_FEEDBACK_MAP_TAG = "feedback_map_tag";
        String COLUMN_NAME_OWNER_ID = "owner_id";
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL(MAP_SQL_CREATE_ENTRIES);
        super.createTable(db);
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
                        SQL.COLUMN_NAME_REFERENCE_COUNT + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_FEEDBACK_MAP_TAG +
                        ") " +
                        "SELECT " +
                        "1" + SQL.COMMA_SEP +
                        "0" + SQL.COMMA_SEP +
                        "0" + SQL.COMMA_SEP +
                        "'" + AACGroupContainerPreferences.ROOT_GROUP_NAME + "'" + SQL.COMMA_SEP +
                        "'" + AACGroupContainerPreferences.ROOT_GROUP_NAME + "'" + SQL.COMMA_SEP +
                        R.drawable.btn_default + SQL.COMMA_SEP +
                        "1" + SQL.COMMA_SEP +
                        "1" + SQL.COMMA_SEP + // 루트 그룹이 이 단어를 쓰기 때문에 참조 카운트값에 0 대신 1이 들어감. 만일 이 조건이 달라진다면 이 또한 바뀌어야 한다.
                        "'" + create_feedback_map_tag(null) + "'" +
                        " WHERE NOT EXISTS (SELECT 1 FROM " +
                        TABLE_NAME + " WHERE " +
                        SQL._ID + " = 1" +
                        ");"
        );
        db.execSQL(
                "INSERT INTO " +
                        MAP_TABLE_NAME + " (" +
                        SQL._ID + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_OWNER_ID +
                        ") SELECT " +
                        1 + SQL.COMMA_SEP +
                        1 +
                        " WHERE NOT EXISTS (SELECT 1 FROM " +
                        MAP_TABLE_NAME + " WHERE " +
                        SQL._ID + "=1)"
        );
        // ActionMain.update_db_collection_count(db, 1); // 기본 키워드의 parent_id가 0이 아니게 세팅될 경우에 주석처리를 지울 것
    }

    @Override
    public void clearTable(SQLiteDatabase db) {
        db.execSQL(MAP_SQL_DELETE_ENTRIES);
        super.clearTable(db);
    }

    @Override
    public void deleteTable(SQLiteDatabase db) {
        db.execSQL(MAP_SQL_DELETE_ENTRIES);
        super.clearTable(db);
    }

    // 주어진 단어가 이미 DB 상에 존재하면 그 단어의 ID를 반환, 없으면 추가 후 추가된 단어의 ID를 반환.
    public long raw_add(ContentValues values) {
        String word = values.getAsString(ActionWord.SQL.COLUMN_NAME_WORD);
        long result = exists(word);
        ActionMain actionMain = ActionMain.getInstance();
        SQLiteDatabase db = actionMain.getDB();
        if (result != -1) {

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
            if (parentID == 0) { // 사실 이제부터는 항상 parentID == 0이겠지만 그래도 일단 워드를 양지로 다시 들일 경우를 대비해서 남겨둔다.
                ContentValues record = new ContentValues();
                record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, values.getAsLong(SQL.COLUMN_NAME_PARENT_ID));
                if (actionMain.itemChain[ActionMain.item.ID_Word].updateWithIDs(actionMain.containerRef.context, record, new long[] {id}) > 0) {
//                    update_reference_count(id, 1); // 이제 더 이상 워드는 가시화되지 않으므로 auto-reference는 카운트를 올리지 못한다.
//                    actionMain.update_db_collection_count(1, 1); // 이제 더 이상 워드는 가시화되지 않으므로 콜렉션에서 제외된다.
                }
            }

            return result;
        }

        values.put(SQL.COLUMN_NAME_FEEDBACK_MAP_TAG, create_feedback_map_tag(null));

        long id = super.raw_add(values);
        if (id != -1) {
            ContentValues map_table_values = new ContentValues();
            map_table_values.put(SQL.COLUMN_NAME_OWNER_ID, id);
            map_table_values.put(SQL.COLUMN_NAME_FEEDBACK_MAP, (byte[])null);

            db.insert(
                    MAP_TABLE_NAME,
                    null,
                    map_table_values
                    );

        }

        return id;

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
//        values.put(SQL.COLUMN_NAME_REFERENCE_COUNT, 1); // 이제 워드는 모두 숨겨지게 되므로 레퍼런스를 0으로 세팅한다.
        values.put(SQL.COLUMN_NAME_REFERENCE_COUNT, 0);

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

    // TODO: 데이터베이스의 부하를 해결하기 위해 한 번에 여러 id를 받아 한 번에 처리하도록 만들기?
    @Override
    public boolean removeWithID(Context context, long id) {
        ActionMain actionMain = ActionMain.getInstance();
        SQLiteDatabase db = actionMain.getDB();

        boolean isReserved = false;
        for (int i : reservedID) if (i == id) {
            isReserved = true;
            break;
        }

        if (actionMain.containerRef.rootGroupElement.ids.contains(id) || isReserved) {
            // 예약어 처리 부분: 이 블록은 상위 클래스 메소드를 호출하지 않고 여기서 끝난다.
            ContentValues values = new ContentValues();
            values.put(SQL.COLUMN_NAME_PARENT_ID, 0);
            boolean effected = updateWithIDs(context, values, new long[]{id}) > 0;

//            update_reference_count(id, -1); 워드의 불가시화로 인해 자기 자신에 대한 레퍼런스 카운트가 없어졌으므로 주석 처리됨.
//            actionMain.update_db_collection_count(-1, -1); 워드의 불가시화 -> 콜렉션 카운트에서 제외 -> 따라서 삭제 시에도 콜렉션 카운트 업데이트 따위 없음. 주석 처리됨.

            return effected;
        }
        else {
            // 단어가 영구히 제거되므로 이 단어와 연관된 모든 피드백 맵에서도 이 단어 부분을 제거한다.
            Cursor c = db.query(
                    TABLE_NAME,
                    new String[]{SQL._ID},
                    SQL.COLUMN_NAME_FEEDBACK_MAP_TAG + " LIKE '%:" + id + ":%'",
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();

            int id_col = c.getColumnIndexOrThrow(SQL._ID);
            if (c.getCount() > 0) for (int i = 0; i < c.getCount(); i++) {
                // 이 단어가 쓰인 피드백 맵을 찾고, 해동한 후 이 단어의 id 부분을 제거하고 데이터베이스를 업데이트한다.
                long effected_id = c.getLong(id_col);
                Cursor fb_c = db.query(
                        MAP_TABLE_NAME,
                        new String[] {SQL.COLUMN_NAME_FEEDBACK_MAP},
                        SQL.COLUMN_NAME_OWNER_ID + "=" + effected_id,
                        null,
                        null,
                        null,
                        null
                );
                fb_c.moveToFirst();

                HashMap<Long, Double> feedback_map = actionMain.thaw_map(fb_c.getBlob(fb_c.getColumnIndexOrThrow(SQL.COLUMN_NAME_FEEDBACK_MAP)));
                feedback_map.remove(id);
                update_feedback(effected_id, feedback_map);

                fb_c.close();
                c.moveToNext();
            }
            c.close();

            return super.removeWithID(context, id);
        }
    }

    // 단어의 참조 카운트 업데이트 메소드
    public void update_reference_count(long id, long diff) {
        ActionMain actionMain = ActionMain.getInstance();
        Cursor c = actionMain.getDB().query(
                TABLE_NAME,
                new String[]{SQL.COLUMN_NAME_REFERENCE_COUNT},
                SQL._ID + "=" + id,
                null,
                null,
                null,
                null
        );
        if (c.getCount() == 0) throw new IllegalArgumentException("Given ID does not exists.");
        c.moveToFirst();

        long ref_count = c.getLong(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_REFERENCE_COUNT)) + diff;
        c.close();
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (ref_count <= 0 && AACGroupContainerPreferences.DATABASE_REMOVE_WORD_WITH_NO_REFERENCE) removeWithID(actionMain.getContext(), id);
        else {
            actionMain.getDB().execSQL("UPDATE " + TABLE_NAME +
                            " SET " + SQL.COLUMN_NAME_REFERENCE_COUNT + "=" + SQL.COLUMN_NAME_REFERENCE_COUNT + "+(" + diff + ")"
                            + " WHERE " + SQL._ID + "=" + id
            );
        }
    }

    // 해당 워드의 아이디와 피드백 맵을 받아 데이터베이스를 업데이트하는 메소드
    public void update_feedback(long id, @NonNull HashMap<Long, Double> map) {
        ActionMain actionMain = ActionMain.getInstance();
        ContentValues map_values = new ContentValues();
        map_values.put(SQL.COLUMN_NAME_FEEDBACK_MAP, actionMain.freeze_map(map));
        actionMain.getDB().update(
                MAP_TABLE_NAME,
                map_values,
                SQL.COLUMN_NAME_OWNER_ID + "=" + id,
                null
        );

        ContentValues values = new ContentValues();
        values.put(SQL.COLUMN_NAME_FEEDBACK_MAP_TAG, create_feedback_map_tag(map));
        updateWithIDs(actionMain.context, values, new long[] {id});
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
    public HashMap<Long, QueryWordInfo> convert_to_id_ref_map(@NonNull HashMap<String, Long> queryMap) {
        HashMap<Long, QueryWordInfo> id_ref_map = new HashMap<>();
        SQLiteDatabase db = ActionMain.getInstance().getDB();

        for (Map.Entry<String, Long> entry : queryMap.entrySet()) {
            String entry_word = entry.getKey();
            Cursor entry_word_cursor = db.query(
                    TABLE_NAME,
                    new String[]{SQL._ID, SQL.COLUMN_NAME_REFERENCE_COUNT, SQL.COLUMN_NAME_STEM},
                    ActionItem.SQL.COLUMN_NAME_STEM + " LIKE '%" + entry_word + "%'",
                    null,
                    null,
                    null,
                    null
            );
            entry_word_cursor.moveToFirst();

            for (int i = 0; i < entry_word_cursor.getCount(); i++) {
                long id = entry_word_cursor.getLong(entry_word_cursor.getColumnIndexOrThrow(SQL._ID));
                // TODO: 만일 형태소 분석기 추가 시 여기서 분석기의 태그를 제거하도록 해야 한다.
                String word_frag = entry_word_cursor.getString(entry_word_cursor.getColumnIndexOrThrow(SQL.COLUMN_NAME_STEM));
                if (!id_ref_map.containsKey(id)) {
                    QueryWordInfo info = new QueryWordInfo(
                            word_frag.equals(entry_word) ? entry.getValue() : 1l,
                            entry_word_cursor.getLong(entry_word_cursor.getColumnIndexOrThrow(SQL.COLUMN_NAME_REFERENCE_COUNT)),
                            ActionMain.cos_sim(entry_word, word_frag),
                            0.0d
                    );

                    id_ref_map.put(
                            id,
                            info
                    );
                }

                entry_word_cursor.moveToNext();
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
                    info.feedback_weight,
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

    @NonNull public HashMap<Long, Long> get_id_count_map(long id) {
        HashMap<Long, Long> map = new HashMap<>(1);
        map.put(id, 1l);
        return map;
    }

    // 아이템의 제거가 발생할 시에 모든 아이템의 피드백 맵을 업데이트해야 할 필요성이 생긴다. 이 때 업데이트 되어야 할 피드백 맵
    // 대상을 찾기 위해서는 모든 객체를 일일이 해동한 후 조사해야 하지만, 그러지 않고 데이터베이스에서 바로 검색할 수 있도록
    // 문자열로 이루어진 일종의 태그를 만들어 붙임으로서 이를 해결한다.
    //
    // 주어진 피드백 맵을 받아 이 피드백 맵의 문자열 태그를 생성해 반환한다. 널 값이 전달될 경우 빈 맵의 문자열 태그를 반환한다.
    @NonNull public String create_feedback_map_tag(@Nullable HashMap<Long, Double> feedback_map) {
        String start_end_char = "|";
        String wrapper_char = ":";
        StringBuilder sb = new StringBuilder(start_end_char);

        if (feedback_map != null) for (Map.Entry<Long, Double> e : feedback_map.entrySet()) {
            sb.append(wrapper_char);
            sb.append(e.getKey());
            sb.append(wrapper_char);
        }

        sb.append(start_end_char);

        return sb.toString();
    }
}
