package clarin.cmdi.componentregistry;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import clarin.cmdi.componentregistry.util.XmlDateAdapter;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
public class TestXmlDateAdapter {

    XmlDateAdapter adapter;
    GregorianCalendar calendar;

    @Before
    public void setup() {
	adapter = new XmlDateAdapter();
	calendar = new GregorianCalendar();
	calendar.set(GregorianCalendar.YEAR, 2013);
	calendar.set(GregorianCalendar.MONTH, 10 - 1); // month is 0-based :(
	calendar.set(GregorianCalendar.DAY_OF_MONTH, 18);
	calendar.set(GregorianCalendar.HOUR, 8);
	calendar.set(GregorianCalendar.MINUTE, 2);
	calendar.set(GregorianCalendar.SECOND, 37);
	calendar.set(GregorianCalendar.MILLISECOND, 0);
	calendar.set(GregorianCalendar.AM_PM, GregorianCalendar.AM);
	calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Test
    public void testFormat1() throws Exception {
	Date date = calendar.getTime();
	String s = adapter.marshal(date);
	assertEquals("2013-10-18T08:02:37+00:00", s);
    }

    @Test
    public void testFormat2() throws Exception {
	calendar.setTimeZone(TimeZone.getTimeZone("CET"));
	Date date = calendar.getTime();
	String s = adapter.marshal(date);
	assertEquals("2013-10-18T06:02:37+00:00", s);
    }

    @Test
    public void testParse1() throws Exception {
	Date date = adapter.unmarshal("2013-10-18T08:02:37+00:00");
	assertEquals(calendar.getTime(), date);
    }

    @Test
    public void testParse2() throws Exception {
	Date date = adapter.unmarshal("2013-10-18T08:02:37+02:00");
	Calendar copy = (Calendar)calendar.clone();
	copy.set(Calendar.HOUR, 6);
	copy.setTimeZone(TimeZone.getTimeZone("CEST"));
	assertEquals(copy.getTime(), date);
    }

}
