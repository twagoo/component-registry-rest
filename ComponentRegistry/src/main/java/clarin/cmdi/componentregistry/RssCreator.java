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

    // creator method, this.desc to rssItem 
    private RssItem toRssItem(AbstractDescription desc) {



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

    //makes (and returns ) a channel out of a list of RssItems
    private Rss makeRssChannelFromItems(List<RssItem> rssItems) {

        final Rss rss = new Rss();
        final RssChannel channel = new RssChannel();
        rss.setChannel(channel);

        for (RssItem currentItem : rssItems) {
            channel.getItem().add(currentItem);
        }

        return rss;
    }

    //makes (nad returns) a list of items out a list of descriptions, return the pointer to the list of items
    private List<RssItem> makeListOfRssItems(List<AbstractDescription> listOfDesc) {
        List<RssItem> listOfItems = new ArrayList<RssItem>();

        for (AbstractDescription currentDesc : listOfDesc) {

            RssItem currentItem = toRssItem(currentDesc);
            listOfItems.add(currentItem);
        }

        return listOfItems;
    }

    //makes (and returns) a channel out a list of descriptions
    /**
     * 
     * @param descrs refers to the list of  component or profile descriptions, which is to be turn into an Rss
     * in principle, "descrs" parameter should not be null 
     * @return the reference to Rss created from descrs
     */
    public Rss makeRssChannelFromDescriptions(List<AbstractDescription> descrs) {

        return (makeRssChannelFromItems(makeListOfRssItems(descrs)));
    }
}
