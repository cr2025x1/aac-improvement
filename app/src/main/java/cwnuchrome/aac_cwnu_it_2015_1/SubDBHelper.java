package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * Created by Chrome on 8/8/15.
 */
public class SubDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    String db_filename;
    String db_table_name_header;
    String db_table_name_expression;

    public SubDBHelper(Context context, String db_filename, String db_table_name_header) {
        super(context, db_filename, null, DATABASE_VERSION);
        this.db_filename = db_filename;
        this.db_table_name_header = db_table_name_header;
        db_table_name_expression = db_table_name_header + "_%";
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onReset(db);
        onCreate(db);
    }

    public void onReset(SQLiteDatabase db) {
        Cursor c = db.query(
                "sqlite_master",
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

    public String getTableName(long id) {
        return db_table_name_header + '_' + id;
    }
}
