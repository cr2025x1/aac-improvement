package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Random;

/**
 * Created by Chrome on 5/5/15.
 *
 * 각 아이템에 대한 핵심 정보가 담긴 싱글턴 객체.
 * 많은 클래스가 이 클래스에 대한 의존성을 가지므로 조심히 다룰 것.
 *
 */
public class ActionMain {
    private static ActionMain ourInstance = new ActionMain();
    public static ActionMain getInstance() {
        return ourInstance;
    }
    private ActionMain() {
        rand = new Random();

        itemChain = new ActionItem[item.ITEM_COUNT];
        itemChain[item.ID_Group] = new ActionGroup();
        itemChain[item.ID_Word] = new ActionWord();
        itemChain[item.ID_Macro] = new ActionMacro();

    }

    Random rand;
    ActionItem itemChain[];
    ActionDBHelper actionDBHelper;
    SQLiteDatabase db;
    AACGroupContainer containerRef;

    public interface item {
        int ITEM_COUNT = 3;

        int ID_Group = 0;
        int ID_Word = 1;
        int ID_Macro = 2;
    }

    public void initDBHelper (Context context) {
        actionDBHelper = new ActionDBHelper(context);
        db = actionDBHelper.getWritableDatabase();
    }

    public void initTables() {
        actionDBHelper.onCreate(db);
        actionDBHelper.initTable(db);
    }

    public void resetTables() {
        actionDBHelper.deleteTable(db);
        actionDBHelper.onCreate(db);
        actionDBHelper.initTable(db);
    }

//    public ActionDBHelper getActionDBHelper() { return actionDBHelper; }
    public SQLiteDatabase getDB() { return db; }
}
