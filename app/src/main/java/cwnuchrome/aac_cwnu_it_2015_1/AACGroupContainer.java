package cwnuchrome.aac_cwnu_it_2015_1;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.ceil;

/**
 * Created by Chrome on 5/9/15.
 */
public class AACGroupContainer {
    protected LinearLayout mainLayout;
    protected GridLayout menuLayout;
//    protected LinearLayout menuLayout;
    protected Context context;
    protected TextView titleView;
    protected ArrayList<View> contentList;
//    protected ArrayList<ActionItem.Button> contentList;
    protected ActionDBHelper actDBHelper;
    protected ArrayList<ActionWord.Button.onClickClass> wordOCCList;
    protected ActionMain actionMain;
    protected long currentGroupID;
    protected TextToSpeech TTS;

    protected int checkBoxMargin;
    protected int checkBoxWidth;
    protected int imageWidth;

    protected final int DURATION = 200;

    boolean isFolded;
    protected AnimatorSet foldAniSet;

    public AACGroupContainer(LinearLayout mainLayout) {
        this.context = mainLayout.getContext();
        actDBHelper = new ActionDBHelper(context);
        this.mainLayout = mainLayout;
        contentList = new ArrayList<View>();
//        contentList = new ArrayList<ActionItem.Button>();
        actionMain = ActionMain.getInstance();
        TTS = new TextToSpeech(context, new TTSListener()); // TODO: Make a option to turn off/on TTS?
        actionMain.containerRef = this;

        checkBoxMargin = 0;

        // 그룹 제목 TextView 설정
        titleView = (TextView)(mainLayout.findViewById(R.id.groupTitle));

        // 메뉴 레이아웃 설정
        menuLayout = (GridLayout)(mainLayout.findViewById(R.id.AACMenuLayout));
    }

    public void exploreGroup(long id) {
        currentGroupID = id;

        for (View item : contentList) menuLayout.removeView(item);
//        for (ActionItem.Button item : contentList) menuLayout.removeView(item);
        contentList.clear();

        SQLiteDatabase db = actDBHelper.getWritableDatabase();
        Cursor c;
        String groupName;
        long parentGroupID;
        long cursorCount;
        ContentValues values = new ContentValues();

        // 그룹 이름 가져오기
        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME,
                new String[] {ActionGroup.SQL.COLUMN_NAME_WORD, ActionGroup.SQL.COLUMN_NAME_PARENT_ID, ActionGroup.SQL.COLUMN_NAME_PICTURE},
                ActionGroup.SQL._ID + " = " + id,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();
        groupName = c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD));
        parentGroupID = c.getLong(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PARENT_ID));
        c.close();

        // 그룹 제목 출력
        titleView.setText(groupName + " 그룹");

        // 쿼리 옵션 설정
        String[] projection = {
                ActionWord.SQL._ID,
                ActionWord.SQL.COLUMN_NAME_PRIORITY,
                ActionWord.SQL.COLUMN_NAME_WORD,
                ActionWord.SQL.COLUMN_NAME_PICTURE
        };
        String sortOrder =
                ActionWord.SQL.COLUMN_NAME_PRIORITY + " DESC";
        String queryClause = ActionItem.SQL.COLUMN_NAME_PARENT_ID  + " = " + id; // 검색 조건

        // 워드 쿼리
        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, // The table to query
                projection, // The columns to return
                queryClause, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                sortOrder // The sort order
        );
        c.moveToFirst();

        cursorCount = c.getCount();

        for (int i = 0; i < cursorCount; i++) {
            long itemId = c.getLong(
                    c.getColumnIndexOrThrow(ActionWord.SQL._ID)
            );

            ActionWord.Button rowText = new ActionWord.Button(context, new ActionWord.Button.onClickClass(context), this);

            values.put(ActionWord.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_WORD)));
            values.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PRIORITY)));
            values.put(ActionWord.SQL.COLUMN_NAME_PICTURE, c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PICTURE)));
            rowText.init(values);
            rowText.setContainer(this);
//            rowText.setBackground(Resources..getDrawable(values.getAsLong(ActionWord.SQL.COLUMN_NAME_PICTURE)));

            RelativeLayout item_layout = (RelativeLayout)View.inflate(context, R.layout.aac_item_layout, null);
            RelativeLayout.LayoutParams item_layoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item_layoutParam.addRule(RelativeLayout.RIGHT_OF, R.id.aac_item_checkbox);
            if  (checkBoxMargin > 0) setMargins(item_layout.findViewById(R.id.aac_item_checkbox), 0, checkBoxMargin, 0, 0);
            item_layout.addView(rowText, item_layoutParam);
            rowText.setId(R.id.aac_item_button_id);

            values.clear();
            contentList.add(item_layout);
