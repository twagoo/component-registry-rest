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

    
    private boolean isFromProfile;
    private AbstractDescription desc;
    
    public RssCreatorComments(AbstractDescription desc){
        this.desc=desc;
    }
    
    
    // creator method, comment to rssItem, ovverrides the dummy method of the RssCreator class
    // ?? is there a better way than boolean flag to arrange switch beween comment for profiles and commentss for components
    
    public void setFlagIsFromProfile(boolean isFromProfile) {
        this.isFromProfile = isFromProfile;
    }
    
    @Override  
    protected RssItem fromArgToRssItem(Comment comm) throws ParseException{

         
        RssItem retval = new RssItem();
        String hrefPostfix = "&view=comments";
        
        String descId;
        if (isFromProfile) {descId=comm.getProfileDescriptionId();} 
        else{descId=comm.getComponentDescriptionId();};
        
                //The content 
        retval.setDescription(comm.getComment()); 
        
       
        //Guid
        retval.setGuid(makeGuid(desc.getHref()+hrefPostfix));
        
       
        //date-Time
        retval.setPubDate(getRFCDateTime(comm.getCommentDate())); 
        
        // Title
        retval.setTitle(makeCommentTitle(comm.getId(), comm.getUserName()));
       
        return retval;
        
        
    }
    
     protected String  makeCommentTitle(String commentId, String user){
         
        return("The comment "+commentId +" by "+user);
           
       }
    
}
