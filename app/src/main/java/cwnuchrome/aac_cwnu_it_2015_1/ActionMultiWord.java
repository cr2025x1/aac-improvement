package cwnuchrome.aac_cwnu_it_2015_1;

import android.provider.BaseColumns;

/**
 * Created by Chrome on 7/7/15.
 */
public abstract class ActionMultiWord extends ActionItem {

    interface SQL extends ActionItem.SQL {
        String COLUMN_NAME_WORDCHAIN = "wordchain";
    }



}
