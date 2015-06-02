package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Random;

/**
 * Created by Chrome on 5/5/15.
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
    ActionDBHelper actDBHelper;
    SQLiteDatabase db;

    public interface item {
        int ITEM_COUNT = 3;

        int ID_Group = 0;
        int ID_Word = 1;
        int ID_Macro = 2;
    }

    public void initDBHelper (Context context) {
        actDBHelper = new ActionDBHelper(context);
        db = actDBHelper.getWritableDatabase();

        actDBHelper.onCreate(db);
        actDBHelper.initTable(db);

        STHolderDebug.getInstance().insertTestRecords(db);
    }

}
