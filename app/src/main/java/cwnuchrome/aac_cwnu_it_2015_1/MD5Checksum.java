package cwnuchrome.aac_cwnu_it_2015_1;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Created by Chrome on 6/25/15.
 *
 * http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
 *
 * 단, 위의 코드 조각에서 InputStream을 받는 부분을 다른 메소드로 분리해냄.
 *
 */
public class MD5Checksum {

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);
        return createChecksumWithStream(fis);
    }

    public static byte[] createChecksumWithStream(InputStream fis) throws Exception {
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

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

//    public static void main(String args[]) {
//        try {
//            System.out.println(getMD5Checksum("apache-tomcat-5.5.17.exe"));
//            // output :
//            //  0bb2827c5eacf570b6064e24e0e6653b
//            // ref :
//            //  http://www.apache.org/dist/
//            //          tomcat/tomcat-5/v5.5.17/bin
//            //              /apache-tomcat-5.5.17.exe.MD5
//            //  0bb2827c5eacf570b6064e24e0e6653b *apache-tomcat-5.5.17.exe
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
