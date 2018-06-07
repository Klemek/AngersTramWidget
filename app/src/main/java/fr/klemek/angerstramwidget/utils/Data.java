package fr.klemek.angerstramwidget.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.klemek.angerstramwidget.R;
import fr.klemek.angerstramwidget.TimeList;

public final class Data {

    private Data(){

    }

    //region STOPS

    private static final BiMap<String,String> stops = HashBiMap.create();
    private static final List<String> stopsOrder = new ArrayList<>();

    private static void loadStops(Context ctx){
        try(InputStream in_s = ctx.getResources().openRawResource(R.raw.stops)){
            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            for(String stop : new String(b, "utf-8").split("\n")){
                String[] spl = stop.split(";");
                if(spl.length >= 2){
                    stops.put(spl[0],spl[1]);
                    stopsOrder.add(spl[1]);
                }
            }
            Log.i(Constants.LOGGER_TAG, "Read "+stops.size()+" stops from file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getStops(Context ctx){
        if(stopsOrder.isEmpty())
            loadStops(ctx);
        return stopsOrder;
    }

    public static String getStopCode(Context ctx, String stopName){
        if(stopsOrder.isEmpty())
            loadStops(ctx);
        return stops.inverse().get(stopName);
    }

    public static String getStopName(Context ctx, String stopCode){
        if(stopsOrder.isEmpty())
            loadStops(ctx);
        return stops.get(stopCode);
    }

    public static String getDestination(Context ctx,boolean forward){
        if(stopsOrder.isEmpty())
            loadStops(ctx);
        if(forward)
            return stopsOrder.get(stopsOrder.size()-1);
        return stopsOrder.get(0);
    }

    //endregion

    //region PREF

    public static boolean isRegisteredPref(Context context, int appWidgetId){
        SharedPreferences prefs = context.getSharedPreferences(Constants.STOP_PREFS_NAME, 0);
        String stopValue = prefs.getString(Constants.PREF_PREFIX_KEY + appWidgetId, null);
        return stopValue != null;
    }

    // Write the prefix to the SharedPreferences object for this widget
    public static void saveStopPref(Context context, int appWidgetId, String stop) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(Constants.STOP_PREFS_NAME, 0).edit();
        prefs.putString(Constants.PREF_PREFIX_KEY + appWidgetId, stop);
        prefs.apply();
    }

    public static void saveDestPref(Context context, int appWidgetId, boolean dest) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(Constants.DEST_PREFS_NAME, 0).edit();
        prefs.putBoolean(Constants.PREF_PREFIX_KEY + appWidgetId, dest);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static String loadStopPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.STOP_PREFS_NAME, 0);
        String stopValue = prefs.getString(Constants.PREF_PREFIX_KEY + appWidgetId, null);
        if (stopValue != null) {
            return stopValue;
        } else {
            return Data.getStopCode(context, Data.getStops(context).get(0));
        }
    }

    public static boolean loadDestPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.DEST_PREFS_NAME, 0);
        return prefs.getBoolean(Constants.PREF_PREFIX_KEY + appWidgetId, true);
    }

    public static void deletePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(Constants.STOP_PREFS_NAME, 0).edit();
        prefs.remove(Constants.PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
        prefs = context.getSharedPreferences(Constants.DEST_PREFS_NAME, 0).edit();
        prefs.remove(Constants.PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
        prefs = context.getSharedPreferences(Constants.DATA_PREFS_NAME, 0).edit();
        prefs.remove(Constants.PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    public static TimeList getSavedDataPref(Context ctx, int appWidgetId) {
        SharedPreferences prefs = ctx.getSharedPreferences(Constants.DATA_PREFS_NAME, 0);
        String timeListValue = prefs.getString(Constants.PREF_PREFIX_KEY + appWidgetId, null);
        if (timeListValue != null) {
            return new TimeList(timeListValue);
        } else {
            return null;
        }
    }

    public static void saveDataPref(Context ctx, int appWidgetId, TimeList tl) {
        SharedPreferences.Editor prefs = ctx.getSharedPreferences(Constants.DATA_PREFS_NAME, 0).edit();
        prefs.putString(Constants.PREF_PREFIX_KEY + appWidgetId, tl.toString());
        prefs.apply();
    }

    //endregion

}
