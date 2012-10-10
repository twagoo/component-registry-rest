/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author olhsha
 */
public class DatesHelperTest {

    /**
     * Test of parseWorks method, of class DatesHelper.
     */
    @Test
    public void testParseWorks() throws ParseException {

        assertEquals(null, DatesHelper.parseWorks("Wrong date"));

        final String testDate = "2012-09-17T13:40:57+00:00";
        Date result = DatesHelper.parseWorks(testDate);
        assertFalse(null == result);
        Date expectedResult = DateUtils.parseDate(testDate, new String[]{DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()});
        assertTrue(expectedResult.equals(result));
    }

    /**
     * Test of compareDateStrings method, of class DatesHelper.
     */
    @Test
    public void testCompareDateStrings() {
        String date1 = "2012-09-17T13:40:57+00:00";
        String date2 = "2012-09-17T13:40:58+00:00";

        int result1 = DatesHelper.compareDateStrings(date1, date2);
        assertEquals(1, result1);
        int result2 = DatesHelper.compareDateStrings(date2, date1);
        assertEquals(-1, result2);

    }

    /**
     * Test of getRFCDateTime method, of class DatesHelper.
     */
    @Test
    public void testGetRFCDateTime() {
        String dateString = "2012-09-17T13:40:57+00:00";
        String expResult = "Mon, 17 Sep 2012 15:40:57 +0200";
        String result = DatesHelper.getRFCDateTime(dateString);
        assertEquals(expResult, result);
    }
}
