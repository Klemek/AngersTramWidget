package fr.klemek.angerstramwidget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.threeten.bp.LocalDateTime;

import fr.klemek.angerstramwidget.refreshjob.AppWidgetAlarm;
import fr.klemek.angerstramwidget.refreshjob.RepeatingJob;
import fr.klemek.angerstramwidget.refreshjob.WidgetBackgroundService;
import fr.klemek.angerstramwidget.utils.Constants;
import fr.klemek.angerstramwidget.utils.Data;
import fr.klemek.angerstramwidget.utils.Utils;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TramWidgetConfigureActivity TramWidgetConfigureActivity}
 */
public abstract class TramWidget extends AppWidgetProvider {

    private boolean small;

    public TramWidget(boolean small){
        this.small = small;
    }

    static void updateAppWidget(Context ctx, AppWidgetManager appWidgetManager,
                                int appWidgetId, boolean small) {
        if (!Data.isRegisteredPref(ctx, appWidgetId))
            return;

        Log.d(Constants.LOGGER_TAG, "Treating widget "+appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.tram_widget);

        updateWidgetLook(ctx, views, appWidgetId, small,
                appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight);

        //Log.d(Constants.LOGGER_TAG, "height : " + height + " density : " + ctx.getResources().getDisplayMetrics().density);

        TimeList tl = Data.getSavedDataPref(ctx, appWidgetId);
        if (tl != null) {
            tl.refresh();

            Log.d(Constants.LOGGER_TAG,appWidgetId+" TimeList age : "+tl.getAge()+" size : "+tl.size());

            if(tl.size() > 0){
                Log.d(Constants.LOGGER_TAG, appWidgetId+" widget : "+Utils.getTimespanText(tl.getList().get(0), "", false));
            }

            updateTimers(ctx, views, tl, small);
        } else {
            Log.d(Constants.LOGGER_TAG,appWidgetId+" TimeList null");
        }

        if (tl == null || tl.shouldReload())
            new AsyncLoad(ctx, appWidgetManager, appWidgetId, tl == null || tl.size() < 2, small).execute();

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);


        Log.d(Constants.LOGGER_TAG, "Updated widget "+appWidgetId);
    }

    static void updateTimers(Context ctx, RemoteViews views, TimeList tl, boolean small){
        if (tl.size() == 0) {
            views.setTextViewText(R.id.time_text, ctx.getString(R.string.time_text_error));
            views.setTextViewText(R.id.info_text, ctx.getString(small ? R.string.info_text_no_data_2 : R.string.info_text_no_data));
        } else {
            LocalDateTime ldt1 = tl.getList().get(0);
            views.setTextViewText(R.id.time_text, Utils.getTimespanText(ldt1, "Dans", small));
            if (tl.size() >= 2) {
                LocalDateTime ldt2 = tl.getList().get(1);
                views.setTextViewText(R.id.info_text, Utils.getTimespanText(ldt2, "Puis dans", small));
            } else {
                views.setTextViewText(R.id.info_text, ctx.getString(small ? R.string.info_text_end_data_2 : R.string.info_text_end_data));
            }
        }
    }

    static void updateWidgetLook(Context ctx, RemoteViews views, int appWidgetId, boolean small, float height){
        String stopName = Data.getStopName(ctx, Data.loadStopPref(ctx, appWidgetId));
        String destName = Data.getDestination(ctx, Data.loadDestPref(ctx, appWidgetId));

        views.setTextViewText(R.id.stop_text, ctx.getString(small ? R.string.stop_text_template_2 : R.string.stop_text_template, stopName.toUpperCase()));
        views.setTextViewText(R.id.dest_text, ctx.getString(small ? R.string.dest_text_template_2 : R.string.dest_text_template, destName.toUpperCase()));

        float factor = height / 110;
        if(factor > 1){
            views.setTextViewTextSize(R.id.time_text, TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.primary_text_size) * factor);
            views.setTextViewTextSize(R.id.info_text, TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.secondary_text_size) * factor);
            views.setTextViewTextSize(R.id.stop_text, TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.tertiary_text_size) * factor);
            views.setTextViewTextSize(R.id.dest_text, TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.tertiary_text_size) * factor);
        }

        views.setTextColor(R.id.time_text, ContextCompat.getColor(ctx, R.color.colorTextDefault));
        views.setTextColor(R.id.info_text, ContextCompat.getColor(ctx, R.color.colorTextDefault));

        Intent configIntent = new Intent(ctx, small ? TramWidgetConfigureActivitySmall.class : TramWidgetConfigureActivityMedium.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        configIntent.putExtra(Constants.WIDGET_CLICK_REQUEST, true);
        configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent configPendingIntent = PendingIntent.getActivity(ctx, appWidgetId, configIntent, 0);
        views.setOnClickPendingIntent(R.id.widget, configPendingIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context.getApplicationContext(), appWidgetManager, appWidgetId, small);
        Log.d(Constants.LOGGER_TAG, this.getClass().getSimpleName()+" Updated "+appWidgetIds.length+" widgets");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            Data.deletePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        restartAll(context);
        Toast.makeText(context, R.string.warn_user, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancelAll();
        } else {
            // stop alarm
            AppWidgetAlarm appWidgetAlarm = new AppWidgetAlarm(context.getApplicationContext());
            appWidgetAlarm.stopAlarm();
        }

        Intent serviceBG = new Intent(context.getApplicationContext(), WidgetBackgroundService.class);
        serviceBG.putExtra("SHUTDOWN", true);
        context.getApplicationContext().startService(serviceBG);
        context.getApplicationContext().stopService(serviceBG);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), this.getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        if (intent.getAction().equals(Constants.SETTINGS_CHANGED)) {
            onUpdate(context, appWidgetManager, appWidgetIds);
            if (appWidgetIds.length > 0) {
                restartAll(context);
            }
        }

        if (intent.getAction().equals(Constants.JOB_TICK) || intent.getAction().equals(Constants.ACTION_TICK) ||
                intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                || intent.getAction().equals(Intent.ACTION_DATE_CHANGED)
                || intent.getAction().equals(Intent.ACTION_TIME_CHANGED)
                || intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            restartAll(context);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    private void restartAll(Context context) {
        Intent serviceBG = new Intent(context.getApplicationContext(), WidgetBackgroundService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getApplicationContext().startForegroundService(serviceBG);
        } else {
            context.getApplicationContext().startService(serviceBG);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scheduleJob(context);
        } else {
            AppWidgetAlarm appWidgetAlarm = new AppWidgetAlarm(context.getApplicationContext());
            appWidgetAlarm.startAlarm();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context.getPackageName(), RepeatingJob.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setPersisted(true);
        builder.setPeriodic(600000);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int jobResult = jobScheduler.schedule(builder.build());
    }

}

