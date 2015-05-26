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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new ActionDBHelper(this);

        // Gets the data repository in write mode
        db = mDbHelper.getWritableDatabase();

        mDbHelper.onCreate(db);
        mDbHelper.initTable(db);

        ActionMain holder = ActionMain.getInstance();
        STHolderDebug holderDebug = STHolderDebug.getInstance();

        holderDebug.insertTestRecords(db);

        LinearLayout baseLayout = (LinearLayout)findViewById(R.id.groupLayout);
        container = new AACGroupContainer(baseLayout);

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
        if (id == R.id.action_add_word) {
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

                                    if (container.addWord(db, values)) {
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
            Intent i = new Intent(getApplicationContext(), AddWordMacroActivity.class);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}