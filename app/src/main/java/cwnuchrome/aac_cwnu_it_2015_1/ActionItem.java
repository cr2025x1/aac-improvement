package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Chrome on 5/2/15.
 *
 * 모든 사용자 메뉴 그룹의 요소들은 ActionItem 추상 클래스를 구현해서 따라야 한다.
 */
public abstract class ActionItem implements Serializable {

    protected int itemClassID;
    protected int[] reservedID;

    protected ActionItem(int itemID, String className) {
        this.itemClassID = itemID;
        this.CLASS_NAME = className;
    }

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
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        db.execSQL(SQL_DELETE_ENTRIES);
    }


    interface SQL extends BaseColumns {
        String INTEGER_PRIMARY_KEY = " INTEGER PRIMARY KEY";
        String NOT_NULL = " NOT NULL";

        String TEXT_TYPE = " TEXT";
        String INTEGER_TYPE = " INTEGER";
        String REAL_TYPE = " REAL";
        String BLOB_TYPE = " BLOB";
        String COMMA_SEP = ", ";

        String COLUMN_NAME_PARENT_ID = "parent_id";
        String COLUMN_NAME_PRIORITY = "priority";
        String COLUMN_NAME_WORD = "word";
        String COLUMN_NAME_STEM = "stem";
        String COLUMN_NAME_PICTURE = "picture";
        String COLUMN_NAME_PICTURE_IS_PRESET = "picture_is_preset";
    }

    String TABLE_NAME;
    String CLASS_NAME;
    String SQL_CREATE_ENTRIES;
    String SQL_DELETE_ENTRIES;

    public String getTableName() { return TABLE_NAME; }

    protected long exists(String word) {
        // 워드 쿼리
        Cursor c = ActionMain.getInstance().getDB().query(
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

    protected long exists(long id) {
        // 워드 쿼리
        Cursor c = ActionMain.getInstance().getDB().query(
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

    public long raw_add(ContentValues values) {
        ActionMain actionMain = ActionMain.getInstance();
        values = ActionMain.getInstance().process_external_images(values);
        long id = actionMain.getDB().insert(TABLE_NAME, null, values);

        // 워드의 불가시화로 인해 공통 코드 부분에서 콜렉션 업데이트를 하면 안 되게 됐음.
//        if (id != -1) {
//            actionMain.update_db_collection_count(1, 1);
//        }

        return id;
   }

    public long find_id_by_word(String word) {
        long id;
        SQLiteDatabase db = ActionMain.getInstance().getDB();

        Cursor c = db.query(
                TABLE_NAME,
                new String[]{ActionWord.SQL._ID},
                ActionWord.SQL.COLUMN_NAME_WORD + "='" + word + "'",
                null,
                null,
                null,
                null
        );
        c.moveToFirst();

        if (c.getCount() > 0) id = c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL._ID));
        else id = -1;
        c.close();

        return id;
    }

    public long updateWithIDs(Context context, ContentValues values, long[] idArray) {
        SQLiteDatabase db = ActionMain.getInstance().getDB();
        StringBuilder sb = new StringBuilder();
        for (long i : idArray) {
            sb.append(SQL._ID);
            sb.append("=");
            sb.append(i);
            sb.append(" OR ");
        }
        sb.delete(sb.length() - 4, sb.length() - 1); // 맨 마지막 " OR "를 삭제
        String whereClause = sb.toString();

        if (values.containsKey(SQL.COLUMN_NAME_PICTURE) && values.containsKey(SQL.COLUMN_NAME_PICTURE_IS_PRESET)) {
            values = ActionMain.getInstance().process_external_images(values);

            sb.append(" AND ");
            sb.append(SQL.COLUMN_NAME_PICTURE);
            sb.append("!='");
            sb.append(values.getAsString(SQL.COLUMN_NAME_PICTURE));
            sb.append("'");
            String pictureWhereClause = sb.toString();

            removeExclusiveImage(context, pictureWhereClause);
        }

        int t = db.update(TABLE_NAME, values, whereClause, null);
        System.out.println("Result value = " + t + " Table = " + TABLE_NAME);
        return t;
    }

    protected int removeExclusiveImage(Context context, String whereClause) {
        SQLiteDatabase db = ActionMain.getInstance().getDB();
        String pictureWhereClause = whereClause + " AND "
                + SQL.COLUMN_NAME_PICTURE_IS_PRESET + "=0";

        ActionMain actionMain = ActionMain.getInstance();
        StringBuilder sb = new StringBuilder("SELECT DISTINCT ");
        sb.append(SQL.COLUMN_NAME_PICTURE);
        sb.append(" FROM ");
        sb.append(TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(pictureWhereClause);
        for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) {
            sb.append(" EXCEPT SELECT DISTINCT ");
            sb.append(SQL.COLUMN_NAME_PICTURE);
            sb.append(" FROM ");
            sb.append(actionMain.itemChain[i].TABLE_NAME);
            if (i == itemClassID) {
                sb.append(" WHERE NOT (");
                sb.append(pictureWhereClause);
                sb.append(")");
            }
        }

        Cursor c = db.rawQuery(sb.toString(), null);
        c.moveToFirst();

        int count = c.getCount();

        if (count > 0) {
            int d_col_i = c.getColumnIndexOrThrow(SQL.COLUMN_NAME_PICTURE);

            System.out.println("*** Exclusive image file list in the range ***");
            for (int i = 0; i < count; i++) {
                String fileName = c.getString(d_col_i);
                System.out.println(fileName);
            }
            System.out.println("*** End of the list ***");

            String path = context.getFilesDir() + "/" + AACGroupContainerPreferences.USER_IMAGE_DIRECTORY_NAME + "/";
            for (int i = 0; i < count; i++) {

                String fileName = c.getString(d_col_i);
                File file = new File(path, fileName);
                if (!file.delete()) {
                    System.out.println("Failed to delete \"" + fileName + "\".");
                }

                c.moveToNext();
            }
        }
        else System.out.println("*** No image file is exclusive ***");

        c.close();
        return count;
    }

    public boolean removeWithID(Context context, long id) {
        if (exists(id) == -1) return false;

        removeExclusiveImage(context, SQL._ID + "=" + id);

        ActionMain actionMain = ActionMain.getInstance();
        actionMain.getDB().delete(
                TABLE_NAME,
                SQL._ID + " = " + id,
                null
        );

//        actionMain.update_db_collection_count(-1, -1); // 워드의 불가시화로 인해 더 이상 공통 코드에서 콜렉션 카운트 업그레이드가 있으면 안 됨.

        return true;
    }

    abstract protected void addToRemovalList(Context context, AACGroupContainer.RemovalListBundle listBundle, int id);
    abstract protected boolean verifyAndCorrectDependencyRemoval(Context context, AACGroupContainer.RemovalListBundle listBundle);

    protected void printRemovalList(AACGroupContainer.RemovalListBundle listBundle) {
        for (long i : listBundle.itemVector.get(itemClassID)) System.out.println(i);
    }

    protected void printMissingDependencyList(AACGroupContainer.RemovalListBundle listBundle) {
        for (ContentValues v : listBundle.missingDependencyPrintVector.get(itemClassID)) {
            System.out.println(v.getAsString(SQL.COLUMN_NAME_WORD) + "(" + v.getAsInteger(SQL._ID) + ")");
        }
    }







    public abstract static class Button extends android.widget.Button {
        protected onClickClass onClickObj;
//        protected long priority;
        int image_half_height;
//        AACGroupContainer container;
        Context context;

        public Button(Context context, onClickClass onClickObj, AACGroupContainer container) {
            super(context);
            this.context = context;
//            this.container = container;
            this.onClickObj = onClickObj;
//            this.onClickObj.setContainer(this.container);
            this.setOnClickListener(this.onClickObj);
//            this.setId(View.generateViewId());
        }

        //        public long getPriority() {return priority; }
//        public void setPriority(long value) {priority = value; }

        public static class itemComparator implements Comparator<View> {
            @Override
            public int compare(View lhs, View rhs) {
                Button lhs_btn = (Button)lhs.findViewById(R.id.aac_item_button_id);
                Button rhs_btn = (Button)rhs.findViewById(R.id.aac_item_button_id);

                return lhs_btn.onClickObj.priority > rhs_btn.onClickObj.priority ? -1 : lhs_btn.onClickObj.priority < rhs_btn.onClickObj.priority ? 1 : 0;
            }
        }

        public void init(ContentValues values) {
            // TODO: Make this use XMLs.

//            priority = values.getAsLong(SQL.COLUMN_NAME_PRIORITY);

            Drawable d;
            if (values.getAsInteger(SQL.COLUMN_NAME_PICTURE_IS_PRESET) == 1)
                d = context.getResources().getDrawable(values.getAsInteger(SQL.COLUMN_NAME_PICTURE));
//                d = context.getResources().getDrawable(values.getAsInteger(SQL.COLUMN_NAME_PICTURE), context.getTheme()); // API 21 이상 필요
            else {
                d = Drawable.createFromPath(context.getFilesDir() + "/" + AACGroupContainerPreferences.USER_IMAGE_DIRECTORY_NAME + "/" +
                        values.getAsString(SQL.COLUMN_NAME_PICTURE));
            }

            // 그림 리사이징
            d = DrawableResizer.fitToAreaByDP(
                    d,
                    context,
                    AACGroupContainerPreferences.IMAGE_WIDTH_DP,
                    AACGroupContainerPreferences.IMAGE_HEIGHT_DP
            );

            this.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
            int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            this.setMaxWidth((int)DisplayUnitConverter.convertDpToPixel((float)AACGroupContainerPreferences.IMAGE_WIDTH_DP, context) + padding * 2);

            image_half_height = d.getIntrinsicHeight() / 2;
            this.setLayoutParams(makeLayoutParam());

            // 패딩 설정
            // TODO: 여기 상수가 왜 남아 있지?!?!?!?!?!?!?!?!?!?!?
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

//        public void setContainer(AACGroupContainer container) {
//            this.container = container;
//        }

    }

    public abstract onClickClass allocOCC(Context context, AACGroupContainer container);

    protected abstract static class onClickClass implements View.OnClickListener {
        protected String message;
        protected Context context;
        protected AACGroupContainer container;
//        protected Button button;
        protected String phonetic;
        protected int itemCategoryID;
        protected long itemID;
        protected boolean isOnline;

        protected double rank;
        protected long priority;

        public abstract void onClick(View v);
        public onClickClass(Context context, AACGroupContainer container) {
            this.context = context;
            isOnline = true;
            this.container = container;
        }

        public void init(ContentValues values) {
            itemID = values.getAsInteger(SQL._ID);
        }
//        public void setContainer (AACGroupContainer container) {
//            this.container = container;
//        }
//        public void setButton (Button button) {
//            this.button = button;
//        }

        public boolean isOnline() {
            return isOnline;
        }

        public void toogleOnline() {
            isOnline = !isOnline;
        }

        public int getItemCategoryID() {
            return itemCategoryID;
        }
        public long getItemID() {
            return itemID;
        }
    }

    // 주어진 쿼리 해시맵에 대해 이 카테고리 아이템의 레코드 전체의 평가값이 담긴 해시맵을 반환.
    @NonNull
    public abstract HashMap<Long, Double> evaluate_by_query_map(
            @NonNull SQLiteDatabase db,
            @NonNull HashMap<Long, QueryWordInfo> queryMap,
            @NonNull HashMap<Long, Double> eval_map,
            long entire_collection_count,
            long average_document_length);

    // 이 아이템의 테이블의 모든 행에 대응하는 1:1 대응하는 키를 모두 가지는 해쉬맵을 만들어 반환한다.
    @NonNull protected HashMap<Long, Double> alloc_evaluation_map(SQLiteDatabase db) {
        HashMap<Long, Double> eval_map = new HashMap<>();

        Cursor entire_item_cursor = db.query(
                TABLE_NAME,
                new String[] {ActionItem.SQL._ID},
                null,
                null,
                null,
                null,
                null
        );
        entire_item_cursor.moveToFirst();
        int colNum = entire_item_cursor.getColumnIndexOrThrow(ActionItem.SQL._ID);

        for (int i = 0; i < entire_item_cursor.getCount(); i++) {
            eval_map.put(
                    entire_item_cursor.getLong(colNum),
                    0d
            );
            entire_item_cursor.moveToNext();
        }
        entire_item_cursor.close();

        return eval_map;
    }

    @NonNull public abstract HashMap<Long, Long> get_id_count_map(long id);
}
