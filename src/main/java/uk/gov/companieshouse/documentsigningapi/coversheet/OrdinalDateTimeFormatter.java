package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Produces date time strings in the format "Monday 20th March 2023 08:36 GMT".
 */
@Component
public class OrdinalDateTimeFormatter {

    private static final String DAY_DATE_MONTH_YEAR_TIME_FORMAT = "EEEE d MMMM yyyy HH:mm z";

    public String getDateTimeString(final Date date) {
        final var format = new SimpleDateFormat(DAY_DATE_MONTH_YEAR_TIME_FORMAT);
        final String withoutOrdinal = format.format(date);
        final String[] parts = withoutOrdinal.split(" ");
        final String day = parts[1];
        final var dayOfMonth = Integer.parseInt(day);
        final String dayOfMonthOrdinal = day + getDayOfMonthSuffix(dayOfMonth);
        parts[1] = dayOfMonthOrdinal;
        return String.join(" ", parts);
    }

    private String getDayOfMonthSuffix(final int dayOfMonth) {
        if (dayOfMonth >= 11 && dayOfMonth <= 13) {
            return "th";
        }
        switch (dayOfMonth % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}
