package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Chrome on 5/9/15.
 */
public class AACGroupContainer {
    protected LinearLayout mainLayout;
    protected LinearLayout menuLayout;
    protected Context context;
    protected TextView titleView;
    protected ArrayList<ActionItem.Button> contentList;
    protected ActionDBHelper actDBHelper;
    protected ArrayList<ActionWord.Button.onClickClass> wordOCCList;
    protected ActionMain actionMain;
    protected long currentGroupID;
    protected TextToSpeech TTS;

    public AACGroupContainer(LinearLayout mainLayout) {
        this.context = mainLayout.getContext();
        actDBHelper = new ActionDBHelper(context);
        this.mainLayout = mainLayout;
        contentList = new ArrayList<ActionItem.Button>();
        actionMain = ActionMain.getInstance();
        TTS = new TextToSpeech(context, new TTSListener()); // TODO: Make a option to turn off/on TTS?
        actionMain.containerRef = this;

        // 그룹 제목 TextView 생성
        titleView = new TextView(context);
        titleView.setId(View.generateViewId());
        titleView.setText("테스트 그룹 제목");
        LinearLayout.LayoutParams titleViewLP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleViewLP.gravity = Gravity.CENTER_HORIZONTAL;
        mainLayout.addView(titleView,titleViewLP);

        // 메뉴 레이아웃 생성
        menuLayout = new LinearLayout(context);
        menuLayout.setId(View.generateViewId());
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        mainLayout.addView(menuLayout, LP);
    }

    public void exploreGroup(long id) {
        currentGroupID = id;

        for (ActionItem.Button item : contentList) menuLayout.removeView(item);
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
                new String[] {ActionGroup.SQL.COLUMN_NAME_WORD, ActionGroup.SQL.COLUMN_NAME_PARENT_ID},
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
                ActionWord.SQL.COLUMN_NAME_WORD
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
            rowText.init(values);
            rowText.setContainer(this);

            values.clear();
            contentList.add(rowText);
            c.moveToNext();
        }


        // 매크로 쿼리
        String[] projectionMacro = {
                ActionMacro.SQL._ID,
                ActionMacro.SQL.COLUMN_NAME_PRIORITY,
                ActionMacro.SQL.COLUMN_NAME_WORD,
                ActionMacro.SQL.COLUMN_NAME_WORDCHAIN
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

            rowText.setContainer(this);
            rowText.init(values);

            values.clear();
            contentList.add(rowText);
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

            rowText.setContainer(this);
            rowText.init(values);

            values.clear();
            contentList.add(rowText);
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

            parentGroupButton.setContainer(this);
            parentGroupButton.init(values);
            parentGroupButton.setText("상위그룹 " + c.getString(c.getColumnIndexOrThrow(ActionGroup.SQL.COLUMN_NAME_WORD)));

            values.clear();
            contentList.add(parentGroupButton);
            c.close();
        }


        Collections.sort(contentList, new ActionItem.Button.itemComparator()); // 정렬된 두 리스트의 병합 알고리즘은 내가 짜야 할지도?

        for (Button btn : contentList) {
            menuLayout.addView(btn);
        }
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

}
