package cwnuchrome.aac_cwnu_it_2015_1;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
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

//    String MAP_TABLE_NAME;
    String map_sql_create_entries_tail;
    public static final String MAP_SQL_CREATE_ENTIRES_HEAD = "CREATE TABLE IF NOT EXISTS ";
//    String MAP_SQL_DELETE_ENTRIES;
    protected SubDBHelper sub_db_helper;
    protected SQLiteDatabase sub_db;

    public ActionWord () {
        super(ActionMain.item.ID_Word, "Word", true);

        reservedID = new int[] {1};

        TABLE_NAME = "LocalWord";
//        MAP_TABLE_NAME = TABLE_NAME + "_FeedbackMap";
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
                        ")";
//        MAP_SQL_CREATE_ENTRIES =
//                "CREATE TABLE IF NOT EXISTS " + MAP_TABLE_NAME + " (" +
//                        SQL._ID + SQL.INTEGER_PRIMARY_KEY + SQL.NOT_NULL + SQL.COMMA_SEP +
//                        SQL.COLUMN_NAME_OWNER_ID + SQL.INTEGER_TYPE + SQL.NOT_NULL + SQL.COMMA_SEP +
//                        SQL.COLUMN_NAME_FEEDBACK_MAP + SQL.BLOB_TYPE + // 맵은 null 값을 가질 수 있음!!!
//                        ")";
//        MAP_SQL_DELETE_ENTRIES =
//                "DROP TABLE IF EXISTS " + MAP_TABLE_NAME;
        map_sql_create_entries_tail =
                " (" +
                        SQL._ID + SQL.INTEGER_PRIMARY_KEY + SQL.NOT_NULL + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_MAP_WEIGHT + SQL.REAL_TYPE + SQL.NOT_NULL +
                        ")";


//        sub_db_helper = new SubDBHelper(actionMain.getContext(), TABLE_NAME + "FeedBack.db" , "id");
//        sub_db = sub_db_helper.getWritableDatabase();
    }

    public int init (ContentValues values) {
        return 0;
    }
    public int execute () {
        return 0;
    }

    public void init_sub_db(Context context) {
        sub_db_helper = new SubDBHelper(context, TABLE_NAME + "Feedback.db" , "id");
        sub_db = sub_db_helper.getWritableDatabase();
    }

    interface SQL extends ActionItem.SQL {
        String COLUMN_NAME_REFERENCE_COUNT = "ref_count";
//        String COLUMN_NAME_FEEDBACK_MAP = "feedback_map";
        String COLUMN_NAME_FEEDBACK_MAP_TAG = "feedback_map_tag";
//        String COLUMN_NAME_OWNER_ID = "owner_id";
        String COLUMN_NAME_MAP_WEIGHT = "weight";
        String ATTACHMENT_FEEDBACK_MAP = "attachment_feedback_map";
    }

    @Override
    public void createTable(SQLiteDatabase db) {
//        db.execSQL(MAP_SQL_CREATE_ENTRIES);
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
//        db.execSQL(
//                "INSERT INTO " +
//                        MAP_TABLE_NAME + " (" +
//                        SQL._ID + SQL.COMMA_SEP +
//                        SQL.COLUMN_NAME_OWNER_ID +
//                        ") SELECT " +
//                        1 + SQL.COMMA_SEP +
//                        1 +
//                        " WHERE NOT EXISTS (SELECT 1 FROM " +
//                        MAP_TABLE_NAME + " WHERE " +
//                        SQL._ID + "=1)"
//        );
        sub_db.execSQL(
                MAP_SQL_CREATE_ENTIRES_HEAD + sub_db_helper.getTableName(1) + map_sql_create_entries_tail
        );
        // ActionMain.update_db_collection_count(db, 1); // 기본 키워드의 parent_id가 0이 아니게 세팅될 경우에 주석처리를 지울 것
    }

    @Override
    public void clearTable(SQLiteDatabase db) {
//        db.execSQL(MAP_SQL_DELETE_ENTRIES);
        sub_db_helper.onReset(sub_db);
        super.clearTable(db);
    }

    @Override
    public void deleteTable(SQLiteDatabase db) {
//        db.execSQL(MAP_SQL_DELETE_ENTRIES);
        sub_db_helper.onReset(sub_db);
        super.clearTable(db);
    }

    // 주어진 단어가 이미 DB 상에 존재하면 그 단어의 ID를 반환, 없으면 추가 후 추가된 단어의 ID를 반환.
    public long raw_add(ContentValues values) {
        write_lock.lock();
        String word = values.getAsString(ActionWord.SQL.COLUMN_NAME_WORD);
        long result = exists(word);
//        ActionMain actionMain = ActionMain.getInstance();
        SQLiteDatabase db = actionMain.getDB();
        if (result != -1) {

            Cursor c = db.query(
                    actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME,
                    new String[] {ActionWord.SQL._ID, ActionWord.SQL.COLUMN_NAME_PARENT_ID},
                    ActionWord.SQL.COLUMN_NAME_WORD + "=?",
                    new String[] {word},
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
                //noinspection StatementWithEmptyBody
                if (actionMain.itemChain[ActionMain.item.ID_Word].updateWithIDs(actionMain.containerRef.context, record, new long[] {id}) > 0) {
//                    update_reference_count(id, 1); // 이제 더 이상 워드는 가시화되지 않으므로 auto-reference는 카운트를 올리지 못한다.
//                    actionMain.update_db_collection_count(1, 1); // 이제 더 이상 워드는 가시화되지 않으므로 콜렉션에서 제외된다.
                }
            }

            write_lock.unlock();
            return result;
        }

        values.put(SQL.COLUMN_NAME_FEEDBACK_MAP_TAG, create_feedback_map_tag(null));

        long id = super.raw_add(values);
        if (id != -1) {
//            ContentValues map_table_values = new ContentValues();
//            map_table_values.put(SQL.COLUMN_NAME_OWNER_ID, id);
//            map_table_values.put(SQL.COLUMN_NAME_FEEDBACK_MAP, (byte[])null);
//
//            db.insert(
//                    MAP_TABLE_NAME,
//                    null,
//                    map_table_values
//                    );
            create_sub_db(id, null);
        }

        write_lock.unlock();
        return id;
    }

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

    protected void addToRemovalList(Context context, AACGroupContainer.RemovalListBundle list, long id) {
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

    // TODO: 데이터베이스의 부하를 해결하기 위해 한 번에 여러 id를 받아 한 번에 처리하도록 만들기? 하지만 만일 그렇게 할 경우 작업 도중 중단시에는 어떤 일이 발생할까? transaction -> commit 구조 흉내내기?
    @Override
    public boolean removeWithID(Context context, long id) {
        write_lock.lock();

        ActionMain actionMain = ActionMain.getInstance();
        SQLiteDatabase db = actionMain.getDB();

        boolean isReserved = false;
        for (int i : reservedID) if (i == id) {
            isReserved = true;
            break;
        }

        if (actionMain.containerRef.rootGroupElement.ids.contains(id) || isReserved) {
            // 예약어 처리 부분: 이 블록은 상위 클래스 메소드를 호출하지 않고 여기서 끝난다.
//            ContentValues values = new ContentValues();
//            values.put(SQL.COLUMN_NAME_PARENT_ID, 0); // 워드 불가시화: 어차피 모든 워드의 parent_id == 0이므로 주석 처리함.

//            boolean effected = updateWithIDs(context, values, new long[]{id}) > 0;

//            update_reference_count(id, -1); 워드의 불가시화로 인해 자기 자신에 대한 레퍼런스 카운트가 없어졌으므로 주석 처리됨.
//            actionMain.update_db_collection_count(-1, -1); 워드의 불가시화 -> 콜렉션 카운트에서 제외 -> 따라서 삭제 시에도 콜렉션 카운트 업데이트 따위 없음. 주석 처리됨.

//            return effected;
//            return updateWithIDs(context, values, new long[]{id}) > 0;

            write_lock.unlock();
            return true;
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
//                Cursor fb_c = db.query(
//                        MAP_TABLE_NAME,
//                        new String[] {SQL.COLUMN_NAME_FEEDBACK_MAP},
//                        SQL.COLUMN_NAME_OWNER_ID + "=" + effected_id + " AND " + SQL.COLUMN_NAME_OWNER_ID + "!=" + id, // 자기 자신의 것은 수정 안 함. 어차피 지워지니까.
//                        null,
//                        null,
//                        null,
//                        null
//                );
//                fb_c.moveToFirst();
//
//                HashMap<Long, Double> feedback_map = actionMain.thaw_map(fb_c.getBlob(fb_c.getColumnIndexOrThrow(SQL.COLUMN_NAME_FEEDBACK_MAP)));
//                feedback_map.remove(id);
//                update_feedback(effected_id, feedback_map);


//                fb_c.close();

                remove_sub_db(effected_id, new long[]{id});
                update_feedback_tag(effected_id);

                c.moveToNext();
            }
            c.close();

            // 자기 자신의 피드백 맵 제거.
//            db.delete(
//                    MAP_TABLE_NAME,
//                    SQL.COLUMN_NAME_OWNER_ID + " = " + id,
//                    null
//            );
            delete_sub_db(id);

            boolean result = super.removeWithID(context, id);
            write_lock.unlock();
            return result;
        }
    }

    // 단어의 참조 카운트 업데이트 메소드
    public void update_reference_count(long id, long diff) {
        write_lock.lock();
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
        if (c.getCount() == 0) {
            write_lock.unlock();
            throw new IllegalArgumentException("Given ID does not exists.");
        }
        c.moveToFirst();

        long ref_count = c.getLong(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_REFERENCE_COUNT)) + diff;
        c.close();

        if (ref_count < 0) {
            write_lock.unlock();
            throw new IllegalStateException("Reference count is below 0.");
        }

        //noinspection PointlessBooleanExpression,ConstantConditions
        if (ref_count == 0 && AACGroupContainerPreferences.DATABASE_REMOVE_WORD_WITH_NO_REFERENCE) removeWithID(actionMain.getContext(), id);
        else {
            actionMain.getDB().execSQL("UPDATE " + TABLE_NAME +
                            " SET " + SQL.COLUMN_NAME_REFERENCE_COUNT + "=" + SQL.COLUMN_NAME_REFERENCE_COUNT + "+(" + diff + ")"
                            + " WHERE " + SQL._ID + "=" + id
            );
        }
        write_lock.unlock();
    }

    // 해당 워드의 아이디와 피드백 맵을 받아 데이터베이스를 업데이트하는 메소드
    // TODO: deprecated 판정 검토 중.
    public void update_feedback(long id, @NonNull HashMap<Long, Double> map) {
        write_lock.lock();
//        ContentValues map_values = new ContentValues();
//        map_values.put(SQL.COLUMN_NAME_FEEDBACK_MAP, actionMain.freeze_map(map));
//        actionMain.getDB().update(
//                MAP_TABLE_NAME,
//                map_values,
//                SQL.COLUMN_NAME_OWNER_ID + "=" + id,
//                null
//        );
        replace_sub_db(id, map);

        ContentValues values = new ContentValues();
        values.put(SQL.COLUMN_NAME_FEEDBACK_MAP_TAG, create_feedback_map_tag(map));
        updateWithIDs(actionMain.context, values, new long[] {id});
        write_lock.unlock();
    }

    public void update_feedback_tag(long id) {
        write_lock.lock();
        ContentValues values = new ContentValues();
        values.put(SQL.COLUMN_NAME_FEEDBACK_MAP_TAG, create_feedback_map_tag(id));
        updateWithIDs(actionMain.context, values, new long[] {id});
        write_lock.unlock();
    }

    public boolean is_hidden_word(String s) {
        read_lock.lock();
        ActionMain actionMain = ActionMain.getInstance();
        long existCheck = exists(s);
        if (existCheck == -1) {
            read_lock.unlock();
            return false;
        }
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

            if (parentID == 0) {
                read_lock.unlock();
                return true;
            }
        }
        read_lock.unlock();
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
    public HashMap<Long, QueryWordInfo> convert_query_map_to_qwi_map(@NonNull HashMap<String, Long> queryMap) {
        read_lock.lock();

        HashMap<Long, QueryWordInfo> id_ref_map = new HashMap<>();
        SQLiteDatabase db = ActionMain.getInstance().getDB();

        for (Map.Entry<String, Long> entry : queryMap.entrySet()) {
            String entry_word = entry.getKey();
            Cursor entry_word_cursor = db.query(
                    TABLE_NAME,
                    new String[] {SQL._ID, SQL.COLUMN_NAME_REFERENCE_COUNT, SQL.COLUMN_NAME_STEM},
                    ActionItem.SQL.COLUMN_NAME_STEM + " LIKE ?",
                    new String[] {"%" + entry_word + "%"},
                    null,
                    null,
                    null
            );
            entry_word_cursor.moveToFirst();

            for (int i = 0; i < entry_word_cursor.getCount(); i++) {
                long id = entry_word_cursor.getLong(entry_word_cursor.getColumnIndexOrThrow(SQL._ID));
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
        read_lock.unlock();
        return id_ref_map;
    }



    @NonNull
    public HashMap<Long, QueryWordInfo> convert_id_map_to_qwi_map(@NonNull HashMap<Long, Long> queryMap) {
        read_lock.lock();

        HashMap<Long, QueryWordInfo> id_ref_map = new HashMap<>();
        SQLiteDatabase db = ActionMain.getInstance().getDB();

        for (Map.Entry<Long, Long> entry : queryMap.entrySet()) {
            Long entry_id = entry.getKey();
            Cursor entry_word_cursor = db.query(
                    TABLE_NAME,
                    new String[]{SQL.COLUMN_NAME_REFERENCE_COUNT},
                    ActionItem.SQL._ID + "=" + entry_id,
                    null,
                    null,
                    null,
                    null
            );
            entry_word_cursor.moveToFirst();

            QueryWordInfo info = new QueryWordInfo(
                    entry.getValue(),
                    entry_word_cursor.getLong(entry_word_cursor.getColumnIndexOrThrow(SQL.COLUMN_NAME_REFERENCE_COUNT)),
                    1l,
                    0.0d
            );

            id_ref_map.put(
                    entry_id,
                    info
            );

            entry_word_cursor.close();
        }
        read_lock.unlock();
        return id_ref_map;
    }


    // 주어진 평가값 해시맵에 있는 문서들을 대상으로 주어진 쿼리 해시맵에 따른 관련도를 평가하여 기록하고, 이 해시맵을 반환한다.
    @NonNull
    public HashMap<Long, Double> evaluate_by_query_map(
            @NonNull final SQLiteDatabase db,
            @NonNull HashMap<Long, QueryWordInfo> queryMap,
            @NonNull HashMap<Long, Double> eval_map,
            final long entire_collection_count,
            final double average_document_length) {
        return evaluate_by_query_map_by_query_processor(
                queryMap,
                eval_map,
                new QueryProcessor() {
                    @Override
                    public void process_query_id(long id, QueryWordInfo qwi, @NonNull String eval_map_id_clause, @NonNull HashMap<Long, Double> query_proc_eval_map) {
                        double eval = ActionMain.ranking_function(
                                qwi.count,
                                qwi.feedback_weight,
                                1,
                                1,
                                average_document_length,
                                entire_collection_count,
                                qwi.ref_count
                        );

                        query_proc_eval_map.put(id, query_proc_eval_map.get(id) + eval);
                    }
                }
        );
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

    @NonNull public String create_feedback_map_tag(long id) {
        String table_name = get_sub_db_table_name(id);
        Cursor c = sub_db.query(
                table_name,
                new String[]{SQL._ID},
                null,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();
        int id_col = c.getColumnIndexOrThrow(SQL._ID);

        String start_end_char = "|";
        String wrapper_char = ":";
        StringBuilder sb = new StringBuilder(start_end_char);

        for (int i = 0; i < c.getCount(); i++) {
            sb.append(wrapper_char);
            sb.append(c.getLong(id_col));
            sb.append(wrapper_char);
            c.moveToNext();
        }
        sb.append(start_end_char);
        c.close();

        return sb.toString();
    }

    // 동시에 여러 개의 워드를 생성하는 메소드. 멀티워드 아이템 생성 시에 유용하다.
    @NonNull public long[] add_multi(@NonNull String[] textTokens) {
        write_lock.lock();
        if (textTokens.length == 0) {
            write_lock.unlock();
            throw new IllegalArgumentException("Size of given String array is 0.");
        }

        long[] wordIDs = new long[textTokens.length];
        int wordPos = 0;
        for (String s : textTokens) {
            long id = exists(s);
            if (is_hidden_word(s) || id == -1) {
//                ActionMain.log(null, "adding word \"" + s + "\"");
                id = add(
//                        current_group_ID,
                        0,
                        0,
                        s,
                        s,
                        R.drawable.btn_default,
                        true
                );
            }
//            else {
//                ActionMain.log(null, "word \"" + s + "\" already exists");
//            }

            wordIDs[wordPos++] = id;
        }

        write_lock.unlock();
        return wordIDs;
    }

    // 이 아이템의 테이블의 모든 행에 대응하는 1:1 대응하는 키를 모두 가지는 해쉬맵을 만들어 반환한다.
    // 그러나 ActionWord는 불가시 아이템 카테고리이므로 빈 해시맵 객체를 반환한다.
//    @Override
//    @NonNull protected HashMap<Long, Double> alloc_evaluation_map(@NonNull SQLiteDatabase db, @Nullable String selection, @Nullable String[] selectionArgs) {
//        return new HashMap<>();
//    }






    // 만일 만일 해시맵의 각 매핑당 해당항목이 있으면 교체, 없으면 삽입 후 기록한다. 즉 업데이트 형식이다.
    public int update_sub_db(long id, HashMap<Long, Double> map) {
        int effected = 0;
        for (Map.Entry<Long, Double> e : map.entrySet()) {
            String table_name = sub_db_helper.getTableName(id);
            Long e_id = e.getKey();
            String where_clause = SQL._ID + "=" + e_id;
            Cursor c = sub_db.query(
                    table_name,
                    new String[]{SQL._ID},
                    where_clause,
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();
            int c_count = c.getCount();
            c.close();
            if (c_count > 0) {
                ContentValues values = new ContentValues(1);
                values.put(SQL.COLUMN_NAME_MAP_WEIGHT, e.getValue());
                sub_db.update(
                        table_name,
                        values,
                        where_clause,
                        null
                );
                effected++;
            } else {
                ContentValues values = new ContentValues(2);
                values.put(SQL._ID, e_id);
                values.put(SQL.COLUMN_NAME_MAP_WEIGHT, e.getValue());
                sub_db.insert(
                        table_name,
                        null,
                        values
                );
                effected++;
            }
        }
        return effected;
    }

    public void accumulate_sub_db(long id, Long key, Double value) {
        String table_name = sub_db_helper.getTableName(id);
        String where_clause = SQL._ID + "=" + key;
        Cursor c = sub_db.query(
                table_name,
                new String[]{SQL._ID, SQL.COLUMN_NAME_MAP_WEIGHT},
                where_clause,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();
        if (c.getCount() > 0) {
            ContentValues values = new ContentValues(1);
            values.put(SQL.COLUMN_NAME_MAP_WEIGHT, value + c.getDouble(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_MAP_WEIGHT)));
            sub_db.update(
                    table_name,
                    values,
                    where_clause,
                    null
            );
        } else {
            ContentValues values = new ContentValues(2);
            values.put(SQL._ID, key);
            values.put(SQL.COLUMN_NAME_MAP_WEIGHT, value);
            sub_db.insert(
                    table_name,
                    null,
                    values
            );
        }
        c.close();
    }

    public void create_sub_db(long id, HashMap<Long, Double> map) {
        // 먼저 해당 ID의 대응 테이블이 있는지 확인, 있으면 추가 작업 없이 참값 반환.
        if (exists_sub_db(id)) return;

        // 해당 ID의 대응 테이블이 없으므로, 먼저 테이블을 생성한다.
        sub_db.execSQL(
                MAP_SQL_CREATE_ENTIRES_HEAD + sub_db_helper.getTableName(id) + map_sql_create_entries_tail
        );

        // 주어진 해시맵을 테이블에 기록한다.
        if (map != null) update_sub_db(id, map);
    }

    public boolean exists_sub_db(long id) {
        boolean exists = false;
        Cursor c = sub_db.query(
                "sqlite_master",
                new String[] {"name"},
                "type='table' and name=?",
                new String[] {sub_db_helper.getTableName(id)},
                null,
                null,
                null
        );
        c.moveToFirst();
        if (c.getCount() > 0) exists = true;
        c.close();
        return exists;
    }

    public void remove_sub_db(long id, long[] keys) {
        String table_name = sub_db_helper.getTableName(id);
        for (long key : keys) {
            sub_db.delete(
                    table_name,
                    SQL._ID + " = " + key,
                    null
            );
        }
    }

    public void delete_sub_db(long id) {
        sub_db.execSQL("drop table " + sub_db_helper.getTableName(id));
    }

    public void replace_sub_db(long id, HashMap<Long, Double> map) {
        String table_name = sub_db_helper.getTableName(id);
        StringBuilder where_clause_builder = new StringBuilder(200);

        where_clause_builder.append("not (");
        for (Map.Entry<Long, Double> e : map.entrySet()) {
            where_clause_builder
                    .append(SQL._ID)
                    .append("=")
                    .append(e.getKey())
                    .append(" OR ");
        }
        where_clause_builder.setLength(where_clause_builder.length() - 4);
        where_clause_builder.append(")");

        sub_db.delete(
                table_name,
                where_clause_builder.toString(),
                null
        );

        update_sub_db(id, map);
    }

    public int accumulate_sub_db(long id, HashMap<Long, Double> map) {
        int effected = 0;
        for (Map.Entry<Long, Double> e : map.entrySet()) {
            String table_name = sub_db_helper.getTableName(id);
            Long e_id = e.getKey();
            String where_clause = SQL._ID + "=" + e_id;
            Cursor c = sub_db.query(
                    table_name,
                    new String[]{SQL._ID, SQL.COLUMN_NAME_MAP_WEIGHT},
                    where_clause,
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();
            if (c.getCount() > 0) {
                ContentValues values = new ContentValues(1);
                values.put(SQL.COLUMN_NAME_MAP_WEIGHT, e.getValue() + c.getDouble(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_MAP_WEIGHT)));
                sub_db.update(
                        table_name,
                        values,
                        where_clause,
                        null
                );
                effected++;
            } else {
                ContentValues values = new ContentValues(2);
                values.put(SQL._ID, e_id);
                values.put(SQL.COLUMN_NAME_MAP_WEIGHT, e.getValue());
                sub_db.insert(
                        table_name,
                        null,
                        values
                );
                effected++;
            }
            c.close();

        }
        return effected;
    }

    public SQLiteDatabase get_sub_db() {
        return sub_db;
    }

    public String get_sub_db_table_name(long id) {
        return sub_db_helper.getTableName(id);
    }
}
