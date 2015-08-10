package cwnuchrome.aac_cwnu_it_2015_1;

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
    public static final String USER_IMAGE_DIRECTORY_NAME = "pictures";

    // 최상위 그룹 이름
    public static final String ROOT_GROUP_NAME = "루트";

    // 검색 알고리즘
    public static final double RANKING_FUNCTION_CONSTANT_B = 0.25d; // 순위 함수의 상수 b
    public static final double RANKING_FUNCTION_CUTOFF_THRESHOLD = 0.0d; // 출력할 아이템의 중요도 평가값 탈락 한계선
    public static final int RANKING_FUNCTION_BEST_MATCH_N = 30; // 평가값 최상위 n개 회수. 이 때의 n값.
//    public static final double FEEDBACK_ROCCHIO_CONSTANT_ALPHA = 1.0d; // 로치오 피드백 공식에서의 상수 알파 (쿼리 본체 적용 계수)
    public static final double FEEDBACK_ROCCHIO_COEFFICIENT_RELEVANT_DOC = 1.0d; // 로치오 피드백 공식: 관련 문서 적용 계수
    public static final double FEEDBACK_ROCCHIO_COEFFICIENT_IRRELEVANT_DOC = 1.0d; // 로치오 피드백 공식: 비관련 문서 적용 계수

    // 데이터베이스
    public static final boolean DATABASE_REMOVE_WORD_WITH_NO_REFERENCE = true; // 레퍼런스 카운트가 0에 도달한 워드 아이템을 자동 제거? 제거된 워드 아이템에 대한 정보는 피드백 정보를 포함, 영구히 데이터베이스에서 소실된다.

    // 형태소 분석기 서버
    public static final String MORPHEME_SERVER_HOSTNAME = "cr2025x1-dorm.ddns.net"; // 서버의 주소
    public static final int MORPHEME_SERVER_PORT = 59002; // 서버의 포트

    // 락 Wrapper
    public static final int LOCK_WRAPPER_STACK_INIT_SIZE = 20;
//    public static final boolean LOCK_WRAPPER_LOG_ACTIVE = false;
    public static final int LOCK_WRAPPER_LOG_VERBOSE_LEVEL = 0;

    // 아이템 추가 액티비티
    public static final int ADD_ITEM_FETCH_SUGGESTION_TIMEOUT = 2000; // 단위: millisecond
    public static final int ADD_ITEM_KEY_EVENT_THREAD_POOL_SIZE = 20;

}
