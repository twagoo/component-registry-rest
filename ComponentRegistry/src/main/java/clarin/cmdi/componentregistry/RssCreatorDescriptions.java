/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.rss.RssItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author olhsha
 */
public class RssCreatorDescriptions<T extends AbstractDescription>  extends RssCreator<T> {
    
 private final static Logger LOG = LoggerFactory.getLogger(RssCreatorDescriptions.class);
    // creator method, desc to rssItem, ovverrides the dummy method of the RssCreatorClass
    @Override 
    protected RssItem fromArgToRssItem(T desc) {

       

        RssItem retval = new RssItem();
        
        //Description (blah-blah)
        retval.setDescription(desc.getDescription());
       
       
        
        //Guid
        retval.setGuid(makeGuid(super.getLink()+"?item="+desc.getId()));
        
        // link
        retval.setLink(super.getLink()+"?item="+desc.getId());
        
        //time-date
        retval.setPubDate(desc.getRegistrationDate());
        
        //Title
        retval.setTitle(makeDescriptionTitle(desc.getName(),desc.getCreatorName(), desc.getGroupName()));
        
        return retval;

    }

    protected String makeDescriptionTitle(String name, String creatorname, String group){
        
        String help;
        if (group==null) {help="is unspecified";} else {help=group;};
         
        return(name+" by user "+creatorname+", group "+help);
       
    }
}
