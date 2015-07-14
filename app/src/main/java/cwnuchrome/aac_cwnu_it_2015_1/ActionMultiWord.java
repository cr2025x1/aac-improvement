package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Chrome on 7/7/15.
 *
 * Word를 여러 개 가질 수 있는 멀티워드 클래스 (그룹과 매크로 등등)
 * 의존 클래스: ActionItem, ActionWord, AACGroupContainer
 */
public abstract class ActionMultiWord extends ActionItem {

    protected ActionMultiWord(int itemID, String className) {
        super(itemID, className);
    }

    interface SQL extends ActionItem.SQL {
        String COLUMN_NAME_WORDCHAIN = "wordchain";
        String COLUMN_NAME_ELEMENT_ID_TAG = "element_id_tag";
    }

    @Override
    public long raw_add(ContentValues values) {
        long id = super.raw_add(values);
        final ActionWord actionWord = (ActionWord)ActionMain.getInstance().itemChain[ActionMain.item.ID_Word];
        if (id != -1) {
            parseWordChain(values.getAsString(SQL.COLUMN_NAME_WORDCHAIN), new onParseCommand() {
                @Override
                public void onParse(long itemID) {
                    actionWord.update_reference_count(itemID, 1);
                }
            });
        }

        return id;
    }

    // 의존성 검사... 이것 때문에 단순하게 생각했던 아이템 제거에서 지옥문이 열렸다.
    protected boolean verifyAndCorrectDependencyRemoval(Context context, AACGroupContainer.RemovalListBundle listBundle) {
        ActionMain actionMain = ActionMain.getInstance();
        ArrayList<Long> wordList = listBundle.itemVector.get(ActionMain.item.ID_Word);
        ArrayList<Long> itemList = listBundle.itemVector.get(itemClassID);
        ArrayList<ContentValues> itemMissingPrintList = listBundle.missingDependencyPrintVector.get(itemClassID);
        ArrayList<Integer> itemMissingList = listBundle.missingDependencyVector.get(itemClassID);

        if (wordList.size() == 0) return true;

        /* 지울 단어들에 대해 의존성을 가지는 멀티워드 아이템들을 찾기 위한 쿼리문의 작성 */

        Iterator<Long> i = wordList.iterator();
        long id = i.next();

        final String OR = " OR ";
        final String LIKE_AND_HEAD = " LIKE '%:";
        final String TAIL = ":%'";

        StringBuilder qBuilder = new StringBuilder("(");
        qBuilder.append(SQL.COLUMN_NAME_WORDCHAIN);
        qBuilder.append(LIKE_AND_HEAD);
        qBuilder.append(id);
        qBuilder.append(TAIL);

        // 매 단어마다 조건문 확장
        while (i.hasNext()) {
            id = i.next();
            qBuilder.append(OR);
            qBuilder.append(SQL.COLUMN_NAME_WORDCHAIN);
            qBuilder.append(LIKE_AND_HEAD);
            qBuilder.append(id);
            qBuilder.append(TAIL);
        }
        qBuilder.append(")");

        // 예약 ID에 대한 조건문 확장
        if (reservedID != null) {
            qBuilder.append(" AND (");
            for (int j : reservedID) {
                qBuilder.append(SQL._ID);
                qBuilder.append("!=");
                qBuilder.append(j);
                qBuilder.append(" AND ");
            }
            qBuilder.delete(qBuilder.length() - 5, qBuilder.length() - 1);
            qBuilder.append(")");
        }

        String whereClause = qBuilder.toString(); // 완성된 조건문을 String으로 변환
        String sortOrder = SQL._ID + " ASC"; // 이후의 알고리즘을 위해 정렬 순서는 ID 기준 오름차순
        String projection[] = { SQL._ID, SQL.COLUMN_NAME_WORD };

        Cursor c;
        int c_count;
        int c_id_col;
        SQLiteDatabase db = actionMain.getDB();
        c = db.query( // 삭제 대상 워드에 대한 의존성이 있는 모든 이 카테고리 아이템이 이 커서에 담김
                actionMain.itemChain[itemClassID].TABLE_NAME, // The table to query
                projection, // The columns to return
                whereClause, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                sortOrder // The sort order
        );
        c.moveToFirst();

            /* 삭제 대상으로 선택된 멀티워드 아이템들과 앞서 찾아낸 멀티워드 아이템 목록과의 대조 */
            /* - 앞서 찾아낸 멀티워드 아이템의 집합은 반드시 선택된 멀티워드 아이템 대상의 집합에 포함된 관계여야 한다. */

        c_count = c.getCount();
        c_id_col = c.getColumnIndexOrThrow(SQL._ID);
        int c_word_col = c.getColumnIndexOrThrow(SQL.COLUMN_NAME_WORD);

        boolean dependencyProper = true;

        if (c_count > 0) {
            int c_id = c.getInt(c_id_col);

            Collections.sort(itemList); // 선택된 멀티워드 아이템 목록도 오름차순으로 정렬

            boolean endOfCursor = false;

            // 선택된 아이템을 순차 검색하며 확인
            for (long j : itemList) {
                // 현재 아이템의 ID값이 커서의 현재 아이템의 ID값보다 크다면?
                while (c_id < j) {
                    // 커서에 있는 아이템이 itemList에 없다: 의존성 문제가 있는 아이템이므로 removeDepArray에 추가
                    ContentValues values = new ContentValues();
                    values.put(SQL.COLUMN_NAME_WORD, c.getString(c_word_col));
                    values.put(SQL._ID, c_id);
                    itemMissingPrintList.add(values);
                    itemMissingList.add(c_id);

                    // 커서가 맨 끝에 도달하지 않은 이상 커서를 뒤로 계속 넘긴다.
                    if (c.isLast()) {
                        endOfCursor = true;
                        break;
                    }

                    c.moveToNext();
                    c_id = c.getInt(c_id_col);
                }

                if (endOfCursor) break; // 커서가 맨 끝에 도달했으므로 더 이상 대조할 대상이 없음. 루프 종료.

                // 같은 ID값을 지니는 멀티워드 아이템을 선택 리스트에서 찾았음.
                if (c_id == j) {
                    // 의존성을 지니는 멀티워드 아이템이 제대로 선택 리스트에도 있음. 따라서 커서를 다음 항목으로 진행시킨 후, for 루프도 다음 회로 넘긴다.
                    if (c.isLast()) {
                        endOfCursor = true;
                        break;
                    }

                    c.moveToNext();
                    c_id = c.getInt(c_id_col);
                }

            }

            // 커서에 아직 여분이 남음 - 즉 선택된 리스트에 모든 의존성 있는 멀티워드 아이템이 포함되지 않았음을 의미.
            if (!endOfCursor) {
                dependencyProper = false;
                while (true) {
                    // 커서에 남은 멀티워드 아이템들은 모두 의존성이 있으나 선택된 리스트에 없는 아이템들이므로 removeDepArray에 넣는다.
                    ContentValues values = new ContentValues();
                    values.put(SQL.COLUMN_NAME_WORD, c.getString(c_word_col));
                    values.put(SQL._ID, c_id);
                    itemMissingPrintList.add(values);
                    itemMissingList.add(c_id);

                    if (c.isLast()) break;

                    c.moveToNext();
                    c_id = c.getInt(c_id_col);
                }
            }
        }

        c.close();
        return dependencyProper;
    }

