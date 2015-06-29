package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RadioGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ImageSelectionActivity extends ActionBarActivity {

    String selectedImagePath;

    protected final int USE_PRESET_IMAGE = 0;
    protected final int USE_USER_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selection);

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
            // TODO: 라디오그룹 선택에 따른 변화 주기
            RadioGroup rg = (RadioGroup)findViewById(R.id.radiogroup_image_selection);
            int btn_id = rg.getCheckedRadioButtonId();

            switch (btn_id) {
                case R.id.radiobutton_image_selection_preset:

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == USE_USER_IMAGE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = selectedImageUri.getPath();
                String filepath = getRealPathFromURI_API19(this, selectedImageUri);
                String filename = filepath.substring(filepath.lastIndexOf("/") + 1);

                // String picturePath = Environment.getExternalStorageDirectory() + "/pictures"; // 외부 저장소 이용
                String picturePath = this.getFilesDir() + "/pictures"; // 내부 저장소 이용
                File picturePathFO = new File(picturePath);
                if (picturePathFO.mkdirs()) System.out.println("Created 'pictures' directory.");
                else System.out.println("'pictures' directory already exists.");


                // 선택한 이미지의 InputStream 인스턴스 생성
                InputStream inStream;
                try {
                    inStream = getContentResolver().openInputStream(selectedImageUri);
                } catch (Exception e) {
                    System.out.println("Error occurred while opening the selected file.");
                    return;
                }

                // inStream을 입력해 파일 이름으로 사용할 MD5 해쉬값 생성
                String hashedFilename;
                try {
                    hashedFilename = Base64.encodeToString(MD5Checksum.createChecksumWithStream(inStream), Base64.DEFAULT);
                    hashedFilename = hashedFilename.substring(0, hashedFilename.lastIndexOf("\n") - 1) + filename.substring(filename.lastIndexOf("."));
                    inStream.close();
                } catch (Exception e){
                    System.out.println("MD5 hashing met an exception.");
                    return;
                }

                // 출력 File 객체 생성
                File imgFile = new File(picturePath, hashedFilename);
                System.out.println("Image file absolute path = " + imgFile.getAbsolutePath());
                if (imgFile.exists()) {
                    System.out.println("Duplicate file detected.");
                }
                else {
                    try {
                        // 파일 복사
                        inStream = getContentResolver().openInputStream(selectedImageUri); // 해쉬 생성 때 기존 InputStream 객체를 썼으므로 재할당
                        OutputStream outStream = new FileOutputStream(imgFile);
                        copyFile(inStream, outStream);
                        inStream.close();
                        outStream.flush();
                        outStream.close();
                    } catch (Exception e) {
                        System.out.println("Error occurred while copying the selected file.");
                        return;
                    }

                }

                System.out.println("Hashcode = " + hashedFilename);

                Intent i = new Intent();
                Bundle extra = new Bundle();
                extra.putString(ActionItem.SQL.COLUMN_NAME_PICTURE, hashedFilename);
                extra.putInt(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, 0);
                i.putExtras(extra);

                setResult(RESULT_OK, i);
                finish();

            }

        }
    }

    protected void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    protected static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

}
