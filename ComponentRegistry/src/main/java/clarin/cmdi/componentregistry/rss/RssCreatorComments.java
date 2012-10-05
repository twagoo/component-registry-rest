package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.security.Principal;
import java.text.ParseException;
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
    @Override
    protected RssItem fromArgToRssItem(Comment comm)  {
	final String href = link + "&commentId=" + comm.getId();
        RssItem retval = new RssItem();
	retval.setDescription(comm.getComment());
	retval.setGuid(makeGuid(href));
	retval.setLink(href);
	retval.setPubDate(getRFCDateTime(comm.getCommentDate()));
	retval.setTitle(String.format("Comment %1$s\nby %2$s", comm.getId(), comm.getUserName()));
	return retval;
    }
    
    /**
     * 
     * @param comments
     * @param descriptionName
     * @param descriptionType
     * @param userspace
     * @param baseUri
     * @param descriptionId
     * @param limit
     * @return
     * @throws ParseException 
     */
    
    public Rss getRssComments(List<Comment> comments, String descriptionName, String descriptionType, 
        boolean userspace, String baseUri, String descriptionId, String limit ) throws ParseException{
        final String title = String.format("Comments feed for the %s  \"%s\"", descriptionType, descriptionName);
        final String space = (userspace) ? "&space=user" : "";
        final String href = baseUri + "/?item=" + descriptionId + space + "&browserview=comments";
	Rss result = getRss(Integer.parseInt(limit), comments, "Update of comments",
		title, href, Comment.COMPARE_ON_DATE);
        return result;

    } 
            
            
}
