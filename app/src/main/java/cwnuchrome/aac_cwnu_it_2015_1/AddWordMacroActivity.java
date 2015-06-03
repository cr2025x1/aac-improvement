package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class AddWordMacroActivity extends AppCompatActivity {
    ListView listView;
    EditText textInput;
    Context context;
    updateList updater;
    ArrayList<String> suggestionList;
    ArrayAdapter<String> adapter;

    ActionDBHelper mDbHelper;
//    SQLiteDatabase db;

    protected ActionMain actionMain;

    public AddWordMacroActivity() {
        super();
        context = this;
        updater = new updateList();
        suggestionList = new ArrayList<String>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word_macro);

        mDbHelper = new ActionDBHelper(this);
        actionMain = ActionMain.getInstance();

        textInput = (EditText)findViewById(R.id.edittext_add_word_macro);
        textInput.setOnKeyListener(new enterKeyListener());

        /* ListView Initialization */
        listView = (ListView) findViewById(R.id.list_add_word_macro);
        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data
        adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, android.R.id.text1, suggestionList);
        listView.setAdapter(adapter);
        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String itemValue = (String) listView.getItemAtPosition(position);

                textInput.setText(itemValue);

                add(itemValue);
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

    protected void add(String itemValue) {
        long currentGroupID = getIntent().getLongExtra("currentGroupID", 0);
        ContentValues values = new ContentValues();

        itemValue = itemValue.trim();
        String[] textTokens = itemValue.split("\\s");
        boolean isMacro = textTokens.length > 1;

        long wordIDs[] = new long[textTokens.length];

        // TODO: Consider handling adding operation fails.
        boolean madeChange = false;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        for (int i = 0; i < textTokens.length; i++) {
            System.out.println("Adding " + textTokens[i]);

            if (actionMain.itemChain[ActionMain.item.ID_Word].exists(db, textTokens[i]) == -1) madeChange = true;

            ContentValues record = new ContentValues();
            record.put(ActionWord.SQL.COLUMN_NAME_PARENT_ID, currentGroupID);
            record.put(ActionWord.SQL.COLUMN_NAME_WORD, textTokens[i]);

            wordIDs[i] = ((ActionWord)actionMain.itemChain[ActionMain.item.ID_Word]).add(db, record);
            record.clear();
        }

        if (isMacro && actionMain.itemChain[ActionMain.item.ID_Macro].exists(db, itemValue) == -1) {
            StringBuilder wordchain = new StringBuilder("|");
            for (int i = 0; i < textTokens.length; i++) {
                wordchain.append(":");
                wordchain.append(wordIDs[i]);
                wordchain.append(":");
            }
            wordchain.append("|");
            String wordChainString = wordchain.toString();

            ContentValues record = new ContentValues();
//        record.put(SQL.COLUMN_NAME_ENTRY_ID, 999); // 임시! 아마도 삭제될 것 같음.
            record.put(ActionMacro.SQL.COLUMN_NAME_PARENT_ID, currentGroupID);
            record.put(ActionMacro.SQL.COLUMN_NAME_PRIORITY, ActionMain.getInstance().rand.nextInt(100)); // 이것도 임시
            record.put(ActionMacro.SQL.COLUMN_NAME_WORD, itemValue);
            record.put(ActionMacro.SQL.COLUMN_NAME_STEM, itemValue);
            record.put(ActionMacro.SQL.COLUMN_NAME_WORDCHAIN, wordChainString);
            actionMain.itemChain[ActionMain.item.ID_Macro].add(db, record);

            record.clear();

            madeChange = true;
        }

        db.close();

        if (madeChange) {
            Intent i = new Intent();
            Bundle extra = new Bundle();
            extra.putString("ItemName", itemValue);
            i.putExtras(extra);

            setResult(RESULT_OK, i);
            finish();
        } else {
            Toast.makeText(this, "Already Exists", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    protected void onDestroy() {
        updater.onComplete(); // Setting Updater thread to end
        super.onDestroy();
    }

    class updateList extends AsyncTask<String, Integer, Long> {
        // TODO: Change this into the multi-threaded version.
        Document doc;
        NodeList descNodes;

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
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        System.out.println("Fetching data...");
                        if (this.fetchSuggestion()) {

                            int suggestion_count = descNodes.getLength();
                            suggestionList.clear();
                            if (suggestion_count > 0) {
                                for (int i = 0; i < suggestion_count; i++) {
                                    suggestionList.add(descNodes.item(i).getAttributes().getNamedItem("data").getNodeValue());
                                }
                            }
                        }
                        else {
                            suggestionList.clear();
                        }

                        runOnUiThread(new updateItem());
                    } else {
                        System.out.println("No network connection available.");
                    }


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

        protected boolean fetchSuggestion() throws Exception
        {
            String queryWord = textInput.getText().toString().trim().replace(" ", "%20");
            if (queryWord.length() == 0) return false;
            URL url = new URL("http://google.com/complete/search?output=toolbar&q=" + queryWord);
            URLConnection connection = url.openConnection();

            // http://stackoverflow.com/questions/15596312/xml-saxparserexception-in-android : 참고한 사이트
            try {
                doc = parseXML(new BufferedInputStream(connection.getInputStream()));
            } catch (UnknownHostException e) {
                System.out.println("Unable to fetch data: Cannot resolve hostname");
                return false;
            }
            descNodes = doc.getElementsByTagName("suggestion");

            for(int i = 0; i < descNodes.getLength(); i++) {
                System.out.println(descNodes.item(i).getAttributes().getNamedItem("data").getNodeValue());
            }

            return true;
        }

        protected Document parseXML(InputStream stream)
                throws Exception
        {
            DocumentBuilderFactory objDocumentBuilderFactory = null;
            DocumentBuilder objDocumentBuilder = null;
            Document doc = null;
            try
            {
                objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
                objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();

                doc = objDocumentBuilder.parse(stream);
            }
            catch(Exception ex)
            {
                System.out.println("Error occurred while parsing fetched XML data.");
                throw ex;
            }

            return doc;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_word_macro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_word_macro) {
            add(textInput.getText().toString());
            return true;
        }
        else if (id == R.id.action_cancel_add_word_macro) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class enterKeyListener implements EditText.OnKeyListener {

        public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    add(textInput.getText().toString());
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    setResult(RESULT_CANCELED);
                    finish();
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
}
