package euphoria.psycho.clock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.*;
import android.widget.*;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
    private static final int MENU_ADD_TIME_TIMER = 1;

    private static final int MENU_ADD_TIMER = 926;
    private static final int MENU_KILL_ALARM = 101;

    private ListView mListView;
    private ListItemsAdapter mListItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.WAKE_LOCK,
            }, 100);
        } else {
            initialize();
        }
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initialize();
    }

    private void initialize() {
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.list_view);
        mListItemsAdapter = new ListItemsAdapter(this, Alarms.getInstance(this).getDescriptions());

        mListView.setAdapter(mListItemsAdapter);

        registerForContextMenu(mListView);
        Alarms.getInstance(MainActivity.this).setNextAlarm();
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
                Alarms.getInstance(this).delete(mListItemsAdapter.getItem(i.position).first);
                mListItemsAdapter.refreshDatas(Alarms.getInstance(this).getDescriptions());
                Alarms.getInstance(this).setNextAlarm();
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD_TIME_TIMER, 0, "计时器");

        menu.add(0, MENU_ADD_TIMER, 0, "添加闹钟");
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
            case MENU_ADD_TIME_TIMER:
                Intent intent = new Intent(this, TimerActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void killAlarm() {
        Intent intent = new Intent(this, AlarmKlaxon.class);
        stopService(intent);
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
                        String value = editText.getText().toString();

                        if (value == null) return;

                        Pair<Integer, Integer> ctime = AlarmUtils.parseCalendar(value);
                        Calendar calendar = null;
                        if (ctime == null) {
//                            int timeInSecond = Utils.toInteger(value);
//                            if (timeInSecond == -1) return;
//
//                            calendar = AlarmUtils.calculateAlarm(timeInSecond);
//
//                            AlarmUtils.setAlarm(MainActivity.this, calendar);
//                            Utils.toast(MainActivity.this, AlarmUtils.formatTime(MainActivity.this, calendar));

                        } else {
                            calendar = AlarmUtils.calculateAlarm(ctime.first, ctime.second);
                            Alarms.getInstance(MainActivity.this).addAlarm(ctime.first * 60 + ctime.second, ctime.first + ":" + ctime.second + " " + value.replaceAll("^[0-9 ]+", ""));
                            Alarms.getInstance(MainActivity.this).setNextAlarm();
                            mListItemsAdapter.refreshDatas(Alarms.getInstance(MainActivity.this).getDescriptions());

                        }


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
        private final List<Pair<Integer, String>> mIntegerList;

        @Override
        public int getCount() {
            return mIntegerList == null ? 0 : mIntegerList.size();
        }

        public void refreshDatas(List<Pair<Integer, String>> datas) {
            mIntegerList.clear();
            mIntegerList.addAll(datas);
            notifyDataSetChanged();
        }

        @Override
        public Pair<Integer, String> getItem(int i) {

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
            String v = mIntegerList.get(i).second;
            viewHolder.textView.setText(v);
            return view;
        }

        private ListItemsAdapter(Context context, List<Pair<Integer, String>> integerList) {
            mContext = context;
            mIntegerList = integerList;
        }
    }
}
