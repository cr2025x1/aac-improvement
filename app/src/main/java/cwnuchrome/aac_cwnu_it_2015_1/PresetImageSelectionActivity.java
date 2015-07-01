package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

/*
 * 이 클래스의 상당 부분은 아래의 예제에 기초함.
 * 참조: http://developer.android.com/guide/topics/ui/layout/gridview.html
 */


public class PresetImageSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_image_selection);

        setTitle(R.string.title_activity_preset_image_selection);

        GridView gridview = (GridView)findViewById(R.id.gridview_preset_image_selection);
        gridview.setColumnWidth((int)DisplayUnitConverter.convertDpToPixel(AACGroupContainerPreferences.IMAGE_WIDTH_DP, this));

        gridview.setAdapter(new ImageAdapter(this, AACGroupContainerPreferences.VALID_PRESET_IMAGE_R_ID));

        // 이보시오, 의사 양반!! 람다 표현을 못 쓰다니 이게 무슨 소리요!!
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                PresetImageSelectionActivity.this.deliverPictureID(AACGroupContainerPreferences.VALID_PRESET_IMAGE_R_ID[position]);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preset_image_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.button_preset_image_selection) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void deliverPictureID(int pictureID) {
        Intent i = new Intent();
        Bundle extra = new Bundle();
        extra.putInt(ActionItem.SQL.COLUMN_NAME_PICTURE, pictureID);
        extra.putInt(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, 1);
        i.putExtras(extra);

        setResult(RESULT_OK, i);
        finish();
    }
}
