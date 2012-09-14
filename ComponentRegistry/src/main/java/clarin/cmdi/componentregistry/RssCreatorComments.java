/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.rss.RssItem;

/**
 *
 * @author olhsha
 */
public class RssCreatorComments extends RssCreator<Comment> {

    
    
    
    // creator method, comment to rssItem, ovverrides the dummy method of the RssCreator class
    
    @Override  
    protected RssItem fromArgToRssItem(Comment comm) {


       
        RssItem retval = new RssItem();

        
        retval.setAuthor(comm.getUserName()); 
        // retval.setCategory(desc.???);
        retval.setComments(comm.getComment());
        
        //retval.setDescription(comm.??); 
        //retval.setEnclosure(desc.???);
        //retval.setGuid(desc.getId()); type mismatch
        //retval.setLink(com.???);
        retval.setPubDate(comm.getCommentDate()); 
        //retval.setSource(desc.???);
        //retval.setTitle(com.getName());

        
        return retval;
        
        
    }
    
}
