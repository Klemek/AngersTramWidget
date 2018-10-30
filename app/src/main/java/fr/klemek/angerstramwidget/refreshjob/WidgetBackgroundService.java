package fr.klemek.angerstramwidget.refreshjob;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import fr.klemek.angerstramwidget.utils.Constants;

public class WidgetBackgroundService extends Service {

    private static BroadcastReceiver mMinuteTickReceiver;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra("SHUTDOWN")) {
                if (intent.getBooleanExtra("SHUTDOWN", false)) {

                    if (mMinuteTickReceiver != null) {
                        unregisterReceiver(mMinuteTickReceiver);
                        mMinuteTickReceiver = null;
                    }
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }
        }

        if (mMinuteTickReceiver == null) {
            registerOnTickReceiver();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_ID = "fr.klemek.angerstramwidget";
            NotificationChannel notificationChannel = new NotificationChannel("fr.klemek.angerstramwidget",
                    CHANNEL_ID, NotificationManager.IMPORTANCE_NONE);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setSound(null, null);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
                startForeground(1, new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build());
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mMinuteTickReceiver != null) {
            unregisterReceiver(mMinuteTickReceiver);
            mMinuteTickReceiver = null;
        }

        super.onDestroy();
    }

    private void registerOnTickReceiver() {
        mMinuteTickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent timeTick = new Intent(Constants.ACTION_TICK);
                sendBroadcast(timeTick);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mMinuteTickReceiver, filter);
    }
}
