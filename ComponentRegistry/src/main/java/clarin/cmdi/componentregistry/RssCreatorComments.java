/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.text.ParseException;

/**
 *
 * @author olhsha
 */
public class RssCreatorComments extends RssCreator<Comment> {

    
    private boolean isFromProfile;
    
    // creator method, comment to rssItem, ovverrides the dummy method of the RssCreator class
    // ?? is there a better way than boolean flag to arrange switch beween comment for profiles and commentss for components
    
    public void setFlagIsFromProfile(boolean isFromProfile) {
        this.isFromProfile = isFromProfile;
    }
    
    @Override  
    protected RssItem fromArgToRssItem(Comment comm) throws ParseException{


       
        RssItem retval = new RssItem();

        
        retval.setAuthor(comm.getUserName()); 
        // retval.setCategory(desc.???);
        //retval.setComments(comm.??);
        
        retval.setDescription(comm.getComment()); 
        //retval.setEnclosure(comm.???);
        //retval.setGuid(desc.getId()); type mismatch
        //retval.setLink(com.???);
        
        retval.setPubDate(getRFCDateTime(comm.getCommentDate())); 
        // retval.setSource(comm.???);
        
        if (isFromProfile) {retval.setTitle("The comment in "+comm.getProfileDescriptionId()+".");}
        else {retval.setTitle("The comment in "+comm.getComponentDescriptionId()+".");};

        
        return retval;
        
        
    }
    
    
    
}
