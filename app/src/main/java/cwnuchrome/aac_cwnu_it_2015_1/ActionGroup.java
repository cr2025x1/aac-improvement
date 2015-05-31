package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
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
                        SQL.COLUMN_NAME_ENTRY_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.TEXT_TYPE + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM + SQL.TEXT_TYPE +
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
                        SQL.COLUMN_NAME_ENTRY_ID + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PARENT_ID + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_PRIORITY + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_WORD + SQL.COMMA_SEP +
                        SQL.COLUMN_NAME_STEM +
                        ") " +
                        "SELECT " +
                        "1" + SQL.COMMA_SEP +
                        "1" + SQL.COMMA_SEP +
                        "1" + SQL.COMMA_SEP +
                        "0" + SQL.COMMA_SEP +
                        "'" + SQL.ROOT_DEFAULT_NAME + "'" + SQL.COMMA_SEP +
                        "'" + SQL.ROOT_DEFAULT_NAME + "'" +
                        " WHERE NOT EXISTS (SELECT 1 FROM " +
                        TABLE_NAME + " WHERE " +
                        SQL._ID + " = 1 AND " +
                        SQL.COLUMN_NAME_ENTRY_ID + " = 1 AND " +
                        SQL.COLUMN_NAME_PARENT_ID + " = 1" +
                        ");"
        );
    }


    /**
     * Created by Chrome on 5/8/15.
     */
    public static class ActionGroupButton extends Button {
        long groupID;

        public ActionGroupButton(Context context, onClickClass onClickObj, AACGroupContainer container) {
            super(context, onClickObj, container);
        }

        public static class onClickClass extends Button.onClickClass {
            String message;

            public onClickClass(Context context) {super(context); }

            public void onClick(View v) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                container.exploreGroup(((ActionGroupButton)button).getGroupID());
            }
            public void init(ContentValues values) {
                message = "그룹 " + values.get(ActionWord.SQL.COLUMN_NAME_WORD) + "," + values.get(ActionWord.SQL.COLUMN_NAME_PRIORITY);
            }
        }

        public void setGroupID (long groupID) {
            this.groupID = groupID;
        }
        public long getGroupID() {
            return groupID;
        }

        public void init(ContentValues values) {
            super.init(values);
            this.setText("그룹 " + values.getAsString(SQL.COLUMN_NAME_WORD));
            this.setGroupID(values.getAsLong(SQL._ID));
            this.onClickObj.setContainer(container);
            this.onClickObj.setButton(this);
            this.onClickObj.init(values);
        }

    }
}