//            contentList.add(rowText);
            c.moveToNext();
        }


        // 매크로 쿼리
        String[] projectionMacro = {
                ActionMacro.SQL._ID,
                ActionMacro.SQL.COLUMN_NAME_PRIORITY,
                ActionMacro.SQL.COLUMN_NAME_WORD,
                ActionMacro.SQL.COLUMN_NAME_WORDCHAIN,
                ActionMacro.SQL.COLUMN_NAME_PICTURE
        };

        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Macro].TABLE_NAME, // The table to query
                projectionMacro, // The columns to return
                queryClause, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                sortOrder // The sort order
        );
        c.moveToFirst();

        // 매크로 쿼리 처리
        cursorCount = c.getCount();

        for (int i = 0; i < cursorCount; i++) {
            long itemId = c.getLong(
                    c.getColumnIndexOrThrow(ActionMacro.SQL._ID)
            );
            System.out.println("DB Query Result " + i + " = " +
                            itemId + ", " +
                            c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_PRIORITY)) + ", " +
                            c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_WORD))
            );

            ActionMacro.Button rowText = new ActionMacro.Button(context, new ActionMacro.Button.onClickClass(context), this);

            values.put(ActionMacro.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_WORD)));
            values.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN)));
            values.put(ActionMacro.SQL.COLUMN_NAME_PRIORITY, c.getLong(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_PRIORITY)));
            values.put(ActionMacro.SQL.COLUMN_NAME_PICTURE, c.getLong(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_PICTURE)));

            rowText.setContainer(this);
            rowText.init(values);

            RelativeLayout item_layout = (RelativeLayout)View.inflate(context, R.layout.aac_item_layout, null);
            rowText.setId(R.id.aac_item_button_id);
            RelativeLayout.LayoutParams item_layoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item_layoutParam.addRule(RelativeLayout.RIGHT_OF, R.id.aac_item_checkbox);
            if  (checkBoxMargin > 0) setMargins(item_layout.findViewById(R.id.aac_item_checkbox), 0, checkBoxMargin, 0, 0);
            item_layout.addView(rowText, item_layoutParam);

            values.clear();
            contentList.add(item_layout);
//            contentList.add(rowText);
            c.moveToNext();
        }

        // 그룹 쿼리
        String queryClauseGroup =
                ActionGroup.SQL.COLUMN_NAME_PARENT_ID  + " = " + id +
                " AND " + ActionGroup.SQL._ID + " != " + id
                ; // 검색 조건
        c = db.query(
                actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, // The table to query
                projection, // The columns to return
                queryClauseGroup, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                sortOrder // The sort order
        );
        c.moveToFirst();

        // 그룹 쿼리 처리
        cursorCount = c.getCount();

        for (int i = 0; i < cursorCount; i++) {
            long itemId = c.getLong(
                    c.getColumnIndexOrThrow(ActionGroup.SQL._ID)
            );
            System.out.println("DB Query Result " + i + " = " +
                            itemId + ", " +
                            c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PRIORITY)) + ", " +
                            c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD))
            );

            ActionGroup.ActionGroupButton rowText = new ActionGroup.ActionGroupButton(context, new ActionGroup.ActionGroupButton.onClickClass(context), this);

            values.put(ActionGroup.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));
            values.put(ActionGroup.SQL.COLUMN_NAME_PRIORITY, c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PRIORITY));
            values.put(ActionGroup.SQL._ID, itemId);
            values.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, c.getLong(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PICTURE)));

            rowText.setContainer(this);
            rowText.init(values);

            RelativeLayout item_layout = (RelativeLayout)View.inflate(context, R.layout.aac_item_layout, null);
            rowText.setId(R.id.aac_item_button_id);
            RelativeLayout.LayoutParams item_layoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item_layoutParam.addRule(RelativeLayout.RIGHT_OF, R.id.aac_item_checkbox);
            if  (checkBoxMargin > 0) setMargins(item_layout.findViewById(R.id.aac_item_checkbox), 0, checkBoxMargin, 0, 0);

            item_layout.addView(rowText, item_layoutParam);

            values.clear();
            contentList.add(item_layout);
