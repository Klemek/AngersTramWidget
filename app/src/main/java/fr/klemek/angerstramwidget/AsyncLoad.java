package fr.klemek.angerstramwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import fr.klemek.angerstramwidget.utils.APIManager;
import fr.klemek.angerstramwidget.utils.Data;

class AsyncLoad extends AsyncTask<Void, Void, Void> {

    private final Context ctx;
    private final AppWidgetManager appWidgetManager;
    private final int appWidgetId;
    private TimeList tl;
    private final boolean showError;
    private final boolean small;
    private final AsyncLoadListener listener;

    AsyncLoad(Context ctx, AppWidgetManager appWidgetManager, int appWidgetId, boolean showError, boolean small) {
        this.ctx = ctx;
        this.appWidgetManager = appWidgetManager;
        this.appWidgetId = appWidgetId;
        this.showError = showError;
        this.listener = null;
        this.small = small;
    }

    AsyncLoad(Context ctx, int appWidgetId, boolean small, AsyncLoadListener listener) {
        this.ctx = ctx;
        this.appWidgetManager = null;
        this.appWidgetId = appWidgetId;
        this.showError = false;
        this.listener = listener;
        this.small = small;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (appWidgetManager != null){
            TimeList tl = Data.getSavedDataPref(ctx, appWidgetId);
            if(tl == null || tl.size() == 0){
                RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.tram_widget);
                views.setTextViewText(R.id.time_text, ctx.getString(R.string.text_loading));
                views.setTextViewText(R.id.info_text, ctx.getString(R.string.text_loading_2));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.tram_widget);
        if (tl == null) {
            if (listener != null)
                listener.onFinished(false, null);
            if (showError && appWidgetManager != null) {
                views.setTextViewText(R.id.time_text, ctx.getString(R.string.time_text_error));
                views.setTextViewText(R.id.info_text, ctx.getString(R.string.info_text_network_error));
            }
        } else {
            Data.saveDataPref(ctx, appWidgetId, tl);
            if (listener != null)
                listener.onFinished(true, tl);
            if (appWidgetManager != null) {
                TramWidget.updateTimers(ctx, views, tl, small);
            }
        }
        if (appWidgetManager != null){
            TramWidget.updateWidgetLook(ctx, views, appWidgetId, small,
                    appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String stopCode = Data.loadStopPref(ctx, appWidgetId);
        boolean dest = Data.loadDestPref(ctx, appWidgetId);
        tl = APIManager.loadList(stopCode, dest);
        return null;
    }

    public interface AsyncLoadListener {
        void onFinished(boolean result, TimeList tl);
    }
}
