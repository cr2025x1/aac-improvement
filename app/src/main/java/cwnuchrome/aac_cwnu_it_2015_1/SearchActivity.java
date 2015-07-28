package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

public class SearchActivity extends AppCompatActivity {
    ListView listView;
    EditText textInput;
    Context context;
    updateList updater;
    ArrayList<String> suggestionList;
    ArrayAdapter<String> adapter;

    HashMap<String, Long> queryMap;
    HashMap<Long, QueryWordInfo> query_id_map;

    ArrayList<ActionItem.onClickClass> suggestionOCCList;

    protected ActionMain actionMain;
    protected SearchImplicitFeedback feedbackHelper;

    public SearchActivity() {
        super();
        context = this;
        updater = new updateList();

        suggestionList = new ArrayList<>();
        suggestionOCCList = new ArrayList<>(ActionMain.item.ITEM_COUNT);

        query_id_map = null;
        queryMap = null;
        feedbackHelper = new SearchImplicitFeedback(
                new SearchImplicitFeedback.DocumentProcessor() {
                    @Override
                    public SearchImplicitFeedback.ItemIDInfo get_doc_id(int pos) {
                        ActionItem.onClickClass occ = suggestionOCCList.get(pos);
                        return new SearchImplicitFeedback.ItemIDInfo(occ.getItemCategoryID(), occ.getItemID());
                    }
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        actionMain = ActionMain.getInstance();

        textInput = (EditText)findViewById(R.id.edittext_search);
        textInput.setOnKeyListener(new EnterKeyBlocker());

        /* ListView Initialization */
        listView = (ListView) findViewById(R.id.list_search);
        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data
        adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, android.R.id.text1, suggestionList);
        listView.setAdapter(adapter);
        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

//                String itemValue = (String) listView.getItemAtPosition(position);
                ActionItem.onClickClass occ = suggestionOCCList.get(position);
                feedbackHelper.add_rel(
                        new SearchImplicitFeedback.ItemIDInfo(occ.getItemCategoryID(), occ.getItemID()),
                        position
                );

                occ.onClick(view);

            }
        });
        /* End of ListView initialization */

        /* Method for text-changing event */
        textInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                updater.onResume();
            }

            // 백슬레시?
        });
        /* End of Method for text-changing event */

        updater.execute(); // Initializing Updater thread
    }

    @Override
    protected void onDestroy() {
        updater.onComplete(); // Setting Updater thread to end
        super.onDestroy();
    }

    class updateList extends AsyncTask<String, Integer, Long> {
        private final Object mPauseLock = new Object();
        private boolean mPaused;
        private boolean mFinished;

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPaused = false;
            mFinished = false;

            System.out.println("ListView updater thread starts.");
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Long doInBackground(String... params) {
            long result = 0;

            while (!mFinished) {
                try {
                    if (fetchSuggestion()) {
                        suggestionList.clear();
                        for (ActionItem.onClickClass occ : suggestionOCCList) {
                            int catID = occ.getItemCategoryID();
                            StringBuilder sb = new StringBuilder();
                            String className = actionMain.itemChain[catID].CLASS_NAME;
                            if (className == null || !className.equals("")) {
                                sb.append(className);
                                sb.append(": ");
                            }
                            sb.append(occ.phonetic);

                            suggestionList.add(sb.toString());
                        }
                    }
                    runOnUiThread(new updateItem());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                onPause();

                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        }
                        catch (InterruptedException e) {
                            System.out.println("InterruptedException error occurred.");
                        }
                    }
                }

            }

            System.out.println("ListView updater thread ends.");
            return result;
        }

        /**
         * Call this on pause.
         */
        public void onPause() {
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        /**
         * Call this on resume.
         */
        public void onResume() {
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }

        // The thread is set to end. Making it escape the loop.
        public void onComplete() {
            mFinished = true;
            this.onResume();
        }

        // Notifying the UI thread to update the GUI.
        class updateItem implements Runnable {
            public void run() {
                adapter.notifyDataSetChanged();
            }
        }

        boolean fetchSuggestion()
        {
            HashMap<String, Long> new_query_map = ActionMain.reduce_to_map(textInput.getText().toString());
            if ((new_query_map.size() == 0 && queryMap == null)||
                    (queryMap != null && new_query_map.equals(queryMap))) return false;
            if (new_query_map.size() == 0 && queryMap != null) {
                feedbackHelper.send_feedback();

                suggestionOCCList.clear();
                queryMap = null;
                return true;
            }

            HashMap<Long, QueryWordInfo> new_query_id_map =
                    ((ActionWord)actionMain.itemChain[ActionMain.item.ID_Word]).convert_query_map_to_qwi_map(new_query_map);
            if ((new_query_id_map.size() == 0 && query_id_map == null) ||
                    (query_id_map != null && new_query_id_map.equals(query_id_map))) return false;

            feedbackHelper.send_feedback();

            suggestionOCCList.clear();

            if (new_query_id_map.size() == 0 && query_id_map != null) {
                queryMap = new_query_map;
                query_id_map = null;
                return true;
            }

            ActionMain actionMain = ActionMain.getInstance();
            queryMap = new_query_map;
            query_id_map = new_query_id_map;
            feedbackHelper.set_query_id_map(query_id_map);
            Vector<ArrayList<Map.Entry<Long, Double>>> rank_vector = actionMain.filter_rank_vector(
                    actionMain.allocEvaluation().evaluate_by_query_map(query_id_map, null, null),
                    ActionMain.FILTER_BY_THRESHOLD
            );

            update_occ_list(rank_vector);

            return true;
        }
    }

    // 키보드의 엔터키를 무시하기 위해 지정한 리스너 클래스
    class EnterKeyBlocker implements EditText.OnKeyListener {

        public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) { // 엔터키 이벤트
                    return true; // 아무 작업도 없이 그대로 true 값을 반환해 이벤트를 종료시킨다.
                }
            }

            return false;
        }
    }

    // 뒤로 가기 버튼을 눌렀을 때의 동작 메소드
    @Override
    public void onBackPressed() {
        // 피드백을 보내고 액티비티 종료 결과를 알린다.
        feedbackHelper.send_feedback();

        setResult(RESULT_CANCELED);

        super.onBackPressed();
    }

    // OCC 객체의 리스트를 업데이트하는 메소드.
    protected void update_occ_list(Vector<ArrayList<Map.Entry<Long, Double>>> rank_list) {

        /*
         * OCC 객체의 리스트는 정렬 순서가 몹시 중요하다.
         * 물론 ListView 객체에 직접 연결된 객체는 suggestionList이지만, suggestionList를 클릭 시 호출되는 객체는
         * suggestionOCCList이며 suggestionList의 객체는 자신의 인덱스 위치와 같은 위치에 있는 suggestionOCCList
         * 의 원소 객체를 호출하기 때문이다.
         * 즉, suggestionList와 그 원소들에 1:1 대응하는 객체들의 리스트인 suggestionOCCList는 정렬 상태가 완전히
         * 똑같아야 한다.
         *
         * rank_list는 최상위 n개만 추려내어 각 카테고리별로 정렬된 채로 있으므로, 매 카테고리별로 제일 앞에 있는 원소의 값을
         * 비교한 후 제일 큰 것을 먼저 OCC 객체 리스트에 삽입하고, 그 삽입한 객체가 있는 리스트의 이터레이터를 1 전진시킨다.
         * 그리고 이 과정을 더 이상 비교할 필요가 없을 때까지 반복한다.
         * 여기서 비교할 필요가 없는 상태라는 말은, 최대 단 하나의 카테고리 리스트만이 남은 원소가 있는 경우이다.
         * 이 때부터는 남은 리스트만을 기반으로 순차적으로 OCC 객체를 생성해 삽입하기를 반복한다.
         */

        Vector<ListIterator<Map.Entry<Long, Double>>> iterator_vector = new Vector<>(ActionMain.item.ITEM_COUNT);
        for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) iterator_vector.add(rank_list.get(i).listIterator());

        int iter_end_count = 0;
        int catID;
        // "ActionMain.item.ITEM_COUNT - 1". 즉 하나의 원소만 남을 때까지 최대값 찾기 - 최대값 가진 원소 추가 루틴 반복
        while (iter_end_count < ActionMain.item.ITEM_COUNT - 1) {
            double max_rank = 0.0d;

            // 각 이터레이터의 현재 위치에서의 최대값을 찾는다.
            for (ListIterator<Map.Entry<Long, Double>> iter : iterator_vector) {
                if (iter.hasNext()) {
                    Map.Entry<Long, Double> e = iter.next();
                    double value = e.getValue();
                    if (value > max_rank) {
                        max_rank = value;
                    }
                    iter.previous(); // next() 때문에 이터레이터가 한 칸 전진했으므로, previous()로 다시 원위치시킨다.
                }
            }

            // 최대값 원소를 가지는 이터레이터를 한 칸 전진시킨다. 그리고 그 최대값을 가진 원소를 기반으로 OCC 객체를 만들어 삽입한다.
            // 이 때 최대값을 가지는 원소가 여럿 있을 수 있다. (공동 1위)
            catID = 0;
            for (ListIterator<Map.Entry<Long, Double>> iter : iterator_vector) {
                if (iter.hasNext()) {
                    Map.Entry<Long, Double> e = iter.next();
                    double value = e.getValue();
                    if (value == max_rank) {
                        addOCC(catID, e);
                    }
                    else iter.previous();
                }
                else iter_end_count++;
                catID++;
            }
        }

        // 이제는 더 이상 비교작업은 필요하지 않다. 남은 리스트의 맴버에 대해 일괄적으로 순차 삽입한다.
        catID = 0;
        for (ListIterator<Map.Entry<Long, Double>> iter : iterator_vector) {
            while (iter.hasNext()) {
                addOCC(catID, iter.next());
            }
            catID++;
        }
    }

    // OCC 객체를 생성하는 메소드.
    protected void addOCC(int catID, Map.Entry<Long, Double> entry) {
        ActionItem actionItem = actionMain.itemChain[catID];
        ActionItem.onClickClass occ;
        AACGroupContainer container = actionMain.getReferrer().get(getIntent().getIntExtra("AACGC_ID", -1));

        // 그룹의 OCC 객체를 생성해야 할 때에는 추가 동작이 필요하므로 특별히 따로 구분해서 처리한다.
        if (catID == ActionMain.item.ID_Group) {
            occ = new GroupOCCWrapper(context, container);
        }
        else {
            occ = actionItem.allocOCC(context, container);
        }

        // TODO: OCC의 초기화 메소드를 이용하는 것이 더 좋지 않을까?
        // OCC를 초기화한다.
        occ.rank = entry.getValue();
        long key = entry.getKey();
        occ.itemID = key;

        Cursor c = actionMain.getDB().query(
                actionItem.TABLE_NAME,
                new String[] {ActionItem.SQL.COLUMN_NAME_WORD},
                ActionItem.SQL._ID + "=" + key,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();
        occ.phonetic = c.getString(c.getColumnIndexOrThrow(ActionItem.SQL.COLUMN_NAME_WORD));
        occ.message = occ.phonetic + " " + occ.rank;
        c.close();

        // 마지막으로 쿼리결과 OCC 리스트에 추가한다.
        suggestionOCCList.add(occ);
    }

    // 그룹 아이템 클릭시에는 기존 그룹의 onClick()의 동작 외에 추가 동작이 필요하므로 서브클래스를 하나 정의한다.
    protected class GroupOCCWrapper extends ActionGroup.onClickClass {
        public GroupOCCWrapper(Context context, AACGroupContainer container) {
            super(context, container);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent();
            setResult(RESULT_OK, i);
            super.onClick(v);

            feedbackHelper.send_feedback(); // 액티비티를 종료하게 되므로 쿼리결과 조회는 자동으로 종료된다. 따라서 피드백을 보낸다.

            finish(); // 이 액티비티를 종료하고, 기존의 탐색 화면으로 돌아간다.
        }
    }

}