//            contentList.add(rowText);
            c.moveToNext();
        }

        // 마지막으로 상위 그룹에 대한 버튼 형성 (단 최상위 그룹은 패스)
        if (id != 1) {
            c = db.query(
                    actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, // The table to query
                    projection, // The columns to return
                    ActionGroup.SQL._ID + " = " + parentGroupID, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    sortOrder // The sort order
            );
            c.moveToFirst();

            ActionGroup.ActionGroupButton parentGroupButton = new ActionGroup.ActionGroupButton(context, new ActionGroup.ActionGroupButton.onClickClass(context), this);

            values.put(ActionGroup.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));
            values.put(ActionGroup.SQL.COLUMN_NAME_PRIORITY, c.getLong(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PRIORITY)));
            values.put(ActionGroup.SQL._ID, parentGroupID);
            values.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, c.getLong(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PICTURE)));

            parentGroupButton.setContainer(this);
            parentGroupButton.init(values);
            parentGroupButton.setText("상위그룹 " + c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));

            RelativeLayout item_layout = (RelativeLayout)View.inflate(context, R.layout.aac_item_layout, null);
            parentGroupButton.setId(R.id.aac_item_button_id);
            RelativeLayout.LayoutParams item_layoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item_layoutParam.addRule(RelativeLayout.RIGHT_OF, R.id.aac_item_checkbox);
            item_layout.addView(parentGroupButton, item_layoutParam);
            if  (checkBoxMargin > 0) setMargins(item_layout.findViewById(R.id.aac_item_checkbox), 0, checkBoxMargin, 0, 0);

            values.clear();
            contentList.add(item_layout);
