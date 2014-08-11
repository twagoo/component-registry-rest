package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.ComponentRegistryException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    protected final boolean isPrivate;
    private List<T> objs;
    private int limit;

    protected RssCreator(boolean isPrivate, String baseURI, int limit, List<T> objs) {
        this.isPrivate = isPrivate;
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
     * @param <T> type of the object for which an rss item will be created
     * (either profile/component description, or a comment)
     * @param limit number of items to include in the RSS-channel
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

    protected String getChannelLink() {
        return channelLink;
    }

    protected String getBaseURI() {
        return baseURI;
    }
}
