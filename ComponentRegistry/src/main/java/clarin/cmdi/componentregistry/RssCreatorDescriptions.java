/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.rss.ElementType;
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
        
       
        
        retval.setCreator(makeElementType(desc.getCreatorName()));
        // retval.setCategory(desc.???);
        // retval.setComments(desc.???);
        retval.setDescription(desc.getDescription());
        //retval.setEnclosure(desc.???);
        
        String message ="The is of the";
        if (desc.isProfile()) {message =  message +"profile is ";} 
            else {message =  message +"component is ";} 
        retval.setGuid(makeGuid(message+desc.getId()));
        
        retval.setLink(desc.getHref());
        retval.setPubDate(desc.getRegistrationDate());
        //retval.setSource(desc.???);
        retval.setTitle(desc.getName());

        
        return retval;

    }

    
}
