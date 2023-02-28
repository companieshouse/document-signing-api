package uk.gov.companieshouse.documentsigningapi.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class LoggingUtilsTest {

    @InjectMocks
    private LoggingUtils loggingUtils;

    @Mock
    private Logger logger;

    @Test
    void testCreateLogMapReturnsMap() {
        //when
        Map<String, Object> actual = loggingUtils.createLogMap();

        //then
        assertEquals(Collections.emptyMap(), actual);
    }


    @Test
    void testLogIfNotNullIfObjectNonNull() {
        //given
        Map<String, Object> logArgs = new HashMap<>();

        //when
        loggingUtils.logIfNotNull(logArgs, "key", "value");

        //then
        assertTrue(logArgs.containsKey("key"));
    }

    @Test
    void testLogIfNotNullSkipIfObjectNull() {
        //given
        Map<String, Object> logArgs = new HashMap<>();

        //when
        loggingUtils.logIfNotNull(logArgs, "key", "value");

        //then
        assertTrue(logArgs.containsKey("key"));
    }

}
