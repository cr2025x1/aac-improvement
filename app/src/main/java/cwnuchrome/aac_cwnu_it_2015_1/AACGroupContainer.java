package cwnuchrome.aac_cwnu_it_2015_1;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Chrome on 5/9/15.
 * AAC 레이아웃 객체
 *
 * 경고: 이 클래스는 직접 선언하면 안 되고, ActionMain의 메소드를 통해 할당받는 것을 추천함. 액티비티간 통신을 위해서임.
 */
public class AACGroupContainer {
    protected LinearLayout mainLayout;
    protected GridLayout menuLayout;
    protected Context context;
    protected TextView titleView;
    protected ArrayList<View> item_list;
    protected ArrayList<View> selectedList;
    protected ActionMain actionMain;
    protected long current_group_ID;
    protected TextToSpeech TTS;

    protected ArrayList<CheckBox> checkBoxes;

    protected boolean isFolded;
    protected AnimatorSet checkbox_appearing_animation;
    protected AnimatorSet checkbox_appearing_animation_reverse;

    protected RemovalListBundle removalListBundle;

    protected String userImageDirectoryPathPrefix;

    final GroupElement rootGroupElement = new GroupElement();

    public final static int MODE_NORMAL = 0;
    public final static int MODE_RENAMING = 1;

    protected int container_id;

//    Lock read_lock;
//    DBWriteLockWrapper write_lock;
    LockWrapper.ReadLockWrapper read_lock;
    LockWrapper.WriteLockWrapper write_lock;

    protected ArrayList<View> old_item_list;
    String parent_group_name;
    Activity activity;

    final Object explore_group_runnable;
    boolean is_on_progress;

    protected SearchImplicitFeedback feedbackHelper = new SearchImplicitFeedback(
            new SearchImplicitFeedback.DocumentProcessor() {
                @Override
                public SearchImplicitFeedback.ItemIDInfo get_doc_id(int pos) {
                    ActionItem.onClickClass occ = ((ActionItem.Button) item_list.get(pos).findViewById(R.id.aac_item_button_id)).onClickObj;
                    return new SearchImplicitFeedback.ItemIDInfo(occ.getItemCategoryID(), occ.getItemID());
                }
            }
    );

    public AACGroupContainer(LinearLayout mainLayout) {
        this.context = mainLayout.getContext();
        this.mainLayout = mainLayout;
        item_list = new ArrayList<>();
        actionMain = ActionMain.getInstance();
        actionMain.setContext(context.getApplicationContext());
        TTS = new TextToSpeech(context, new TTSListener());
        actionMain.containerRef = this;

        checkBoxes = new ArrayList<>();
        selectedList = new ArrayList<>();

        removalListBundle = new RemovalListBundle();

        userImageDirectoryPathPrefix = actionMain.get_picture_directory_path();

        // 그룹 제목 TextView 설정
        titleView = (TextView)(mainLayout.findViewById(R.id.groupTitle));

        // 메뉴 레이아웃 설정
        menuLayout = (GridLayout)(mainLayout.findViewById(R.id.AACMenuLayout));

        read_lock = actionMain.getReadLock();
        write_lock = actionMain.getWriteLock();
        activity = (Activity)context;

        explore_group_runnable = new Object();
        is_on_progress = false;
    }

    public void explore_group_MT(long id, Runnable after) {
//        run_off_ui_thread(activity, new explore_group_runnable(id));
        ConcurrentLibrary.run_off_ui_thread(activity, new explore_group_runnable(id), after);
    }

    protected class explore_group_runnable implements Runnable {
        long id;
        public explore_group_runnable(long id) {
            this.id = id;
        }

