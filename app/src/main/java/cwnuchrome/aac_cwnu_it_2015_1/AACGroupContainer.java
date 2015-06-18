package cwnuchrome.aac_cwnu_it_2015_1;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;
import android.speech.tts.TextToSpeech;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by Chrome on 5/9/15.
 */
public class AACGroupContainer {
    protected LinearLayout mainLayout;
    protected GridLayout menuLayout;
    protected Context context;
    protected TextView titleView;
    protected ArrayList<View> contentList;
    protected ArrayList<View> selectedList;
    protected ActionDBHelper actDBHelper;
    protected ArrayList<ActionWord.Button.onClickClass> wordOCCList;
    protected ActionMain actionMain;
    protected long currentGroupID;
    protected TextToSpeech TTS;

    protected ArrayList<CheckBox> checkBoxes;

    protected final int DURATION = 200;

    protected boolean isFolded;
    protected AnimatorSet foldAniSet;
    protected AnimatorSet foldAniSet_reverse;

    protected int removeDepArray[];

    public AACGroupContainer(LinearLayout mainLayout) {
        this.context = mainLayout.getContext();
        actDBHelper = new ActionDBHelper(context);
        this.mainLayout = mainLayout;
        contentList = new ArrayList<View>();
        actionMain = ActionMain.getInstance();
        TTS = new TextToSpeech(context, new TTSListener());
        actionMain.containerRef = this;

        checkBoxes = new ArrayList<CheckBox>();
        selectedList = new ArrayList<View>();

        removeDepArray = null;

        // 그룹 제목 TextView 설정
        titleView = (TextView)(mainLayout.findViewById(R.id.groupTitle));

        // 메뉴 레이아웃 설정
        menuLayout = (GridLayout)(mainLayout.findViewById(R.id.AACMenuLayout));
    }

