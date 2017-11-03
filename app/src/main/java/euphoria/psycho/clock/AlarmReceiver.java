package euphoria.psycho.clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    private final static int STALE_WINDOW = 60 * 30;
    public static final String ALARM_KILLED = "alarm_killed";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ALARM_KILLED.equals(intent.getAction())) {

            return;
        }
        AlarmAlertWakeLock.acquireCpuWakeLock(context);
        Alarms.getInstance(context).setNextAlarm();
        Intent playAlarm = new Intent(AlarmUtils.ALARM_ALERT_ACTION);
        context.startService(playAlarm);
    }
}
