package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SearchActivity extends AppCompatActivity {
    ListView listView;
    EditText textInput;
    Context context;
    updateList updater;
    ArrayList<String> suggestionList;
    ArrayAdapter<String> adapter;

    ArrayList<ActionItem.onClickClass> suggestionOCCList;

    protected ActionMain actionMain;

    public SearchActivity() {
        super();
        context = this;
        updater = new updateList();

        suggestionList = new ArrayList<>();
        suggestionOCCList = new ArrayList<>(ActionMain.item.ITEM_COUNT);
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
                        Collections.sort(suggestionOCCList, new Comparator<ActionItem.onClickClass>() {
                            @Override
                            public int compare(ActionItem.onClickClass lhs, ActionItem.onClickClass rhs) {
                                return lhs.rank > rhs.rank ? -1 : 1;
                            }
                        });

                        for (ActionItem.onClickClass occ : suggestionOCCList) {
                            suggestionList.add(occ.phonetic);
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
            HashMap<String, Long> queryMap = ActionMain.reduce_to_map(textInput.getText().toString());
            if (queryMap.size() == 0) return false;

            ActionMain actionMain = ActionMain.getInstance();
            Vector<HashMap<Long, Double>> rank_vector = actionMain.allocEvaluation().evaluate_by_query_map(queryMap);

            suggestionOCCList.clear();
            for (int i = 0 ; i < ActionMain.item.ITEM_COUNT; i++) {
                update_occ_list(i, rank_vector.get(i));
            }

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
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void update_occ_list(int categoryID, HashMap<Long, Double> map) {
        ArrayList<Map.Entry<Long, Double>> entries =
                new ArrayList<>(map.entrySet());

        for (Map.Entry<Long, Double> entry : entries) if (entry.getValue() > AACGroupContainerPreferences.RANKING_FUNCTION_CUTOFF_THRESHOLD) {
            ActionItem actionItem = actionMain.itemChain[categoryID];
            ActionItem.onClickClass occ = actionItem.allocOCC(
                    context,
                    actionMain.getReferrer().get(getIntent().getIntExtra("AACGC_ID", -1))
            );
            occ.rank = entry.getValue();
            occ.itemID = entry.getKey();

            Cursor c = actionMain.getDB().query(
                    actionItem.TABLE_NAME,
                    new String[] {ActionItem.SQL.COLUMN_NAME_WORD},
                    ActionItem.SQL._ID + "=" + entry.getKey(),
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

    }

}
