package fr.klemek.angerstramwidget;

import android.app.Application;
import android.util.Log;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import fr.klemek.angerstramwidget.utils.Constants;

public class GlobalState extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
