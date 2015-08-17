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

/*
 *  아이템을 검색하는 액티비티
 *  매 키 입력이 발생할 때마다 그 쿼리로 데이터베이스를 검색한 후, 실시간으로 결과를 제공한다.
 */
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
                        // 피드백 객체에게 아이템의 위치 정보로부터 아이템의 카테고리 ID, 자신의 ID 값을 받아가는 방법을 설정.
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
                        // 클릭된 아이템의 ID를 피드백 객체에 전달
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
                // EditText 객체의 텍스트 내용이 바뀔 때마다 검색이 되도록 지정.
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


    /*
     *  키 입력 이벤트에 따라 검색, 결과를 화면에 업데이트하는 클래스
     *  이 클래스의 코드들은 절대 UI 스레드에서 돌아가면 안 된다.
     *
     *  기반에 깔린 핵심 설계는 상위 클래스인 KeyEventHandler 참조.
     */
    protected class KEHSearch extends KeyEventHandler {

        // 스레드에서 실행되는 코드 블럭
        @Override
        public void run() {
            // 먼저 주어진 쿼리로 검색.
            if (search_by_query()) {
                // 검색 결과에 변화가 있었다면...
                if (!check_mutual_exclusive_interrupt()) { // 먼저 검색 스레드간 상호배제 확인.
                    // 다른 스레드가 이 스레드를 인터럽트시켰다.
                    // 즉, 이미 새 쿼리가 주어져 진행 중이다. 따라서 작업을 중단한다.
                    return;
                }

                // 쿼리에 따라 출력되는 아이템 목록을 업데이트할 준비를 한다.
                // 먼저 search_list는 thread-safe하지 않으므로 보호 블럭을 씌운다.
                synchronized (search_list) {
                    search_list.views.clear();
                    for (ActionItem.onClickClass occ : search_list.occs) {
                        // 먼저 각 아이템들을 위한 OCC 객체를 형성한다.
                        int catID = occ.getItemCategoryID();
                        StringBuilder sb = new StringBuilder();
                        String className = actionMain.itemChain[catID].CLASS_NAME;
                        if (className == null || !className.equals("")) {
                            sb.append(className);
                            sb.append(": ");
                        }
                        sb.append(occ.phonetic);

                        // 그리고 텍스트뷰들을 위한 String도 준비해둔다.
                        search_list.views.add(sb.toString());
                    }
                }

                // 동기화 블록이 끝난 이후 다시 한 번 상호배제를 점검.
                if (!check_mutual_exclusive_interrupt()) {
                    return;
                }

                // 인터럽트가 없었다면 아까 작업했던 것대로 리스트뷰가 갱신되게 리스트뷰에 알린다.
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

        /*
         *  EditText 뷰에서 쿼리를 받고, 가공한 후 그 쿼리로 검색을 하는 메소드.
         */
        protected boolean search_by_query() {
            /*
             *  쿼리 가공 및 필터링 단계
             */
            // 먼저 원 텍스트를 EditText 뷰에서 가져온 후, 해시맵으로 가공한다.
            HashMap<String, Long> new_query_map = ActionMain.reduce_to_map(textInput.getText().toString());
            if ((new_query_map.size() == 0 && queryMap == null)||
                    (queryMap != null && new_query_map.equals(queryMap))) {
                /*
                 *  1. 기존의 쿼리맵이 null인데, 새 쿼리맵 또한 빈 해시맵이다. --> 둘 다 빈 해시맵. 따라서 쿼리값의 변화는 없음으로 간주.
                 *  2. 기존 쿼리맵이 있긴 한데, 새로 만들어진 해시맵과 내용이 동일 --> 쿼리맵이 같으면 쿼리값의 변화는 없음으로 간주.
                 *  결론: 쿼리의 변화가 없는데 쿼리 결과의 변동이 있을 수가 없다. false 반환.
                 */
                return false;
            }
            if (new_query_map.size() == 0 && queryMap != null) {
                /*
                 *  기존 쿼리맵이 존재했는데, 새 쿼리맵의 크기가 0이다.
                 *  --> 기존의 입력은 있었으나, 사용자가 쿼리를 지웠다. 즉, 현재 쿼리는 공백이다.
                 *  --> 기존의 검색 결과가 있었을 수 있다.
                 *  1. 쿼리맵의 결과를 피드백해야 한다.
                 *  2. 그러나 검색 작업을 진행할 필요는 없다.
                 */
                actionMain.write_lock.lock();
                feedbackHelper.send_feedback();
                actionMain.write_lock.unlock();
                search_list.occs.clear();
                queryMap = null;
                return true;
            }

            // 쿼리 확장 & 피드백 적용 파트
            actionMain.write_lock.lock();
            /*
             *  쿼리에 포함된 문자열들에 대응되는 데이터베이스 내 Word의 ID를 찾고, 그 ID에 대응되는 QWI 맵을 불러온다.
             *  여기서 쿼리의 복원 및 유추가 진행된다.
             */
            HashMap<Long, QueryWordInfo> new_query_id_map =
                    ((ActionWord)actionMain.itemChain[ActionMain.item.ID_Word]).convert_query_map_to_qwi_map(new_query_map);
            if ((new_query_id_map.size() == 0 && query_id_map == null) ||
                    (query_id_map != null && new_query_id_map.equals(query_id_map))) {
                /*
                 *  기존의 입력값이 존재했는데, 기존의 입력값에 대응되는 QWI 맵과 이번 입력값(쿼리)의 대응 QWI 맵이 같다.
                 *  --> QWI 맵을 기반으로 검색이 진행되는데, QWI 맵이 같다.
                 *  --> 입력값이 같으니 결과값이 다를 수 없다.
                 *  또는, 기존의 입력이 없었는데 새 입력값에 대응되는 QWI 맵이 빈 맵이다.
                 *  --> 입력이 비었으니 출력도 빌 것이다.
                 *  --> 원래 입력이 없었으니 원래 결과도 비었을 것이다.
                 *  --> 기존의 결과도 비었고, 현재 입력값의 결과도 비었으니, 결과가 같다.
                 *  최종: 검색 결과가 바뀌지 않는다. 따라서 검색작업을 추가 진행할 필요가 없다.
                 */
                actionMain.write_lock.unlock();
                return false;
            }

            // 기존의 결과가 있었던 상황에서 QWI 맵이 변경되므로, 피드백을 보낸다.
            feedbackHelper.send_feedback();
            // 이제 쓰기 락이 필요없어 졌으므로 읽기 락으로 하강한다.
            actionMain.read_lock.lock();
            actionMain.write_lock.unlock();

            // 리스트뷰의 각 항목에 연결되어 있는 OCC 목록 소거
            search_list.occs.clear();

            if (new_query_id_map.size() == 0 && query_id_map != null) {
                // 만일 새 QWI 맵이 빈 맵이면 랭킹 평가를 진행할 필요가 없다.
                // 기존의 변수들을 업데이트한 후, 메소드를 끝낸다.
                queryMap = new_query_map;
                query_id_map = null;
                actionMain.read_lock.unlock();
                return true;
            }

            // 기존 변수 업데이트.
            queryMap = new_query_map;
            query_id_map = new_query_id_map;
            feedbackHelper.set_query_id_map(query_id_map);
            // 쿼리에 따른 각 아이템들의 중요도 평가.
            Vector<HashMap<Long, Double>> rank_vector_raw = actionMain.allocEvaluation().evaluate_by_query_map(query_id_map, null, null);
            if (rank_vector_raw == null) {
                actionMain.read_lock.unlock();
                return false;
            }
            // 주어진 평가값들을 필터링
            Vector<ArrayList<Map.Entry<Long, Double>>> rank_vector = actionMain.filter_rank_vector(
                    rank_vector_raw,
                    ActionMain.FILTER_BY_THRESHOLD
            );

            // 평가값에 따라 OCC 목록을 업데이트한다.
            update_occ_list(rank_vector);

            actionMain.read_lock.unlock();
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
        if (back_button_pressed) return; // 뒤로 가기 버튼의 동작이 중첩되어 호출되는 경우를 방지한다.

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

    /*
     * 그룹 아이템 클릭 시에는 기존 그룹의 onClick() 동작 외에 추가 동작이 하나 더 필요하다.
     * 그러나 다른 카테고리 아이템과의 동일하게 취급될 수 있어야 하므로, ActionGroup.onClickClass의 하위 클래스를 정의한다.
     */
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
                            // 기존의 그룹 버튼 동작에 이은 추가 동작 부분: 액티비티를 종료하게 되므로 쿼리결과 조회는 자동으로 종료된다. 따라서 피드백을 보낸다.
                            actionMain.write_lock.lock();
                            feedbackHelper.send_feedback();
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
