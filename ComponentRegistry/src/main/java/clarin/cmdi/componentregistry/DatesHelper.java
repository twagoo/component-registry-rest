/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.model.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;

/**
 *
 * @author olhsha
 */
public class DatesHelper {

    /**
     *
     * @param dateString the value of dateString
     */
    public static Date parseWorks(String dateString) {
        try {
            return ComponentUtils.getDate(dateString);
        } catch (ParseException pe) {
            return null;
        }
    }

    /**
     *
     * @param date1 the value of date1
     * @param date2 the value of date2
     * @return -1 if d1 is younger than d2, 1 if d1 is older then d2
     */
    public static int compareDateStrings(String date1, String date2) {
        final Date d1 = parseWorks(date1);
        final Date d2 = parseWorks(date2);
        if (d1 == null) {
            if (d2 == null) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (d2 == null) {
                return -1;
            } else {
                return d2.compareTo(d1);
            }
        }
    }
    
    private static SimpleDateFormat getRFC822DATEFORMAT() {
        SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z");
        return RFC822DATEFORMAT;
    }

    /**
     *
     * @param dateString the value of dateString
     */
    public static String getRFCDateTime(String dateString) {
        final Date date = parseWorks(dateString);
        if (date == null) {
            return dateString;
        } else {
            SimpleDateFormat RFC822DATEFORMAT = getRFC822DATEFORMAT();
            return RFC822DATEFORMAT.format(date);
        }
    }


    /**
    *
    * @param dateString the value of dateString
    */
   public static String getRFCDateTime(Date date) {
     String s = DateFormatUtils.format(date,DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
     return getRFCDateTime(s);
   }
   
   public static Date parseRFCDateTime(String dateTime) {
       SimpleDateFormat RFC822DATEFORMAT = getRFC822DATEFORMAT();
       Date date;
    try {
	date = RFC822DATEFORMAT.parse(dateTime);
	       return date;
    } catch (ParseException e) {
	return null;
    }
   }
   
   public static String createNewDate() {
       return createNewDate(new Date().getTime());
   }

   public static String createNewDate(long time) {
       return DateFormatUtils.formatUTC(time, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
   }

}
