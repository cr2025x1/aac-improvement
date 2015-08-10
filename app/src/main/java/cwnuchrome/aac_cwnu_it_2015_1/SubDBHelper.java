package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Chrome on 8/8/15.
 *
 * 하위 데이터베이스를 설정하고 접속하는 SQLiteOpenHelper 하위 클래스
 *
 * 3차원 이상의 정보를 요구할 때, 이 정보들은 단순히 테이블 하나만으로는 저장할 수 없다.
 * 따라서 테이블 하나하나가 그 정보에 대응하는 row가 되어야 한다.
 * 이를 위해서 메인 데이터베이스의 테이블 당 하나하나의 데이터베이스를 추가해주고, 내부 테이블 하나하나가 한 row의 데이터를 저장한다.
 */
public class SubDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    String db_filename;
    String db_table_name_header; // 각 테이블 이름의 앞부분에 붙는 접두사
    String db_table_name_expression; // SQLite 쿼리 시 like 연산자로 모든 row 대응 테이블을 찾기 위한 표현식

    public SubDBHelper(Context context, String db_filename, String db_table_name_header) {
        super(context, db_filename, null, DATABASE_VERSION);
        this.db_filename = db_filename;
        this.db_table_name_header = db_table_name_header;
        db_table_name_expression = db_table_name_header + "_%";
    }

    public void onCreate(SQLiteDatabase db) {
        // 아무 테이블도 만들지 않는다. 테이블 그 자체가 row니까.
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onReset(db);
        onCreate(db);
    }

    /*
     * 데이터베이스 내의 모든 내용을 지우는 메소드
     */
    public void onReset(SQLiteDatabase db) {
        Cursor c = db.query(
                "sqlite_master", // 이 테이블은 데이터베이스 내의 모든 테이블에 대한 정보를 저장하고 있다.
                new String[] {"name"},
                "type='table' and name like ?",
                new String[] {db_table_name_expression},
                null,
                null,
                null
        );
        c.moveToFirst();

        int table_name_column = c.getColumnIndexOrThrow("name");
        for (int i = 0; i < c.getCount(); i++) {
            String table_name = c.getString(table_name_column);
            db.execSQL("drop table " + table_name);
            c.moveToNext();
        }
        c.close();
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /*
     * 아이템의 ID를 받아, 그 아이템 ID에 대응하는 테이블 이름을 반환한다.
     */
    public String getTableName(long id) {
        return db_table_name_header + '_' + id;
    }
}
