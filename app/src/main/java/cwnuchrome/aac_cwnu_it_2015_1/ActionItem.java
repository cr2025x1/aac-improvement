package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Chrome on 5/2/15.
 */
public abstract class ActionItem implements Serializable {
    /*
        모든 사용자 메뉴 그룹의 요소들은 ActionItem 추상 클래스를 구현해서 따라야 한다.
     */

    public abstract int execute(); // 버튼 클릭 시 수행되는 메소드
    public abstract int init(ContentValues values); // 초기화 메소드 (중복 호출 가능)

    public void createTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void clearTable(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
    }
    public void initTable(SQLiteDatabase db) {

    }
    public void deleteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
    }


    interface SQL extends BaseColumns {
        static final String TEXT_TYPE = " TEXT";
        static final String COMMA_SEP = ",";

        static final String COLUMN_NAME_ENTRY_ID = "entryid";
        static final String COLUMN_NAME_PARENT_ID = "parent_id";
        static final String COLUMN_NAME_PRIORITY = "priority";
        static final String COLUMN_NAME_WORD = "word";
        static final String COLUMN_NAME_STEM = "stem";
    }

    String TABLE_NAME;
    String SQL_CREATE_ENTRIES;
    String SQL_DELETE_ENTRIES;

    protected boolean exists(SQLiteDatabase db, String word) {
        // 워드 쿼리
        Cursor c = db.query(
                TABLE_NAME, // The table to query
                new String[] {SQL._ID}, // The columns to return
                SQL.COLUMN_NAME_WORD + " = '" + word + "'", // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null // The sort order
        );
        c.moveToFirst();
        long cursorCount = c.getCount();

        if (cursorCount > 0) return true;
        return false;
    }

    public boolean add(SQLiteDatabase db, ContentValues values) {
        db.insert(TABLE_NAME, null, values);
        return true;
    }

    public boolean remove(SQLiteDatabase db, String word) {
        if (!exists(db, word)) return false;

        db.delete(
                TABLE_NAME,
                SQL.COLUMN_NAME_WORD + " = '" + word + "'",
                null
        );
        return true;
    }

    /**
     * Created by Chrome on 5/8/15.
     */
    public abstract static class Button extends android.widget.Button {
        protected onClickClass onClickObj;
        protected long priority;
        AACGroupContainer container;

        public Button(Context context, onClickClass onClickObj, AACGroupContainer container) {
            super(context);
            this.container = container;
            this.onClickObj = onClickObj;
            this.onClickObj.setContainer(this.container);
            this.setOnClickListener(this.onClickObj);
            this.setId(View.generateViewId());
        }

        protected abstract static class onClickClass implements OnClickListener {
            Context context;
            AACGroupContainer container;
            Button button;
            public abstract void onClick(View v);
            public onClickClass(Context context) {
                this.context = context;
            }
            public abstract void init(ContentValues values);
            public void setContainer (AACGroupContainer container) {
                this.container = container;
            }
            public void setButton (Button button) {
                this.button = button;
            }
        }

        public long getPriority() {return priority; }
        public void setPriority(long value) {priority = value; }

        public static class itemComparator implements Comparator<Button> {
            @Override
            public int compare(Button lhs, Button rhs) {
                return lhs.priority > rhs.priority ? -1 : lhs.priority < rhs.priority ? 1 : 0;
            }
        }

        public void init(ContentValues values) {
            priority = values.getAsLong(SQL.COLUMN_NAME_PRIORITY);
            this.setLayoutParams(makeLayoutParam());
        }

        public LinearLayout.LayoutParams makeLayoutParam() {
            LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            LP.gravity = Gravity.CENTER_HORIZONTAL;

            return LP;
        }

        public void setContainer(AACGroupContainer container) {
            this.container = container;
        }
    }
}
