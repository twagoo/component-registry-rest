package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.rss.RssItem;

/**
 *
 * @author olhsha
 */
public class RssCreatorDescriptions<T extends AbstractDescription> extends RssCreator<T> {

    /**
     * creator method, desc to rssItem, overrides the dummy method of the RssCreatorClass
     *
     * @param desc
     * @return
     */
    @Override
    protected RssItem fromArgToRssItem(T desc) {

	//TODO: Make more generic by requiring a link that has at least one (dummy) parameter in URL
	String connectParam;
	if (super.getUserspace()) {
	    connectParam = "&";
	} else {
	    connectParam = "?";
	}
	String href = super.getLink() + connectParam + "item=" + desc.getId();

	RssItem retval = new RssItem();
	retval.setDescription(desc.getDescription());
	retval.setGuid(makeGuid(href));
	retval.setLink(href);
	retval.setPubDate(desc.getRegistrationDate());
	retval.setTitle(makeDescriptionTitle(desc.getName(), desc.getCreatorName(), desc.getGroupName()));
	return retval;
    }

    protected String makeDescriptionTitle(String name, String creatorname, String group) {
	final String help = (group == null) ? "is unspecified" : group;
	return (name + " by user " + creatorname + ", group " + help);
    }
}
