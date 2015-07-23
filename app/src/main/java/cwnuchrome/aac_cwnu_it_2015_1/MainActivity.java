package cwnuchrome.aac_cwnu_it_2015_1;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    protected AACGroupContainer container;

    protected ActionMain actionMain;
    protected Resources r;

    protected boolean isInited;

    protected int menu_prep_command_status;
    protected final int STATUS_NO_PREP_ISSUED = 0;
    protected final int STATUS_MAIN = 1;
    protected final int STATUS_ITEM_REMOVAL = 2;
    protected final int STATUS_ITEM_RENAMING = 3;

    protected final int ACTIVITY_ADD = 0;
    protected final int ACTIVITY_SEARCH = 1;
    protected final int ACTIVITY_IMAGE_SELECTION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionMain = ActionMain.getInstance();
        actionMain.initDBHelper(getApplicationContext());
        actionMain.initTables();
        r = getResources();

        LinearLayout baseLayout = (LinearLayout)findViewById(R.id.groupLayout);
        container = new AACGroupContainer(baseLayout);
        container.setContainerID(actionMain.getReferrer().attach(container));

        menu_prep_command_status = STATUS_MAIN;

        isInited = false;
        container.exploreGroup(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);

        menu.clear();

        switch (menu_prep_command_status) {
            case STATUS_MAIN:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;

            case STATUS_ITEM_REMOVAL:
                getMenuInflater().inflate(R.menu.menu_main_item_selection_mode, menu);
                break;

            case STATUS_ITEM_RENAMING:
                getMenuInflater().inflate(R.menu.menu_main_item_renaming, menu);
                break;
        }

        menu_prep_command_status = STATUS_NO_PREP_ISSUED;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_word_macro) {
            Intent i = new Intent(this, AddItemActivity.class);
            i.putExtra("currentGroupID", container.getCurrentGroupID());
            startActivityForResult(i, ACTIVITY_ADD);

            return true;
        }

        if (id == R.id.action_search) {
            Intent i = new Intent(this, SearchActivity.class);
            i.putExtra("currentGroupID", container.getCurrentGroupID());
            i.putExtra("AACGC_ID", container.getContainerID());
            startActivityForResult(i, ACTIVITY_SEARCH);

            return true;
        }

        if (id == R.id.action_set_debug_db) {
            actionMain.resetTables();
            ActionDebug.getInstance().deleteFlag();
            ActionDebug.getInstance().insertTestRecords(this);

            isInited = false;
            container.exploreGroup(1);

            return true;
        }

        if (id == R.id.action_set_default_db) {
            actionMain.resetTables();
            ActionPreset.getInstance().revert_to_default(this);

            isInited = false;
            container.exploreGroup(1);

            return true;
        }

        if (id == R.id.action_rename_item) {
            set_menu(R.string.title_activity_select_item_to_rename, STATUS_ITEM_RENAMING);
            container.setMode(AACGroupContainer.MODE_RENAMING);
            return true;
        }

        /* 아이템 제거 메뉴 선택 시의 메뉴들 */

        if (id == R.id.action_remove_item) {
            set_menu(R.string.title_item_select, STATUS_ITEM_REMOVAL);
            container.toggleFold();
            return true;
        }

        if (id == R.id.action_item_select_cancel) {
            revert_menu_to_main();
            container.toggleFold();
            return true;
        }

        if (id == R.id.action_item_select_select_all) {
            container.selectAll();
            return true;
        }

        if (id == R.id.action_item_select_deselect_all) {
            container.deselectAll();
            return true;
        }

        if (id == R.id.action_item_select_remove) {
            if (container.selectedList.size() == 0) {
                Toast.makeText(getBaseContext(), "Nothing is selected.", Toast.LENGTH_SHORT)
                        .show();
                return true;
            }

            container.removeSelected();
            revert_menu_to_main();

            return true;
        }

        if (id == R.id.action_item_select_set_image) {
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

        if (id == R.id.action_item_select_move) {
            if (container.selectedList.size() == 0) {
                Toast.makeText(getBaseContext(), "Nothing is selected.", Toast.LENGTH_SHORT)
                        .show();
                return true;
            }

            // TODO: 액티비티 추가 - 트리 구조 출력 액티비티
            // container.moveSelected();
            revert_menu_to_main();
            return true;
        }

        /* 아이템 수정 메뉴 선택시의 메뉴들 */

        if (id == R.id.action_main_item_rename_cancel) {
            container.setMode(AACGroupContainer.MODE_NORMAL);
            revert_menu_to_main();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        container.onDestroy();
        actionMain.getDB().close();
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

                    revertMenu();

                    container.exploreGroup(container.getCurrentGroupID());


                    break;
            }

        }
    }

    public void confirmDependency() {
        final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.aac_confirm_dependency, null);

        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_remove_item_dependency_warning_title)
                .setView(linear)
                .setPositiveButton(R.string.menu_remove_item_dependency_warning_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                container.invokeRemoval();
                            }
                        })
                .setNegativeButton(R.string.menu_remove_item_dependency_warning_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();

    }

    public void revertMenu() {
        revert_menu_to_main();
        container.toggleFold();
    }

    protected void revert_menu_to_main() {
        set_menu(R.string.app_name, STATUS_MAIN);
    }

    protected void set_menu(String title, int command_status) {
        this.setTitle(title);
        menu_prep_command_status = command_status;
        supportInvalidateOptionsMenu();
    }

    protected void set_menu(int title_string_id, int command_status) {
        set_menu(r.getString(title_string_id), command_status);
    }

    public void dialog_rename(int category_id, long item_id) {
        final int category_id_wrapper = category_id;
        final long item_id_wrapper = item_id;
        final LinearLayout linear = (LinearLayout)View.inflate(this, R.layout.dialog_item_rename, null);

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_dialog_item_rename)
                .setView(linear)
                .setPositiveButton(R.string.button_dialog_item_rename_positive,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContentValues values = new ContentValues();
                                values.put(
                                        ActionWord.SQL.COLUMN_NAME_WORD,
                                        ((EditText) linear.findViewById(R.id.dialog_item_rename_edit_text))
                                                .getText()
                                                .toString()
                                                .trim()
                                );

                                if (actionMain.itemChain[category_id_wrapper].updateWithIDs(getBaseContext(), values, new long[]{item_id_wrapper}) > 0) {
                                    // TODO: 아니면 버튼 텍스트만 업데이트하게 변경?
                                    container.exploreGroup(container.getCurrentGroupID());
                                    Toast
                                            .makeText(getBaseContext(), R.string.toast_dialog_item_rename_success, Toast.LENGTH_SHORT)
                                            .show();
                                }
                                else {
                                    Toast
                                            .makeText(getBaseContext(), R.string.toast_dialog_item_rename_failure, Toast.LENGTH_SHORT)
                                            .show();
                                }

                                revert_menu_to_main();
                            }
                        })
                .setNegativeButton(R.string.button_dialog_item_rename_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        container.setMode(AACGroupContainer.MODE_NORMAL);
                        revert_menu_to_main();
                    }
                })
                .show();
    }

}
