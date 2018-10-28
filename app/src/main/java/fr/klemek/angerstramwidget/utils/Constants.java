package fr.klemek.angerstramwidget.utils;

public final class Constants {

    private Constants() {

    }

    public static final String LOGGER_TAG = "AngersTramWidget";

    public static final String API_URL = "https://data.angers.fr/api/records/1.0/search/";

    public static final String STOP_PREFS_NAME = "fr.klemek.angerstramwidget.TramWidget.stop";
    public static final String DEST_PREFS_NAME = "fr.klemek.angerstramwidget.TramWidget.dest";
    public static final String DATA_PREFS_NAME = "fr.klemek.angerstramwidget.TramWidget.data";
    public static final String PREF_PREFIX_KEY = "appwidget_";

    public static final int INTERVAL_MILLIS = 60000; //1 minute

    public static final String WIDGET_CLICK_REQUEST = "widget_click_request";

    public static final String ACTION_TICK = "CLOCK_TICK";
    public static final String SETTINGS_CHANGED = "SETTINGS_CHANGED";
    public static final String JOB_TICK = "JOB_CLOCK_TICK";
}
