package cwnuchrome.aac_cwnu_it_2015_1;

import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Chrome on 5/9/15.
 *
 * AAC에 대한 상수 세팅값들을 저장함.
 */
public class AACGroupContainerPreferences {
    private static AACGroupContainerPreferences ourInstance = new AACGroupContainerPreferences();
    public static AACGroupContainerPreferences getInstance() {
        return ourInstance;
    }

    private AACGroupContainerPreferences() {
    }

    /*
    *   현재 표준 이미지 사이즈
    *    123 * 123 (dp)
    */

    public static final int IMAGE_WIDTH_DP = 123;
    public static final int IMAGE_HEIGHT_DP = 123;

    public static final int VALID_PRESET_IMAGE_R_ID[] = {
            R.drawable.animal,
            R.drawable.bookmark,
            R.drawable.color,
            R.drawable.daily,
            R.drawable.expression,
            R.drawable.family,
            R.drawable.feeling,
            R.drawable.food,
            R.drawable.location,
            R.drawable.number,
            R.drawable.place,
            R.drawable.play,
            R.drawable.sense
    };
}
