package cwnuchrome.aac_cwnu_it_2015_1;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Chrome on 5/5/15.
 *
 * 매크로 클래스. 단어 클래스에 대한 의존성을 가진다.
 */
public class ActionMacro extends ActionItem {

    public ActionMacro() {
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
        record.put(SQL.COLUMN_NAME_PARENT_ID, values.getAsString(SQL.COLUMN_NAME_PARENT_ID));
        record.put(SQL.COLUMN_NAME_PRIORITY, ActionMain.getInstance().rand.nextInt(100)); // 이것도 임시
        record.put(SQL.COLUMN_NAME_WORD, word);
        record.put(SQL.COLUMN_NAME_STEM, word);
        record.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, values.getAsString(SQL.COLUMN_NAME_WORDCHAIN));
        record.put(SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        record.put(SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        result = db.insert(TABLE_NAME, null, record);
        record.clear();

        return result;
    }

    protected void addToRemovalList(Context context, AACGroupContainer.RemovalListBundle list, int id) {
        list.add(ActionMain.item.ID_Macro, id);
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
                itemCategoryID = ActionMain.item.ID_Macro;
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
                super.init(values);
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

    // 의존성 검사... 이것 때문에 단순하게 생각했던 아이템 제거에서 지옥문이 열렸다.
    protected boolean checkDependencyRemoval(Context context, AACGroupContainer.RemovalListBundle listBundle) {
        ActionMain actionMain = ActionMain.getInstance();
        ArrayList<Integer> wordList = listBundle.itemVector.get(ActionMain.item.ID_Word);
        ArrayList<Integer> macroList = listBundle.itemVector.get(ActionMain.item.ID_Macro);
        ArrayList<ContentValues> macroMissingList = listBundle.missingDependencyVector.get(ActionMain.item.ID_Macro);
        ActionDBHelper actDBHelper = new ActionDBHelper(context);

        if (wordList.size() == 0) return true;

        /* 지울 단어들에 대해 의존성을 가지는 매크로들을 찾기 위한 쿼리문의 작성 */

        Iterator<Integer> i = wordList.iterator();
        int id = i.next();

        final String OR = " OR ";
        final String LIKE_AND_HEAD = " LIKE '%:";
        final String TAIL = ":%'";

        StringBuilder qBuilder = new StringBuilder();
        qBuilder.append(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN);
        qBuilder.append(LIKE_AND_HEAD);
        qBuilder.append(id);
        qBuilder.append(TAIL);

        // 매 단어마다 조건문 확장
        while (i.hasNext()) {
            id = i.next();
            qBuilder.append(OR);
            qBuilder.append(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN);
            qBuilder.append(LIKE_AND_HEAD);
            qBuilder.append(id);
            qBuilder.append(TAIL);
        }

        String whereClause = qBuilder.toString(); // 완성된 조건문을 String으로 변환
        String sortOrder = ActionWord.SQL._ID + " ASC"; // 이후의 알고리즘을 위해 정렬 순서는 ID 기준 오름차순
        String projection[] = { ActionItem.SQL._ID, ActionItem.SQL.COLUMN_NAME_WORD };

        Cursor c;
        int c_count;
        int c_id_col;
        SQLiteDatabase db = actDBHelper.getWritableDatabase();
        c = db.query( // 삭제 대상 워드에 대한 의존성이 있는 모든 매크로가 이 커서에 담김
                actionMain.itemChain[ActionMain.item.ID_Macro].TABLE_NAME, // The table to query
                projection, // The columns to return
                whereClause, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                sortOrder // The sort order
        );
        c.moveToFirst();

            /* 삭제 대상으로 선택된 매크로와 앞서 찾아낸 매크로 목록과의 대조 */
            /* - 앞서 찾아낸 매크로의 집합은 반드시 선택된 매크로 대상의 집합에 포함된 관계여야 한다. */

        c_count = c.getCount();
        c_id_col = c.getColumnIndexOrThrow(ActionItem.SQL._ID);
        int c_word_col = c.getColumnIndexOrThrow(ActionItem.SQL.COLUMN_NAME_WORD);

        boolean dependencyProper = true;

        if (c_count > 0) {
            int c_id = c.getInt(c_id_col);

            Collections.sort(macroList); // 선택된 매크로 목록도 오름차순으로 정렬

            boolean endOfCursor = false;

            // 선택된 아이템을 순차 검색하며 확인
            for (int j : macroList) {
                // 현재 아이템의 ID값이 커서의 현재 아이템의 ID값보다 크다면?
                while (c_id < j) {
                    // 커서에 있는 아이템이 macroList에 없다: 의존성 문제가 있는 아이템이므로 removeDepArray에 추가
                    ContentValues values = new ContentValues();
                    values.put(ActionItem.SQL.COLUMN_NAME_WORD, c.getString(c_word_col));
                    values.put(ActionItem.SQL._ID, c_id);
                    macroMissingList.add(values);

                    // 커서가 맨 끝에 도달하지 않은 이상 커서를 뒤로 계속 넘긴다.
                    if (c.isLast()) {
                        endOfCursor = true;
                        break;
                    }

                    c.moveToNext();
                    c_id = c.getInt(c_id_col);
                }

                if (endOfCursor) break; // 커서가 맨 끝에 도달했으므로 더 이상 대조할 대상이 없음. 루프 종료.

                // 같은 ID값을 지니는 매크로를 선택 리스트에서 찾았음.
                if (c_id == j) {
                    // 의존성을 지니는 매크로가 제대로 선택 리스트에도 있음. 따라서 커서를 다음 항목으로 진행시킨 후, for 루프도 다음 회로 넘긴다.
                    if (c.isLast()) {
                        endOfCursor = true;
                        break;
                    }

                    c.moveToNext();
                    c_id = c.getInt(c_id_col);
                }

            }

            // 커서에 아직 여분이 남음 - 즉 선택된 리스트에 모든 의존성 있는 매크로가 포함되지 않았음을 의미.
            if (!endOfCursor) {
                dependencyProper = false;
                while (true) {
                    // 커서에 남은 매크로 아이템들은 모두 의존성이 있으나 선택된 리스트에 없는 아이템들이므로 removeDepArray에 넣는다.
                    ContentValues values = new ContentValues();
                    values.put(ActionItem.SQL.COLUMN_NAME_WORD, c.getString(c_word_col));
                    values.put(ActionItem.SQL._ID, c_id);
                    macroMissingList.add(values);

                    if (c.isLast()) break;

                    c.moveToNext();
                    c_id = c.getInt(c_id_col);
                }
            }
        }

        c.close();
        return dependencyProper;
    }

    protected void printRemovalList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Macros -");
        for (int i : listBundle.itemVector.get(ActionMain.item.ID_Macro)) System.out.println(i);
    }

    protected void printMissingDependencyList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Macros -");
        for (ContentValues v : listBundle.missingDependencyVector.get(ActionMain.item.ID_Macro)) {
            System.out.println(v.getAsString(ActionItem.SQL.COLUMN_NAME_WORD) + "(" + v.getAsInteger(ActionItem.SQL._ID) + ")");
        }
    }
}
