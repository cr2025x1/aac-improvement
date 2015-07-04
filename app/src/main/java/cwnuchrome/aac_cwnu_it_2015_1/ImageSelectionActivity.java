package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;

public class ImageSelectionActivity extends AppCompatActivity {

    String selectedImagePath;

    protected final int USE_PRESET_IMAGE = 0;
    protected final int USE_USER_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_selection);
        setTitle(R.string.title_activity_image_selection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.button_image_selection) {
            RadioGroup rg = (RadioGroup)findViewById(R.id.radiogroup_image_selection);
            int btn_id = rg.getCheckedRadioButtonId();

            switch (btn_id) {
                case R.id.radiobutton_image_selection_preset:
                    Intent i = new Intent(this, PresetImageSelectionActivity.class);
                    startActivityForResult(i, USE_PRESET_IMAGE);

                    break;

                case R.id.radiobutton_image_selection_user:
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), USE_USER_IMAGE);

                    break;
            }


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == USE_USER_IMAGE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = selectedImageUri.getPath();
                String filepath = ExternalImageProcessor.getRealPathFromURI_API19(this, selectedImageUri);

                Intent i = new Intent();
                Bundle extra = new Bundle();
                extra.putString(ActionItem.SQL.COLUMN_NAME_PICTURE, filepath);
                extra.putInt(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, 0);
                i.putExtras(extra);

                setResult(RESULT_OK, i);
                finish();

                return;
            }

            if (requestCode == USE_PRESET_IMAGE) {
                Intent i = new Intent();
                Bundle extra = new Bundle();
                Bundle dataExtra = data.getExtras();
                extra.putString(ActionItem.SQL.COLUMN_NAME_PICTURE, Integer.toString(dataExtra.getInt(ActionItem.SQL.COLUMN_NAME_PICTURE)));
                extra.putInt(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, dataExtra.getInt(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET));
                i.putExtras(extra);

                setResult(RESULT_OK, i);
                finish();
            }
        }
    }

}
