package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
    ArrayAdapter<String> adapter;

    HashMap<String, Long> queryMap;
    HashMap<Long, QueryWordInfo> query_id_map;

    protected final SearchList search_list;
    KeyEventHandler search_key_event_handler;

    protected ActionMain actionMain;
    protected SearchImplicitFeedback feedbackHelper;

    boolean back_button_pressed;


    public SearchActivity() {
        super();
        context = this;

        search_list = new SearchList();
        search_key_event_handler = new KEHSearch();

        query_id_map = null;
        queryMap = null;
        feedbackHelper = new SearchImplicitFeedback(
                new SearchImplicitFeedback.DocumentProcessor() {
                    @Override
                    public SearchImplicitFeedback.ItemIDInfo get_doc_id(int pos) {
                        ActionItem.onClickClass occ = search_list.occs.get(pos);
                        return new SearchImplicitFeedback.ItemIDInfo(occ.getItemCategoryID(), occ.getItemID());
                    }
                }
        );

        back_button_pressed = false;
    }

    protected class SearchList {
        ArrayList<String> views = new ArrayList<>();
        ArrayList<ActionItem.onClickClass> occs = new ArrayList<>();
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
                android.R.layout.simple_list_item_1, android.R.id.text1, search_list.views);
        listView.setAdapter(adapter);
        // ListView Item Click Listener
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String itemValue = (String) listView.getItemAtPosition(position);
                        ActionItem.onClickClass occ = search_list.occs.get(position);
                        feedbackHelper.add_rel(
                                new SearchImplicitFeedback.ItemIDInfo(occ.getItemCategoryID(), occ.getItemID()),
                                position
                        );
                        occ.onClick(view);
                    }
                }
        );
        /* End of ListView initialization */

        /* Method for text-changing event */
        textInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search_key_event_handler.execute();
            }
        });
        /* End of Method for text-changing event */
    }

    @Override
    protected void onDestroy() {
        search_key_event_handler.soft_off();
        super.onDestroy();
    }



    protected class KEHSearch extends KeyEventHandler {
        @Override
        public void run() {
            if (search_by_query()) {
                if (!check_mutual_exclusive_interrupt()) {
                    return;
                }

                synchronized (search_list) {
                    search_list.views.clear();
                    for (ActionItem.onClickClass occ : search_list.occs) {
                        int catID = occ.getItemCategoryID();
                        StringBuilder sb = new StringBuilder();
                        String className = actionMain.itemChain[catID].CLASS_NAME;
                        if (className == null || !className.equals("")) {
                            sb.append(className);
                            sb.append(": ");
                        }
                        sb.append(occ.phonetic);

                        search_list.views.add(sb.toString());
                    }
                }

                if (!check_mutual_exclusive_interrupt()) {
                    return;
                }
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        }
                );
            } else {
                check_mutual_exclusive_interrupt();
            }
        }

        protected boolean search_by_query() {
            HashMap<String, Long> new_query_map = ActionMain.reduce_to_map(textInput.getText().toString());
            if ((new_query_map.size() == 0 && queryMap == null)||
                    (queryMap != null && new_query_map.equals(queryMap))) {
                return false;
            }
            if (new_query_map.size() == 0 && queryMap != null) {
                feedbackHelper.send_feedback();
                search_list.occs.clear();
                queryMap = null;
                return true;
            }

            actionMain.write_lock.lock();
            HashMap<Long, QueryWordInfo> new_query_id_map =
                    ((ActionWord)actionMain.itemChain[ActionMain.item.ID_Word]).convert_query_map_to_qwi_map(new_query_map);
            if ((new_query_id_map.size() == 0 && query_id_map == null) ||
                    (query_id_map != null && new_query_id_map.equals(query_id_map))) {
                actionMain.write_lock.unlock();
                return false;
            }

            feedbackHelper.send_feedback();
            actionMain.read_lock.lock();
            actionMain.write_lock.unlock();
//            actionMain.write_lock.unlock_without_read_lock_check();

            search_list.occs.clear();

            if (new_query_id_map.size() == 0 && query_id_map != null) {
                queryMap = new_query_map;
                query_id_map = null;
                actionMain.read_lock.unlock();
                return true;
            }

            ActionMain actionMain = ActionMain.getInstance();
            queryMap = new_query_map;
            query_id_map = new_query_id_map;
            feedbackHelper.set_query_id_map(query_id_map);
            Vector<HashMap<Long, Double>> rank_vector_raw = actionMain.allocEvaluation().evaluate_by_query_map(query_id_map, null, null);
            if (rank_vector_raw == null) {
                actionMain.read_lock.unlock();
                return false;
            }
            Vector<ArrayList<Map.Entry<Long, Double>>> rank_vector = actionMain.filter_rank_vector(
                    rank_vector_raw,
                    ActionMain.FILTER_BY_THRESHOLD
            );

            update_occ_list(rank_vector);

            actionMain.read_lock.unlock();
//            actionMain.read_lock.unlock_without_write_lock_check();
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
        if (back_button_pressed) return;

        // 피드백을 보내고 액티비티 종료 결과를 알린다.
        back_button_pressed = true;
        ConcurrentLibrary.run_off_ui_thread(
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        actionMain.write_lock.lock();
                        feedbackHelper.send_feedback();
                        actionMain.write_lock.unlock();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        SearchActivity.this.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        SearchActivity.this.setResult(RESULT_CANCELED);
                                        SearchActivity.super.onBackPressed();
                                        back_button_pressed = false;
                                    }
                                }
                        );
                    }
                }
        );
    }

    // OCC 객체의 리스트를 업데이트하는 메소드.
    protected void update_occ_list(Vector<ArrayList<Map.Entry<Long, Double>>> rank_list) {

        /*
         * OCC 객체의 리스트는 정렬 순서가 몹시 중요하다.
         * 물론 ListView 객체에 직접 연결된 객체는 suggestionList이지만, suggestionList를 클릭 시 호출되는 객체는
         * suggestionOCCList이며 suggestionList의 객체는 자신의 인덱스 위치와 같은 위치에 있는 search_list.occs
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

        actionMain.read_lock.lock();

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

        actionMain.read_lock.unlock();
    }

    // OCC 객체를 생성하는 메소드.
    protected void addOCC(int catID, Map.Entry<Long, Double> entry) {
        actionMain.read_lock.lock();
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
        search_list.occs.add(occ);
        actionMain.read_lock.unlock();
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
            final View v_final = v;

            ConcurrentLibrary.run_off_ui_thread(
                    SearchActivity.this,
                    new Runnable() {
                        @Override
                        public void run() {
                            actionMain.write_lock.lock();
                            feedbackHelper.send_feedback(); // 액티비티를 종료하게 되므로 쿼리결과 조회는 자동으로 종료된다. 따라서 피드백을 보낸다.
                            actionMain.write_lock.unlock();
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            GroupOCCWrapper.super.onClick(v_final);
                                            finish(); // 이 액티비티를 종료하고, 기존의 탐색 화면으로 돌아간다.
                                        }
                                    }
                            );
                        }
                    }
            );
        }
    }

}
