package euphoria.psycho.clock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;

public class TimerService extends Service {
    private static final int ID = 25;
    private Vibrator mVibrator;
    private static final long[] sVibratePattern = new long[]{500, 500};

    @Override
    public void onCreate() {

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        AlarmAlertWakeLock.acquireCpuWakeLock(this);


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stop();
        // Stop listening for incoming calls.
        AlarmAlertWakeLock.releaseCpuLock();
    }

    private void stop() {
        mVibrator.cancel();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stop();
            stopSelf();
        }
        long timespan = intent.getLongExtra("timespan", 0);
        if (timespan == 0) {
            stop();
            stopSelf();
            return START_NOT_STICKY;

        }
        final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this).setContentTitle("时间")
                .setContentText(timespan / 1000 + "秒").setSmallIcon(R.mipmap.ic_launcher_alarmclock).build();

        startForeground(ID, notification);
        notificationManager.notify(ID, notification);
        DownTimer timer = new DownTimer();
        timer.setTotalTime(timespan);
        timer.setIntervalTime(10000);
        timer.setTimerLiener(new DownTimer.TimeListener() {
            @Override
            public void onFinish() {
                mVibrator.vibrate(sVibratePattern, 0);

            }

            @Override
            public void onInterval(long remainTime) {
                notificationManager.notify(ID, new Notification.Builder(TimerService.this).setContentTitle("时间").setSmallIcon(R.mipmap.ic_launcher_alarmclock).setContentText(remainTime / 1000 + "秒").build());

            }
        });
        timer.start();
        return START_NOT_STICKY;
    }
}
