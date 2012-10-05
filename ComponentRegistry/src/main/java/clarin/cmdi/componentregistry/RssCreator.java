package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.rss.Category;
import clarin.cmdi.componentregistry.rss.Cloud;
import clarin.cmdi.componentregistry.rss.Guid;
import clarin.cmdi.componentregistry.rss.Image;
import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssChannel;
import clarin.cmdi.componentregistry.rss.RssItem;
import clarin.cmdi.componentregistry.rss.SkipDaysList;
import clarin.cmdi.componentregistry.rss.SkipHoursList;
import clarin.cmdi.componentregistry.rss.TextInput;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author olhsha
 */
public abstract class RssCreator<T> { // extends nothing so far, throuw nothing // make it abstract

    private String title;
    private String link;
    private boolean userspace;
    private String description;
    private String language;
    private String copyright;
    private String managingEditor;
    private String webMaster;
    private String pubDate;
    private String lastBuildDate;
    private Category category;
    private String generator;
    private String docs;
    private Cloud cloud;
    private BigInteger ttl;
    private Image image;
    private String rating;
    private TextInput textInput;
    private SkipHoursList skipHours;
    private SkipDaysList skipDays;
    private double version = 2.0; // of an rss

    public double getVersion() {
	return (version);
    }

    /**
     *
     * @param version
     */
    public void setVersion(double version) {
	this.version = version;
    }

    /**
     * Sets the value of the title property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setTitle(String value) {
	this.title = value;
    }

    public String getTitle() {
	return this.title;
    }

    /**
     * Sets the value of the link property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setLink(String value) {
	this.link = value;
    }

    public String getLink() {
	return this.link;
    }
    
    public boolean getUserspace(){
        return userspace;
    }
    public void setUserspace(boolean userspace){
        this.userspace = userspace;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setDescription(String value) {
	this.description = value;
    }

    /**
     * Sets the value of the language property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setLanguage(String value) {
	this.language = value;
    }

    /**
     * Sets the value of the copyright property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setCopyright(String value) {
	this.copyright = value;
    }

    /**
     * Sets the value of the managingEditor property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setManagingEditor(String value) {
	this.managingEditor = value;
    }

    /**
     * Sets the value of the webMaster property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setWebMaster(String value) {
	this.webMaster = value;
    }

    /**
     * Sets the value of the pubDate property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setPubDate(String value) {
	this.pubDate = value;
    }

    /**
     * Sets the value of the lastBuildDate property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setLastBuildDate(String value) {
	this.lastBuildDate = value;
    }

    /**
     * Sets the value of the category property.
     *
     * @param value allowed object is {@link Category }
     *
     */
    public void setCategory(Category value) {
	this.category = value;
    }

    /**
     * Sets the value of the generator property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setGenerator(String value) {
	this.generator = value;
    }

    /**
     * Sets the value of the docs property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setDocs(String value) {
	this.docs = value;
    }

    /**
     * Sets the value of the cloud property.
     *
     * @param value allowed object is {@link Cloud }
     *
     */
    public void setCloud(Cloud value) {
	this.cloud = value;
    }

    /**
     * Sets the value of the ttl property.
     *
     * @param value allowed object is {@link BigInteger }
     *
     */
    public void setTtl(BigInteger value) {
	this.ttl = value;
    }

    /**
     * Sets the value of the image property.
     *
     * @param value allowed object is {@link Image }
     *
     */
    public void setImage(Image value) {
	this.image = value;
    }

    /**
     * Sets the value of the rating property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setRating(String value) {
	this.rating = value;
    }

    /**
     * Sets the value of the textInput property.
     *
     * @param value allowed object is {@link TextInput }
     *
     */
    public void setTextInput(TextInput value) {
	this.textInput = value;
    }

    /**
     * Sets the value of the skipHours property.
     *
     * @param value allowed object is {@link SkipHoursList }
     *
     */
    public void setSkipHours(SkipHoursList value) {
	this.skipHours = value;
    }

    /**
     * Sets the value of the skipDays property.
     *
     * @param value allowed object is {@link SkipDaysList }
     *
     */
    public void setSkipDays(SkipDaysList value) {
	this.skipDays = value;
    }

    // creator method,  to rssItem 
    protected abstract RssItem fromArgToRssItem(T obj) throws ParseException;

    /**
     * Makes an rss out of a list of RssItems
     *
     * @param rssItems
     * @return rss from the RssItems
     */
    private Rss makeRssChannel(List<RssItem> rssItems) {
	final Rss rss = new Rss();
	final RssChannel channel = new RssChannel();

	channel.setCategory(category);
	channel.setCloud(cloud);
	channel.setCopyright(copyright);
	channel.setDescription(description);
	channel.setDocs(docs);
	channel.setGenerator(generator);
	channel.setImage(image);
	channel.setLanguage(language);
	channel.setLastBuildDate(lastBuildDate);
	channel.setLink(link);
	channel.setManagingEditor(managingEditor);
	channel.setPubDate(pubDate);
	channel.setRating(rating);
	channel.setSkipDays(skipDays);
	channel.setSkipHours(skipHours);
	channel.setTextInput(textInput);
	channel.setTitle(title);
	channel.setTtl(ttl);
	channel.setWebMaster(webMaster);

	rss.setChannel(channel);
	rss.setVersion(version);

	for (RssItem currentItem : rssItems) {
	    channel.getItem().add(currentItem);
	}

	return rss;
    }

    /**
     * Makes a list of items out a list of descriptions, return the pointer to the list of items
     *
     * @param descriptions
     * @return items out a list of descriptions
     * @throws ParseException
     */
    private List<RssItem> makeListOfRssItems(List<T> objs) throws ParseException {
	List<RssItem> listOfItems = new ArrayList<RssItem>();

        for (T currentObj : objs) {
            RssItem currentItem = fromArgToRssItem(currentObj);
	    listOfItems.add(currentItem);
	}

	return listOfItems;
    }

    public Rss makeRss(List<T> objs) throws ParseException {
	return (makeRssChannel(makeListOfRssItems(objs)));
    }

    // Helping stuff 
    protected Guid makeGuid(String href) {
	Guid result = new Guid();
	// result.setIsPermaLink(null);
	result.setValue(href);
	return result;
    }

    /// Helping stuff: for working with dates
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
     * @return 1 if date1 is older (before) than date2, returns -1 if date1 is younger (after) than date2, 0 if they are the same
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
}
