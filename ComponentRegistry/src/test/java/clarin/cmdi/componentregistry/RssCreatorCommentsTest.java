/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
            String descrId, String userName){
        
        Comment comm =new Comment();
        
        comm.setCanDelete(canDelete);
        comm.setComment(comtext);
        comm.setCommentDate(date);
        if (isFromProfile) {comm.setComponentDescriptionId(descrId);}
                else {comm.setProfileDescriptionId(descrId);}
        comm.setId(commentId);
        comm.setUserName(userName);
        
        return comm;
        
    }
    
    /**
     * 
     */
    
    private void compareInputsVsRssItems(String href, String commtext, String date, String title, RssItem rssItem){
        
        
        assertEquals(href, rssItem.getGuid().getValue());
        assertEquals(commtext, rssItem.getDescription());
        assertEquals(date, rssItem.getPubDate());
        assertEquals(title, rssItem.getTitle());
       
    }
            
            
    
    @Test
    public void testMakeRss() throws ParseException{
        
        String href = "http";
        Boolean isFromProfile = true;
         
        Comment comm1 = makeTestComment(true, isFromProfile, "this is comment # 1", "2012-04-02T11:38:23+00:00", "commentId1", "DescrId1", 
             "userello");
        
        Comment comm2 = makeTestComment(false, isFromProfile, "this is comment # 2", "2011-04-02T11:38:22+00:00", "commentId2", "DescrId2", 
             "userino");
        
         Comment comm3 = makeTestComment(true, isFromProfile, "this is comment # 3", "2010-05-02T11:38:22+00:00", "commentId3", "DescrId3", 
             "userito");
        
        Comment[] commar = {comm1, comm2, comm3};
        List<Comment> comms = new ArrayList<Comment>(Arrays.asList(commar));
        
        List<ExtendedComment> extcomms = new ArrayList<ExtendedComment>();
        for (Comment currentcom : comms){
            ExtendedComment currentextcom = new ExtendedComment();
            currentextcom.setCom(currentcom);
            currentextcom.setHref(href);
            extcomms.add(currentextcom);
        }
        
        RssCreatorComments instance = new RssCreatorComments();
        assertEquals(Double.toString(instance.getVersion()) , "2.0"); // check if the default version is set properly
        
        instance.setFlagIsFromProfile(isFromProfile);
        instance.setVersion(3.0); 
        
        Rss result = instance.makeRss(extcomms);
        
        assertEquals(Double.toString(result.getVersion()), "3.0"); // now, check if updating version has taken place
        
        String rfcdate1 = instance.getRFCDateTime("2012-04-02T11:38:23+00:00");
        String rfcdate2 = instance.getRFCDateTime("2011-04-02T11:38:22+00:00");
        String rfcdate3 = instance.getRFCDateTime("2010-05-02T11:38:22+00:00");
        
        List<RssItem> resitems = result.getChannel().getItem();
        compareInputsVsRssItems(href, "this is comment # 1", rfcdate1, 
                 instance.makeCommentTitle("commentId1", "descrId1", "userello"), resitems.get(0));
        compareInputsVsRssItems(href, "this is comment # 2", rfcdate2, 
                instance.makeCommentTitle("commentId2", "descrId2", "userino"),
                resitems.get(1));
        compareInputsVsRssItems(href, "this is comment # 3", rfcdate3,
                instance.makeCommentTitle("commentId3", "descrId3", "userito")
                , resitems.get(2));
        
        
        
    }
    
    /*@Test
    public void testMakeRssNullComm() {
        
        System.out.println("fromArgToRssItem");
        
         
        Comment comm1 = null;
        
        List<Comment> comms = new ArrayList<Comment>(0);
        comms.add(comm1);
        
        RssCreatorComments instance = new RssCreatorComments();
        Rss result = instance.makeRss(comms);
        
        
        assertEquals(null, result.getChannel().getItem());
        
    }*/
    
    
}
