package uk.gov.companieshouse.documentsigningapi.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StructuredLoggingTest {
    static String COMPANY_NAME_KEY = "company_name_includes";
    static String COMPANY_NAME_VALUE = "Acme Inc";
    static String COMPANY_NUMBER_KEY = "company_number";
    static String COMPANY_NUMBER_VALUE = "777";
    static String UPSERT_COMPANY_NUMBER_KEY = "upsert_company_number";
    static String UPSERT_COMPANY_NUMBER_VALUE = "Upsert-777";
    static String COMPANY_TYPE_KEY = "company_type";
    static List<String> COMPANY_TYPE_VALUE = List.of("TYPE-1", "TYPE-2");
    static String COMPANY_STATUS_KEY = "company_status";
    static List<String> COMPANY_STATUS_VALUE = List.of("STATUS-1", "STATUS-2", "STATUS-3");
    static String LOCATION_KEY = "location";
    static String LOCATION_VALUE = "Cardiff";
    static String SIC_CODES_KEY = "sic_codes";
    static List<String> SIC_CODES_VALUE = List.of("SIC-1", "SIC-2");
    static String INCORPORATED_FROM_KEY = "incorporated_from";
    static String INCORPORATED_TO_KEY = "incorporated_to";
    static String DISSOLVED_FROM_KEY = "dissolved_from";
    static String DISSOLVED_TO_KEY = "dissolved_to";

    @Test
    @DisplayName("Company name and location")
    void locationTest() {
        Map<String, Object> logMap = new DataMap.Builder(COMPANY_NAME_VALUE)
            .startIndex(String.valueOf(1))
            .location(LOCATION_VALUE)
            .build()
            .getLogMap();

        assertTrue(logMap.containsKey(COMPANY_NAME_KEY));
        assertTrue(logMap.containsValue(COMPANY_NAME_VALUE));

        assertTrue(logMap.containsKey(LOCATION_KEY));
        assertTrue(logMap.containsValue(LOCATION_VALUE));
    }

    @Test
    @DisplayName("All Date methods incorporatedFrom() - incorporatedTo() - dissolvedFrom() - dissolvedTo()")
    void dateMethodsTest() throws ParseException {
        final String incorporatedFrom = "2025-13-32";
        final String incorporatedTo = "1970-01-01";
        final String dissolvedFrom = "1970-01-01";
        final String dissolvedTo = "2099-15-99";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date incorporatedFromDate = null;
        Date incorporatedToDate = null;
        Date dissolvedFromDate = null;
        Date dissolvedToDate = null;

        incorporatedFromDate = formatter.parse(incorporatedFrom);
        incorporatedToDate = formatter.parse(incorporatedTo);
        dissolvedFromDate = formatter.parse(dissolvedFrom);
        dissolvedToDate = formatter.parse(dissolvedTo);

        Map<String, Object> logMap = new DataMap.Builder(COMPANY_NAME_VALUE)
            .incorporatedFrom(incorporatedFromDate)
            .incorporatedTo(incorporatedToDate)
            .dissolvedFrom(dissolvedFromDate)
            .dissolvedTo(dissolvedToDate)
            .build()
            .getLogMap();

        assertTrue(logMap.containsKey(COMPANY_NAME_KEY));
        assertTrue(logMap.containsValue(COMPANY_NAME_VALUE));

        assertTrue(logMap.containsKey(INCORPORATED_FROM_KEY));
        assertEquals(incorporatedFromDate, logMap.get(INCORPORATED_FROM_KEY));

        assertTrue(logMap.containsKey(INCORPORATED_TO_KEY));
        assertEquals(incorporatedToDate, logMap.get(INCORPORATED_TO_KEY));

        assertTrue(logMap.containsKey(DISSOLVED_FROM_KEY));
        assertEquals(dissolvedFromDate, logMap.get(DISSOLVED_FROM_KEY));

        assertTrue(logMap.containsKey(DISSOLVED_TO_KEY));
        assertEquals(dissolvedToDate, logMap.get(DISSOLVED_TO_KEY));
    }

    @Test
    @DisplayName("List<String> for companyStatus - companyType - sicCodes")
    void testStringList() {
        Map<String, Object> logMap = new DataMap.Builder(COMPANY_NAME_VALUE)
            .companyStatus(COMPANY_STATUS_VALUE)
            .companyType(COMPANY_TYPE_VALUE)
            .sicCodes(SIC_CODES_VALUE)
            .build()
            .getLogMap();

        assertTrue(logMap.containsKey(COMPANY_NAME_KEY));
        assertTrue(logMap.containsValue(COMPANY_NAME_VALUE));

        assertTrue(logMap.containsKey(COMPANY_STATUS_KEY));
        assertEquals(COMPANY_STATUS_VALUE, logMap.get(COMPANY_STATUS_KEY));

        assertTrue(logMap.containsKey(COMPANY_TYPE_KEY));
        assertEquals(COMPANY_TYPE_VALUE, logMap.get(COMPANY_TYPE_KEY));

        assertTrue(logMap.containsKey(SIC_CODES_KEY));
        assertEquals(SIC_CODES_VALUE, logMap.get(SIC_CODES_KEY));
    }

    @Test
    @DisplayName("Merge 2 log maps with Map.putAll()")
    void testMergeLogMaps_PutAll() {
        DataMap mainDataMap = new DataMap.Builder(COMPANY_NAME_VALUE)
            .companyType(COMPANY_TYPE_VALUE)
            .build();

        DataMap subDataMap = new DataMap.Builder(COMPANY_NAME_VALUE)
            .companyStatus(COMPANY_STATUS_VALUE)
            .build();
        //
        // Merge.
        //
        Map<String, Object> mergeMap = mainDataMap.getLogMap();
        mergeMap.putAll(subDataMap.getLogMap());

        assertTrue(mergeMap.containsKey(COMPANY_NAME_KEY));
        assertTrue(mergeMap.containsValue(COMPANY_NAME_VALUE));

        assertTrue(mergeMap.containsKey(COMPANY_TYPE_KEY));
        assertEquals(COMPANY_TYPE_VALUE, mergeMap.get(COMPANY_TYPE_KEY));

        assertTrue(mergeMap.containsKey(COMPANY_STATUS_KEY));
        assertEquals(COMPANY_STATUS_VALUE, mergeMap.get(COMPANY_STATUS_KEY));
    }

    @Test
    @DisplayName("Merge 2 log maps with Map.putAll() with duplicate key companyType")
    void testMergeLogMaps_Duplicate_PutAll() {
        //
        // companyType only.
        //
        DataMap mainDataMap = new DataMap.Builder(COMPANY_NAME_VALUE)
            .companyType(COMPANY_TYPE_VALUE)
            .build();
        //
        // companyType AND companyStatus
        //
        DataMap subDataMap = new DataMap.Builder(COMPANY_NAME_VALUE)
            .companyStatus(COMPANY_STATUS_VALUE)
            .companyType(COMPANY_TYPE_VALUE)
            .build();
        //
        // Merge.
        //
        Map<String, Object> mergeMap = mainDataMap.getLogMap();
        mergeMap.putAll(subDataMap.getLogMap());

        assertTrue(mergeMap.containsKey(COMPANY_NAME_KEY));
        assertTrue(mergeMap.containsValue(COMPANY_NAME_VALUE));

        assertTrue(mergeMap.containsKey(COMPANY_TYPE_KEY));
        assertEquals(COMPANY_TYPE_VALUE, mergeMap.get(COMPANY_TYPE_KEY));

        assertTrue(mergeMap.containsKey(COMPANY_STATUS_KEY));
        assertEquals(COMPANY_STATUS_VALUE, mergeMap.get(COMPANY_STATUS_KEY));
    }

    @Test
    @DisplayName("Company NAME, company NUMBER and UPSERT company number")
    void testCompanyNumber_UpsertCompanyNumber() {
        DataMap dataMap = new DataMap.Builder(COMPANY_NAME_VALUE)
            .companyNumber(COMPANY_NUMBER_VALUE)
            .upsertCompanyNumber(UPSERT_COMPANY_NUMBER_VALUE)
            .build();

        assertTrue(dataMap.getLogMap().containsKey(COMPANY_NAME_KEY));
        assertTrue(dataMap.getLogMap().containsValue(COMPANY_NAME_VALUE));

        assertTrue(dataMap.getLogMap().containsKey(COMPANY_NUMBER_KEY));
        assertTrue(dataMap.getLogMap().containsValue(COMPANY_NUMBER_VALUE));

        assertTrue(dataMap.getLogMap().containsKey(UPSERT_COMPANY_NUMBER_KEY));
        assertTrue(dataMap.getLogMap().containsValue(UPSERT_COMPANY_NUMBER_VALUE));
    }
}