        @Override
        public void run() {
            synchronized (explore_group_runnable) {
                read_lock.lock();
                long old_current_group_ID = current_group_ID;
                current_group_ID = id;
                old_item_list = new ArrayList<>(item_list);
                item_list.clear();
                checkBoxes.clear();
                selectedList.clear();
                rootGroupElement.ids.clear();
                rootGroupElement.words.clear();
                isFolded = false;


                SQLiteDatabase db = actionMain.getDB();
                Cursor c;
                long cursorCount;
                ContentValues values = new ContentValues();

                // 부모 그룹 정보 가져오기
                c = db.query(
                        actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME,
                        new String[] {
                                ActionGroup.SQL.COLUMN_NAME_WORD,
                                ActionGroup.SQL.COLUMN_NAME_PARENT_ID,
                                ActionGroup.SQL.COLUMN_NAME_PICTURE,
                                ActionGroup.SQL.COLUMN_NAME_PICTURE_IS_PRESET,
                                ActionGroup.SQL.COLUMN_NAME_ELEMENT_ID_TAG
                        },
                        ActionGroup.SQL._ID + " = ?",
                        new String[] {Long.toString(id)},
                        null,
                        null,
                        null
                );
                c.moveToFirst();
                parent_group_name = c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD));
                long parent_group_ID = c.getLong(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PARENT_ID));
                HashMap<Long, Long> group_id_map = ActionMultiWord.parse_element_id_count_tag(c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_ELEMENT_ID_TAG)));
                c.close();

                // 피드백 파트
                if (old_current_group_ID != current_group_ID) {
                    HashMap<Long, QueryWordInfoRaw> qwir_map = new HashMap<>(group_id_map.size());
                    for (Map.Entry<Long, Long> e : group_id_map.entrySet()) {
                        QueryWordInfoRaw qwir = new QueryWordInfoRaw(e.getValue(), 1);
                        qwir_map.put(e.getKey(), qwir);
                    }
                    feedbackHelper.set_query_id_map(qwir_map);
                }

        /* DB 쿼리 파트 */


                // 최상위 그룹의 이름에 사용되는 단어 파악
                Cursor rName_c = db.query(
                        actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME,
                        new String[] {ActionGroup.SQL.COLUMN_NAME_WORDCHAIN},
                        ActionItem.SQL._ID + "=" + 1,
                        null,
                        null,
                        null,
                        null
                );
                rName_c.moveToFirst();
                ((ActionGroup)actionMain.itemChain[ActionMain.item.ID_Group]).parseWordChain(
                        rName_c.getString(rName_c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORDCHAIN)),
                        new ActionMultiWord.onParseCommand() {
                            @Override
                            public void onParse(long itemID) {
                                rootGroupElement.ids.add(itemID);

                                Cursor itemID_c = actionMain.getDB().query(
                                        actionMain.itemChain[ActionMain.item.ID_Word].TABLE_NAME,
                                        new String[]{ActionWord.SQL.COLUMN_NAME_WORD},
                                        ActionWord.SQL._ID + "=" + itemID,
                                        null,
                                        null,
                                        null,
                                        null
                                );
                                itemID_c.moveToFirst();
                                rootGroupElement.words.add(itemID_c.getString(itemID_c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_WORD)));

                                itemID_c.close();
                            }
                        });
                rName_c.close();


                // 쿼리 인수 설정
                String[] projection = {
                        ActionWord.SQL._ID,
                        ActionWord.SQL.COLUMN_NAME_PRIORITY,
                        ActionWord.SQL.COLUMN_NAME_WORD,
                        ActionWord.SQL.COLUMN_NAME_PICTURE,
                        ActionWord.SQL.COLUMN_NAME_PICTURE_IS_PRESET
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
                    int itemId = c.getInt(
                            c.getColumnIndexOrThrow(ActionWord.SQL._ID)
                    );

                    values.put(ActionItem.SQL._ID, itemId);
                    values.put(ActionWord.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_WORD)));
                    values.put(ActionWord.SQL.COLUMN_NAME_PRIORITY, c.getLong(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PRIORITY)));
                    values.put(ActionWord.SQL.COLUMN_NAME_PICTURE, c.getString(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PICTURE)));
                    values.put(ActionWord.SQL.COLUMN_NAME_PICTURE_IS_PRESET, c.getInt(c.getColumnIndexOrThrow(ActionWord.SQL.COLUMN_NAME_PICTURE_IS_PRESET)));

                    addMenuWithCheckBox(
                            new ActionWord.Button(context, new ActionWordOCCWrapper(context, AACGroupContainer.this), AACGroupContainer.this),
                            values
                    );

                    values.clear();
                    c.moveToNext();
                }

                // 매크로 쿼리
                String[] projectionMacro = {
                        ActionMacro.SQL._ID,
                        ActionMacro.SQL.COLUMN_NAME_PRIORITY,
                        ActionMacro.SQL.COLUMN_NAME_WORD,
                        ActionMacro.SQL.COLUMN_NAME_WORDCHAIN,
                        ActionMacro.SQL.COLUMN_NAME_PICTURE,
                        ActionMacro.SQL.COLUMN_NAME_PICTURE_IS_PRESET,
                        ActionMacro.SQL.COLUMN_NAME_ELEMENT_ID_TAG
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

                System.out.println("*** ActionMacro Querying ***");
                for (int i = 0; i < cursorCount; i++) {
                    long itemId = c.getLong(
                            c.getColumnIndexOrThrow(ActionMacro.SQL._ID)
                    );
                    System.out.println("No. " + i + " : " +
                                    "Word \"" + c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_WORD)) + "\", " +
                                    "ID=" + itemId + ", " +
                                    "Priority=" + c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_PRIORITY))
                    );

                    System.out.print("HashMap regenerated --> ");
                    ActionMacro.print_hashmap(ActionMultiWord.parse_element_id_count_tag(c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_ELEMENT_ID_TAG))));

                    values.put(ActionItem.SQL._ID, itemId);
                    values.put(ActionMacro.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_WORD)));
                    values.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN)));
                    values.put(ActionMacro.SQL.COLUMN_NAME_PRIORITY, c.getLong(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_PRIORITY)));
                    values.put(ActionMacro.SQL.COLUMN_NAME_PICTURE, c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_PICTURE)));
                    values.put(ActionMacro.SQL.COLUMN_NAME_PICTURE_IS_PRESET, c.getInt(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_PICTURE_IS_PRESET)));

                    addMenuWithCheckBox(
                            new ActionMacro.Button(context, new ActionMacroOCCWrapper(context, AACGroupContainer.this), AACGroupContainer.this),
                            values);

                    values.clear();
                    c.moveToNext();
                }
                System.out.println("*** ActionMacro Query Complete ***");


                // 그룹 쿼리

                String[] projectionGroup = {
                        ActionMacro.SQL._ID,
                        ActionMacro.SQL.COLUMN_NAME_PRIORITY,
                        ActionMacro.SQL.COLUMN_NAME_WORD,
                        ActionMacro.SQL.COLUMN_NAME_WORDCHAIN,
                        ActionMacro.SQL.COLUMN_NAME_PICTURE,
                        ActionMacro.SQL.COLUMN_NAME_PICTURE_IS_PRESET,
                        ActionMacro.SQL.COLUMN_NAME_ELEMENT_ID_TAG
                };

                String queryClauseGroup =
                        ActionGroup.SQL.COLUMN_NAME_PARENT_ID  + " = " + id +
                                " AND " + ActionGroup.SQL._ID + " != " + id
                        ; // 검색 조건
                c = db.query(
                        actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, // The table to query
                        projectionGroup, // The columns to return
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

                    System.out.print("HashMap regenerated --> ");
                    ActionMacro.print_hashmap(ActionMultiWord.parse_element_id_count_tag(c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_ELEMENT_ID_TAG))));

                    values.put(ActionGroup.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));
                    values.put(ActionGroup.SQL.COLUMN_NAME_PRIORITY, c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PRIORITY));
                    values.put(ActionGroup.SQL._ID, itemId);
                    values.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PICTURE)));
                    values.put(ActionGroup.SQL.COLUMN_NAME_PICTURE_IS_PRESET, c.getInt(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PICTURE_IS_PRESET)));

                    addMenuWithCheckBox(
                            new ActionGroup.Button(context, new ActionGroupOCCWrapper(context, AACGroupContainer.this), AACGroupContainer.this),
                            values);

                    values.clear();
                    c.moveToNext();
                }

                ActionWord actionWord = (ActionWord)actionMain.itemChain[ActionMain.item.ID_Word];
                HashMap<Long, QueryWordInfo> group_qwi_map = actionWord.convert_id_map_to_qwi_map(group_id_map);
                final Vector<HashMap<Long, Double>> rank_vector =
                        actionMain.allocEvaluation().evaluate_by_query_map(
                                group_qwi_map,
                                ActionItem.SQL.COLUMN_NAME_PARENT_ID + "=" + current_group_ID,
                                null);

                Collections.sort(
                        item_list,
                        new Comparator<View>() {
                            @Override
                            public int compare(View lhs, View rhs) {
                                ActionItem.onClickClass lhs_occ = ((ActionItem.Button)lhs.findViewById(R.id.aac_item_button_id)).onClickObj;
                                ActionItem.onClickClass rhs_occ = ((ActionItem.Button)rhs.findViewById(R.id.aac_item_button_id)).onClickObj;

                                return rank_vector.get(lhs_occ.getItemCategoryID()).get(lhs_occ.getItemID())
                                        > rank_vector.get(rhs_occ.getItemCategoryID()).get(rhs_occ.getItemID())
                                        ? -1 : 1;
                            }
                        }
                );

                // 마지막으로 상위 그룹에 대한 버튼 형성 (단 최상위 그룹은 패스)
                if (id != 1) {
                    c = db.query(
                            actionMain.itemChain[ActionMain.item.ID_Group].TABLE_NAME, // The table to query
                            projectionGroup, // The columns to return
                            ActionGroup.SQL._ID + " = " + parent_group_ID, // The columns for the WHERE clause
                            null, // The values for the WHERE clause
                            null, // don't group the rows
                            null, // don't filter by row groups
                            sortOrder // The sort order
                    );
                    c.moveToFirst();

                    ActionGroup.Button parentGroupButton = new ActionGroup.Button(context, new ActionGroupParentOCCWrapper(context, AACGroupContainer.this), AACGroupContainer.this);

                    System.out.print("HashMap regenerated --> ");
                    ActionMacro.print_hashmap(ActionMultiWord.parse_element_id_count_tag(c.getString(c.getColumnIndexOrThrow(ActionMacro.SQL.COLUMN_NAME_ELEMENT_ID_TAG))));

                    values.put(ActionGroup.SQL.COLUMN_NAME_WORD, c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));
                    values.put(ActionGroup.SQL.COLUMN_NAME_PRIORITY, c.getLong(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PRIORITY)));
                    values.put(ActionGroup.SQL._ID, parent_group_ID);
                    values.put(ActionGroup.SQL.COLUMN_NAME_PICTURE, c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PICTURE)));
                    values.put(ActionGroup.SQL.COLUMN_NAME_PICTURE_IS_PRESET, c.getInt(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_PICTURE_IS_PRESET)));

                    addMenuWithoutCheckBox(parentGroupButton, values);

                    parentGroupButton.setText("상위그룹 " + c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));

                    values.clear();
                    c.close();
                }

        /* DB 쿼리 파트 끝 */

                setFoldAnimation();
                activity.runOnUiThread(new explore_group_ui());

                read_lock.unlock();
            }
        }
    }


    void addMenuWithCheckBox(ActionItem.Button btn, ContentValues values) {
        btn.init(values);
//        btn.setContainer(this);

        LinearLayout item_layout = (LinearLayout) View.inflate(context, R.layout.aac_item_layout, null);
        CheckBox checkBox = (CheckBox)item_layout.findViewById(R.id.aac_item_checkbox);
        item_layout.addView(btn);
        btn.setId(R.id.aac_item_button_id);

        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            selectedList.add((LinearLayout) buttonView.getParent());
                        } else {
                            View v = (View) buttonView.getParent();
                            selectedList.remove(v);
                        }

                    }
                }
        );

        checkBoxes.add(checkBox);
        item_list.add(item_layout);
    }

    void addMenuWithoutCheckBox(ActionItem.Button btn, ContentValues values) {
        btn.init(values);
//        btn.setContainer(this);

        LinearLayout item_layout = (LinearLayout) View.inflate(context, R.layout.aac_item_layout, null);
        CheckBox checkBox = (CheckBox)item_layout.findViewById(R.id.aac_item_checkbox);
        item_layout.addView(btn);
        btn.setId(R.id.aac_item_button_id);

        checkBox.setVisibility(View.INVISIBLE);
        checkBox.setEnabled(false);

        item_list.add(item_layout);
    }

    public long getCurrentGroupID() {
        return current_group_ID;
    }

    class TTSListener implements TextToSpeech.OnInitListener {
        public void onInit(int status) {}
    }

    public TextToSpeech getTTS() { return TTS; }

    // 작업 중.
    public void onDestroy() {
        TTS.shutdown();
        write_lock.lock();
        ConcurrentLibrary.run_off_ui_thread(
                activity,
                new Runnable() {
                    @Override
                    public void run() {
                        feedbackHelper.send_feedback();
                    }
                },
                null
        );
        write_lock.unlock();
    }

    public void refresh() {
//        explore_group(current_group_ID);
        explore_group_MT(current_group_ID, null);
    }

    public void setFoldAnimation() {
        isFolded = false;

        int listSize = item_list.size();

        if (listSize <= 0) return;

        AnimatorSet list[] = new AnimatorSet[listSize];

        int listPos = 0;
//        Path p = new Path();
//        p.moveTo(1f, 1f);
//        p.lineTo(0f, 0f);
        for (View v : item_list) {
            CheckBox cbox = (CheckBox)v.findViewById(R.id.aac_item_checkbox);
            list[listPos] = new AnimatorSet();
            list[listPos].playTogether(
//                    ObjectAnimator.ofFloat(cbox, View.SCALE_X, View.SCALE_Y, p), // Over API Lv 21
                    ObjectAnimator.ofFloat(cbox, View.SCALE_X, 1f, 0f),
                    ObjectAnimator.ofFloat(cbox, View.SCALE_Y, 1f, 0f),
                    ObjectAnimator.ofFloat(cbox, View.ALPHA, 1f, 0f)
            );
            list[listPos].setDuration(AACGroupContainerPreferences.CHECKBOX_ANIMATION_DURATION);
            listPos++;
        }

        checkbox_appearing_animation = new AnimatorSet();
        checkbox_appearing_animation.playTogether(list);
        checkbox_appearing_animation.addListener(new Animator.AnimatorListener() {
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
        for (View v : item_list) {
            CheckBox cbox = (CheckBox)v.findViewById(R.id.aac_item_checkbox);
            list[listPos] = new AnimatorSet();
            list[listPos].playTogether(
//                    ObjectAnimator.ofFloat(cbox, View.SCALE_X, View.SCALE_Y, p), // Over API Lv 21
                    ObjectAnimator.ofFloat(cbox, View.SCALE_X, 0f, 1f),
                    ObjectAnimator.ofFloat(cbox, View.SCALE_Y, 0f, 1f),
                    ObjectAnimator.ofFloat(cbox, View.ALPHA, 0f, 1f)
            );
            list[listPos].setDuration(AACGroupContainerPreferences.CHECKBOX_ANIMATION_DURATION);
            listPos++;
        }

        checkbox_appearing_animation_reverse = new AnimatorSet();
        checkbox_appearing_animation_reverse.playTogether(list);
        checkbox_appearing_animation_reverse.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (CheckBox cbox : checkBoxes) cbox.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isFolded = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        // Setting default folded status
        for (View v : item_list) {
            CheckBox cbox = (CheckBox)v.findViewById(R.id.aac_item_checkbox);

            cbox.setVisibility(View.INVISIBLE);
            cbox.setScaleX(0f);
            cbox.setScaleY(0f);
        }

        isFolded = true;
    }

    public void toggleFold() {
        if (checkbox_appearing_animation == null || checkbox_appearing_animation_reverse == null) return;

        if (!(checkbox_appearing_animation.isRunning() || checkbox_appearing_animation_reverse.isRunning())) {
            if (isFolded) {
                checkbox_appearing_animation_reverse.start();

                for (View v : item_list) {
                    ActionItem.Button btn = (ActionItem.Button)v.findViewById(R.id.aac_item_button_id);
                    btn.onClickObj.toogleOnline();
                }
            }
            else {
                for (View v : item_list) {
                    ActionItem.Button btn = (ActionItem.Button)v.findViewById(R.id.aac_item_button_id);
                    btn.onClickObj.toogleOnline();
                }

                checkbox_appearing_animation.start();
            }

        }
    }

    public void selectAll() {
        for (View v : item_list) {
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

    public void remove_selected_MT(Runnable after) {
//        run_off_ui_thread(activity, new remove_selected_runnable());
        ConcurrentLibrary.run_off_ui_thread(activity, new remove_selected_runnable(), after);
    }

    // 단순한 아이템 삭제가 이렇게 길어질 줄 누가 알았으랴...
    public class remove_selected_runnable implements Runnable {
        @Override
        public void run() {
            write_lock.lock();
            if (selectedList.size() == 0) {
                write_lock.unlock();
                return;
            }

            removalListBundle.clear();

            for (View v : selectedList)
                removalListBundle.addByOCC(((ActionItem.Button) v.findViewById(R.id.aac_item_button_id)).onClickObj);
            removalListBundle.printList();

            // 만일 의존성 검사에서 문제 발생 시 유저의 확인을 받음 (문제가 없으면 확인 없이 바로 삭제)
            if (!removalListBundle.checkNoDependencyLeft()) {
                removalListBundle.printMissingDependencyList();
                ((MainActivity) context).confirmDependency();
            }
            else invokeRemoval();
            write_lock.unlock();
        }
    }

    // 아이템 삭제 명령 수행
    public void invokeRemoval() {
        write_lock.lock();
        removalListBundle.execRemoval();
//        explore_group(current_group_ID);
        explore_group_MT(current_group_ID, null);
        write_lock.unlock();
    }

    protected class RemovalListBundle {
        protected String[] projection;

        protected Vector<ArrayList<Long>> itemVector;
        protected Vector<ArrayList<ContentValues>> missingDependencyPrintVector;
        protected Vector<ArrayList<Long>> missingDependencyVector;

        protected ActionMain actionMain;

        public RemovalListBundle() {
            projection = new String[] { ActionItem.SQL._ID };

            itemVector = new Vector<>(ActionMain.item.ITEM_COUNT);
            for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) itemVector.add(new ArrayList<Long>());

            missingDependencyPrintVector = new Vector<>(ActionMain.item.ITEM_COUNT);
            for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) missingDependencyPrintVector.add(new ArrayList<ContentValues>());

            missingDependencyVector = new Vector<>(ActionMain.item.ITEM_COUNT);
            for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) missingDependencyVector.add(new ArrayList<Long>());

            actionMain = ActionMain.getInstance();
        }

        public void add(int category_id, long id) {
//            itemVector.get(category_id).add(id);
            itemVector = actionMain.itemChain[category_id].expand_item_vector(id, itemVector);
        }

        public void addByOCC(ActionItem.onClickClass occ) {
            add(occ.itemCategoryID, occ.itemID);

        }

        // 의존성 여부 검사
        public boolean checkNoDependencyLeft() {
            for (ArrayList<ContentValues> l : missingDependencyPrintVector) l.clear();
            for (ArrayList<Long> l : missingDependencyVector) l.clear();

            boolean result = true;
            for (ActionItem i : actionMain.itemChain) {
                result = result && i.verifyAndCorrectDependencyRemoval(context, this);
            }

            return result;
        }

        public void printList() {
            System.out.println("Selected item list for removal:");

            for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) {
                actionMain.itemChain[i].printRemovalList(this);
            }

            System.out.println("End of the list");
        }

        public void printMissingDependencyList() {
            System.out.println("List of non-selected dependencies:");

            for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) {
                actionMain.itemChain[i].printMissingDependencyList(this);
            }

            System.out.println("End of the list");
        }

        public void execRemoval() {
            write_lock.lock();
            int cat_id;

            // 선택 리스트에는 없으나 삭제 대상에 의존성을 가지는 아이템들을 모두 삭제 대상 리스트에 포함
            cat_id = 0;
            for (ArrayList<Long> l : missingDependencyVector) {
                for (long i : l) {
                    actionMain.itemChain[cat_id].addToRemovalList(context, this, i);
                }
                l.clear();
                cat_id++;
            }

            // 삭제 리스트 상의 모든 아이템 제거 (선택되지 않은 삭제 대상들도 모두 포함됨)
            cat_id = 0;
            for (ArrayList<Long> i : itemVector) {
                for (long id : i) actionMain.itemChain[cat_id].removeWithID(context, id);
                i.clear();
                cat_id++;
            }
            write_lock.unlock();
        }

        public void clear() {
            for (ArrayList<Long> i : itemVector) i.clear();
        }

    }

    public String getUserImagePathPrefix() { return userImageDirectoryPathPrefix; }

    public void set_image_for_selected_MT(ContentValues values, Runnable after) {
        ConcurrentLibrary.run_off_ui_thread(activity, new set_image_for_selected_runnable(values), after);
    }

    protected class set_image_for_selected_runnable implements Runnable {
        private ContentValues values;
        public set_image_for_selected_runnable(ContentValues values) {
            this.values = values;
        }

        @Override
        public void run() {
            write_lock.lock();
            Vector<ArrayList<Long>> id_Vector = new Vector<>(ActionMain.item.ITEM_COUNT);
            ActionMain actionMain = ActionMain.getInstance();

            // 각 분류별 리스트를 가지는 백터 객체 생성
            for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) id_Vector.add(new ArrayList<Long>());

            // 선택된 리스트의 각 객체별로 OCC 객체에서 아이템의 분류 ID와 각 레코드 ID를 추출해 넣음.
            for (View v : selectedList) {
                ActionItem.onClickClass occ = ((ActionItem.Button)v.findViewById(R.id.aac_item_button_id)).onClickObj;
                System.out.println("OCC CatID = " + occ.itemCategoryID);
                id_Vector.get(occ.itemCategoryID).add(occ.itemID);
            }

            // 각 분류 단위로 지정된 이미지를 쓰도록 DB 업데이트
            int i = 0;
            for (ArrayList<Long> list : id_Vector) {
                if (list.size() == 0) {
                    i++;
                    continue;
                }

                long[] id_ary = new long[list.size()];
                int j = 0;

                for (long k : list) id_ary[j++] = k;

                actionMain.itemChain[i++].updateWithIDs(context, values, id_ary);
            }

            write_lock.unlock();
        }
    }

    protected class GroupElement {
        ArrayList<Long> ids;
        ArrayList<String> words;
        public GroupElement() {
            ids = new ArrayList<>();
            words = new ArrayList<>();
        }
    }

    public int getContainerID() {
        return container_id;
    }

    public void setContainerID(int containerID) {
        this.container_id = containerID;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void setMode(int mode) {
        if (mode == MODE_RENAMING) {
            for (View v : item_list) {
                ActionItem.Button btn =  (ActionItem.Button)v.findViewById(R.id.aac_item_button_id);
                final ActionItem.onClickClass occ = btn.onClickObj;

                // 루트 그룹의 개명은 방지. (가능하게 만들 수는 있지만 그냥 놔두겠음.)
                if (occ.getItemCategoryID() == ActionMain.item.ID_Group && occ.getItemID() == 1) {
                    occ.isOnline = false;
                }
                else {
                    btn.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ((MainActivity)context).dialog_rename(occ.getItemCategoryID(), occ.getItemID());
                                }
                            }
                    );
                }

            }

            return;
        }

        if (mode == MODE_NORMAL) {
            for (View v : item_list) {
                ActionItem.Button btn = (ActionItem.Button)v.findViewById(R.id.aac_item_button_id);

                // 루트 그룹만 특별 처리.
                if (btn.onClickObj.getItemCategoryID() == ActionMain.item.ID_Group && btn.onClickObj.getItemID() == 1) {
                    btn.onClickObj.isOnline = true;
                }
                else {
                    btn.setOnClickListener(btn.onClickObj);
                }
            }
            return;
        }
    }

    public void move_selected_runnable_MT(long new_parent_id, Runnable after) {
        ConcurrentLibrary.run_off_ui_thread(activity, new move_selected_runnable(new_parent_id), after);
    }

    protected class move_selected_runnable implements Runnable {
        long new_parent_id;
        public move_selected_runnable(long new_parent_id) {
            this.new_parent_id = new_parent_id;
        }

        @Override
        public void run() {
            write_lock.lock();
            if (current_group_ID == new_parent_id) {
                write_lock.unlock();
                return;
            }

            Vector<ArrayList<Long>> itemVector = new Vector<>(ActionMain.item.ITEM_COUNT);
            for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) itemVector.add(new ArrayList<Long>());

            for (View v : selectedList) {
                ActionItem.onClickClass occ = ((ActionItem.Button)v.findViewById(R.id.aac_item_button_id)).onClickObj;
                itemVector.get(occ.getItemCategoryID()).add(occ.getItemID());
            }

            ContentValues values = new ContentValues();
            values.put(ActionItem.SQL.COLUMN_NAME_PARENT_ID, new_parent_id);

            int cat_id = 0;
            for (ArrayList<Long> list : itemVector) {
                if (list.isEmpty()) {
                    cat_id++;
                    continue;
                }

                long[] array = new long[list.size()];
                int i = 0;
                for (long l : list) {
                    array[i] = l;
                    i++;
                }
                actionMain.itemChain[cat_id].updateWithIDs(context, values, array);

                cat_id++;
            }
            write_lock.unlock();
        }
    }

    public ArrayList<Long> getSelectedGroups() {
        ArrayList<Long> list  = new ArrayList<>();
        for (View v : selectedList) {
            ActionItem.onClickClass occ = ((ActionItem.Button)v.findViewById(R.id.aac_item_button_id)).onClickObj;
            if (occ.getItemCategoryID() == ActionMain.item.ID_Group) list.add(occ.getItemID());
        }

        return list;
    }

    // 제네릭 타입이 안 됨... ㅡㅡ
    protected class ActionWordOCCWrapper extends ActionWord.onClickClass {
        public ActionWordOCCWrapper(Context context, AACGroupContainer container) {
            super(context, container);
        }

        @Override
        public void onClick(View v) {
            add_feedback_info(this, v);
            super.onClick(v);
        }
    }

    protected class ActionMacroOCCWrapper extends ActionMacro.onClickClass {
        public ActionMacroOCCWrapper(Context context, AACGroupContainer container) {
            super(context, container);
        }

        @Override
        public void onClick(View v) {
            add_feedback_info(this, v);
            super.onClick(v);
        }
    }

    protected class ActionGroupOCCWrapper extends ActionGroup.onClickClass {
        public ActionGroupOCCWrapper(Context context, AACGroupContainer container) {
            super(context, container);
        }

        @Override
        public void onClick(View v) {
            add_feedback_info(this, v);
            final View v_final = v;
            ConcurrentLibrary.run_off_ui_thread(
                    activity,
                    new Runnable() {
                        @Override
                        public void run() {
                            feedbackHelper.send_feedback();
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            ActionGroupOCCWrapper.super.onClick(v_final);
                                        }
                                    }
                            );
                        }
                    },
                    null);
        }
    }

    protected class ActionGroupParentOCCWrapper extends ActionGroup.onClickClass {
        public ActionGroupParentOCCWrapper(Context context, AACGroupContainer container) {
            super(context, container);
        }

        @Override
        public void onClick(View v) {
            final View v_final = v;
            ConcurrentLibrary.run_off_ui_thread(
                    activity,
                    new Runnable() {
                        @Override
                        public void run() {
                            feedbackHelper.send_feedback();
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            ActionGroupParentOCCWrapper.super.onClick(v_final);
                                        }
                                    }
                            );
                        }
                    },
                    null);
        }
    }

    protected void add_feedback_info(ActionItem.onClickClass occ, View v) {
        SearchImplicitFeedback.ItemIDInfo idInfo = new SearchImplicitFeedback.ItemIDInfo(occ.getItemCategoryID(), occ.getItemID());
        View item_layout = (View)v.getParent();
        feedbackHelper.add_rel(idInfo, item_list.indexOf(item_layout));
    }

    protected class explore_group_ui implements Runnable {
        @Override
        public void run() {
            for (View item : old_item_list) menuLayout.removeView(item);
            titleView.setText(parent_group_name + " 그룹");
            for (View btn : item_list) {
                menuLayout.addView(btn);
            }
        }
    }

    public void modify_item_MT(int cat_id, long id, String new_word, RunnableWithResult<Long> after) {
        ConcurrentLibrary.run_off_ui_thread_with_result(activity, new modify_item_runnable(cat_id, id, new_word), after);
    }

    protected class modify_item_runnable extends RunnableWithResult<Long> {
        int cat_id;
        long id;
        String new_word;

        public modify_item_runnable(int cat_id, long id, String new_word) {
            this.cat_id = cat_id;
            this.id = id;
            this.new_word = new_word;
        }

        @Override
        public void run() {
            write_lock.lock();
            ContentValues values = new ContentValues();
            values.put(ActionWord.SQL.COLUMN_NAME_WORD, new_word);

            setResult(actionMain.itemChain[cat_id].updateWithIDs(context, values, new long[]{id}));
            write_lock.unlock();
        }
    }

    public void set_to_defaults_MT() {
        ConcurrentLibrary.run_off_ui_thread(
                activity,
                new Runnable() {
                    @Override
                    public void run() {
                        ActionPreset.getInstance().revert_to_default(context);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        explore_group_MT(1, null);
                    }
                }
        );
    }

}
