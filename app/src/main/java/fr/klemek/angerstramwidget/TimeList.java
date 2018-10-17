package fr.klemek.angerstramwidget;

import android.util.Log;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.klemek.angerstramwidget.utils.Constants;
import fr.klemek.angerstramwidget.utils.Utils;

public class TimeList {

    private List<LocalDateTime> list;
    private LocalDateTime creationTime;
    private static ZoneId zoneId = ZoneId.of("Europe/Paris");

    public TimeList() {
        list = new ArrayList<>();
        creationTime = LocalDateTime.now(zoneId);
    }

    public TimeList(String toString) {
        this();
        for (String localDateTime : toString.split(";")) {
            if (localDateTime.length() > 0) {
                list.add(LocalDateTime.parse(localDateTime));
            }
        }
        creationTime = list.get(list.size() - 1);
        list = list.subList(0, list.size() - 1);
    }

    public void addOffsetDateTime(String strTime) {
        //Log.d(Constants.LOGGER_TAG, ""+strTime);
        list.add(OffsetDateTime.parse(strTime).atZoneSameInstant(zoneId).plusMinutes(1).toLocalDateTime());
        //Log.d(Constants.LOGGER_TAG, ">"+list.get(list.size()-1));
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

    public long getAge() {
        return ChronoUnit.MINUTES.between(creationTime, LocalDateTime.now(zoneId));
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
        sb.append(creationTime);
        return sb.toString();
    }
}