    protected void parseWordChain(String wordChain, onParseCommand command) {
        StringBuilder buffer = new StringBuilder();
        boolean inChain = false;
        boolean inWord = false;
        int pos = 0;
        if (wordChain.charAt(pos++) == '|') inChain = true;
        for (;inChain;pos++) {
            if (wordChain.charAt(pos) == '|') {inChain = false; continue;}
            if (wordChain.charAt(pos) == ':') {
                inWord = !inWord;
                if (!inWord) {
                    command.onParse(Integer.parseInt(buffer.toString(), 10));
                    buffer.setLength(0);
                }
                continue;
            }
            buffer.append(wordChain.charAt(pos));
        }
    }

    protected static String create_wordchain(long wordIDs[]) {
        if (wordIDs == null || wordIDs.length == 0) return null;

        StringBuilder wordchain = new StringBuilder("|");

        for (long l : wordIDs) {
            wordchain.append(":");
            wordchain.append(l);
            wordchain.append(":");
        }

        wordchain.append("|");

        return wordchain.toString();
    }

    protected static HashMap<Long, Long> create_element_id_count_map(long wordIDs[]) {
        HashMap<Long, Long> map = new HashMap<>();

        for (long l : wordIDs) {
            if (map.containsKey(l)) map.put(l, map.get(l) + 1);
            else map.put(l, 1l);
        }

        return map;
    }

