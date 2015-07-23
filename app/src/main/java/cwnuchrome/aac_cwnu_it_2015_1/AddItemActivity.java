package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class AddItemActivity extends AppCompatActivity {
    ListView listView;
    EditText textInput;
    Context context;
    updateList updater;
    ArrayList<String> suggestionList;
    ArrayAdapter<String> adapter;

    protected ActionMain actionMain;

    protected ContentValues mode_values;
    protected int mode;
    protected static final int ADD_WORD_MACRO = 0;
    protected static final int ADD_GROUP = 1;

    protected static final int ACTIVITY_IMAGE_SELECTION = 0;

    public AddItemActivity() {
        super();
        context = this;
        updater = new updateList();
        suggestionList = new ArrayList<>();
        mode = -1;
        mode_values = new ContentValues();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        actionMain = ActionMain.getInstance();

        textInput = (EditText)findViewById(R.id.edittext_add_item);
        textInput.setOnKeyListener(new enterKeyListener());

        /* ListView Initialization */
        listView = (ListView) findViewById(R.id.list_add_item);
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

                String itemValue = (String) listView.getItemAtPosition(position);

                textInput.setText(itemValue);

//                add(itemValue);
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

    // TODO: 단일 단어 길이의 워드도 매크로로 취급하도록 만들기
    // TODO: 작업 시 워드가 자동 삭제되게 만들어야 한다.
    protected void add(String itemText, int mod) {
        ActionMain.log("*** ", "started ***");
        long currentGroupID = getIntent().getLongExtra("currentGroupID", 0);

        String[] textTokens = ActionMain.tokenize(itemText);

//        boolean isMultiwordRequired = textTokens.length > 1;
//        switch (mod) {
//            case ADD_WORD_MACRO:
//                ActionMain.log(null, "identified as a macro needed\"");
//                isMultiwordRequired = textTokens.length > 1;
//                break;
//            case ADD_GROUP:
//                isMultiwordRequired = true;
//                break;
//        }

        long[] wordIDs = ((ActionWord)actionMain.itemChain[ActionMain.item.ID_Word]).add_multi(textTokens);

        boolean madeChange = false;
        // 코드 공통화가 안 되는 이유... add 메소드는 상속 메소드가 아니기 때문임.
        switch (mod) {
            case ADD_WORD_MACRO:
//                if (textTokens.length > 1) { // 워드의 매크로화를 위해 주석 처리됨.
//                    ActionMain.log(null, "identified as a macro needed\"");
                    ActionMacro actionMacro = (ActionMacro)actionMain.itemChain[ActionMain.item.ID_Macro];
                    if (actionMacro.exists(itemText) == -1) {
                        madeChange = true;

                        actionMacro.add(
                                currentGroupID,
                                0,
                                itemText,
                                itemText,
                                wordIDs,
                                mode_values.getAsString(ActionMacro.SQL.COLUMN_NAME_PICTURE),
                                mode_values.getAsInteger(ActionMacro.SQL.COLUMN_NAME_PICTURE_IS_PRESET) == 1
                        );
                    }
                    else ActionMain.log(null, " macro \"" + itemText + "\" already exists");
//                }
                break;

            case ADD_GROUP:
                ActionGroup actionGroup = (ActionGroup)actionMain.itemChain[ActionMain.item.ID_Group];
                if (actionGroup.exists(itemText) == -1) {
                    madeChange = true;

                    actionGroup.add(
                            currentGroupID,
                            0,
                            itemText,
                            itemText,
                            wordIDs,
                            mode_values.getAsString(ActionMacro.SQL.COLUMN_NAME_PICTURE),
                            mode_values.getAsInteger(ActionMacro.SQL.COLUMN_NAME_PICTURE_IS_PRESET) == 1
                    );
                }
                break;
        }

        if (madeChange) {
            Intent i = new Intent();
            Bundle extra = new Bundle();
            extra.putString("ItemName", itemText);
            i.putExtras(extra);

            setResult(RESULT_OK, i);
            finish();
        } else {
            Toast.makeText(this, "Already Exists", Toast.LENGTH_SHORT)
                    .show();
        }
        ActionMain.log("*** ", "ended ***");
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
            String queryWord = textInput.getText().toString().trim();
            if (queryWord.length() == 0) return false;
            // 출처: http://stackoverflow.com/questions/10786042/java-url-encoding-of-query-string-parameters
            URL url = new URL("http://google.com/complete/search?output=toolbar&q=" + URLEncoder.encode(queryWord, "UTF-8"));
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
            DocumentBuilderFactory objDocumentBuilderFactory;
            DocumentBuilder objDocumentBuilder;
            Document doc;
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
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
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
            mode = ADD_WORD_MACRO;
            getImage();
//            add(textInput.getText().toString(), ADD_WORD_MACRO);
            return true;
        }

        if (id == R.id.action_add_group) {
            mode = ADD_GROUP;
            getImage();
//            add(textInput.getText().toString(), ADD_GROUP);
            return true;
        }

        if (id == R.id.action_cancel_add_word_macro) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void getImage() {
        Intent i = new Intent(this, ImageSelectionActivity.class);
        startActivityForResult(i, ACTIVITY_IMAGE_SELECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_IMAGE_SELECTION) {
            switch (resultCode) {
                case RESULT_CANCELED:
                    break;

                case RESULT_OK:
                    int isPreset = data.getIntExtra(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, -1);
                    if (isPreset == -1) {
                        System.out.println("Extra doesn't contain a required data. (IS_PRESET)");
                        break;
                    }
                    else mode_values.put(ActionItem.SQL.COLUMN_NAME_PICTURE_IS_PRESET, isPreset);

                    String pictureFilename = data.getStringExtra(ActionItem.SQL.COLUMN_NAME_PICTURE);
                    if (pictureFilename == null) {
                        System.out.println("Extra doesn't contain a required data. (PICTURE_FILENAME)");
                        break;
                    }
                    else mode_values.put(ActionItem.SQL.COLUMN_NAME_PICTURE, pictureFilename);

                    add(textInput.getText().toString(), mode);
                    break;
            }

        }
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
}
