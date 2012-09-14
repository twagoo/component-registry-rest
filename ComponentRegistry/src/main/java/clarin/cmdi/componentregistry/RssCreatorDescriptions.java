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

        retval.setAuthor(desc.getCreatorName());
        // retval.setCategory(desc.???);
        // retval.setComments(desc.???);
        retval.setDescription(desc.getDescription());
        //retval.setEnclosure(desc.???);
        //retval.setGuid(desc.getId()); type mismatch
        retval.setLink(desc.getHref());
        retval.setPubDate(desc.getRegistrationDate());
        //retval.setSource(desc.???);
        retval.setTitle(desc.getName());

        
        return retval;

    }

    
}
