package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class AddItemActivity extends AppCompatActivity {
    ListView listView;
    EditText textInput;
    Context context;
    final ArrayList<String> suggestion_list;
    ArrayAdapter<String> adapter;

    protected ActionMain actionMain;

    protected ContentValues mode_values;
    protected int mode;
    protected static final int ADD_WORD_MACRO = 0;
    protected static final int ADD_GROUP = 1;
    protected static final int ACTIVITY_IMAGE_SELECTION = 0;

    AISuggest key_event_handler;

    public AddItemActivity() {
        super();
        context = this;
        suggestion_list = new ArrayList<>();
        mode = -1;
        mode_values = new ContentValues();
        key_event_handler = new AISuggest();
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
                android.R.layout.simple_list_item_1, android.R.id.text1, suggestion_list);
        listView.setAdapter(adapter);
        // ListView Item Click Listener
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String itemValue = (String) listView.getItemAtPosition(position);
                        textInput.setText(itemValue);
                    }
                }
        );
        /* End of ListView initialization */

        /* Method for text-changing event */
        textInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                key_event_handler.execute();
            }
        });
        /* End of Method for text-changing event */
    }

    protected void add(String itemText, int mod) {
        ActionMain.log("*** ", "started ***");
        long currentGroupID = getIntent().getLongExtra("current_group_ID", 0);

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
        key_event_handler.soft_off();
        super.onDestroy();
    }

    protected class AISuggest extends KeyEventHandler {
        public AISuggest() {
            super(AACGroupContainerPreferences.ADD_ITEM_KEY_EVENT_THREAD_POOL_SIZE);
        }

        @Override
        public void run() {
            ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (!(networkInfo != null && networkInfo.isConnected())) {
                return;
            }

            Document doc;
            if ((doc = fetch_suggest()) != null) {
                if (!check_mutual_exclusive_interrupt()) {
                    return;
                }

                synchronized (suggestion_list) {
                    NodeList nodes = doc.getElementsByTagName("suggestion");

                    System.out.println("*** Suggestions ***");
                    StringBuilder sb = new StringBuilder(100);
                    for(int i = 0; i < nodes.getLength(); i++) {
                        sb
                                .append("[")
                                .append(i)
                                .append("] ")
                                .append(nodes.item(i).getAttributes().getNamedItem("data").getNodeValue());
                        System.out.println(sb.toString());
                        sb.setLength(0);
                    }

                    suggestion_list.clear();
                    if (nodes.getLength() > 0) {
                        for (int i = 0; i < nodes.getLength(); i++) {
                            suggestion_list.add(nodes.item(i).getAttributes().getNamedItem("data").getNodeValue());
                        }
                    }
                }
            }
            else {
                synchronized (suggestion_list) {
                    suggestion_list.clear();
                }
            }

            if (!check_mutual_exclusive_interrupt()) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }

        @Nullable protected Document fetch_suggest() {
            String query = textInput.getText().toString().trim();
            if (query.length() == 0) return null;

            int code;
            URL site_url;
            try {
                site_url = new URL("http://google.com/complete/search?output=toolbar&q=" + URLEncoder.encode(query, "UTF-8"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection)site_url.openConnection();
                connection.setConnectTimeout(AACGroupContainerPreferences.ADD_ITEM_FETCH_SUGGESTION_TIMEOUT);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
                return null;
            }
            try {
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            try {
                code = connection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            if (code == 200) {
                Document doc;
                try {
                    doc = parse_xml(new BufferedInputStream(connection.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return doc;
            }

            ActionMain.log("***", "Finishing with unexpected response code " + code);
            return null;
        }

        protected Document parse_xml(InputStream stream) {
            DocumentBuilderFactory objDocumentBuilderFactory;
            DocumentBuilder objDocumentBuilder;
            Document doc;
            objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                return null;
            }

            try {
                doc = objDocumentBuilder.parse(stream);
            } catch (SAXException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
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
