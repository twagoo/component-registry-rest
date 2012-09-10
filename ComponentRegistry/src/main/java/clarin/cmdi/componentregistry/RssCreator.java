
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssChannel;

import clarin.cmdi.componentregistry.rss.RssItem;
import java.util.ArrayList;
import java.util.List;


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
        retval.setLink(desc.getHref()); 
        retval.setPubDate(desc.getRegistrationDate());
        //retval.setSource(desc.???);
        retval.setTitle(desc.getName());
        
        
        return retval;
        }
    }
    
    //makes (and returns ) a channel out of a list of RssItems
    private Rss makeRssChannelFromItems(List<RssItem> rssItems){
        
        final Rss rss = new Rss();
        final RssChannel channel = new RssChannel();
	rss.setChannel(channel);
        
        for (RssItem currentItem : rssItems) {
            channel.getItem().add(currentItem);
        }
        
        return rss;
    }
    
    //makes (nad returns) a list of items out a list of descriptions, return the pointer to the list of items
    private List<RssItem> makeListOfRssItems(List<AbstractDescription> listOfDesc){
        List<RssItem> listOfItems = new ArrayList<RssItem>();
        
        for (AbstractDescription currentDesc : listOfDesc) {
            RssCreator currentCreator = new RssCreator(currentDesc);
            RssItem currentItem=currentCreator.toRssItem();
            listOfItems.add(currentItem);
        }
        
        return listOfItems;
    }
    
    
    //makes (and returns) a channel out a list of descriptions
    public Rss makeRssChannelFromDescriptions(List<AbstractDescription> descrs){
        
        return(makeRssChannelFromItems(makeListOfRssItems(descrs)));
    }
}
