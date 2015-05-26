package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
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
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class AddWordMacroActivity extends AppCompatActivity {
    ListView listView;
    EditText textInput;
//    Document doc;
    Context context = this;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word_macro);

        textInput = (EditText)findViewById(R.id.edittext_add_word_macro);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list_add_word_macro);

        new updateList().execute();

        // Defined Array values to show in ListView
//        String[] values = new String[] { "Android List View",
//                "Adapter implementation",
//                "Simple List View In Android",
//                "Create List View Android",
//                "Android Example",
//                "List View Source Code",
//                "List View Array Adapter",
//                "Android Example List View"
//        };



        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, android.R.id.text1, values);
//
//
//        // Assign adapter to ListView
//        listView.setAdapter(adapter);

//        // ListView Item Click Listener
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//
//                // ListView Clicked item index
//                int itemPosition     = position;
//
//                // ListView Clicked item value
//                String  itemValue    = (String) listView.getItemAtPosition(position);
//
//                // Show Alert
//                Toast.makeText(getApplicationContext(),
//                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
//                        .show();
//
//                textInput.setText(itemValue);
//            }
//
//        });
    }

    class updateList extends AsyncTask<String, Integer, Long> {
        Document doc;
        NodeList descNodes;

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Long result) {
//            btn.setText("Thread END");
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
//            btn.setText("Thread START!!!!");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            pb.setProgress(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected Long doInBackground(String... params) {
            long result = 0;

            try {
                this.start();
                int suggestion_count = descNodes.getLength();
                if (suggestion_count > 0) {
                    String[] suggestion_list = new String[suggestion_count];
//                    ArrayList<String> suggestion_list = new ArrayList<String>();
                    for (int i = 0; i < suggestion_count; i++) {
                        suggestion_list[i] = descNodes.item(i).getAttributes().getNamedItem("data").getNodeValue();
//                        suggestion_list.add(descNodes.item(i).getAttributes().getNamedItem("data").getNodeValue());
                    }

                    adapter = new ArrayAdapter<String>(context,
                            android.R.layout.simple_list_item_1, android.R.id.text1, suggestion_list);
//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getParent(),
//                            android.R.layout.simple_list_item_1, android.R.id.text1, suggestion_list);

                    runOnUiThread(new updateItem());

//                    // Assign adapter to ListView
//                    listView.setAdapter(adapter);
//
//                    // ListView Item Click Listener
//                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view,
//                                                int position, long id) {
//
//                            // ListView Clicked item index
//                            int itemPosition = position;
//
//                            // ListView Clicked item value
//                            String itemValue = (String) listView.getItemAtPosition(position);
//
//                            // Show Alert
//                            Toast.makeText(getApplicationContext(),
//                                    "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
//                                    .show();
//
//                            textInput.setText(itemValue);
//                        }
//
//                    });
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        class updateItem implements Runnable {
            public void run() {
                // Assign adapter to ListView
                listView.setAdapter(adapter);

                // ListView Item Click Listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // ListView Clicked item index
                        int itemPosition = position;

                        // ListView Clicked item value
                        String itemValue = (String) listView.getItemAtPosition(position);

                        // Show Alert
                        Toast.makeText(getApplicationContext(),
                                "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                                .show();

                        textInput.setText(itemValue);
                    }

                });
            }
        }

        protected void start() throws Exception
        {
            URL url = new URL("http://google.com/complete/search?output=toolbar&q=theoric");
            URLConnection connection = url.openConnection();

            // http://stackoverflow.com/questions/15596312/xml-saxparserexception-in-android : 참고한 사이트
            doc = parseXML(new BufferedInputStream(connection.getInputStream()));
            descNodes = doc.getElementsByTagName("suggestion");

            for(int i=0; i<descNodes.getLength();i++)
            {
//            System.out.println(descNodes.item(i).getTextContent());
                System.out.println(descNodes.item(i).getAttributes().getNamedItem("data").getNodeValue());
            }
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
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }
}
