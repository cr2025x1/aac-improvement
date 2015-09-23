package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Chrome on 7/3/15.
 *
 * 파일 경로가 주어질 시 그 파일의 해쉬값을 계산해 해쉬값을 이름으로 가지는 파일로 지정한 디렉토리 내에 복사하는 클래스.
 */
public class ExternalImageProcessor {
    public static String copyAfterHashing(Context context, String filepath) {
        File imageFile = new File(filepath);

        // String picturePath = Environment.getExternalStorageDirectory() + "/pictures"; // 외부 저장소 이용
        String picturePath = context.getFilesDir() + "/" + AACGroupContainerPreferences.USER_IMAGE_DIRECTORY_NAME; // 내부 저장소 이용
        File picturePathFO = new File(picturePath);
        if (picturePathFO.mkdirs()) System.out.println("Created 'pictures' directory.");
        else System.out.println("'pictures' directory already exists.");


        // 선택한 이미지의 InputStream 인스턴스 생성
        InputStream inStream;
        try {
            inStream = new FileInputStream(imageFile);
        } catch (Exception e) {
            System.out.println("Error occurred while opening the selected file.");
            return null;
        }

        // inStream을 입력해 파일 이름으로 사용할 MD5 해쉬값 생성
        String hashedFilename;
        try {
            /*
            // 아파치 커먼즈 라이브러리 이용 Base32 코딩
            Base32 base32 = new Base32();
            hashedFilename = base32.encodeToString(MD5Checksum.createChecksum(inStream));
            */

            // 안드로이드 자체 라이브러리 이용 Base64 코딩
            StringBuilder sb = new StringBuilder(
                    Base64.encodeToString(
                            MD5Checksum.createChecksum(inStream),
                            Base64.URL_SAFE) // 파일 이름에 사용되는 Base64 코딩이므로 일부 특수문자 대체 (RFC 3548 section 4)
            );
                    sb.setLength(sb.lastIndexOf("\n"));
                    sb.append(filepath.substring(filepath.lastIndexOf(".")));
            hashedFilename = sb.toString();

//
//            hashedFilename =
//            hashedFilename = hashedFilename.substring(0, hashedFilename.lastIndexOf("\n") - 1) + filepath.substring(filepath.lastIndexOf("."));
//
//
//            hashedFilename = hashedFilename + filepath.substring(filepath.lastIndexOf("."));
            inStream.close();
        } catch (Exception e){
            System.out.println("MD5 hashing met an exception.");
            e.printStackTrace();
            return null;
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
                inStream = new FileInputStream(imageFile); // 해쉬 생성 때 기존 InputStream 객체를 썼으므로 재할당
                OutputStream outStream = new FileOutputStream(imgFile);
                copyFile(inStream, outStream);
                inStream.close();
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                System.out.println("Error occurred while copying the selected file.");
                e.printStackTrace();
                return null;
            }

        }

        System.out.println("Hashcode = " + hashedFilename);

        return hashedFilename;
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static String getRealPathFromURI_API19(Context context, Uri uri){
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

    public static int remove_all_images (Context context) {
        System.out.println("*** Method: remove_all_images ***");
        int fail_count = 0;
        File dir = new File(context.getFilesDir() + "/" + AACGroupContainerPreferences.USER_IMAGE_DIRECTORY_NAME);

        String[] children = dir.list();
        if (children == null) {
            System.out.println("The directory is not exists. No need to proceed.");
            return 0;
        }
        if (children.length == 0) {
            System.out.println("The directory is empty.");
            return 0;
        }

        for (String f : children) {
            System.out.print(f + " --> ");
            if (new File(dir, f).delete()) System.out.println("Deleted");
            else {
                System.out.println("FAILED!!!");
                fail_count++;
            }
        }

        System.out.println("*** End of the method: remove_all_images ***");
        return fail_count;
    }

}
