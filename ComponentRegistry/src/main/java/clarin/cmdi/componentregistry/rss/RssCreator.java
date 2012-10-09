package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 *
 * @author olhsha
 */
public abstract class RssCreator<T> {

    public static final double RSS_VERSION = 2.0;
    private final String baseURI;
    private String channelLink;
    private String channelDescription;
    private String channelTitle;
    private Comparator<T> comparator;
    protected final boolean userspace;
    private List<T> objs;
    private int limit;

    public RssCreator(boolean userspace, String baseURI, int limit, List<T> objs) {
        this.userspace = userspace;
        this.baseURI = baseURI;
        this.limit = limit;
        this.objs = objs;
    }

    protected abstract RssItem fromArgToRssItem(T obj);

    /**
     * Makes a list of items out a list of descriptions, return the pointer to
     * the list of items
     *
     * @param descriptions
     * @return items out a list of descriptions
     * @throws ParseException
     */
    private List<RssItem> makeListOfRssItems(List<T> objSublist) throws ParseException {
        List<RssItem> listOfItems = new ArrayList<RssItem>();
        for (T currentObj : objSublist) {
            RssItem currentItem = fromArgToRssItem(currentObj);
            listOfItems.add(currentItem);
        }
        return listOfItems;
    }

    /**
     *
     * @param channelLink
     * @param channelDescription
     * @param channelTitle
     * @param obj
     * @return
     * @throws ParseException
     */
    private Rss makeRssChannel(List<T> objSublist) throws ParseException {
        final Rss rss = new Rss();
        rss.setVersion(RSS_VERSION);
        final RssChannel channel = new RssChannel();
        channel.setDescription(channelDescription);
        channel.setLink(channelLink);
        channel.setTitle(channelTitle);
        channel.getItem().addAll(makeListOfRssItems(objSublist));
        rss.setChannel(channel);
        return rss;
    }

    /**
     * Generates RSS feeds for profile and component descriptions
     *
     * @param <T> type of description
     * @param limit number of items to include in the RSS
     * @param descriptions descriptions to include
     * @param channelDescription channel description
     * @param channelTitle channel title
     * @param channelLink channel link
     * @return
     * @throws ComponentRegistryException
     * @throws ParseException
     */
    public Rss getRss() throws ParseException {
        Collections.sort(objs, comparator);
        final int length = (objs.size() < limit) ? objs.size() : limit;
        List<T> sublist = objs.subList(0, length);
        return (makeRssChannel(sublist));
    }

    /* Helping stuff
     * 
     */
    protected Guid makeGuid(String href) {
        Guid result = new Guid();
        // result.setIsPermaLink(null);
        result.setValue(href);
        return result;
    }

    /* Helping stuff: for working with dates
     * 
     */
    private static Date parseWorks(String dateString) {
        try {
            return AbstractDescription.getDate(dateString);
        } catch (ParseException pe) {
            return null;
        }
    }

    protected String getRFCDateTime(String dateString) {
        final Date date = parseWorks(dateString);

        if (date == null) {
            return dateString;
        } else {
            SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z");
            return RFC822DATEFORMAT.format(date);
        }
    }

    /**
     * Compares two date strings
     *
     * @param date1
     * @param date2
     * @return 1 if date1 is older (before) than date2, returns -1 if date1 is
     * younger (after) than date2, 0 if they are the same
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
                // compareTo returns:
                // a value less than 0 if d2 is before d1 (d1 is younger than d2);
                // a value greater than 0 if d2 is after  d1 (d1 is older than d2)
                return d2.compareTo(d1);
            }
        }
    }

    /**
     * @param channelLink the channelLink to set
     */
    protected void setChannelLink(String channelLink) {
        this.channelLink = channelLink;
    }

    /**
     * @param channelDescription the channelDescription to set
     */
    protected void setChannelDescription(String channelDescription) {
        this.channelDescription = channelDescription;
    }

    /**
     * @param channelTitle the channelTitle to set
     */
    protected void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    /**
     * @param comparator the comparator to set
     */
    protected void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }
    
    protected String getChannelLink(){
        return channelLink;
    }
    
    protected String getBaseURI(){
        return baseURI;
    }
}
