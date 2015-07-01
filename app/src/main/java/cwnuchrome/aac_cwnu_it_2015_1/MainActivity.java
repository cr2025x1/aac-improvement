package cwnuchrome.aac_cwnu_it_2015_1;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    protected AACGroupContainer container;
    protected ActionDBHelper mDbHelper;
    protected SQLiteDatabase db;

    protected boolean isInited;

    protected int status;
    protected final int STATUS_NOT_QUEUED = 0;
    protected final int STATUS_MAIN = 1;
    protected final int STATUS_ITEM_REMOVAL = 2;

    protected final int ACTIVITY_ADD = 0;
    protected final int ACTIVITY_SEARCH = 1;
    protected final int ACTIVITY_IMAGE_SELECTION = 2;

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

        status = STATUS_MAIN;

        isInited = false;
        container.exploreGroup(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch (status) {
            case STATUS_MAIN:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;

            case STATUS_ITEM_REMOVAL:
                getMenuInflater().inflate(R.menu.menu_main_item_removal, menu);
                break;
        }

        status = STATUS_NOT_QUEUED;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (status) {
            case STATUS_MAIN:
                menu.removeGroup(R.id.menu_group_remove_item);
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;

            case STATUS_ITEM_REMOVAL:
                menu.removeGroup(R.id.menu_group_main);
                getMenuInflater().inflate(R.menu.menu_main_item_removal, menu);
                break;
        }

        status = STATUS_NOT_QUEUED;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_add_word) { // TODO: Going to be deleted
//            final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.aac_confirm_dependency, null);
//
//            new AlertDialog.Builder(this)
//                    .setTitle("Add Word Menu(Temp)")
//                    .setView(linear)
//                    .setPositiveButton("Confirm",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    EditText ETWord = (EditText) linear.findViewById(R.id.etword);
//                                    ContentValues values = new ContentValues();
//                                    values.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, container.getCurrentGroupID());
//                                    values.put(ActionWord.SQL.COLUMN_NAME_WORD, ETWord.getText().toString());
//
//                                    if (container.addWord(db, values) != -1) {
//                                        container.exploreGroup(container.getCurrentGroupID());
//                                        Toast.makeText(getBaseContext(), "Word Added", Toast.LENGTH_SHORT)
//                                                .show();
//                                    } else {
//                                        Toast.makeText(getBaseContext(), "Word Already Exists", Toast.LENGTH_SHORT)
//                                                .show();
//                                    }
//
//                                }
//                            })
//                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    })
//                    .show();
//
//            return true;
//        }

//        if (id == R.id.action_remove_word) { // 아직 작업 필요
//            final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.remove_word, null);
//
//            new AlertDialog.Builder(this)
//                    .setTitle("Remove Word Menu(Temp)")
//                    .setView(linear)
//                    .setPositiveButton("Confirm",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    EditText ETWord = (EditText) linear.findViewById(R.id.etword);
//                                    ContentValues values = new ContentValues();
//
//                                    if (container.removeWord(db, ETWord.getText().toString())) {
//                                        isInited = false;
//                                        container.exploreGroup(container.getCurrentGroupID());
//                                        Toast.makeText(getBaseContext(), "Word Removed", Toast.LENGTH_SHORT)
//                                                .show();
//                                    } else {
//                                        Toast.makeText(getBaseContext(), "Word Doesn't Exists", Toast.LENGTH_SHORT)
//                                                .show();
//                                    }
//
//                                }
//                            })
//                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    })
//                    .show();
//
//            return true;
//        }

        if (id == R.id.action_add_word_macro) {
            Intent i = new Intent(this, AddWordMacroActivity.class);
            i.putExtra("currentGroupID", container.getCurrentGroupID());
            startActivityForResult(i, ACTIVITY_ADD);

            return true;
        }

        if (id == R.id.action_search) {
            Intent i = new Intent(this, AddWordMacroActivity.class);
            i.putExtra("currentGroupID", container.getCurrentGroupID());
            startActivityForResult(i, ACTIVITY_SEARCH);

            return true;
        }

        if (id == R.id.action_set_debug_db) {
            ActionMain actionMain = ActionMain.getInstance();
            actionMain.actDBHelper.deleteTable(db);
            actionMain.actDBHelper.onCreate(db);
            actionMain.actDBHelper.initTable(db);
            ActionDebug.getInstance().deleteFlag(db);
            ActionDebug.getInstance().insertTestRecords(db, this);

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
            ActionPreset.getInstance().insertDefaultRecords(db, this);

            isInited = false;
            container.exploreGroup(1);

            return true;
        }

        /* 아이템 제거 메뉴 선택 시의 메뉴들 */

        if (id == R.id.action_remove_item) {
            this.setTitle(R.string.title_select_item);
            status = STATUS_ITEM_REMOVAL;
            supportInvalidateOptionsMenu();

            container.toggleFold();
            return true;
        }

        if (id == R.id.action_remove_item_cancel) {
            this.setTitle(R.string.app_name);
            status = STATUS_MAIN;
            supportInvalidateOptionsMenu();

            container.toggleFold();
            return true;
        }

        if (id == R.id.action_remove_item_select_all) {
            container.selectAll();
            return true;
        }

        if (id == R.id.action_remove_item_deselect_all) {
            container.deselectAll();
            return true;
        }

        if (id == R.id.action_remove_item_remove) {
            if (container.selectedList.size() == 0) {
                Toast.makeText(getBaseContext(), "Nothing is selected.", Toast.LENGTH_SHORT)
                        .show();
                return true;
            }

            container.removeSelected();

            return true;
        }

        if (id == R.id.action_remove_item_set_image) {
            if (container.selectedList.size() == 0) {
                Toast.makeText(getBaseContext(), "Nothing is selected.", Toast.LENGTH_SHORT)
                        .show();
                return true;
            }

            Intent i = new Intent(this, ImageSelectionActivity.class);
            i.putExtra("currentGroupID", container.getCurrentGroupID());
            startActivityForResult(i, ACTIVITY_IMAGE_SELECTION);

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
        if (requestCode == ACTIVITY_ADD) { // Please, use a final int instead of hardcoded
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

        if (requestCode == ACTIVITY_IMAGE_SELECTION) {
            switch (resultCode) {
                case RESULT_CANCELED:
                    break;

                case RESULT_OK:
                    ContentValues v = new ContentValues();

                    int isPreset = data.getIntExtra(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, -1);
                    if (isPreset == -1) {
                        System.out.println("Extra doesn't contain a required data. (IS_PRESET)");
                        break;
                    }
                    else v.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, isPreset);

                    String pictureFilename = data.getStringExtra(ActionItem.SQL.COLUMN_NAME_PICTURE);
                    if (pictureFilename == null) {
                        System.out.println("Extra doesn't contain a required data. (PICTURE_FILENAME)");
                        break;
                    }
                    else v.put(ActionItem.SQL.COLUMN_NAME_PICTURE, pictureFilename);

                    container.setImageForSelected(v);

                    container.exploreGroup(container.getCurrentGroupID());

                    revertMenu();

                    break;
            }

        }
    }

    public void confirmDependency() {
        final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.aac_confirm_dependency, null);
        Resources r = this.getResources();

        new AlertDialog.Builder(this)
                .setTitle(r.getString(R.string.menu_remove_item_dependency_warning_title))
                .setView(linear)
                .setPositiveButton(r.getString(R.string.menu_remove_item_dependency_warning_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                container.invokeRemoval();
                            }
                        })
                .setNegativeButton(r.getString(R.string.menu_remove_item_dependency_warning_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();

    }

    public void revertMenu() {
        this.setTitle(R.string.app_name);
        status = STATUS_MAIN;
        supportInvalidateOptionsMenu();
        container.toggleFold();
    }

}