    public void exploreGroup(long id) {
        currentGroupID = id;

        for (View item : contentList) menuLayout.removeView(item);
        contentList.clear();
        checkBoxes.clear();
        selectedList.clear();

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

            values.put(ActionItem.SQL._ID, itemId);
            values.put(ActionWord.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_WORD)));
            values.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PRIORITY)));
            values.put(ActionWord.SQL.COLUMN_NAME_PICTURE, c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PICTURE)));

            addMenuWithCheckBox(
                    new ActionWord.Button(context, new ActionWord.Button.onClickClass(context), this),
                    values);

            values.clear();
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

            values.put(ActionItem.SQL._ID, itemId);
            values.put(ActionMacro.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_WORD)));
            values.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN)));
            values.put(ActionMacro.SQL.COLUMN_NAME_PRIORITY, c.getLong(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_PRIORITY)));
            values.put(ActionMacro.SQL.COLUMN_NAME_PICTURE, c.getLong(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_PICTURE)));

            addMenuWithCheckBox(
                    new ActionMacro.Button(context, new ActionMacro.Button.onClickClass(context), this),
                    values);

            values.clear();
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

            values.put(ActionGroup.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));
            values.put(ActionGroup.SQL.COLUMN_NAME_PRIORITY, c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PRIORITY));
            values.put(ActionGroup.SQL._ID, itemId);
            values.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, c.getLong(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PICTURE)));

            addMenuWithCheckBox(
                    new ActionGroup.Button(context, new ActionGroup.Button.onClickClass(context), this),
                    values);

            values.clear();
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

            ActionGroup.Button parentGroupButton = new ActionGroup.Button(context, new ActionGroup.Button.onClickClass(context), this);

            values.put(ActionGroup.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));
            values.put(ActionGroup.SQL.COLUMN_NAME_PRIORITY, c.getLong(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PRIORITY)));
            values.put(ActionGroup.SQL._ID, parentGroupID);
            values.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, c.getLong(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PICTURE)));

            addMenuWithoutCheckBox(parentGroupButton, values);

            parentGroupButton.setText("상위그룹 " + c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));

            values.clear();
            c.close();
        }


        Collections.sort(contentList, new ActionItem.Button.itemComparator()); // 정렬된 두 리스트의 병합 알고리즘은 내가 짜야 할지도?

        for (View btn : contentList) {
            menuLayout.addView(btn);
        }

        setFoldAnimation();
    }

    void addMenuWithCheckBox(ActionItem.Button btn, ContentValues values) {
        btn.init(values);
        btn.setContainer(this);

        LinearLayout item_layout = (LinearLayout) View.inflate(context, R.layout.aac_item_layout, null);
        CheckBox checkBox = (CheckBox)item_layout.findViewById(R.id.aac_item_checkbox);
        item_layout.addView(btn);
        btn.setId(R.id.aac_item_button_id);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                View aac_btn = ((LinearLayout) buttonView.getParent()).findViewById(R.id.aac_item_button_id);

                if (isChecked) {
                    selectedList.add((LinearLayout) buttonView.getParent());
                } else {
                    selectedList.remove((LinearLayout) buttonView.getParent());
                }
            }
        });

        checkBoxes.add(checkBox);
        contentList.add(item_layout);
    }

    void addMenuWithoutCheckBox(ActionItem.Button btn, ContentValues values) {
        btn.init(values);
        btn.setContainer(this);

        LinearLayout item_layout = (LinearLayout) View.inflate(context, R.layout.aac_item_layout, null);
        CheckBox checkBox = (CheckBox)item_layout.findViewById(R.id.aac_item_checkbox);
        item_layout.addView(btn);
        btn.setId(R.id.aac_item_button_id);

        checkBox.setVisibility(View.INVISIBLE);
        checkBox.setEnabled(false);

        contentList.add(item_layout);
    }

    public long addWord(SQLiteDatabase db, ContentValues values) {
        return actionMain.itemChain[ActionMain.item.ID_Word].add(db, values);
    }

    public boolean removeWord(SQLiteDatabase db, String word) {
        return actionMain.itemChain[ActionMain.item.ID_Word].remove(db, word);
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

    public void setFoldAnimation() {
        isFolded = false;

        int listSize = contentList.size();

        if (listSize <= 0) return;

        AnimatorSet list[] = new AnimatorSet[listSize];

        int listPos = 0;
//        Path p = new Path();
//        p.moveTo(1f, 1f);
//        p.lineTo(0f, 0f);
        for (View v : contentList) {
            CheckBox cbox = (CheckBox)v.findViewById(R.id.aac_item_checkbox);
            list[listPos] = new AnimatorSet();
            list[listPos].playTogether(
//                    ObjectAnimator.ofFloat(cbox, View.SCALE_X, View.SCALE_Y, p), // Over API Lv 21
                    ObjectAnimator.ofFloat(cbox, View.SCALE_X, 1f, 0f),
                    ObjectAnimator.ofFloat(cbox, View.SCALE_Y, 1f, 0f),
                    ObjectAnimator.ofFloat(cbox, View.ALPHA, 1f, 0f)
            );
            list[listPos].setDuration(DURATION);
            listPos++;
        }

        foldAniSet = new AnimatorSet();
        foldAniSet.playTogether(list);
        foldAniSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                for (CheckBox cbox : checkBoxes) cbox.setVisibility(View.INVISIBLE);
                deselectAll();
                isFolded = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        listPos = 0;
//        p.reset();
//        p.moveTo(0f, 0f);
//        p.lineTo(1f, 1f);
        for (View v : contentList) {
            CheckBox cbox = (CheckBox)v.findViewById(R.id.aac_item_checkbox);
            list[listPos] = new AnimatorSet();
            list[listPos].playTogether(
//                    ObjectAnimator.ofFloat(cbox, View.SCALE_X, View.SCALE_Y, p), // Over API Lv 21
                    ObjectAnimator.ofFloat(cbox, View.SCALE_X, 0f, 1f),
                    ObjectAnimator.ofFloat(cbox, View.SCALE_Y, 0f, 1f),
                    ObjectAnimator.ofFloat(cbox, View.ALPHA, 0f, 1f)
            );
            list[listPos].setDuration(DURATION);
            listPos++;
        }

        foldAniSet_reverse = new AnimatorSet();
        foldAniSet_reverse.playTogether(list);
        foldAniSet_reverse.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (CheckBox cbox : checkBoxes) cbox.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isFolded = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        // Setting default folded status
        for (View v : contentList) {
            CheckBox cbox = (CheckBox)v.findViewById(R.id.aac_item_checkbox);

            cbox.setVisibility(View.INVISIBLE);
            cbox.setScaleX(0f);
            cbox.setScaleY(0f);
        }

        isFolded = true;
    }

    public void toggleFold() {
        if (!(foldAniSet.isRunning() || foldAniSet_reverse.isRunning())) {
            if (isFolded) foldAniSet_reverse.start();
            else  foldAniSet.start();
        }
    }

    public void selectAll() {
        for (View v : contentList) {
            selectedList.add(v);
            CheckBox cbox = ((CheckBox)v.findViewById(R.id.aac_item_checkbox));
            cbox.setChecked(true);
        }
    }

    public void deselectAll() {
        // 왜인지는 모르겠으나 아래의 코드는 동작하지 않음.
//        for (View v : selectedList) ((CheckBox)v.findViewById(R.id.aac_item_checkbox)).setChecked(false);

        View selListAry[] = selectedList.toArray(new View[selectedList.size()]);
        for (View v : selListAry) ((CheckBox)v.findViewById(R.id.aac_item_checkbox)).setChecked(false);

        selectedList.clear();
    }

    public void removeSelected() {
        if (selectedList.size() == 0) return;

        SQLiteDatabase db = actDBHelper.getWritableDatabase();
        boolean dependencyWarning = false;

        Collections.sort(selectedList, new Comparator<View>() {
            @Override
            public int compare(View lhs, View rhs) {
                int lhs_id = ((ActionItem.Button)lhs.findViewById(R.id.aac_item_button_id)).onClickObj.itemID;
                int rhs_id = ((ActionItem.Button)rhs.findViewById(R.id.aac_item_button_id)).onClickObj.itemID;
                return lhs_id < rhs_id ? -1 : lhs_id > rhs_id ? 1 : 0;
            }
        });

        for (View v : selectedList) {
            System.out.println(((ActionItem.Button)v.findViewById(R.id.aac_item_button_id)).onClickObj.itemID);
        }

        for (View v : selectedList) {
            ActionItem.Button.onClickClass occ = ((ActionItem.Button)v.findViewById(R.id.aac_item_button_id)).onClickObj;
            String word = occ.phonetic;

            if (occ.itemCategoryID == ActionMain.item.ID_Word) {
                Cursor c;

                String[] projection = {
                        ActionMacro.SQL._ID
                };

                // 단어의 ID 찾기

                String wordQueryClause = ActionWord.SQL.COLUMN_NAME_WORD  + " = '" + word + "'"; // 검색 조건
                c = db.query(
                        actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, // The table to query
                        projection, // The columns to return
                        wordQueryClause, // The columns for the WHERE clause
                        null, // The values for the WHERE clause
                        null, // don't group the rows
                        null, // don't filter by row groups
                        null // The sort order
                );
                c.moveToFirst();

                int wordID = c.getInt(c.getColumnIndexOrThrow(ActionGroup.SQL._ID));
                c.close();

                // 이 단어를 사용하는 매크로가 현재 탐색 중인 그룹 외의 그룹에 존재하는지 먼저 확인

                String macroQueryClause_NotCurrentGroup = ActionMacro.SQL.COLUMN_NAME_WORDCHAIN  + " LIKE '%:" + wordID + ":%'" +
                        " AND " + ActionMacro.SQL.COLUMN_NAME_PARENT_ID + " != " + currentGroupID;
                c = db.query(
                        actionMain.itemChain[ActionMain.item.ID_Macro].TABLE_NAME, // The table to query
                        projection, // The columns to return
                        macroQueryClause_NotCurrentGroup, // The columns for the WHERE clause
                        null, // The values for the WHERE clause
                        null, // don't group the rows
                        null, // don't filter by row groups
                        null // The sort order
                );
                c.moveToFirst();

                // 의존성이 있는 매크로가 있음 : 경고 필요
                if (c.getCount() > 0) dependencyWarning = true; // 추후 확장 가능

                c.close();

                // 이 단어를 사용하는 매크로가 현재 탐색 중인 그룹에 있는지 확인

                String macroQueryClause_CurrentGroup = ActionMacro.SQL.COLUMN_NAME_WORDCHAIN  + " LIKE '%:" + wordID + ":%'" +
                        " AND " + ActionMacro.SQL.COLUMN_NAME_PARENT_ID + " = " + currentGroupID;
                String sortOrder =
                        ActionWord.SQL._ID + " ASC";
                c = db.query(
                        actionMain.itemChain[ActionMain.item.ID_Macro].TABLE_NAME, // The table to query
                        projection, // The columns to return
                        macroQueryClause_CurrentGroup, // The columns for the WHERE clause
                        null, // The values for the WHERE clause
                        null, // don't group the rows
                        null, // don't filter by row groups
                        sortOrder // The sort order
                );
                c.moveToFirst();

                // 해당 매크로가 있다면, 그 매크로가 삭제 대상인지 확인
                if (c.getCount() > 0) {
                    int IDCol = c.getColumnIndexOrThrow(ActionGroup.SQL._ID);
                    int ciID = c.getInt(IDCol);
                    boolean endOfCursor = false;
                    boolean foundMacro = false;

                    // 선택된 아이템을 순차 검색하며 확인
                    for (View vi : selectedList) {
                        ActionItem.Button btn = (ActionItem.Button)vi.findViewById(R.id.aac_item_button_id);
                        ActionItem.Button.onClickClass viOCC = btn.onClickObj;

                        // 매크로만 검색 - 나머지는 넘김
                        if (viOCC.itemCategoryID != ActionMain.item.ID_Macro) continue;

                        // 현재 아이템의 ID값이 커서의 현재 아이템의 ID값보다 크다면?
                        while (ciID < viOCC.itemID) {
                            // 커서가 맨 끝에 도달하지 않은 이상 커서를 뒤로 계속 넘긴다.
                            if (c.isLast()) {
                                endOfCursor = true;
                                break;
                            }
                            else {
                                c.moveToNext();
                                ciID = c.getInt(IDCol);
                            }
                        }

                        // 동일 ID값 혹은 더 큰 ID값에 도달하지 못했는데 커서의 끝에 도달 : 선택된 리스트 중 의존성을 가지는 매크로가 포함되어 있지 않으므로 경고 표시
                        if (endOfCursor) {
                            dependencyWarning = true;
                            break;
                        }

                        // 커서의 ID값이 현재 아이템의 ID값보다 크다 : 더 높은 ID값을 가지는 아이템을 찾아 리스트의 다음으로 넘어감.
                        if (ciID > viOCC.itemID) continue;

                        // 같은 ID값을 지니는 매크로를 선택 리스트에서 찾음.
                        if (ciID == viOCC.itemID) {
                            foundMacro = true;
                            break;
                        }

                    }


                    // 의존성을 가지는 매크로가 있으나 선택 리스트에서 빠져 있음 - 의존성 경고 표시 필요
                    if (!foundMacro) {
                        dependencyWarning = true;

                        c.moveToFirst();
                        removeDepArray = new int[c.getCount()];
                        for (int i = 0; i < c.getCount(); i++) {
                            removeDepArray[i] = c.getInt(IDCol);
                            c.moveToNext();
                        }
                    }

                }

                c.close();
                // TODO: 중복 검색의 문제가 발생할 수 있음. (예: 한 매크로가 A+B+C이고 A, B, C에 맞춰 각각 검색시 이 매크로는 3번 중복검사됨)
            }

        }

        if (dependencyWarning) {
            System.out.println("Dependency warning!"); // TODO: 의존성 문제를 유저에게 확인받도록 함.
        }

        for (View v : selectedList) {
            ActionItem.Button.onClickClass occ = ((ActionItem.Button)v.findViewById(R.id.aac_item_button_id)).onClickObj;
            actionMain.itemChain[occ.itemCategoryID].removeWithID(db, occ.itemID);
        }

        // 선택 리스트에는 없으나 삭제 대상에 의존성을 가지는 매크로는 모두 삭제
        if (removeDepArray != null) {
            for (int i : removeDepArray) {
                actionMain.itemChain[ActionMain.item.ID_Macro].removeWithID(db, i);
            }
            removeDepArray = null;
        }

        db.close();
        exploreGroup(currentGroupID);
    }

    public void test() {
        for (View item : selectedList)  {
            System.out.println(((ActionItem.Button)(item.findViewById(R.id.aac_item_button_id))).onClickObj.phonetic);
        }
    }
}