    protected static String create_element_id_count_tag(HashMap<Long, Long> map) {
        StringBuilder s = new StringBuilder("|");

        for (Map.Entry<Long, Long> e : map.entrySet()) {
            s.append(":");
            s.append(e.getKey());
            s.append(",");
            s.append(e.getValue());
            s.append(":");
        }

        s.append("|");
        return s.toString();
    }

    @NonNull public static HashMap<Long, Long> parse_element_id_count_tag(String id_tag) {
        HashMap<Long, Long> map = new HashMap<>();
        final String syntax_error = "ID Tag syntax error.";

        StringBuilder buffer = new StringBuilder();
        boolean inChain = true;
        boolean inEntry = false;
        int pos = 0;
        long key = 0;
        if (id_tag.charAt(pos++) != '|') throw new IllegalArgumentException(syntax_error);
        while (id_tag.length() > pos) {
            char posChar = id_tag.charAt(pos);
            if (posChar == '|') {inChain = false; break;}
            if (posChar == ':') {
                inEntry = !inEntry;

                if (!inEntry) {
                    // 값 파싱
                    try {
                        map.put(key, Long.parseLong(buffer.toString()));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(syntax_error);
                    }

                    buffer.setLength(0);
                }

                pos++;
                continue;
            }
            if (posChar == ',') {
                // 키 파싱
                try {
                    key = Long.parseLong(buffer.toString());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(syntax_error);
                }

                buffer.setLength(0);

                pos++;
                continue;
            }

            buffer.append(posChar);
            pos++;
        }
        if (inChain) throw new IllegalArgumentException(syntax_error);

        return map;
    }

    public static void print_hashmap(@NonNull HashMap<Long, Long> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Long, Long> e : map.entrySet()) {
            sb.append('(');
            sb.append(e.getKey());
            sb.append(',');
            sb.append(e.getValue());
            sb.append(") ");
            System.out.print(sb.toString());
            sb.setLength(0);
        }
        System.out.println();
    }

    public interface onParseCommand {
        void onParse(long itemID);
    }

    @NonNull
    public HashMap<Long, Double> evaluate_by_query_map(
            @NonNull SQLiteDatabase db,
            @NonNull HashMap<Long, QueryWordInfo> queryMap,
            @NonNull HashMap<Long, Double> eval_map,
            long entire_collection_count
    ) {

        for (Map.Entry<Long, QueryWordInfo> entry : queryMap.entrySet()) {
            QueryWordInfo info = entry.getValue();
            long query_word_id = entry.getKey();

            Cursor c = db.query(
                    TABLE_NAME,
                    new String[] {SQL._ID, SQL.COLUMN_NAME_ELEMENT_ID_TAG},
                    SQL.COLUMN_NAME_WORDCHAIN + " LIKE '%:" + query_word_id + ":%'",
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();

            int multiword_id_col = c.getColumnIndexOrThrow(SQL._ID);
            int id_tag_col = c.getColumnIndexOrThrow(SQL.COLUMN_NAME_ELEMENT_ID_TAG);
            for (int i = 0; i < c.getCount(); i++) {
                HashMap<Long, Long> map = parse_element_id_count_tag(c.getString(id_tag_col));
                double k = AACGroupContainerPreferences.RANKING_FUNCTION_CONSTANT_K;
                long ref_count = map.get(query_word_id);

                double eval = info.count * (k + 1) * ref_count / (ref_count + k) * Math.log(entire_collection_count + 1 / info.ref_count);
                long id = c.getLong(multiword_id_col);
                eval_map.put(id, eval_map.get(id) + eval);
                c.moveToNext();
            }
            c.close();
        }

        return eval_map;
    }

}
