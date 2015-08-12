package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by Chrome on 5/9/15.
 *
 * 테이블을 리셋하고 프리셋을 생성하는 클래스
 */
public final class ActionPreset {
    private static ActionPreset ourInstance = new ActionPreset();
    public static ActionPreset getInstance() {
        return ourInstance;
    }

    ActionMain actionMain;
    LockWrapper.ReadLockWrapper read_lock;
    LockWrapper.WriteLockWrapper write_lock;

    private ActionPreset() {
        actionMain = ActionMain.getInstance();
        read_lock = actionMain.getReadLock();
        write_lock = actionMain.getWriteLock();
    }

    public void revert_to_default(Context context) {
        write_lock.lock();
        delete_flag();
        actionMain.resetTables();
        ExternalImageProcessor.remove_all_images(context);
        mark_flag();
        recover_preset();
        write_lock.unlock();
    }

    public void recover_preset() {
        write_lock.lock();
        ActionMacro actionMacro = (ActionMacro)actionMain.itemChain[ActionMain.item.ID_Macro];
        ActionGroup actionGroup = (ActionGroup)actionMain.itemChain[ActionMain.item.ID_Group];

        long id;

        id = actionGroup.add(1, 0, "가족", R.drawable.color);
        actionMacro.add(id, 0, "나이", R.drawable.family_age);
        actionMacro.add(id, 0, "남동생", R.drawable.family_young_brother);
        actionMacro.add(id, 0, "누나", R.drawable.family_older_sister);
        actionMacro.add(id, 0, "언니", R.drawable.family_older_sister);
        actionMacro.add(id, 0, "사는 곳", R.drawable.family_residence);
        actionMacro.add(id, 0, "생일", R.drawable.family_birthday);
        actionMacro.add(id, 0, "아빠", R.drawable.family_father);
        actionMacro.add(id, 0, "엄마", R.drawable.family_mother);
        actionMacro.add(id, 0, "여동생", R.drawable.family_young_sister);
        actionMacro.add(id, 0, "전화번호", R.drawable.family_phone_number);
        actionMacro.add(id, 0, "할머니", R.drawable.family_grandfather);
        actionMacro.add(id, 0, "할아버지", R.drawable.family_grandmother);
        actionMacro.add(id, 0, "형", R.drawable.family_older_brother);
        actionMacro.add(id, 0, "오빠", R.drawable.family_older_brother);
        actionMacro.add(id, 0, "얘가 남동생입니다.", R.drawable.family_young_brother);
        actionMacro.add(id, 0, "엄마, 배가 고파요.", R.drawable.expression_i_am_hungry);
        actionMacro.add(id, 0, "아빠, 공놀이가 재미있어요.", R.drawable.feeling_joy);
        actionMacro.add(id, 0, "오빠, 고마워.", R.drawable.daily_thanks);
        actionMacro.add(id, 0, "할아버지 전화번호는 모르겠어요.", R.drawable.feeling_ignorance);
        actionMacro.add(id, 0, "할머니, 화장실에 가고 싶어요.", R.drawable.expression_i_want_to_go_toilet);
        actionMacro.add(id, 0, "누나, 배가 고파요.", R.drawable.expression_i_am_hungry);
        actionMacro.add(id, 0, "언니, 음악이 좋아요.", R.drawable.daily_good);
        actionMacro.add(id, 0, "엄마, 그림을 다 그렸어요.", R.drawable.expression_i_am_done);
        actionMacro.add(id, 0, "여동생 나이는 5살입니다.", R.drawable.number_5);
        actionMacro.add(id, 0, "오빠, 잘 가.", R.drawable.daily_bye);
        actionMacro.add(id, 0, "할머니 반찬이 맛있어요.", R.drawable.daily_good);

        id = actionGroup.add(1, 0, "감각", R.drawable.sense);
        actionMacro.add(id, 0, "냄새나요", R.drawable.sense_smell);
        actionMacro.add(id, 0, "달콤해요", R.drawable.sense_sweet);
        actionMacro.add(id, 0, "맛없어요", R.drawable.sense_awful);
        actionMacro.add(id, 0, "맛있어요", R.drawable.sense_delicious);
        actionMacro.add(id, 0, "매워요", R.drawable.sense_spicy);
        actionMacro.add(id, 0, "시큼해요", R.drawable.sense_sour);
        actionMacro.add(id, 0, "짜요", R.drawable.sense_salty);
        actionMacro.add(id, 0, "반찬이 맛있어요.", R.drawable.sense_delicious);
        actionMacro.add(id, 0, "국이 짜요.", R.drawable.sense_salty);
        actionMacro.add(id, 0, "우유가 냄새나요.", R.drawable.sense_smell);
        actionMacro.add(id, 0, "반찬이 매워요.", R.drawable.sense_spicy);
        actionMacro.add(id, 0, "과일이 시큼해요.", R.drawable.sense_sour);
        actionMacro.add(id, 0, "우유가 맛없어요.", R.drawable.sense_awful);
        actionMacro.add(id, 0, "과일이 달콤해요.", R.drawable.sense_sweet);

        id = actionGroup.add(1, 0, "감정", R.drawable.feeling);
        actionMacro.add(id, 0, "모르겠어요.", R.drawable.feeling_ignorance);
        actionMacro.add(id, 0, "무서워요.", R.drawable.feeling_fear);
        actionMacro.add(id, 0, "슬퍼요.", R.drawable.feeling_sorrow);
        actionMacro.add(id, 0, "아파요.", R.drawable.feeling_pain);
        actionMacro.add(id, 0, "재미있어요.", R.drawable.feeling_joy);
        actionMacro.add(id, 0, "피곤해요.", R.drawable.feeling_weariness);
        actionMacro.add(id, 0, "행복해요.", R.drawable.feeling_happiness);
        actionMacro.add(id, 0, "화가 나요.", R.drawable.feeling_rage);
        actionMacro.add(id, 0, "공놀이가 재미있어요.", R.drawable.feeling_joy);
        actionMacro.add(id, 0, "그네가 무서워요.", R.drawable.feeling_fear);
        actionMacro.add(id, 0, "엄마, 행복해요.", R.drawable.feeling_happiness);
        actionMacro.add(id, 0, "아빠의 검정색 자동차가 무서워요.", R.drawable.feeling_fear);
        actionMacro.add(id, 0, "그림은 그만 그릴래요.", R.drawable.expression_i_want_to_stop);
        actionMacro.add(id, 0, "엄마, 피곤해요.", R.drawable.feeling_weariness);
        actionMacro.add(id, 0, "형, 피곤해요.", R.drawable.feeling_weariness);
        actionMacro.add(id, 0, "전화번호는 잘 모르겠어요.", R.drawable.feeling_ignorance);
        actionMacro.add(id, 0, "미끄럼틀이 무서워요.", R.drawable.feeling_fear);

        id = actionGroup.add(1, 0, "놀이", R.drawable.play);
        actionMacro.add(id, 0, "공놀이", R.drawable.play_ball);
        actionMacro.add(id, 0, "그네", R.drawable.play_swing);
        actionMacro.add(id, 0, "그림 그리기", R.drawable.play_drawing);
        actionMacro.add(id, 0, "까꿍", R.drawable.play_peek_a_boo);
        actionMacro.add(id, 0, "미끄럼틀", R.drawable.play_slide);
        actionMacro.add(id, 0, "블록", R.drawable.play_block);
        actionMacro.add(id, 0, "비눗방울", R.drawable.play_bubble);
        actionMacro.add(id, 0, "음악", R.drawable.play_music);
        actionMacro.add(id, 0, "인형", R.drawable.play_doll);
        actionMacro.add(id, 0, "자동차", R.drawable.play_car);
        actionMacro.add(id, 0, "책", R.drawable.play_book);
        actionMacro.add(id, 0, "그림 그리기를 더 하고 싶어요.", R.drawable.play_drawing);
        actionMacro.add(id, 0, "음악이 좋아요.", R.drawable.play_music);
        actionMacro.add(id, 0, "책이 빨강색입니다.", R.drawable.play_book);
        actionMacro.add(id, 0, "비눗방울이 흰색입니다.", R.drawable.play_bubble);
        actionMacro.add(id, 0, "분홍색 인형이 좋아요.", R.drawable.play_doll);
        actionMacro.add(id, 0, "주황색 블록이 싫어요.", R.drawable.play_block);
        actionMacro.add(id, 0, "미끄럼틀이 재미있어요.", R.drawable.play_slide);
        actionMacro.add(id, 0, "자동차가 회색입니다.", R.drawable.play_car);
        actionMacro.add(id, 0, "갈색 그네가 재미있어요.", R.drawable.play_swing);
        actionMacro.add(id, 0, "형, 공놀이가 좋아요.", R.drawable.play_ball);

        id = actionGroup.add(1, 0, "색깔", R.drawable.color);
        actionMacro.add(id, 0, "갈색", R.drawable.color_brown);
        actionMacro.add(id, 0, "검정색", R.drawable.color_black);
        actionMacro.add(id, 0, "노란색", R.drawable.color_yellow);
        actionMacro.add(id, 0, "보라색", R.drawable.color_purple);
        actionMacro.add(id, 0, "분홍색", R.drawable.color_pink);
        actionMacro.add(id, 0, "빨강색", R.drawable.color_red);
        actionMacro.add(id, 0, "주황색", R.drawable.color_orange);
        actionMacro.add(id, 0, "초록색", R.drawable.color_green);
        actionMacro.add(id, 0, "파랑색", R.drawable.color_blue);
        actionMacro.add(id, 0, "회색", R.drawable.color_gray);
        actionMacro.add(id, 0, "흰색", R.drawable.color_white);
        actionMacro.add(id, 0, "우유가 흰색입니다.", R.drawable.color_white);
        actionMacro.add(id, 0, "수영장이 파랑색입니다.", R.drawable.color_blue);
        actionMacro.add(id, 0, "그네가 갈색입니다.", R.drawable.color_brown);
        actionMacro.add(id, 0, "흰색 미끄럼틀이 재미있어요.", R.drawable.color_white);
        actionMacro.add(id, 0, "노란색 책은 잘 모르겠어요.", R.drawable.color_yellow);
        actionMacro.add(id, 0, "보라색 과일이 싫어요.", R.drawable.color_purple);
        actionMacro.add(id, 0, "초록색 책이 아니야.", R.drawable.color_green);
        actionMacro.add(id, 0, "주황색 책이 맞아.", R.drawable.color_orange);
        actionMacro.add(id, 0, "초록색 반찬이 싫어요.", R.drawable.daily_i_dont_like_it);
        actionMacro.add(id, 0, "노란색 아이스크림이 맛있어요.", R.drawable.color_yellow);

        id = actionGroup.add(1, 0, "숫자", R.drawable.number);
        actionMacro.add(id, 0, "일", R.drawable.number_1);
        actionMacro.add(id, 0, "이", R.drawable.number_2);
        actionMacro.add(id, 0, "삼", R.drawable.number_3);
        actionMacro.add(id, 0, "사", R.drawable.number_4);
        actionMacro.add(id, 0, "오", R.drawable.number_5);
        actionMacro.add(id, 0, "육", R.drawable.number_6);
        actionMacro.add(id, 0, "칠", R.drawable.number_7);
        actionMacro.add(id, 0, "팔", R.drawable.number_8);
        actionMacro.add(id, 0, "구", R.drawable.number_9);
        actionMacro.add(id, 0, "십", R.drawable.number_10);

        id = actionGroup.add(1, 0, "위치", R.drawable.location);
        actionMacro.add(id, 0, "위", R.drawable.location_upside);
        actionMacro.add(id, 0, "아래", R.drawable.location_below);
        actionMacro.add(id, 0, "여기", R.drawable.location_here);
        actionMacro.add(id, 0, "저기", R.drawable.location_there);
        actionMacro.add(id, 0, "앞", R.drawable.location_front);
        actionMacro.add(id, 0, "뒤", R.drawable.location_behind);
        actionMacro.add(id, 0, "옆", R.drawable.location_side);
        actionMacro.add(id, 0, "안", R.drawable.location_inside);
        actionMacro.add(id, 0, "밖", R.drawable.location_outside);
        actionMacro.add(id, 0, "저기는 학교입니다.", R.drawable.place_school);
        actionMacro.add(id, 0, "밖에 갈래요.", R.drawable.location_outside);
        actionMacro.add(id, 0, "여기는 도서관입니다.", R.drawable.place_library);
        actionMacro.add(id, 0, "뒤의 자동차가 검정색입니다.", R.drawable.color_black);
        actionMacro.add(id, 0, "앞의 인형이 분홍색입니다.", R.drawable.color_pink);
        actionMacro.add(id, 0, "학교 옆의 미끄럼틀이 무서워요.", R.drawable.feeling_fear);
        actionMacro.add(id, 0, "도서관 안의 책이 재미있어요.", R.drawable.feeling_joy);
        actionMacro.add(id, 0, "저기는 공항입니다.", R.drawable.place_airport);

        id = actionGroup.add(1, 0, "음식", R.drawable.food);
        actionMacro.add(id, 0, "국", R.drawable.food_soup);
        actionMacro.add(id, 0, "물", R.drawable.food_water);
        actionMacro.add(id, 0, "반찬", R.drawable.food_dish);
        actionMacro.add(id, 0, "밥", R.drawable.food_rice);
        actionMacro.add(id, 0, "우유", R.drawable.food_milk);
        actionMacro.add(id, 0, "계란", R.drawable.food_egg);
        actionMacro.add(id, 0, "아이스크림", R.drawable.food_icecream);
        actionMacro.add(id, 0, "과일", R.drawable.food_fruit);
        actionMacro.add(id, 0, "과일이 맛있어요.", R.drawable.food_fruit);
        actionMacro.add(id, 0, "아이스크림이 달콤해요.", R.drawable.food_icecream);
        actionMacro.add(id, 0, "계란이 노란색입니다.", R.drawable.food_egg);
        actionMacro.add(id, 0, "흰색 밥이 맛있어요.", R.drawable.food_rice);
        actionMacro.add(id, 0, "국이 냄새나요.", R.drawable.food_soup);
        actionMacro.add(id, 0, "빨강색 반찬이 매워요.", R.drawable.food_dish);
        actionMacro.add(id, 0, "물이 좋아요.", R.drawable.food_water);
        actionMacro.add(id, 0, "우유가 싫어요.", R.drawable.food_milk);
        actionMacro.add(id, 0, "여기 반찬이 맛있어요.", R.drawable.food_dish);
        actionMacro.add(id, 0, "계란 옆에 반찬이 좋아요.", R.drawable.food_dish);

        id = actionGroup.add(1, 0, "일상", R.drawable.daily);
        actionMacro.add(id, 0, "안녕", R.drawable.daily_bye);
        actionMacro.add(id, 0, "미안해", R.drawable.daily_im_sorry);
        actionMacro.add(id, 0, "아니야", R.drawable.daily_no);
        actionMacro.add(id, 0, "고마워", R.drawable.daily_thanks);
        actionMacro.add(id, 0, "맞아", R.drawable.daily_thats_right);
        actionMacro.add(id, 0, "잘 가", R.drawable.daily_bye);
        actionMacro.add(id, 0, "싫어요", R.drawable.daily_i_dont_like_it);
        actionMacro.add(id, 0, "좋아요", R.drawable.daily_good);
        actionMacro.add(id, 0, "친구야", R.drawable.daily_friend);
        actionMacro.add(id, 0, "친구야, 고마워.", R.drawable.daily_thanks);
        actionMacro.add(id, 0, "책이 싫어요.", R.drawable.daily_i_dont_like_it);
        actionMacro.add(id, 0, "오빠, 미안해.", R.drawable.daily_im_sorry);
        actionMacro.add(id, 0, "누나, 아이스크림이 좋아요.", R.drawable.daily_good);
        actionMacro.add(id, 0, "형, 잘가.", R.drawable.daily_bye);
        actionMacro.add(id, 0, "흰색 비눗방울이 아니야.", R.drawable.daily_no);
        actionMacro.add(id, 0, "음악이 좋아요.", R.drawable.daily_good);
        actionMacro.add(id, 0, "할머니가 준 우유가 좋아요.", R.drawable.daily_good);
        actionMacro.add(id, 0, "친구야, 잘 가.", R.drawable.daily_bye);
        actionMacro.add(id, 0, "언니, 미안해.", R.drawable.daily_im_sorry);

        id = actionGroup.add(1, 0, "장소", R.drawable.place);
        actionMacro.add(id, 0, "교회", R.drawable.place_church);
        actionMacro.add(id, 0, "수영장", R.drawable.place_swimming_pool);
        actionMacro.add(id, 0, "학교", R.drawable.place_school);
        actionMacro.add(id, 0, "집", R.drawable.place_house);
        actionMacro.add(id, 0, "도서관", R.drawable.place_library);
        actionMacro.add(id, 0, "열람실", R.drawable.place_library_reading_room);
        actionMacro.add(id, 0, "멀티미디어실", R.drawable.place_library_multimedia);
        actionMacro.add(id, 0, "영화관", R.drawable.place_theater);
        actionMacro.add(id, 0, "공항", R.drawable.place_airport);
        actionMacro.add(id, 0, "병원", R.drawable.place_hospital);
        actionMacro.add(id, 0, "집에 갈래요.", R.drawable.place_house);
        actionMacro.add(id, 0, "엄마, 병원이 무서워요.", R.drawable.place_hospital);
        actionMacro.add(id, 0, "저기는 공항입니다.", R.drawable.place_airport);
        actionMacro.add(id, 0, "영화관에 갈래요.", R.drawable.place_theater);
        actionMacro.add(id, 0, "여기는 멀티미디어실입니다.", R.drawable.place_library_multimedia);
        actionMacro.add(id, 0, "교회 옆의 병원이 싫어요.", R.drawable.place_hospital);
        actionMacro.add(id, 0, "수영장이 좋아요.", R.drawable.place_swimming_pool);
        actionMacro.add(id, 0, "여기는 할머니 집입니다. ", R.drawable.place_house);

        id = actionGroup.add(1, 0, "표현", R.drawable.expression);
        actionMacro.add(id, 0, "화장실에 가고 싶어요.", R.drawable.expression_i_want_to_go_toilet);
        actionMacro.add(id, 0, "갈래요", R.drawable.expression_i_want_to_leave);
        actionMacro.add(id, 0, "목이 말라요", R.drawable.expression_i_thirsts);
        actionMacro.add(id, 0, "배가 고파요", R.drawable.expression_i_am_hungry);
        actionMacro.add(id, 0, "다했어요", R.drawable.expression_i_am_done);
        actionMacro.add(id, 0, "그만할래요", R.drawable.expression_i_want_to_stop);
        actionMacro.add(id, 0, "더 하고 싶어요", R.drawable.expression_i_want_more);

        write_lock.unlock();
    }

    public static final class SQL implements BaseColumns {
        public static final String TABLE_NAME = "LocalPreset";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" +
                        " )";
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    private void delete_flag() {
        actionMain.getDB().execSQL(SQL.SQL_DELETE_ENTRIES);
    }

    // 프리셋 삽입 마크 여부 확인 - 존재시
    private void mark_flag() {
        write_lock.lock();
        SQLiteDatabase db = actionMain.getDB();

        // 디버그 자료형식 삽입 여부 확인 - 존재시 생성 생략, 없다면 생성 후 마킹
        db.execSQL(SQL.SQL_CREATE_ENTRIES);
        Cursor c = db.rawQuery("SELECT " + SQL._ID + " FROM " + SQL.TABLE_NAME + " WHERE " + SQL._ID + "=1", null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            write_lock.unlock();
            return;
        }

        ContentValues record = new ContentValues();
        record.put(SQL._ID, 1);
        db.insert(SQL.TABLE_NAME, null, record);
        c.close();
        record.clear();
        write_lock.unlock();
    }

}
