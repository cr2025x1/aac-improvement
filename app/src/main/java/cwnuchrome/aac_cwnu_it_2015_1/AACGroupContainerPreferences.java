package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.widget.GridView;
import android.widget.ImageView;

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

    // 버튼 상단 이미지 크기 (DP 단위)
    public static final int IMAGE_WIDTH_DP = 123;
    public static final int IMAGE_HEIGHT_DP = 123;

    // 프리셋 이미지 목록들 - 여기에 추가 안 되면 프로젝트에 추가했어도 안 뜸!
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

    // 체크박스 페이드 인/아웃 소요시간
    public static final int CHECKBOX_ANIMATION_DURATION = 200;

    // 프리셋 이미지 선택 액티비티
    public static final int PRESET_IMAGE_SELECTION_IMAGE_PADDING_DP = 4; // 그리드뷰 이미지의 패딩 (DP)
    public static final int PRESET_IMAGE_SELECTION_GRIDVIEW_COLUMNS = 3; // 그리드뷰 열 개수

    // 외부 이미지 보관 디렉토리
    public static final String USER_IMAGE_DIRECTORY = "pictures";

    // 최상위 그룹 이름
    public static final String ROOT_GROUP_NAME = "최상위";
}
