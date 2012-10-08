package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.model.Comment;
import java.text.ParseException;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class RssCreatorComments extends RssCreator<Comment> {
    /**
     * creator method, comment to rssItem, overrides the dummy method of the RssCreator class
     *
     * @param comm
     * @return
     * @throws ParseException
     */
    public RssCreatorComments(boolean userspace, String baseURI, int limit, 
            String descriptionId, String descriptionName, String descriptionType, List<Comment> comms, Comparator<Comment> comparator) {
        super(userspace, baseURI, limit, comms);
        this.channelLink= baseURI + "?item=" + descriptionId + ((userspace) ? "&space=user" : "") + "&browserview=comments";
        this.channelTitle = (userspace ? "Your workspace " : "Public ") + descriptionType+"s";
        this.channelDescription = String.format("Comments feed for the %s \"%s\"", descriptionType, descriptionName);;
        this.comparator = comparator;
   }
    
    
    
    @Override
    protected RssItem fromArgToRssItem(Comment comm)  {
	final String itemLink = channelLink + "&commentId=" + comm.getId();
        RssItem retval = new RssItem();
	retval.setDescription(comm.getComment());
	retval.setGuid(makeGuid(itemLink));
	retval.setLink(itemLink);
	retval.setPubDate(getRFCDateTime(comm.getCommentDate()));
	retval.setTitle(String.format("Comment %1$s\nby %2$s", comm.getId(), comm.getUserName()));
	return retval;
    }
    
}
