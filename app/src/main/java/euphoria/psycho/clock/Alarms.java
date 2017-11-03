package euphoria.psycho.clock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Alarms {

    private DataBaseHelper mDatabaseHelper;

    private final Context mContext;

    private static Alarms sAlarms;

    public Alarms(Context context) {
        mContext = context;
        mDatabaseHelper = new DataBaseHelper(context);
    }

    public void addAlarm(int ranking, String description) {
        mDatabaseHelper.insert(ranking, description);
    }

    public void delete(int ranking) {
        mDatabaseHelper.delete(ranking);
    }

    public List<Integer> listClock() {

        return mDatabaseHelper.listClock();
    }

    public List<Pair<Integer, String>> getDescriptions() {
        return mDatabaseHelper.getDescriptions();
    }

    public void setNextAlarm() {
        List<Integer> clock = mDatabaseHelper.listClock();
        if (clock.size() < 1) return;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        int now = nowHour * 60 + nowMinute;
        boolean isChanged = false;
        for (int n : clock) {
            if (n > now) {
                now = n;
                isChanged = true;
                break;
            }
        }
        if (!isChanged) {
            now = clock.get(0);
        }

        Calendar calendar = AlarmUtils.calculateAlarm(now / 60, now % 60);
        Utils.toast(mContext, AlarmUtils.formatTime(mContext, calendar));

        AlarmUtils.setAlarm(mContext, calendar);
    }

    public static Alarms getInstance(Context context) {
        if (sAlarms == null)
            sAlarms = new Alarms(context);
        return sAlarms;
    }

    public void insertTimer(float time, String description) {
        mDatabaseHelper.insertTimer(time, description);
    }

    public void deleteTimer(int id) {
        mDatabaseHelper.deleteTimer(id);
    }

    public List<Tuple3<Integer, Float, String>> getTimerDescriptions() {
        return mDatabaseHelper.getTimerDescriptions();
    }

    private class DataBaseHelper extends SQLiteOpenHelper {

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `clock` (\n" +
                    "\t`_id`\tINTEGER,\n" +
                    "\t`ranking`\tINTEGER,\n" +
                    "\t`description`\tTEXT,\n" +

                    "\tPRIMARY KEY(`_id`)\n" +
                    ");");

            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `timer` (\n" +
                    "\t`_id`\tINTEGER,\n" +
                    "\t`time`\tINTEGER,\n" +
                    "\t`description`\tTEXT,\n" +

                    "\tPRIMARY KEY(`_id`)\n" +
                    ");");
        }

        public void insertTimer(float time, String description) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("time", time);
            contentValues.put("description", description);
            getWritableDatabase().insert("timer", null, contentValues);
        }

        public void insert(int ranking, String description) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("ranking", ranking);
            contentValues.put("description", description);
            getWritableDatabase().insert("clock", null, contentValues);
        }

        public void delete(int ranking) {
            getWritableDatabase().delete("clock", "_id=?", new String[]{Integer.toString(ranking)});
        }

        public void deleteTimer(int id) {
            getWritableDatabase().delete("timer", "_id=?", new String[]{Integer.toString(id)});
        }

        public List<Integer> listClock() {

            List<Integer> clock = new ArrayList<>();

            Cursor cursor = getReadableDatabase().rawQuery("select ranking from clock order by ranking", null);

            while (cursor.moveToNext()) {
                clock.add(cursor.getInt(0));
            }
            cursor.close();
            return clock;
        }

        public List<Tuple3<Integer, Float, String>> getTimerDescriptions() {
            List<Tuple3<Integer, Float, String>> clock = new ArrayList<>();

            Cursor cursor = getReadableDatabase().rawQuery("select _id,time,description from timer order by time", null);

            while (cursor.moveToNext()) {

                clock.add(new Tuple3<Integer, Float, String>(cursor.getInt(0), cursor.getFloat(1), cursor.getString(2)));
            }
            cursor.close();
            return clock;
        }

        public List<Pair<Integer, String>> getDescriptions() {
            List<Pair<Integer, String>> clock = new ArrayList<>();

            Cursor cursor = getReadableDatabase().rawQuery("select _id,description from clock order by ranking", null);

            while (cursor.moveToNext()) {

                clock.add(Pair.create(cursor.getInt(0), cursor.getString(1)));
            }
            cursor.close();
            return clock;
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }

        public DataBaseHelper(Context context) {
            super(context, new File(Environment.getExternalStorageDirectory(), "clock.db").getAbsolutePath(), null, 1);
        }
    }
}
