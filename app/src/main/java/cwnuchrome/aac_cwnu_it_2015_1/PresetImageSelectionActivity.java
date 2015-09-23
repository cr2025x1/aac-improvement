package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/*
 * 아이템을 위한 프리셋 이미지를 선택하는 액티비티
 *
 * 이 클래스의 상당 부분은 아래의 예제에 기초함.
 * 참조: http://developer.android.com/guide/topics/ui/layout/gridview.html
 */
public class PresetImageSelectionActivity extends AppCompatActivity {

    GridView gridView;

    ImageLoader imageLoader;
    DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_image_selection);

        setTitle(R.string.title_activity_preset_image_selection);

        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.btn_default)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .delayBeforeLoading(1)
                .displayer(new FadeInBitmapDisplayer(500))
                .build();


        gridView = (GridView)findViewById(R.id.gridview_preset_image_selection);
        prepareGridViewRelativeValues();
        initGridView();

        gridView.setAdapter(new ImageAdapter(this, AACGroupContainerPreferences.VALID_PRESET_IMAGE_R_ID));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

    public class ImageAdapter extends BaseAdapter {
            private Context mContext;
            private int[] mThumbIds;

            public ImageAdapter(Context c, int[] mThumbIds) {
                mContext = c;
                this.mThumbIds = mThumbIds;
            }

            public int getCount() {
                return mThumbIds.length;
            }

            public Object getItem(int position) {
                return null;
            }

            public long getItemId(int position) {
                return 0;
            }

            // create a new ImageView for each item referenced by the Adapter
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView;
                if (convertView == null) {
                    // if it's not recycled, initialize some attributes
                    imageView = new ImageView(mContext);
                    imageView.setLayoutParams(new GridView.LayoutParams(preset_image_selection_column_width, preset_image_selection_column_width));
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageView.setPadding(
                            preset_image_selection_image_padding_pixel,
                            preset_image_selection_image_padding_pixel,
                            preset_image_selection_image_padding_pixel,
                            preset_image_selection_image_padding_pixel
                    );
                } else {
                    imageView = (ImageView) convertView;
                }

                // imageView.setImageResource(mThumbIds[position]); // OOM의 원천?
//                imageLoader.displayImage("drawable://" + mThumbIds[position], imageView);
                imageLoaderMemoryCache(imageView, R.drawable.btn_default, "drawable://" + mThumbIds[position]);


                return imageView;
            }
    }

    protected int preset_image_selection_column_width;
    protected Point displaySize;
    protected int preset_image_selection_image_padding_pixel;

    // 그리드뷰 세팅값 준비
    protected void prepareGridViewRelativeValues() {
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);
        preset_image_selection_column_width = (displaySize.x - gridView.getPaddingLeft() - gridView.getPaddingRight()) / AACGroupContainerPreferences.PRESET_IMAGE_SELECTION_GRIDVIEW_COLUMNS;

        preset_image_selection_image_padding_pixel = (int)DisplayUnitConverter.convertDpToPixel(AACGroupContainerPreferences.PRESET_IMAGE_SELECTION_IMAGE_PADDING_DP, this);
    }

    // 그리드뷰 세팅값 적용
    protected void initGridView() {
        gridView.setColumnWidth(preset_image_selection_column_width);
        gridView.setNumColumns(AACGroupContainerPreferences.PRESET_IMAGE_SELECTION_GRIDVIEW_COLUMNS); // 열 개수값을 받아오는 getNumColumns가 이상하게 동작함. 그리하여 이 방식으로 전환함.
    }

    public void imageLoaderMemoryCache(final ImageView img, final int failImgID, String url)
    {
        imageLoader.displayImage(url, img, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String url, View view) {
                img.setImageResource(failImgID);
            }

            @Override
            public void onLoadingFailed(String url, View view, FailReason failReason) {
                img.setImageResource(failImgID);
            }

            @Override
            public void onLoadingComplete(String url, View view, Bitmap loadedImage) {
            }

            @Override
            public void onLoadingCancelled(String url, View view) {
            }
        });
    }
}
