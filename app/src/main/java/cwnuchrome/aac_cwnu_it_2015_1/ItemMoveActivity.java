package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.vivin.GenericTree;
import net.vivin.GenericTreeNode;
import net.vivin.GenericTreeTraversalOrderEnum;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


public class ItemMoveActivity extends AppCompatActivity {

    protected ListView listView;
    InfoAdapter adapter;
    ArrayList<Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_move);

        ConcurrentLibrary.run_off_ui_thread_with_result(
                this,
                new RunnableWithResult<ArrayList<Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer>>>() {
                    @Override
                    public void run() {
                        setResult(get_list());
                    }
                },
                new RunnableWithResult<ArrayList<Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer>>>() {
                    @Override
                    public void run() {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        list = getParam();

                                    /* ListView Initialization */
                                        listView = (ListView) findViewById(R.id.listview_tree);
                                        adapter = new InfoAdapter(ItemMoveActivity.this,
                                                android.R.layout.simple_list_item_1, android.R.id.text1, list);
                                        listView.setAdapter(adapter);
                                        listView.setOnItemClickListener(
                                                new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                        Intent i = new Intent();
                                                        i.putExtra("new_group_id", id);
                                                        ItemMoveActivity.this.setResult(RESULT_OK, i);

                                                        finish();
                                                    }
                                                }
                                        );

                                    }
                                }
                        );
                    }
                }

        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_move, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_main_item_move_cancel) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected ArrayList<Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer>> get_list() {
        ActionMain actionMain = ActionMain.getInstance();
        actionMain.read_lock.lock();
        int blacklist_id = getIntent().getIntExtra("blacklist", -1);
        if (blacklist_id == -1) throw new IllegalArgumentException("Required parameter is not given by intent.");
        ArrayList<Long> blacklist = actionMain.getIDReferrer().detach(blacklist_id);

        GenericTree<ActionGroup.Info> group_tree = ((ActionGroup)(ActionMain.getInstance().itemChain[ActionMain.item.ID_Group])).get_sub_tree(1, blacklist); // 1 == 루트 그룹의 ID
        Set<Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer>> depth_set = group_tree.buildWithDepth(GenericTreeTraversalOrderEnum.PRE_ORDER).entrySet();

        actionMain.read_lock.unlock();
        return new ArrayList<>(depth_set);
    }

    public class InfoAdapter extends BaseAdapter {
        private Context mContext;
        private int mResource;
        private int mFieldId = 0;
        private LayoutInflater mInflater;
        private ArrayList<Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer>> mObjects;

        public InfoAdapter(
                Context context,
                int resource,
                int textViewResourceId,
                ArrayList<Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer>> objects) {
            init(context, resource, textViewResourceId, objects);
        }

        private void init(
                Context context,
                int resource,
                int textViewResourceId,
                ArrayList<Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer>> objects) {
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mResource = resource;
            mObjects = objects;
            mFieldId = textViewResourceId;
        }

        public InfoAdapter(
                Context c, ArrayList<Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer>> mObjects) {
            mContext = c;
            this.mObjects = mObjects;
            mInflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return mObjects.size();
        }

        public Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer> getItem(int position) {
            return mObjects.get(position);
        }

        public long getItemId(int position) {
            return getItem(position).getKey().getData().id;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            TextView text;

            if (convertView == null) {
                view = mInflater.inflate(mResource, parent, false);
            } else {
                view = convertView;
            }

            try {
                if (mFieldId == 0) {
                    //  If no custom field is assigned, assume the whole resource is a TextView
                    text = (TextView) view;
                } else {
                    //  Otherwise, find the TextView field within the layout
                    text = (TextView) view.findViewById(mFieldId);
                }
            } catch (ClassCastException e) {
                Log.e("InfoAdapter", "You must supply a resource ID for a TextView");
                throw new IllegalStateException(
                        "InfoAdapter requires the resource ID to be a TextView", e);
            }

            Map.Entry<GenericTreeNode<ActionGroup.Info>, Integer> item = getItem(position);
            StringBuilder sb = new StringBuilder();
            ActionGroup.Info info = item.getKey().getData();
            int depth = item.getValue();
            for (int i = 0; i < depth; i++) sb.append("    ");
            sb.append(info.name);
            text.setText(sb.toString());

            return view;
        }
    }

}
