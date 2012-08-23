
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rss.RssItem;


/**
 *
 * @author olhsha
 */
public class RssCreator { // extends nothing so far, throuw nothing // make it abstract
    
    
    private final AbstractDescription desc;
    
    // constructor
    public RssCreator(AbstractDescription newdesc){
        desc=newdesc;
    }

    
    
    
    
    // creator method, this.desc to rssItem 
    
    public RssItem toRssItem(){
        
        if (desc == null) {return null;}
        else{
        
        RssItem retval = new RssItem();
        
        retval.setAuthor(desc.getCreatorName());
        // retval.setCategory(desc.???);
        // retval.setComments(desc.???);
        retval.setDescription(desc.getDescription());
        //retval.setEnclosure(desc.???);
        //retval.setGuid(desc.getId()); type mismatch
        retval.setLink(desc.getHref()); //???
        retval.setPubDate(desc.getRegistrationDate());
        //retval.setSource(desc.???);
        retval.setTitle(desc.getName());
        
        
        return retval;
        }
    }
}
