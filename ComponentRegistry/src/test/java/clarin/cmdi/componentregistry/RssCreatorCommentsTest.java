package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author olhsha
 */
public class RssCreatorCommentsTest {

    private Comment makeTestComment(boolean canDelete, boolean isFromProfile, String comtext, String date, String commentId,
	    String descrId, String userName) {
	Comment comm = new Comment();

	comm.setCanDelete(canDelete);
	comm.setComment(comtext);
	comm.setCommentDate(date);
	if (isFromProfile) {
	    comm.setProfileDescriptionId(descrId);
	} else {
	    comm.setComponentDescriptionId(descrId);
	}
	comm.setId(commentId);
	comm.setUserName(userName);

	return comm;
    }

    /**
     *
     */
    private void compareInputsVsRssItems(String href, String commtext, String date, String title, RssItem rssItem) {
	assertEquals(href, rssItem.getGuid().getValue());
	assertEquals(href, rssItem.getLink());
	assertEquals(commtext, rssItem.getDescription());
	assertEquals(date, rssItem.getPubDate());
	assertEquals(title, rssItem.getTitle());

    }

    @Test
    public void testMakeRss() throws ParseException {
	String testPrfId = "p_1234";
	String href = "http://catalog.clarin.eu/ds/ComponentRegistry/?item=" + testPrfId + "&view=comments";
	boolean isFromProfile = true;

	Comment comm1 = makeTestComment(true, isFromProfile, "this is comment # 1", "2012-04-02T11:38:23+00:00", "commentId1", testPrfId,
		"userello");
	Comment comm2 = makeTestComment(false, isFromProfile, "this is comment # 2", "2011-04-02T11:38:22+00:00", "commentId2", testPrfId,
		"userino");
	Comment comm3 = makeTestComment(true, isFromProfile, "this is comment # 3", "2010-05-02T11:38:22+00:00", "commentId3", testPrfId,
		"userito");
	List<Comment> comms = new ArrayList<Comment>(Arrays.asList(comm1, comm2, comm3));

	RssCreatorComments instance = new RssCreatorComments();
	instance.setLink(href);
	instance.setVersion(3.0);

	Rss result = instance.makeRss(comms);

	assertEquals(Double.toString(result.getVersion()), "3.0"); // now, check if updating version has taken place

	String rfcdate1 = instance.getRFCDateTime("2012-04-02T11:38:23+00:00");
	String rfcdate2 = instance.getRFCDateTime("2011-04-02T11:38:22+00:00");
	String rfcdate3 = instance.getRFCDateTime("2010-05-02T11:38:22+00:00");

	List<RssItem> resitems = result.getChannel().getItem();

	compareInputsVsRssItems(href + "&commentId=commentId1", "this is comment # 1", rfcdate1,
		"Comment commentId1\nby userello", resitems.get(0));
	compareInputsVsRssItems(href + "&commentId=commentId2", "this is comment # 2", rfcdate2,
		"Comment commentId2\nby userino", resitems.get(1));
	compareInputsVsRssItems(href + "&commentId=commentId3", "this is comment # 3", rfcdate3,
		"Comment commentId3\nby userito", resitems.get(2));
    }
}
