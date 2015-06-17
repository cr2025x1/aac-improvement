package cwnuchrome.aac_cwnu_it_2015_1;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    AACGroupContainer container;
    ActionDBHelper mDbHelper;
    SQLiteDatabase db;

    boolean isInited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionMain holder = ActionMain.getInstance();
        holder.initDBHelper(getApplicationContext());
        holder.initTables();

        mDbHelper = holder.actDBHelper;
        db = holder.db;

        LinearLayout baseLayout = (LinearLayout)findViewById(R.id.groupLayout);
        container = new AACGroupContainer(baseLayout);

        isInited = false;
        container.exploreGroup(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_add_word) { // TODO: Going to be deleted
            final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.add_word, null);

            new AlertDialog.Builder(this)
                    .setTitle("Add Word Menu(Temp)")
                    .setView(linear)
                    .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText ETWord = (EditText) linear.findViewById(R.id.etword);
                                    ContentValues values = new ContentValues();
                                    values.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, container.getCurrentGroupID());
                                    values.put(ActionWord.SQL.COLUMN_NAME_WORD, ETWord.getText().toString());

                                    if (container.addWord(db, values) != -1) {
                                        container.exploreGroup(container.getCurrentGroupID());
                                        Toast.makeText(getBaseContext(), "Word Added", Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Word Already Exists", Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                }
                            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();

            return true;
        }
        */

        if (id == R.id.action_remove_word) { // 아직 작업 필요
            final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.remove_word, null);

            new AlertDialog.Builder(this)
                    .setTitle("Remove Word Menu(Temp)")
                    .setView(linear)
                    .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText ETWord = (EditText) linear.findViewById(R.id.etword);
                                    ContentValues values = new ContentValues();

                                    if (container.removeWord(db, ETWord.getText().toString())) {
                                        isInited = false;
                                        container.exploreGroup(container.getCurrentGroupID());
                                        Toast.makeText(getBaseContext(), "Word Removed", Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Word Doesn't Exists", Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                }
                            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();

            return true;
        }

        if (id == R.id.action_add_word_macro) {
            Intent i = new Intent(this, AddWordMacroActivity.class);
            i.putExtra("currentGroupID", container.getCurrentGroupID());
            startActivityForResult(i, 0);

            return true;
        }

        if (id == R.id.action_remove_word_macro) {
            Intent i = new Intent(this, AddWordMacroActivity.class);
            i.putExtra("currentGroupID", container.getCurrentGroupID());
            startActivityForResult(i, 0);

            return true;
        }

        if (id == R.id.action_set_debug_db) {
            ActionMain actionMain = ActionMain.getInstance();
            actionMain.actDBHelper.deleteTable(db);
            actionMain.actDBHelper.onCreate(db);
            actionMain.actDBHelper.initTable(db);
            ActionDebug.getInstance().deleteFlag(db);
            ActionDebug.getInstance().insertTestRecords(db);

            isInited = false;
            container.exploreGroup(1);

            return true;
        }

        if (id == R.id.action_set_default_db) {
            ActionMain actionMain = ActionMain.getInstance();
            actionMain.actDBHelper.deleteTable(db);
            actionMain.actDBHelper.onCreate(db);
            actionMain.actDBHelper.initTable(db);
            ActionPreset.getInstance().deleteFlag(db);
            ActionPreset.getInstance().insertDefaultRecords(db);

            isInited = false;
            container.exploreGroup(1);

            return true;
        }

        if (id == R.id.action_fold) {
            container.fold();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        container.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) { // Please, use a final int instead of hardcoded
            // int value
            switch (resultCode)
            {
                case RESULT_OK:
                    container.refresh();
                    Toast.makeText(this, "Added \"" + data.getExtras().getString("ItemName")+ "\"", Toast.LENGTH_SHORT)
                            .show();
                    break;
            }

        }
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//
//        if (!isInited) {
//            container.initDimInfo();
//
//            isInited = true;
//        }
//
//    }
}
