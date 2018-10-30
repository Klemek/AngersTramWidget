package fr.klemek.angerstramwidget;

import android.util.Log;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.klemek.angerstramwidget.utils.Constants;
import fr.klemek.angerstramwidget.utils.Utils;

public class TimeList {

    private List<LocalDateTime> list;
    private LocalDateTime lastUpdateTime;
    private static ZoneId zoneId = ZoneId.of("Europe/Paris");

    public TimeList() {
        list = new ArrayList<>();
        lastUpdateTime = LocalDateTime.now(zoneId);
    }

    public TimeList(String toString) {
        this();
        for (String localDateTime : toString.split(";")) {
            if (localDateTime.length() > 0) {
                list.add(LocalDateTime.parse(localDateTime));
            }
        }
        lastUpdateTime = list.get(list.size() - 1);
        list = list.subList(0, list.size() - 1);
    }

    public boolean shouldReload() {
        return getAge() < 0 || getAge() > Constants.MAX_TIMELIST_AGE;
    }

    public void addOffsetDateTime(String strTime) {
        //Log.d(Constants.LOGGER_TAG, ""+strTime);
        LocalDateTime newDate = OffsetDateTime.parse(strTime).atZoneSameInstant(zoneId).plusMinutes(1).toLocalDateTime();
        for (LocalDateTime ldt : list)
            if (ChronoUnit.MINUTES.between(ldt, newDate) == 0)
                return;
        list.add(newDate);
        lastUpdateTime = LocalDateTime.now(zoneId);
        Log.d(Constants.LOGGER_TAG, "Added new time : " + newDate);
    }

    public void sort() {
        this.refresh();
        Collections.sort(list);
    }

    public void refresh() {
        List<LocalDateTime> tmp = new ArrayList<>();
        for (LocalDateTime ldt : list) {
            try {
                if (ldt.isAfter(LocalDateTime.now(zoneId)))
                    tmp.add(ldt);
                else
                    Log.d(Constants.LOGGER_TAG, "Removed old time : " + ldt);
            } catch (Exception e) {
                tmp.add(ldt);
            }
        }
        list = tmp;
    }

    public int size() {
        return list.size();
    }

    public List<LocalDateTime> getList() {
        return list;
    }

    public TimeList clone() {
        return new TimeList(this.toString());
    }

    public long getAge() {
        return ChronoUnit.MINUTES.between(lastUpdateTime, LocalDateTime.now(zoneId));
    }

    public ArrayList<String> toStringList() {
        ArrayList<String> slist = new ArrayList<>();
        for (LocalDateTime ldt : list)
            slist.add(String.format("%s (%s)",
                    Utils.getTimespanText(ldt, "Dans", false),
                    ldt.format(DateTimeFormatter.ofPattern("HH:mm"))));
        return slist;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LocalDateTime ldt : list) {
            sb.append(ldt);
            sb.append(';');
        }
        sb.append(lastUpdateTime);
        return sb.toString();
    }
}
