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
    
    
    
    private Comment makeTestComment(boolean flag, String comtext, String date, String commentDescrId, 
            String id, String profileDescrId, String userId, String userName){
        
        Comment comm =new Comment();
        
        comm.setCanDelete(flag);
        comm.setComment(comtext);
        comm.setCommentDate(date);
        comm.setComponentDescriptionId(commentDescrId);
        comm.setId(id);
        comm.setProfileDescriptionId(profileDescrId);
        comm.setUserId(userId);
        comm.setUserName(userName);
        
        return comm;
        
    }
    
    /**
     * 
     */
    
    private void compareInputsVsRssItems(String userName, String commtext, String date, String title, RssItem rssItem){
        
        
        assertEquals(userName, rssItem.getAuthor());  
        assertEquals(commtext, rssItem.getDescription());
        assertEquals(date, rssItem.getPubDate());
        assertEquals(title, rssItem.getTitle());
       
    }
            
            
    
    @Test
    public void testMakeRss() throws ParseException{
        
         
        Comment comm1 = makeTestComment(true, "this is comment # 1", "2012-04-02T11:38:23+00:00", "DescrId1", 
                "Id1", "ProfileDescId1", "us1", "userello");
        
        Comment comm2 = makeTestComment(false, "this is comment # 2", "2012-05-02T12:38:23+00:00", "DescrId2", 
                "Id2", "ProfileDescId2", "us2", "userino");
        
        Comment comm3 = makeTestComment(true, "this is comment # 3", "2012-04-07T11:38:23+01:00", "DescrId3", 
                "Id3", "ProfileDescId3", "us3", "userito");
        
        Comment[] commar = {comm1, comm2, comm3};
        List<Comment> comms = new ArrayList<Comment>(Arrays.asList(commar));
        
        RssCreatorComments instance = new RssCreatorComments();
        assertEquals(Double.toString(instance.getVersion()) , "2.0"); // check if the default version is set properly
        
        instance.setFlagIsFromProfile(true);
        instance.setVersion(2.0);
        
        Rss result = instance.makeRss(comms);
        
        assertEquals(Double.toString(result.getVersion()), "2.0");
        
        List<RssItem> resitems = result.getChannel().getItem();
        //compareInputsVsRssItems("userello", "this is comment # 1", "2012-04-02T11:38:23+00:00", "The comment in ProfileDescId1.", resitems.get(0));
        //compareInputsVsRssItems("userino", "this is comment # 2", "2012-10-13", "The comment in ProfileDescId2.", resitems.get(1));
        //compareInputsVsRssItems("userito", "this is comment # 3", "2012-09-14", "The comment in ProfileDescId3.", resitems.get(2));
        
        
        System.out.println(resitems.get(0).getPubDate());
        System.out.println("hallo");
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