//            contentList.add(parentGroupButton);
            c.close();
        }


        Collections.sort(contentList, new ActionItem.Button.itemComparator()); // 정렬된 두 리스트의 병합 알고리즘은 내가 짜야 할지도?

        for (View btn : contentList) {
            menuLayout.addView(btn);
        }

        if (checkBoxWidth > 0) setFoldAnimation();
    }

    public long addWord(SQLiteDatabase db, ContentValues values) {
        return ((ActionWord) actionMain.itemChain[ActionMain.item.ID_Word]).add(db, values);
    }

    public boolean removeWord(SQLiteDatabase db, String word) {
        return ((ActionWord) actionMain.itemChain[ActionMain.item.ID_Word]).remove(db, word);
    }

    public long getCurrentGroupID() {
        return currentGroupID;
    }

    class TTSListener implements TextToSpeech.OnInitListener {
        public void onInit(int status) {
//            String myText1 = "Hello, World!";
//            String myText2 = "This is Text-to-Speech speaking.";
//            TTS.speak(myText1, TextToSpeech.QUEUE_FLUSH, null, null);
//            TTS.speak(myText2, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    public TextToSpeech getTTS() { return TTS; }

    public void onDestroy() {
        TTS.shutdown();
    }

    public void refresh() { exploreGroup(currentGroupID); }

    protected static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public void initDimInfo() {
        if (contentList.size() > 0) {
            View item = contentList.get(0);
            View v = item.findViewById(R.id.aac_item_checkbox);
            checkBoxMargin = ((ActionItem.Button)item.findViewById(R.id.aac_item_button_id)).image_half_height - v.getHeight() / 2;
            checkBoxWidth = v.getWidth();
            imageWidth = ((ActionItem.Button)item.findViewById(R.id.aac_item_button_id)).image_half_height * 2; // TODO: 바꿀 것
        }

        for (View item : contentList) {
            View v = item.findViewById(R.id.aac_item_checkbox);
            setMargins(v, 0, checkBoxMargin, 0, 0);
        }

        setFoldAnimation();
    }

    public void setFoldAnimation() {
        isFolded = false;

        int listSize = contentList.size();

        if (listSize <= 0) return;

        int column_count = menuLayout.getColumnCount();
        int fold_column_count = column_count;
        if (listSize < column_count) fold_column_count = listSize;

        fold f[] = new fold[fold_column_count];
        move m[] = new move[fold_column_count];

        int side_pass_size = fold_column_count / 2;
        int row_count = (int)ceil(listSize / column_count);
        int last_row_leftover = listSize % column_count;
        float left_mod;
        if (fold_column_count % 2 == 1) left_mod = (-1) * checkBoxWidth;
        else left_mod = 0;

        int pos;
        float mod;

        if (fold_column_count > 1) {

            // Left-pass
            pos = side_pass_size - 1; // 음수인 경우를 고려해야 한다.
            mod = left_mod;
            int m_mod_seg = 0;

            for (int j = 0; j < side_pass_size; j++) {
                int mod_seg = (-1) * checkBoxWidth;
                f[pos] = new fold(mod_seg, mod);
                m[pos] = new move(m_mod_seg, mod);
                mod -= checkBoxWidth;
                m_mod_seg -= checkBoxWidth;
                pos--;
            }

            // Right-pass
            pos = column_count - side_pass_size; // 음수인 경우를 고려해야 한다.
            mod = 0;

            for (int j = 0; j < side_pass_size; j++) {
                f[pos] = new fold(checkBoxWidth, mod);
                m[pos] = new move(checkBoxWidth, mod);
                mod += checkBoxWidth;
                pos++;
            }

        }

        // middle-pass
        if (fold_column_count % 2 == 1) {
            f[side_pass_size] = new fold((-1) * checkBoxWidth, 0);
            m[side_pass_size] = new move(0, 0);
        }

        AnimatorSet list[] = new AnimatorSet[listSize * 2];
        pos = 0;
        int listPos = 0;
        for (View v : contentList) {
            ActionItem.Button btn = (ActionItem.Button)v.findViewById(R.id.aac_item_button_id);
            CheckBox cbox = (CheckBox)v.findViewById(R.id.aac_item_checkbox);

            list[listPos++] = f[pos].getAs(cbox);
            list[listPos++] = m[pos].getAs(btn);

            pos = (pos + 1) % fold_column_count;
        }

        foldAniSet = new AnimatorSet();
        foldAniSet.playTogether(list);
    }

    public void fold() {
        foldAniSet.start();
    }


    protected class move {
        protected ObjectAnimator oa_l;
        protected AnimatorSet as;
        protected Path p;
        protected float mod;
        protected int width;

        public move(int width, float mod) {
            this.mod = mod;
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            this.width = width;

            p = new Path();
            p.moveTo(0f, 0f);
            p.lineTo(-0.5f * width - mod, 0f); // TODO: 부호 이용 개조 (후진/전진 가능하게)
        }

        public AnimatorSet getAs(View v) {
            oa_l = ObjectAnimator.ofFloat(v, View.TRANSLATION_X, View.TRANSLATION_Y, p);
            oa_l.setDuration(DURATION);
            oa_l.setInterpolator(AnimationUtils.loadInterpolator(context, android.R.interpolator.linear));

            as = new AnimatorSet();
            as.playTogether(oa_l);

            return as;
        }

        public void toReverse() {
            p.reset();
            p.moveTo(-0.5f * width - mod, 0f);
            p.lineTo(0f, 0f);
        }
    }

    protected class fold extends move {
        protected ObjectAnimator oa_s;
        protected ObjectAnimator oa_a;
        protected AnimatorSet as;
        protected Path s;
        protected float a[];
        protected float mod;
        protected int width;

        public fold(int width, float mod) {
            super(width, mod);

            s = new Path();
            s.moveTo(1f, 1f);
            s.lineTo(0f, 1f);

            a = new float[2];
            a[0] = 1f;
            a[1] = 0f;
        }

        public AnimatorSet getAs(View v) {
            oa_s = ObjectAnimator.ofFloat(v, View.SCALE_X, View.SCALE_Y, s);
            oa_a = ObjectAnimator.ofFloat(v, View.ALPHA, a[0], a[1]);
            oa_s.setDuration(DURATION);
            oa_a.setDuration(DURATION);
            oa_s.setInterpolator(AnimationUtils.loadInterpolator(context, android.R.interpolator.linear));
            oa_a.setInterpolator(AnimationUtils.loadInterpolator(context, android.R.interpolator.linear));

            as = new AnimatorSet();
            as.playTogether(oa_a, oa_s, super.getAs(v));

            return as;
        }

        public void toReverse() {
            s.reset();
            p.reset();

            s.moveTo(0f, 1f);
            s.lineTo(1f, 1f);

            a[0] = 0f;
            a[1] = 1f;

            p.moveTo(-0.5f * width - mod, 0f);
            p.lineTo(0f, 0f);
        }

    }
}
