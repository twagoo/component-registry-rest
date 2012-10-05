package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.text.ParseException;

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
    protected RssItem fromArgToRssItem(Comment comm) throws ParseException {
	final String href = super.getLink() + "&commentId=" + comm.getId();

	RssItem retval = new RssItem();
	retval.setDescription(comm.getComment());
	retval.setGuid(makeGuid(href));
	retval.setLink(href);
	retval.setPubDate(getRFCDateTime(comm.getCommentDate()));
	retval.setTitle(String.format("Comment %1$s\nby %2$s", comm.getId(), comm.getUserName()));
	return retval;
    }
}
