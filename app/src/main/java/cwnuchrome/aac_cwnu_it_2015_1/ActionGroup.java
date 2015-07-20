package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * Created by Chrome on 5/5/15.
 *
 * 그룹 클래스.
 */

public class ActionGroup extends ActionMultiWord {

    public ActionGroup() {
        super(ActionMain.item.ID_Group, "Group");

        reservedID = new int[] {1};
        TABLE_NAME = "LocalGroup";
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
    public void initTable(SQLiteDatabase db) {
        HashMap<Long, Long> map = new HashMap<>();
        map.put(1l, 1l);

        Cursor c = db.rawQuery("SELECT " + SQL._ID + " FROM " + TABLE_NAME +
                " WHERE " + SQL._ID + "=1 AND " +
                SQL.COLUMN_NAME_PARENT_ID + "=1",
                null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            c.close();
            return;
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put(SQL._ID, 1);
        values.put(SQL.COLUMN_NAME_PARENT_ID, 1);
        values.put(SQL.COLUMN_NAME_PRIORITY, 0);
        values.put(SQL.COLUMN_NAME_WORD, AACGroupContainerPreferences.ROOT_GROUP_NAME);
        values.put(SQL.COLUMN_NAME_STEM, AACGroupContainerPreferences.ROOT_GROUP_NAME);
        values.put(SQL.COLUMN_NAME_WORDCHAIN, "|:1:|");
        values.put(SQL.COLUMN_NAME_PICTURE, R.drawable.btn_default);
        values.put(SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        values.put(SQL.COLUMN_NAME_ELEMENT_ID_TAG, create_element_id_count_tag(map));
        db.insert(TABLE_NAME, null, values);

        ActionMain.update_db_collection_count(db, 1, 1); // 워드체인의 길이가 1이므로 문서 길이 1임. 워드체인이 더 길어지면 변경 필요.
    }

    // TODO: 그룹 추가 기능 넣기... 아직도 안 넣고 있었다니!

    /**
     * Created by Chrome on 5/8/15.
     */
    public static class Button extends ActionItem.Button {
//        long groupID;

        public Button(Context context, onClickClass onClickObj, AACGroupContainer container) {
            super(context, onClickObj, container);
        }

        public void init(ContentValues values) {
            super.init(values);
            this.setText("그룹 " + values.getAsString(SQL.COLUMN_NAME_WORD));
            this.onClickObj.init(values);
        }

    }

    protected void addToRemovalList(Context context, AACGroupContainer.RemovalListBundle listBundle, int id) {
        ActionMain actionMain = ActionMain.getInstance();
        SQLiteDatabase db = actionMain.getDB();
        String[] projection = new String[] { ActionItem.SQL._ID };

        listBundle.add(ActionMain.item.ID_Group, id);

        Cursor c;
        int c_count;
        int c_col;
        String whereClause = ActionItem.SQL.COLUMN_NAME_PARENT_ID  + " = " + id;

        for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) {
            c = db.query(
                    actionMain.itemChain[i].TABLE_NAME, // The table to query
                    projection, // The columns to return
                    whereClause, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
            );
            c.moveToFirst();

            c_count = c.getCount();
            c_col = c.getColumnIndexOrThrow(ActionItem.SQL._ID);
            if (c_count > 0) {
                for (int j = 0; j < c_count; j++) {
                    int j_id = c.getInt(c_col);
                    actionMain.itemChain[i].addToRemovalList(context, listBundle, j_id); // 재귀 호출이 될 수 있음. (만일 i = ActionMain.item.ID_Group이면 재귀)
                    c.moveToNext();
                }
            }

            c.close();
        }
    }

    @Override
    protected boolean verifyAndCorrectDependencyRemoval(Context context, AACGroupContainer.RemovalListBundle listBundle) {
        boolean result = true;
        ActionMain actionMain = ActionMain.getInstance();
        SQLiteDatabase db = actionMain.getDB();

        int[] endIndex = new int[ActionMain.item.ITEM_COUNT];

        while (true) {
            boolean loopResult = super.verifyAndCorrectDependencyRemoval(context, listBundle);
            result = result && loopResult;
            if (loopResult) break;

            for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) {
                if (i == ActionMain.item.ID_Group) endIndex[i] = listBundle.itemVector.get(i).size() + 1;
                else endIndex[i] = listBundle.itemVector.get(i).size();
            }

            int cat_id = 0;
            for (ArrayList<Integer> l : listBundle.missingDependencyVector) {
                for (int i : l) {
                    actionMain.itemChain[cat_id].addToRemovalList(context, listBundle, i);
                }
                l.clear();
                cat_id++;
            }

            for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) {
                ArrayList<Long> list = listBundle.itemVector.get(i);
                if (endIndex[i] < list.size()) {
                    ListIterator<Long> lI = list.listIterator(endIndex[i]);
                    while (lI.hasNext()) {
                        long j = lI.next();

                        Cursor c = db.query(
                                actionMain.itemChain[i].TABLE_NAME,
                                new String[] {SQL.COLUMN_NAME_WORD},
                                SQL._ID + "=" + j,
                                null,
                                null,
                                null,
                                null
                                );
                        c.moveToFirst();

                        ContentValues values = new ContentValues();
                        values.put(SQL._ID, j);
                        values.put(SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_WORD)));
                        listBundle.missingDependencyPrintVector.get(i).add(values);
                        c.close();
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected void printRemovalList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Groups -");
        super.printRemovalList(listBundle);
    }

    @Override
    protected void printMissingDependencyList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Groups -");
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
        values.put(SQL.COLUMN_NAME_PARENT_ID, parentID);
        values.put(SQL.COLUMN_NAME_PRIORITY, priority);
        values.put(SQL.COLUMN_NAME_WORD, word);
        values.put(SQL.COLUMN_NAME_STEM, stem);
        values.put(SQL.COLUMN_NAME_WORDCHAIN, create_wordchain(wordIDs));
        values.put(SQL.COLUMN_NAME_PICTURE, picture);
        values.put(SQL.COLUMN_NAME_PICTURE_IS_PRESET, is_picture_preset ? 1 : 0);

        values.put(SQL.COLUMN_NAME_ELEMENT_ID_TAG, create_element_id_count_tag(map));
        values.put(SQL.ATTACHMENT_ID_MAP, map_carrier.attach(map));

        return raw_add(values);
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

    public onClickClass allocOCC(Context context, AACGroupContainer container) {
        return new onClickClass(context, container);
    }

    public static class onClickClass extends ActionItem.onClickClass {

        public onClickClass(Context context, AACGroupContainer container) {
            super(context, container);
            itemCategoryID = ActionMain.item.ID_Group;
        }

        public void onClick(View v) {
            if (!isOnline) return;

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            container.exploreGroup(itemID);
        }
        public void init(ContentValues values) {
            super.init(values);
            message = "그룹 " + values.get(ActionWord.SQL.COLUMN_NAME_WORD) + "," + values.get(ActionWord.SQL.COLUMN_NAME_PRIORITY);
        }
    }
}
