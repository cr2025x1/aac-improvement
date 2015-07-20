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
import android.view.Menu;
import android.view.MenuItem;
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
    int position_max;
    ArrayList<Integer> clickedList;

    ArrayList<ActionItem.onClickClass> suggestionOCCList;

    protected ActionMain actionMain;

    public SearchActivity() {
        super();
        context = this;
        updater = new updateList();

        suggestionList = new ArrayList<>();
        suggestionOCCList = new ArrayList<>(ActionMain.item.ITEM_COUNT);

        position_max = 0;
        clickedList = new ArrayList<>(AACGroupContainerPreferences.RANKING_FUNCTION_BEST_MATCH_N);
        query_id_map = null;
        queryMap = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        actionMain = ActionMain.getInstance();

        textInput = (EditText)findViewById(R.id.edittext_search);
        textInput.setOnKeyListener(new enterKeyListener());

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

                if (position_max < position) position_max = position;
                clickedList.add(position);
                suggestionOCCList.get(position).onClick(view);

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
                    suggestionList.clear();
                    if (fetchSuggestion()) {
                        for (ActionItem.onClickClass occ : suggestionOCCList) {
                            int catID = occ.getItemCategoryID();
                            suggestionList.add(actionMain.itemChain[catID].CLASS_NAME + " " + occ.phonetic);
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
            if (new_query_map.size() == 0 ||
                    (queryMap != null && new_query_map.equals(queryMap))) return false;
            HashMap<Long, QueryWordInfo> new_query_id_map =
                    ((ActionWord)actionMain.itemChain[ActionMain.item.ID_Word]).convert_to_id_ref_map(new_query_map);
            if (new_query_id_map.size() == 0 ||
                    (query_id_map != null && new_query_id_map.equals(query_id_map))) return false;
            send_feedback();
            suggestionOCCList.clear();

            ActionMain actionMain = ActionMain.getInstance();
            queryMap = new_query_map;
            query_id_map = new_query_id_map;
            Vector<ArrayList<Map.Entry<Long, Double>>> rank_vector = actionMain.allocEvaluation().evaluate_by_query_map(query_id_map);

            update_occ_list(rank_vector);

            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_add_word_macro) {
//            return true;
//        }
//        else if (id == R.id.action_cancel_add_word_macro) {
//            setResult(RESULT_CANCELED);
//            finish();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    class enterKeyListener implements EditText.OnKeyListener {

        public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public void onBackPressed() {
        send_feedback();

        setResult(RESULT_CANCELED);
        finish();
    }

    protected void update_occ_list(Vector<ArrayList<Map.Entry<Long, Double>>> rank_list) {
        Vector<ListIterator<Map.Entry<Long, Double>>> iterator_vector = new Vector<>(ActionMain.item.ITEM_COUNT);
        for (int i = 0; i < ActionMain.item.ITEM_COUNT; i++) iterator_vector.add(rank_list.get(i).listIterator());

        int iter_end_count = 0;
        int catID;
        while (iter_end_count < ActionMain.item.ITEM_COUNT - 1) {
            double max_rank = 0.0d;

            for (ListIterator<Map.Entry<Long, Double>> iter : iterator_vector) {
                if (iter.hasNext()) {
                    Map.Entry<Long, Double> e = iter.next();
                    double value = e.getValue();
                    if (value > max_rank) {
                        max_rank = value;
                    }
                    iter.previous();
                }
            }

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

        catID = 0;
        for (ListIterator<Map.Entry<Long, Double>> iter : iterator_vector) {
            while (iter.hasNext()) {
                addOCC(catID, iter.next());
            }
            catID++;
        }
    }

    protected void addOCC(int catID, Map.Entry<Long, Double> entry) {
        ActionItem actionItem = actionMain.itemChain[catID];
        ActionItem.onClickClass occ;
        AACGroupContainer container = actionMain.getReferrer().get(getIntent().getIntExtra("AACGC_ID", -1));
        if (catID == ActionMain.item.ID_Group) {
            occ = new GroupOCCWrapper(context, container);
        }
        else {
            occ = actionItem.allocOCC(context, container);
        }

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
        suggestionOCCList.add(occ);

    }

    // 그룹 아이템 클릭시에는 액티비티를 벗어나는 추가 동작이 필요하므로 서브클래스를 하나 정의한다.
    protected class GroupOCCWrapper extends ActionGroup.onClickClass {
        public GroupOCCWrapper(Context context, AACGroupContainer container) {
            super(context, container);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent();
            setResult(RESULT_OK, i);
            super.onClick(v);
            send_feedback();

            finish();
        }
    }

    protected void send_feedback() {
        if (clickedList.isEmpty()) return;

        ActionMain.SearchFeedbackInfo[] feedbackInfos = new ActionMain.SearchFeedbackInfo[position_max + 1];
        for (int i = 0; i <= position_max; i++) {
            ActionItem.onClickClass occ = suggestionOCCList.get(i);
            feedbackInfos[i] = new ActionMain.SearchFeedbackInfo(occ.getItemCategoryID(), occ.getItemID(), clickedList.contains(i));
        }
        actionMain.applyFeedback(query_id_map, feedbackInfos);

        clickedList.clear();
        position_max = 0;
    }


    // TODO: 그룹을 클릭해서 다시 넘어갈 때에도 반드시 피드백이 진행되게 할 것
}
