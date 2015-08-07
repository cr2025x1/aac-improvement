package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import net.vivin.GenericTree;
import net.vivin.GenericTreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Vector;

/**
 * Created by Chrome on 5/5/15.
 *
 * 그룹 클래스.
 */

public class ActionGroup extends ActionMultiWord {

    public ActionGroup() {
        super(ActionMain.item.ID_Group, "Group", false);

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
                        SQL.COLUMN_NAME_ELEMENT_ID_TAG + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_IS_REFINED + SQL.INTEGER_TYPE +
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
        values.put(SQL.COLUMN_NAME_IS_REFINED, 0);
        db.insert(TABLE_NAME, null, values);

        ActionMain.update_db_collection_count(db, 1, 1); // 워드체인의 길이가 1이므로 문서 길이 1임. 워드체인이 더 길어지면 변경 필요.
    }

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

    protected void addToRemovalList(Context context, AACGroupContainer.RemovalListBundle listBundle, long id) {
        read_lock.lock();
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
        read_lock.unlock();
    }

    @Override
    protected boolean verifyAndCorrectDependencyRemoval(Context context, AACGroupContainer.RemovalListBundle listBundle) {
        read_lock.lock();

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
            for (ArrayList<Long> l : listBundle.missingDependencyVector) {
                for (long i : l) {
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

        read_lock.unlock();

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
        write_lock.lock();
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

        write_lock.unlock();
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

    long add(
            long parentID,
            int priority,
            String word,
            int picture
    ) {
        ActionWord actionWord = (ActionWord)actionMain.itemChain[ActionMain.item.ID_Word];
        return add(parentID, priority, word, word, actionWord.add_multi(ActionMain.tokenize(word)), Integer.toString(picture), true);
    }

    long add(
            long parentID,
            int priority,
            String word,
            String picture
    ) {
        ActionWord actionWord = (ActionWord)actionMain.itemChain[ActionMain.item.ID_Word];
        return add(parentID, priority, word, word, actionWord.add_multi(ActionMain.tokenize(word)), picture, false);
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
//            container.explore_group(itemID);
            container.explore_group_MT(itemID, null);
        }
        public void init(ContentValues values) {
            super.init(values);
            message = "그룹 " + values.get(ActionWord.SQL.COLUMN_NAME_WORD) + "," + values.get(ActionWord.SQL.COLUMN_NAME_PRIORITY);
        }
    }

    @NonNull
    public Vector<ArrayList<Long>> expand_item_vector(long id, @Nullable Vector<ArrayList<Long>> itemVector) {
        read_lock.lock();

        ActionMain actionMain = ActionMain.getInstance();
        SQLiteDatabase db = actionMain.getDB();

        Vector<ArrayList<Long>> v = super.expand_item_vector(id, itemVector);

        for (ActionItem actionItem : actionMain.itemChain) {
            Cursor c = db.query(
                    actionItem.TABLE_NAME,
                    new String[] {SQL._ID},
                    SQL.COLUMN_NAME_PARENT_ID + "=" + id,
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();
            int col_id = c.getColumnIndexOrThrow(SQL._ID);

            for (int j = 0; j < c.getCount(); j++) {
                actionItem.expand_item_vector(c.getLong(col_id), v);
                c.moveToNext();
            }

            c.close();
        }

        read_lock.unlock();

        return v;
    }

    @NonNull
    public GenericTree<Info> get_sub_tree(long id, @Nullable Collection<Long> blacklist) {
        read_lock.lock();

        ActionMain actionMain = ActionMain.getInstance();
        SQLiteDatabase db = actionMain.getDB();

        String id_name;
        Cursor c = db.query(
                TABLE_NAME,
                new String[]{SQL.COLUMN_NAME_WORD},
                SQL._ID + "=" + id,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();

        if (c.getCount() > 0) {
            id_name = c.getString(c.getColumnIndexOrThrow(SQL.COLUMN_NAME_WORD));
            c.close();
        }
        else {
            c.close();
            throw new IllegalArgumentException("No group exists with that ID.");
        }

        String blacklist_clause = null;
        if (blacklist != null && blacklist.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (long l : blacklist) {
                sb
                        .append(SQL._ID)
                        .append("=")
                        .append(l)
                        .append(" OR ");
            }
            sb.setLength(sb.length() - 4);
            blacklist_clause = sb.toString();
        }

        GenericTreeNode<Info> root = get_sub_tree_node(new Info(id, id_name), blacklist_clause);
        GenericTree<Info> tree = new GenericTree<>();
        tree.setRoot(root);

        read_lock.unlock();

        return tree;
    }

    @NonNull
    private GenericTreeNode<Info> get_sub_tree_node(Info info, String blacklist_clause) {
        read_lock.lock();

        ActionMain actionMain = ActionMain.getInstance();
        SQLiteDatabase db = actionMain.getDB();

        GenericTreeNode<Info> root = new GenericTreeNode<>(info);

        StringBuilder wcb = new StringBuilder();
        wcb
                .append(SQL.COLUMN_NAME_PARENT_ID)
                .append("=")
                .append(info.id)
                .append(" AND ")
                .append(SQL._ID)
                .append("!=")
                .append(1); // 루트 그룹의 아이디

        if (blacklist_clause != null) {
            wcb
                    .append(" AND NOT (")
                    .append(blacklist_clause)
                    .append(")");
        }

        String whereClause = wcb.toString();
        Cursor c = db.query(
                TABLE_NAME,
                new String[]{SQL._ID, SQL.COLUMN_NAME_WORD},
                whereClause,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();
        int col_id = c.getColumnIndexOrThrow(SQL._ID);
        int col_name = c.getColumnIndexOrThrow(SQL.COLUMN_NAME_WORD);

        Info[] info_list = new Info[c.getCount()];
        for (int i = 0; i < c.getCount(); i++) {
            info_list[i] = new Info(c.getLong(col_id), c.getString(col_name));
            c.moveToNext();
        }
        c.close();

        for (Info i : info_list) {
            root.addChild(get_sub_tree_node(i, blacklist_clause));
        }

        read_lock.unlock();

        return root;
    }

    public class Info {
        final long id;
        final String name;

        public Info(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

}
