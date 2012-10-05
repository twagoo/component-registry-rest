package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class RssCreatorDescriptions<T extends AbstractDescription> extends RssCreator<T> {
     private boolean userspace;
    /**
     * creator method, desc to rssItem, overrides the dummy method of the RssCreatorClass
     *
     * @param desc
     * @return
     */
    @Override
    protected RssItem fromArgToRssItem(T desc) {
        String href = link + ((userspace) ? "&item=" : "/?item=") + desc.getId();
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

    
   public Rss getRssDescriptions(List<T> descriptions, boolean userspace, String descriptionType, String limit, String baseUri) throws ParseException{
        this.userspace=userspace;
        final String title = (userspace ? ("Workspace ") : ("Public "))+descriptionType;
        final RssCreatorDescriptions instance = new RssCreatorDescriptions();
        final String baseUriUserspace = baseUri+((userspace) ? "/?space=user" : "");
	final Rss rss = instance.getRss(Integer.parseInt(limit), descriptions, "Updates for " +descriptionType, title, baseUriUserspace, AbstractDescription.COMPARE_ON_DATE);
	return rss;
   }
}
