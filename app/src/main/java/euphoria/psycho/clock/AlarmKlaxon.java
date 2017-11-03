package euphoria.psycho.clock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

public class AlarmKlaxon extends Service {
    private static final String TAG = "AlarmKlaxon";

    private static final int ALARM_TIMEOUT_SECONDS = 10 * 60;
    private static final int KILLER = 1000;
    private static final long[] sVibratePattern = new long[]{500, 500};
    private long mStartTime;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILLER:


                    stopSelf();
                    break;
            }
        }
    };
    private MediaPlayer mMediaPlayer;
    private boolean mPlaying = false;
    private Vibrator mVibrator;

    private void disableKiller() {
        mHandler.removeMessages(KILLER);
    }

    private void enableKiller() {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER),
                1000 * ALARM_TIMEOUT_SECONDS);
    }

    @Override
    public void onCreate() {

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        AlarmAlertWakeLock.acquireCpuWakeLock(this);
    }

    @Override
    public void onDestroy() {
        stop();
        // Stop listening for incoming calls.
        AlarmAlertWakeLock.releaseCpuLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void play() {
        // stop() checks to see if we are already playing.
        stop();


        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.stop();
                mp.release();
                mMediaPlayer = null;
                return true;
            }
        });


        try {
            // Must reset the media player to clear the error state.
            mMediaPlayer.reset();
            setDataSourceFromResource(getResources(), mMediaPlayer,
                    R.raw.win);
            startAlarm(mMediaPlayer);
        } catch (Exception ex2) {
            Log.e(TAG, ex2.getMessage());
        }


        mVibrator.vibrate(sVibratePattern, 0);


        enableKiller();
        mPlaying = true;
        mStartTime = System.currentTimeMillis();
    }

    private void startAlarm(MediaPlayer player)
            throws java.io.IOException, IllegalArgumentException,
            IllegalStateException {
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // do not play alarms if stream volume is 0
        // (typically because ringer mode is silent).
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            player.start();
        }
    }

    private void setDataSourceFromResource(Resources resources,
                                           MediaPlayer player, int res) throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No intent, tell the system not to restart us.
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        play();
        Log.e(TAG,"PLAY");
        return START_STICKY;
    }

    public void stop() {
        if (mPlaying) {
            mPlaying = false;

            // Stop audio playing
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            // Stop vibrator
            mVibrator.cancel();
        }
        disableKiller();
    }
}
