/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.rss.RssItem;

/**
 *
 * @author olhsha
 */
public class RssCreatorDescriptions<T extends AbstractDescription>  extends RssCreator<T> {
    
 
    // creator method, desc to rssItem, ovverrides the dummy method of the RssCreatorClass
    @Override 
    protected RssItem fromArgToRssItem(T desc) {

       

        RssItem retval = new RssItem();
        
        //Description (blah-blah)
        retval.setDescription(desc.getDescription());
       
       
        
        //Guid
        //retval.setGuid(makeGuid(link));
        
        // link
        retval.setLink(super.getLink()+"?item="+desc.getId()+"#");
        
        //time-date
        retval.setPubDate(desc.getRegistrationDate());
        
        //Title
        retval.setTitle(makeDescriptionTitle(desc.getName(),desc.getCreatorName(), desc.getGroupName(),desc.getDomainName()));

        
        return retval;

    }

    protected String makeDescriptionTitle(String name, String creatorname, String group, String domain){
        String retval =  name+"by user "+creatorname+", group "+group+",domain "+domain;
        
        return retval;
    }
}
