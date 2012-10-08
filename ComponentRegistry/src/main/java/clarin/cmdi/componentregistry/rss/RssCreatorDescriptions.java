package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class RssCreatorDescriptions<T extends AbstractDescription> extends RssCreator<T> {
    
     public RssCreatorDescriptions(boolean userspace, String baseURI, String descriptionType, 
             int limit, List<T> descriptions, Comparator<T> comparator) {
        super(userspace, baseURI, limit, descriptions);
        this.channelLink= baseURI + "/"+ descriptionType + ((userspace) ? "?space=user" : "");
        this.channelTitle = (userspace ? "Your workspace " : "Public ") + descriptionType;
        this.channelDescription = String.format("News feed for the %s", descriptionType);
        this.comparator = comparator;
   }
    
    
    /**
     * creator method, desc to rssItem, overrides the dummy method of the RssCreatorClass
     *
     * @param desc
     * @return
     */
    @Override
    protected RssItem fromArgToRssItem(T desc) {
        String href = channelLink + (userspace ? "&item=" : "?item=") + desc.getId();
        RssItem retval = new RssItem();
	retval.setDescription(desc.getDescription());
	retval.setGuid(makeGuid(href));
	retval.setLink(href);
	retval.setPubDate(getRFCDateTime(desc.getRegistrationDate()));
	retval.setTitle(makeDescriptionTitle(desc.getName(), desc.getCreatorName(), desc.getGroupName()));
	return retval;
    }

    protected String makeDescriptionTitle(String name, String creatorname, String group) {
	final String help = (group == null) ? "is unspecified" : group;
	return (name + " by user " + creatorname + ", group " + help);
    }
}
