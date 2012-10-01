/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.text.ParseException;

/**
 *
 * @author olhsha
 */
public class RssCreatorComments extends RssCreator<Comment> {

   
    // creator method, comment to rssItem, ovverrides the dummy method of the RssCreator class
    // ?? is there a better way than boolean flag to arrange switch beween comment for profiles and commentss for components
    
   
    
    @Override  
    protected RssItem fromArgToRssItem(Comment comm) throws ParseException{

         
        RssItem retval = new RssItem();
        
        
                //The content 
        retval.setDescription(comm.getComment()); 
        
       
        //Guid
        retval.setGuid(makeGuid(super.getLink()+"&commentId="+comm.getId()));
        
        // link
        retval.setLink(super.getLink()+"&commentId="+comm.getId());
        
        
        
       
        //date-Time
        retval.setPubDate(getRFCDateTime(comm.getCommentDate())); 
        
        // Title
        retval.setTitle(makeCommentTitle(comm.getId(), comm.getUserName()));
       
        return retval;
        
        
    }
    
     protected String  makeCommentTitle(String commentId, String user){
         
        return("Comment "+commentId +"\n by "+user+" ");
           
       }
    
}
