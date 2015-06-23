package cwnuchrome.aac_cwnu_it_2015_1;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Chrome on 5/9/15.
 * AAC 레이아웃 객체
 */
public class AACGroupContainer {
    protected LinearLayout mainLayout;
    protected GridLayout menuLayout;
    protected Context context;
    protected TextView titleView;
    protected ArrayList<View> contentList;
    protected ArrayList<View> selectedList;
    protected ActionDBHelper actDBHelper;
    protected ActionMain actionMain;
    protected long currentGroupID;
    protected TextToSpeech TTS;

    protected ArrayList<CheckBox> checkBoxes;

    protected final int DURATION = 200;

    protected boolean isFolded;
    protected AnimatorSet foldAniSet;
    protected AnimatorSet foldAniSet_reverse;

    protected ArrayList<ContentValues> removeDepArray;
    RemovalListBundle removalListBundle;

    public AACGroupContainer(LinearLayout mainLayout) {
        this.context = mainLayout.getContext();
        actDBHelper = new ActionDBHelper(context);
        this.mainLayout = mainLayout;
        contentList = new ArrayList<>();
        actionMain = ActionMain.getInstance();
        TTS = new TextToSpeech(context, new TTSListener());
        actionMain.containerRef = this;

        checkBoxes = new ArrayList<>();
        selectedList = new ArrayList<>();

        removeDepArray = new ArrayList<>();
        removalListBundle = new RemovalListBundle();

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

        isFolded = false;

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
                if (isChecked) {
                    selectedList.add((LinearLayout) buttonView.getParent());
                } else {
                    View v = (View)buttonView.getParent();
                    selectedList.remove(v);
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

    public long getCurrentGroupID() {
        return currentGroupID;
    }

    class TTSListener implements TextToSpeech.OnInitListener {
        public void onInit(int status) {}
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
            CheckBox cbox = ((CheckBox)v.findViewById(R.id.aac_item_checkbox));
            if (!cbox.isEnabled() || cbox.isChecked()) continue;
            selectedList.add(v);
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

    // 단순한 아이템 삭제가 이렇게 길어질 줄 누가 알았으랴...
    public void removeSelected() {
        if (selectedList.size() == 0) return;

        removalListBundle.clear();

        for (View v : selectedList)
            removalListBundle.addByOCC(((ActionItem.Button) v.findViewById(R.id.aac_item_button_id)).onClickObj);
        removalListBundle.printList();

        System.out.println("List of non-selected dependencies:");
        for (ContentValues v : removeDepArray)
            System.out.println(v.getAsString(ActionItem.SQL._ID));
        System.out.println("End of the list");

        if (!removalListBundle.checkNoDependencyLeft()) ((MainActivity) context).confirmDependency();
        else invokeRemoval();
    }

    public void invokeRemoval() {
        removalListBundle.execRemoval();

        // 선택 리스트에는 없으나 삭제 대상에 의존성을 가지는 매크로는 모두 삭제
        SQLiteDatabase db = actDBHelper.getWritableDatabase();
        for (ContentValues v : removeDepArray) {
            actionMain.itemChain[ActionMain.item.ID_Macro].removeWithID(db, v.getAsInteger(ActionItem.SQL._ID));
        }
        removeDepArray.clear();
        db.close();

        ((MainActivity)context).revertMenu();

        exploreGroup(currentGroupID);
    }

    protected class RemovalListBundle {
        protected ArrayList<Integer> wordList;
        protected ArrayList<Integer> macroList;
        protected ArrayList<Integer> groupList;
        protected String[] projection;

        public RemovalListBundle() {
            wordList = new ArrayList<>(); // 자바 버전에 따른 컴파일 에러 발생 가능
            macroList = new ArrayList<>();
            groupList = new ArrayList<>();
            projection = new String[] { ActionItem.SQL._ID };
        }

        public void addGroup(int id) {
            groupList.add(id);

            Cursor c;
            int c_count;
            int c_col;
            SQLiteDatabase db = actDBHelper.getWritableDatabase();
            String whereClause = ActionItem.SQL.COLUMN_NAME_PARENT_ID  + " = " + id;

            // 해당 그룹 내의 그룹 처리
            c = db.query(
                    actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, // The table to query
                    projection, // The columns to return
                    whereClause, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
            );
            c.moveToFirst();

            c_count = c.getCount();
            c_col = c.getColumnIndexOrThrow(ActionItem.SQL._ID);
            if (c_count > 0) {
                for (int i = 0; i < c_count; i++) {
                    int i_id = c.getInt(c_col);
                    addGroup(i_id); // 재귀 호출
                    c.moveToNext();
                }
            }

            c.close();


            // 해당 그룹 내의 매크로 처리
            c = db.query(
                    actionMain.itemChain[ActionMain.item.ID_Macro].TABLE_NAME, // The table to query
                    projection, // The columns to return
                    whereClause, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
            );
            c.moveToFirst();

            c_count = c.getCount();
            c_col = c.getColumnIndexOrThrow(ActionItem.SQL._ID);
            if (c_count > 0) {
                for (int i = 0; i < c_count; i++) {
                    int i_id = c.getInt(c_col);
                    addMacro(i_id);
                    c.moveToNext();
                }
            }

            c.close();


            // 해당 그룹 내의 워드 처리
            c = db.query(
                    actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME, // The table to query
                    projection, // The columns to return
                    whereClause, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
            );
            c.moveToFirst();

            c_count = c.getCount();
            c_col = c.getColumnIndexOrThrow(ActionItem.SQL._ID);
            if (c_count > 0) {
                for (int i = 0; i < c_count; i++) {
                    int i_id = c.getInt(c_col);
                    addWord(i_id);
                    c.moveToNext();
                }
            }

            c.close();
        }

        public void addMacro(int id) {
            macroList.add(id);
        }

        public void addWord(int id) {
            wordList.add(id);
        }

        public void addByOCC(ActionItem.Button.onClickClass occ) {
            switch (occ.itemCategoryID) {
                case ActionMain.item.ID_Group:
                    addGroup(occ.itemID);
                    break;

                case ActionMain.item.ID_Macro:
                    addMacro(occ.itemID);
                    break;

                case ActionMain.item.ID_Word:
                    addWord(occ.itemID);
                    break;
            }
        }

        // 의존성 여부 검사
        public boolean checkNoDependencyLeft() {
            removeDepArray.clear();
            if (wordList.size() == 0) return true;

            /* 지울 단어들에 대해 의존성을 가지는 매크로들을 찾기 위한 쿼리문의 작성 */

            Iterator<Integer> i = wordList.iterator();
            int id = i.next();

            final String OR = " OR ";
            final String LIKE_AND_HEAD = " LIKE '%:";
            final String TAIL = ":%'";

            StringBuilder qBuilder = new StringBuilder();
            qBuilder.append(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN);
            qBuilder.append(LIKE_AND_HEAD);
            qBuilder.append(id);
            qBuilder.append(TAIL);

            // 매 단어마다 조건문 확장
            while (i.hasNext()) {
                id = i.next();
                qBuilder.append(OR);
                qBuilder.append(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN);
                qBuilder.append(LIKE_AND_HEAD);
                qBuilder.append(id);
                qBuilder.append(TAIL);
            }

            String whereClause = qBuilder.toString(); // 완성된 조건문을 String으로 변환
            String sortOrder = ActionWord.SQL._ID + " ASC"; // 이후의 알고리즘을 위해 정렬 순서는 ID 기준 오름차순
            String projection[] = { ActionItem.SQL._ID, ActionItem.SQL.COLUMN_NAME_WORD };

            Cursor c;
            int c_count;
            int c_id_col;
            SQLiteDatabase db = actDBHelper.getWritableDatabase();
            c = db.query( // 삭제 대상 워드에 대한 의존성이 있는 모든 매크로가 이 커서에 담김
                    actionMain.itemChain[ActionMain.item.ID_Macro].TABLE_NAME, // The table to query
                    projection, // The columns to return
                    whereClause, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    sortOrder // The sort order
            );
            c.moveToFirst();

            /* 삭제 대상으로 선택된 매크로와 앞서 찾아낸 매크로 목록과의 대조 */
            /* - 앞서 찾아낸 매크로의 집합은 반드시 선택된 매크로 대상의 집합에 포함된 관계여야 한다. */

            c_count = c.getCount();
            c_id_col = c.getColumnIndexOrThrow(ActionItem.SQL._ID);
            int c_word_col = c.getColumnIndexOrThrow(ActionItem.SQL.COLUMN_NAME_WORD);

            boolean dependencyProper = true;

            if (c_count > 0) {
                int c_id = c.getInt(c_id_col);

                Collections.sort(macroList); // 선택된 매크로 목록도 오름차순으로 정렬

                boolean endOfCursor = false;

                // 선택된 아이템을 순차 검색하며 확인
                for (int j : macroList) {
                    // 현재 아이템의 ID값이 커서의 현재 아이템의 ID값보다 크다면?
                    while (c_id < j) {
                        // 커서에 있는 아이템이 macroList에 없다: 의존성 문제가 있는 아이템이므로 removeDepArray에 추가
                        ContentValues values = new ContentValues();
                        values.put(ActionItem.SQL.COLUMN_NAME_WORD, c.getString(c_word_col));
                        values.put(ActionItem.SQL._ID, c_id);
                        removeDepArray.add(values);

                        // 커서가 맨 끝에 도달하지 않은 이상 커서를 뒤로 계속 넘긴다.
                        if (c.isLast()) {
                            endOfCursor = true;
                            break;
                        }

                        c.moveToNext();
                        c_id = c.getInt(c_id_col);
                    }

                    if (endOfCursor) break; // 커서가 맨 끝에 도달했으므로 더 이상 대조할 대상이 없음. 루프 종료.

                    // 같은 ID값을 지니는 매크로를 선택 리스트에서 찾았음.
                    if (c_id == j) {
                        // 의존성을 지니는 매크로가 제대로 선택 리스트에도 있음. 따라서 커서를 다음 항목으로 진행시킨 후, for 루프도 다음 회로 넘긴다.
                        if (c.isLast()) {
                            endOfCursor = true;
                            break;
                        }

                        c.moveToNext();
                        c_id = c.getInt(c_id_col);
                    }

                }

                // 커서에 아직 여분이 남음 - 즉 선택된 리스트에 모든 의존성 있는 매크로가 포함되지 않았음을 의미.
                if (!endOfCursor) {
                    dependencyProper = false;
                    while (true) {
                        // 커서에 남은 매크로 아이템들은 모두 의존성이 있으나 선택된 리스트에 없는 아이템들이므로 removeDepArray에 넣는다.
                        ContentValues values = new ContentValues();
                        values.put(ActionItem.SQL.COLUMN_NAME_WORD, c.getString(c_word_col));
                        values.put(ActionItem.SQL._ID, c_id);
                        removeDepArray.add(values);

                        if (c.isLast()) break;

                        c.moveToNext();
                        c_id = c.getInt(c_id_col);
                    }
                }
            }

            c.close();
            return dependencyProper;
        }

        public void printList() {
            System.out.println("Selected item list for removal:");

            System.out.println("Groups -");
            for (int i : groupList) System.out.println(i);

            System.out.println("Macros -");
            for (int i : macroList) System.out.println(i);

            System.out.println("Words -");
            for (int i : wordList) System.out.println(i);

            System.out.println("End of the list");
        }

        public void execRemoval() {
            SQLiteDatabase db = actDBHelper.getWritableDatabase();

            for (int i : macroList) actionMain.itemChain[ActionMain.item.ID_Macro].removeWithID(db, i);
            macroList.clear();
            for (int i : wordList) actionMain.itemChain[ActionMain.item.ID_Word].removeWithID(db, i);
            wordList.clear();
            for (int i : groupList) actionMain.itemChain[ActionMain.item.ID_Group].removeWithID(db, i);
            groupList.clear();
        }

        public void clear() {
            groupList.clear();
            macroList.clear();
            wordList.clear();
        }

        // TODO: removeDepArray도 removeBundle 내로 통합시키기?
    }
}
