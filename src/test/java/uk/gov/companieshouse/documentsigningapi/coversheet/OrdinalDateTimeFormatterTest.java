package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OrdinalDateTimeFormatterTest {

    private static final String[] DATES_IN_MARCH = new String[] {
            "1st",  "2nd",  "3rd",  "4th",  "5th",  "6th",  "7th",  "8th",  "9th", "10th",
            "11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th", "19th", "20th",
            "21st", "22nd", "23rd", "24th", "25th", "26th", "27th", "28th", "29th", "30th",
            "31st" };

    private OrdinalDateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new OrdinalDateTimeFormatter();
    }

    @Test
    @DisplayName("Produces date time strings with ordinal days of month")
    void producesDateTimeStringsWithOrdinalDaysOfMonth() {

        final Map<String, String> dates = new HashMap<>();
        Arrays.stream(DATES_IN_MARCH).forEach(date -> dates.put(date, date));

        for (int dayOfMonth = 1; dayOfMonth <= 31; dayOfMonth++) {
            final Calendar calendar = Calendar.getInstance();
            calendar.set(2023, Calendar.MARCH, dayOfMonth);
            final Date date = calendar.getTime();
            final String formattedDate = formatter.getDateTimeString(date);

            final String[] parts = formattedDate.split(" ");
            final String day = parts[1];
            assertThat(dates.containsKey(day), is(true));
        }

    }

}