package fr.klemek.angerstramwidget.utils;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Locale;

public final class Utils {

    private Utils() {

    }

    public static String getTimespanText(LocalDateTime ldt, String prefix, boolean small) {
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), ldt);
        if (minutes < 1)
            return "Maintenant";
        if (minutes == 1)
            return small ? "1 min." : (prefix + " 1 minute");
        if (minutes < 60)
            return String.format(Locale.FRENCH, small ? "%d min." : (prefix + " %d minutes"), minutes);
        long hours = minutes / 60;
        if (hours == 1)
            return small ? "1 h." : (prefix + " 1 heure");
        if (hours < 24)
            return String.format(Locale.FRENCH, small ? "%d h." : (prefix + " %d heures"), hours);
        return "Demain";
    }

}
