package cwnuchrome.aac_cwnu_it_2015_1;

import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Chrome on 5/9/15.
 */
public class STSettings {
    private static STSettings ourInstance = new STSettings();
    public static STSettings getInstance() {
        return ourInstance;
    }

    public LinearLayout.LayoutParams titleViewLP;

    private STSettings() {
        titleViewLP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

    }

}
