package cwnuchrome.aac_cwnu_it_2015_1;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Created by Chrome on 6/25/15.
 *
 * MD5 해시 함수를 이용, 주어진 파일의 체크섬을 생성하는 정적 메소드를 가지는 클래스이다.
 * 참조 위치: http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
 * - 단, 위의 코드 조각에서 InputStream을 받는 부분을 다른 메소드로 분리해냄.
 *
 */
@SuppressWarnings("unused")
public class MD5Checksum {

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);
        return createChecksum(fis);
    }

    public static byte[] createChecksum(InputStream fis) throws Exception {
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}
