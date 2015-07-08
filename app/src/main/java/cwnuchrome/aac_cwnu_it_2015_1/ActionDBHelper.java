package cwnuchrome.aac_cwnu_it_2015_1;

/**
 * Created by Chrome on 5/3/15.
 *
 * 데이터베이스 Helper 클래스. 그 이상 뭐 무슨 말이 필요한가?
 */

import android.content.Context;
import android.database.sqlite.*;

public class ActionDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Action.db";
    ActionMain actionMain;

    public ActionDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        actionMain = ActionMain.getInstance();
    }

    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and fetchSuggestion over
        for (int i = 0; i < actionMain.itemChain.length; i++) {
            actionMain.itemChain[i].clearTable(db);
        }
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createTable(SQLiteDatabase db) {
        for (int i = 0; i < actionMain.itemChain.length; i++) {
            actionMain.itemChain[i].createTable(db);
        }
    }

    public void initTable(SQLiteDatabase db) {
        for (int i = 0; i < actionMain.itemChain.length; i++) {
            actionMain.itemChain[i].initTable(db);
        }
    }

    public void deleteTable(SQLiteDatabase db) {
        for (int i = 0; i < actionMain.itemChain.length; i++) {
            actionMain.itemChain[i].deleteTable(db);
        }
    }

}