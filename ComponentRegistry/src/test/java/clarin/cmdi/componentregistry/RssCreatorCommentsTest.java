/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ProfileDescription;
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
        if (isFromProfile) {comm.setProfileDescriptionId(descrId);}
                else {comm.setComponentDescriptionId(descrId);}
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
        
        String hrefPrefix = "http://catalog.clarin.eu/ds/ComponentRegistry/";
        String hrefInfix="?item=";
        String hrefPostfix = "&view=comments";
        Boolean isFromProfile = true;
        
        //making a test profile
        ProfileDescription testPrf = new ProfileDescription();
        String testPrfId = "p_1234";
        testPrf.setId(testPrfId);
         
        Comment comm1 = makeTestComment(true, isFromProfile, "this is comment # 1", "2012-04-02T11:38:23+00:00", "commentId1", testPrf.getId(), 
             "userello");
        
        Comment comm2 = makeTestComment(false, isFromProfile, "this is comment # 2", "2011-04-02T11:38:22+00:00", "commentId2", testPrf.getId(), 
             "userino");
        
         Comment comm3 = makeTestComment(true, isFromProfile, "this is comment # 3", "2010-05-02T11:38:22+00:00", "commentId3", testPrf.getId(), 
             "userito");
        
        Comment[] commar = {comm1, comm2, comm3};
        List<Comment> comms = new ArrayList<Comment>(Arrays.asList(commar));
        
        testPrf.setCommentsCount(3);
        // comments cannot be referred from the profile desc directly
        // on needs to know componentregistry instance, and via this instance, given pofileId, 
        //one can get the list of comments from the profile
       
        
        RssCreatorComments instance = new RssCreatorComments(hrefPrefix);
        assertEquals(Double.toString(instance.getVersion()) , "2.0"); // check if the default version is set properly
        
        instance.setFlagIsFromProfile(isFromProfile);
        instance.setVersion(3.0); 
        
        Rss result = instance.makeRss(comms);
        
        assertEquals(Double.toString(result.getVersion()), "3.0"); // now, check if updating version has taken place
        
        String rfcdate1 = instance.getRFCDateTime("2012-04-02T11:38:23+00:00");
        String rfcdate2 = instance.getRFCDateTime("2011-04-02T11:38:22+00:00");
        String rfcdate3 = instance.getRFCDateTime("2010-05-02T11:38:22+00:00");
        
        List<RssItem> resitems = result.getChannel().getItem();
        
        String checkUri =hrefPrefix+hrefInfix+testPrfId+hrefPostfix;
        
        compareInputsVsRssItems(checkUri, "this is comment # 1", rfcdate1, 
                 instance.makeCommentTitle("commentId1", "userello"), resitems.get(0));
        compareInputsVsRssItems(checkUri, "this is comment # 2", rfcdate2, 
                instance.makeCommentTitle("commentId2", "userino"),
                resitems.get(1));
        compareInputsVsRssItems(checkUri, "this is comment # 3", rfcdate3,
                instance.makeCommentTitle("commentId3", "userito")
                , resitems.get(2));
        
        System.out.println("  "); 
        for (int i=0; i<3; i++) {
            System.out.print("Rss-item for comment "); 
            System.out.println(i);
            System.out.println(resitems.get(i).getTitle());
            System.out.println(resitems.get(i).getPubDate());
            System.out.println(resitems.get(i).getDescription());
            System.out.println(resitems.get(i).getGuid().getValue());
            System.out.println("  "); 
        }
        
        
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
