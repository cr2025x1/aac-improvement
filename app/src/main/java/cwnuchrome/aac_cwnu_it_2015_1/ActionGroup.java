package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Chrome on 5/5/15.
 */
public class ActionGroup extends ActionItem {

    public ActionGroup() {
        TABLE_NAME = "LocalGroup";
        SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
        SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY," +
//                        SQL.COLUMN_NAME_ENTRY_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE + SQL.TEXT_TYPE +
                        " )";
    }

    public int init (ContentValues values) {
        return 0;
    }
    public int execute () {
        return 0;
    }

    interface SQL extends ActionItem.SQL {
        String ROOT_DEFAULT_NAME = "root";
    }
    
    public void initTable(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " +
                        TABLE_NAME + " (" +
                        SQL._ID + SQL.COMMA_SEP +
//                        SQL.COLUMN_NAME_ENTRY_ID + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PICTURE +
                        ") " +
                        "SELECT " +
                        "1" + SQL.COMMA_SEP +
//                        "1" + SQL.COMMA_SEP +
                        "1" + SQL.COMMA_SEP +
                        "0" + SQL.COMMA_SEP +
                        "'" + SQL.ROOT_DEFAULT_NAME + "'" + SQL.COMMA_SEP +
                        "'" + SQL.ROOT_DEFAULT_NAME + "'" + SQL.COMMA_SEP +
                        R.drawable.btn_default +
                        " WHERE NOT EXISTS (SELECT 1 FROM " +
                        TABLE_NAME + " WHERE " +
                        SQL._ID + " = 1 AND " +
//                        SQL.COLUMN_NAME_ENTRY_ID + " = 1 AND " +
                        SQL.COLUMN_NAME_PARENT_ID + " = 1" +
                        ");"
        );
    }


    /**
     * Created by Chrome on 5/8/15.
     */
    public static class Button extends ActionItem.Button {
//        long groupID;

        public Button(Context context, onClickClass onClickObj, AACGroupContainer container) {
            super(context, onClickObj, container);
        }

        public static class onClickClass extends ActionItem.Button.onClickClass {
            String message;

            public onClickClass(Context context) {
                super(context);
                itemCategoryID = ActionMain.item.ID_Group;
            }

            public void onClick(View v) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                container.exploreGroup(itemID);
            }
            public void init(ContentValues values) {
                super.init(values);
                message = "그룹 " + values.get(ActionWord.SQL.COLUMN_NAME_WORD) + "," + values.get(ActionWord.SQL.COLUMN_NAME_PRIORITY);
            }
        }

//        public void setGroupID (long groupID) {
//            this.groupID = groupID;
//        }
//        public long getGroupID() {
//            return groupID;
//        }

        public void init(ContentValues values) {
            super.init(values);
            this.setText("그룹 " + values.getAsString(SQL.COLUMN_NAME_WORD));
//            this.setGroupID(values.getAsLong(SQL._ID));
            this.onClickObj.setContainer(container);
            this.onClickObj.setButton(this);
            this.onClickObj.init(values);
        }

    }

    protected void addToRemovalList(Context context, AACGroupContainer.RemovalListBundle listBundle, int id) {
        ActionDBHelper actDBHelper = new ActionDBHelper(context);
        ActionMain actionMain = ActionMain.getInstance();
        String[] projection = new String[] { ActionItem.SQL._ID };

//        listBundle.add(id);
        listBundle.add(ActionMain.item.ID_Group, id);

        Cursor c;
        int c_count;
        int c_col;
        SQLiteDatabase db = actDBHelper.getWritableDatabase();
        String whereClause = ActionItem.SQL.COLUMN_NAME_PARENT_ID  + " = " + id;

        // 해당 그룹 내의 그룹 처리
        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, // The table to query
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
            for (int i = 0; i < c_count; i++) {
                int i_id = c.getInt(c_col);
                addToRemovalList(context, listBundle, i_id); // 재귀 호출
//                addToRemovalList(i_id); // 재귀 호출
                c.moveToNext();
            }
        }

        c.close();


        // 해당 그룹 내의 매크로 처리
        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Macro].TABLE_NAME, // The table to query
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
            for (int i = 0; i < c_count; i++) {
                int i_id = c.getInt(c_col);
//                addMacro(i_id);
                listBundle.add(ActionMain.item.ID_Macro, id);
                c.moveToNext();
            }
        }

        c.close();


        // 해당 그룹 내의 워드 처리
        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, // The table to query
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
            for (int i = 0; i < c_count; i++) {
                int i_id = c.getInt(c_col);
//                addWord(i_id);
                listBundle.add(ActionMain.item.ID_Word, id);
                c.moveToNext();
            }
        }

        c.close();
    }

    protected boolean checkDependencyRemoval(Context context, AACGroupContainer.RemovalListBundle listBundle) { return true; }

    protected void printRemovalList(AACGroupContainer.RemovalListBundle listBundle) {
        System.out.println("Groups -");
        for (int i : listBundle.itemVector.get(ActionMain.item.ID_Group)) System.out.println(i);
    }

    protected void printMissingDependencyList(AACGroupContainer.RemovalListBundle listBundle) {}
}
