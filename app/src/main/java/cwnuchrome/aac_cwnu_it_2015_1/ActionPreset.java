package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by Chrome on 5/9/15.
 *
 * 테이블을 리셋하고 프리셋을 생성하는 클래스
 */
public final class ActionPreset {
    private static ActionPreset ourInstance = new ActionPreset();
    public static ActionPreset getInstance() {
        return ourInstance;
    }

    ActionMain actionMain;

    private ActionPreset() {
        actionMain = ActionMain.getInstance();
    }

    public void revert_to_default(Context context) {
        delete_flag();
        actionMain.resetTables();
        ExternalImageProcessor.remove_all_images(context);
        mark_flag();
        recover_preset();
    }

    public void recover_preset() {
        ActionMain actionMain = ActionMain.getInstance();
        ActionWord actionWord = (ActionWord)actionMain.itemChain[ActionMain.item.ID_Word];
        ActionMacro actionMacro = (ActionMacro)actionMain.itemChain[ActionMain.item.ID_Macro];
        ActionGroup actionGroup = (ActionGroup)actionMain.itemChain[ActionMain.item.ID_Group];

        long id;

        // TODO: 이후 프리셋이 정규로 편입될 때 레퍼 클래스를 만들어서 대량으로 한꺼번에 삽입하게 만들기.
        id = actionWord.add(
                1,
                2,
                "테스트",
                "테스트",
                Integer.toString(R.drawable.number),
                true
        );

        id = actionGroup.add(
                1,
                1,
                "테스트",
                "테스트",
                new long[] {id},
                R.drawable.color,
                true
        );
        if (id == -1) id = actionGroup.find_id_by_word("테스트");

        long[] chain = new long[3];
        chain[0] = actionWord.add(
                id,
                2,
                "나는",
                "나는",
                R.drawable.play,
                true
        );
        if (chain[0] == -1) chain[0] = actionWord.find_id_by_word("나는");

        chain[1] = actionWord.add(
                id,
                3,
                "당신을",
                "당신을",
                R.drawable.family,
                true
        );
        if (chain[1] == -1) chain[1] = actionWord.find_id_by_word("당신을");

        chain[2] = actionWord.add(
                id,
                4,
                "사랑합니다",
                "사랑합니다",
                R.drawable.feeling,
                true
        );
        if (chain[2] == -1) chain[2] = actionWord.find_id_by_word("사랑합니다");

        actionMacro.add(
                id,
                5,
                "나는 당신을 사랑합니다",
                "나는 당신을 사랑합니다",
                chain,
                R.drawable.bookmark,
                true
        );
    }

    public static final class SQL implements BaseColumns {
        public static final String TABLE_NAME = "LocalPreset";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" +
                        " )";
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    private void delete_flag() {
        actionMain.getDB().execSQL(SQL.SQL_DELETE_ENTRIES);
    }

    // 프리셋 삽입 마크 여부 확인 - 존재시
    private void mark_flag() {
        SQLiteDatabase db = actionMain.getDB();

        // 디버그 자료형식 삽입 여부 확인 - 존재시 생성 생략, 없다면 생성 후 마킹
        db.execSQL(SQL.SQL_CREATE_ENTRIES);
        Cursor c = db.rawQuery("SELECT " + SQL._ID + " FROM " + SQL.TABLE_NAME + " WHERE " + SQL._ID + "=1", null);
        c.moveToFirst();
        if (c.getCount() > 0) return;

        ContentValues record = new ContentValues();
        record.put(SQL._ID, 1);
        db.insert(SQL.TABLE_NAME, null, record);
        c.close();
        record.clear();
    }
}
