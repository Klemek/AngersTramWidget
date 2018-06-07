package fr.klemek.angerstramwidget.refreshjob;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import fr.klemek.angerstramwidget.utils.Constants;

public class AppWidgetAlarm {

    private final int ALARM_ID = 0;

    private final Context mContext;


    public AppWidgetAlarm(Context context){
        mContext = context;
    }


    public void startAlarm() {
        Log.d(Constants.LOGGER_TAG, "StartAlarm");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, Constants.INTERVAL_MILLIS);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(Constants.ACTION_TICK);
        PendingIntent removedIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(removedIntent);
        // needs RTC_WAKEUP to wake the device
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), Constants.INTERVAL_MILLIS, pendingIntent);
    }

    public void stopAlarm()
    {
        Log.d(Constants.LOGGER_TAG, "StopAlarm");
        Intent alarmIntent = new Intent(Constants.ACTION_TICK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
