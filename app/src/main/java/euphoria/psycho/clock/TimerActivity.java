package euphoria.psycho.clock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import java.util.List;

public class TimerActivity extends Activity {

    private static final int MENU_ADD_TIMER = 926;
    private static final int MENU_KILL_ALARM = 101;
    private ListView mListView;
    private ListItemsAdapter mListItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize() {
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.list_view);
        mListItemsAdapter = new ListItemsAdapter(this, Alarms.getInstance(this).getTimerDescriptions());

        mListView.setAdapter(mListItemsAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Tuple3<Integer, Float, String> timer = mListItemsAdapter.getItem(i);

                Intent intent = new Intent(TimerActivity.this, TimerService.class);
                stopService(intent);

                long timespan = (long) (timer.v2 * 60) * 1000L;
                intent.putExtra("timespan", timespan);
                startService(intent);
            }
        });
        registerForContextMenu(mListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, 0, 0, "删除");

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case 0:
                Alarms.getInstance(this).deleteTimer(mListItemsAdapter.getItem(i.position).v1);
                refreshList();
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, MENU_ADD_TIMER, 0, "添加计时器");
        menu.add(0, MENU_KILL_ALARM, 0, "关闭");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_TIMER:
                addTimer();
                return true;
            case MENU_KILL_ALARM:
                killAlarm();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void killAlarm() {
        Intent intent = new Intent(this, TimerService.class);
        stopService(intent);
    }


    private void addTimerImplement(String value) {
        if (TextUtils.isEmpty(value)) return;

        float timespan = Utils.toFloat(value);

        Alarms.getInstance(this).insertTimer(timespan, value.replaceFirst("[\\s0-9\\.]+", ""));
        refreshList();
    }

    private void refreshList() {
        mListItemsAdapter.refreshDatas(Alarms.getInstance(this).getTimerDescriptions());
    }

    private void addTimer() {
        final EditText editText = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(editText).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        addTimerImplement(editText.getText().toString());
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private class ViewHolder {
        TextView textView;
    }

    private class ListItemsAdapter extends BaseAdapter {

        private final Context mContext;
        private final List<Tuple3<Integer, Float, String>> mIntegerList;

        @Override
        public int getCount() {
            return mIntegerList == null ? 0 : mIntegerList.size();
        }

        public void refreshDatas(List<Tuple3<Integer, Float, String>> datas) {
            mIntegerList.clear();
            mIntegerList.addAll(datas);
            notifyDataSetChanged();
        }

        @Override
        public Tuple3<Integer, Float, String> getItem(int i) {

            return mIntegerList == null ? null : mIntegerList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            ViewHolder viewHolder = null;

            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_item, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.textView = view.findViewById(R.id.text_view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            String v = mIntegerList.get(i).v2 + " " + mIntegerList.get(i).v3;
            viewHolder.textView.setText(v);
            return view;
        }

        private ListItemsAdapter(Context context, List<Tuple3<Integer, Float, String>> integerList) {
            mContext = context;
            mIntegerList = integerList;
        }
    }
}
