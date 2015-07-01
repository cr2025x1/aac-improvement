package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.provider.BaseColumns;
import android.speech.tts.TextToSpeech;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

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
        String TEXT_TYPE = " TEXT";
        String INTEGER_TYPE = " INTEGER";
        String REAL_TYPE = " REAL";
        String COMMA_SEP = ",";

        String COLUMN_NAME_PARENT_ID = "parent_id";
        String COLUMN_NAME_PRIORITY = "priority";
        String COLUMN_NAME_WORD = "word";
        String COLUMN_NAME_STEM = "stem";
        String COLUMN_NAME_PICTURE = "picture";
        String COLUMN_NAME_PICTURE_IS_PRESET = "picture_is_preset";

        String VARIABLE_CONTAINER = "AACGroupContainer";
    }

    String TABLE_NAME;
    String SQL_CREATE_ENTRIES;
    String SQL_DELETE_ENTRIES;

    public String getTableName() { return TABLE_NAME; }

    protected long exists(SQLiteDatabase db, String word) {
        // 워드 쿼리
        Cursor c = db.query(
                TABLE_NAME, // The table to query
                new String[]{SQL._ID}, // The columns to return
                SQL.COLUMN_NAME_WORD + " = '" + word + "'", // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null // The sort order
        );
        c.moveToFirst();
        long cursorCount = c.getCount();

        if (cursorCount > 0) {
            long result = c.getLong(c.getColumnIndexOrThrow(SQL._ID));
            c.close();
            return result;
        }
        c.close();
        return -1;
    }

    protected long existsWithID(SQLiteDatabase db, int id) {
        // 워드 쿼리
        Cursor c = db.query(
                TABLE_NAME, // The table to query
                new String[]{SQL._ID}, // The columns to return
                SQL._ID + " = " + id, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null // The sort order
        );
        c.moveToFirst();
        long cursorCount = c.getCount();

        c.close();
        if (cursorCount > 0) return id;
        return -1;
    }

    public long add(SQLiteDatabase db, ContentValues values) {
        return db.insert(TABLE_NAME, null, values);
    }

    public boolean remove(SQLiteDatabase db, String word) {
        if (exists(db, word) == -1) return false;

        db.delete(
                TABLE_NAME,
                SQL.COLUMN_NAME_WORD + " = '" + word + "'",
                null
        );
        return true;
    }

    public long updateWithIDs(SQLiteDatabase db, ContentValues values, int[] idArray) {
        StringBuilder sb = new StringBuilder();
        for (int i : idArray) {
            sb.append(SQL._ID);
            sb.append("=");
            sb.append(i);
            sb.append(" OR ");
        }
        sb.delete(sb.length() - 4, sb.length() - 1); // 맨 마지막 " OR "를 삭제

        System.out.println(sb.toString());

        int t = db.update(TABLE_NAME, values, sb.toString(), null);
        System.out.println("Result value = " + t + " Table = " + TABLE_NAME);
        return t;
//        return db.update(TABLE_NAME, values, sb.toString(), null);
    }


    public boolean removeWithID(SQLiteDatabase db, int id) {
        if (existsWithID(db, id) == -1) return false;

        db.delete(
                TABLE_NAME,
                SQL._ID + " = " + id,
                null
        );
        return true;
    }

    abstract protected void addToRemovalList(Context context, AACGroupContainer.RemovalListBundle listBundle, int id);
    abstract protected boolean checkDependencyRemoval(Context context, AACGroupContainer.RemovalListBundle listBundle);
    abstract protected void printRemovalList(AACGroupContainer.RemovalListBundle listBundle);
    abstract protected void printMissingDependencyList(AACGroupContainer.RemovalListBundle listBundle);

    /**
     * Created by Chrome on 5/8/15.
     */
    public abstract static class Button extends android.widget.Button {
        protected onClickClass onClickObj;
        protected long priority;
        int image_half_height;
        AACGroupContainer container;
        Context context;

        public Button(Context context, onClickClass onClickObj, AACGroupContainer container) {
            super(context);
            this.context = context;
            this.container = container;
            this.onClickObj = onClickObj;
            this.onClickObj.setContainer(this.container);
            this.setOnClickListener(this.onClickObj);
            this.setId(View.generateViewId());
        }

        protected abstract static class onClickClass implements OnClickListener {
            protected Context context;
            protected AACGroupContainer container;
            protected Button button;
            protected String phonetic;
            protected int itemCategoryID;
            protected int itemID;
            protected boolean isOnline;
            public abstract void onClick(View v);
            public onClickClass(Context context) {
                this.context = context;
                isOnline = true;
            }

            public void init(ContentValues values) {
                itemID = values.getAsInteger(SQL._ID);
            }
            public void setContainer (AACGroupContainer container) {
                this.container = container;
            }
            public void setButton (Button button) {
                this.button = button;
            }
            public boolean isOnline() {
                return isOnline;
            }
            public void toogleOnline() {
                isOnline = !isOnline;
            }
        }

        public long getPriority() {return priority; }
        public void setPriority(long value) {priority = value; }

        public static class itemComparator implements Comparator<View> {
            @Override
            public int compare(View lhs, View rhs) {
                Button lhs_btn = (ActionItem.Button)lhs.findViewById(R.id.aac_item_button_id);
                Button rhs_btn = (ActionItem.Button)rhs.findViewById(R.id.aac_item_button_id);

                return lhs_btn.priority > rhs_btn.priority ? -1 : lhs_btn.priority < rhs_btn.priority ? 1 : 0;
            }
        }

        public void init(ContentValues values) {
            // TODO: Make this use XMLs.

            priority = values.getAsLong(SQL.COLUMN_NAME_PRIORITY);

            Drawable d;
            if (values.getAsInteger(SQL.COLUMN_NAME_PICTURE_IS_PRESET) == 1)
                d = context.getResources().getDrawable(values.getAsInteger(SQL.COLUMN_NAME_PICTURE), context.getTheme());
            else {
                d = Drawable.createFromPath(container.userImageDirectoryPathPrefix +
                        values.getAsString(SQL.COLUMN_NAME_PICTURE));
            }

            // 그림 리사이징
            d = DrawableResizer.fitToAreaByDP(
                    d,
                    context,
                    AACGroupContainerPreferences.IMAGE_WIDTH_DP,
                    AACGroupContainerPreferences.IMAGE_HEIGHT_DP
            );

//            Drawable d = context.getResources().getDrawable(values.getAsInteger(SQL.COLUMN_NAME_PICTURE));
            // TODO: 그림 크기에 따라 왜곡이 발생할 것 같으니 이에 따른 코드 수정 필요.
            this.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
            int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            this.setMaxWidth((int)DisplayUnitConverter.convertDpToPixel((float)AACGroupContainerPreferences.IMAGE_WIDTH_DP, context) + padding * 2);

            image_half_height = d.getIntrinsicHeight() / 2;
            this.setLayoutParams(makeLayoutParam());

            // 패딩 설정
            int paddingX = (369 - d.getIntrinsicWidth()) / 2 + padding;
            int paddingY = (369 - d.getIntrinsicHeight()) / 2;
            this.setPadding(paddingX, paddingY, paddingX, paddingY);

            this.setBackgroundColor(0x00000000);
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
