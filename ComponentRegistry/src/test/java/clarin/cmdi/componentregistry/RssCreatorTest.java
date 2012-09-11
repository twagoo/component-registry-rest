/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssChannel;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author olhsha
 */
public class RssCreatorTest {

    
    

    private void createTestDescription(AbstractDescription desc, int commentcount, String creatorname,
            String description, String domainname, String groupname, String href,
            String name, String uid, String date) {

        desc.setCommentsCount(commentcount);
        desc.setCreatorName(creatorname);
        desc.setDescription(description);
        desc.setDomainName(domainname);
        desc.setGroupName(groupname);
        desc.setHref(href);
        desc.setName(name);
        desc.setUserId(uid);
        desc.setRegistrationDate(date);

    }

    private ProfileDescription createTestProfileDescription(int commentcount, String creatorname,
            String description, String domainname, String groupname, String href,
            String name, boolean editorFlag, String uid, String date) {

        ProfileDescription pdesc = ProfileDescription.createNewDescription();

        createTestDescription(pdesc, commentcount, creatorname, description, domainname, groupname, href, name, uid, date);

        pdesc.setShowInEditor(editorFlag);

        return pdesc;


    }

    private ComponentDescription createTestComponentDescription(int commentcount, String creatorname,
            String description, String domainname, String groupname, String href,
            String name, String uid, String date) {

        ComponentDescription cdesc = ComponentDescription.createNewDescription();

        createTestDescription(cdesc, commentcount, creatorname, description, domainname, groupname, href, name, uid, date);

        return cdesc;


    }

    //////////////////////////////////////
    private void compareRssVsValues(String creatorname, String description, String href, String date, String nametitle, RssItem item) {
        assertEquals(creatorname, item.getAuthor());
        assertEquals(description, item.getDescription());
        assertEquals(href, item.getLink());
        assertEquals(date, item.getPubDate());
        assertEquals(nametitle, item.getTitle());
    }

    /**
     * Creates 3 profile descriptions, makes them into a list, and makes a 3-item Rss out of this list
     * Checks if the values of the fields in each item are as expected
     */
    @Test
    public void testMakeRssChannelFromDescriptions() {


        AbstractDescription desc1 = createTestProfileDescription(23, "Joe Unit",
                "description-1", "domainname-1", "groupname-1", "href-1", "titlename-1", true, "uid1", "2001-01-01");

        AbstractDescription desc2 = createTestProfileDescription(23, "Joe Unit",
                "description-2", "domainname-2", "groupname-2", "href-2", "titlename-2", false, "uid-2", "2001-01-02");

        AbstractDescription desc3 = createTestProfileDescription(23, "Terminator",
                "description-3", "domainname-3", "groupname-3", "href-3", "titlename-3", true, "uid-3", "2001-01-03");

        List<AbstractDescription> descriptions = Arrays.asList(desc1, desc2, desc3);

        RssCreator instance = new RssCreator();
        Rss result = instance.makeRssChannelFromDescriptions(descriptions);

        List<RssItem> items = result.getChannel().getItem();

        assertEquals(3, result.getChannel().getItem().size());

        // String creatorname, String description, String href, String date, String nametitle, RssItem item
        compareRssVsValues("Joe Unit",
                "description-1", "href-1", "2001-01-01", "titlename-1", items.get(0));

        compareRssVsValues("Joe Unit",
                "description-2", "href-2", "2001-01-02", "titlename-2", items.get(1));

        compareRssVsValues("Terminator",
                "description-3", "href-3", "2001-01-03", "titlename-3", items.get(2));


    }
}
// String comp1 = "Component1.xml";
// String path = RegistryTestHelper.openTestDir("MyTestXmls");
// FileInputStream is1 = new FileInputStream(path + comp1);
// ComponentDescription desc1 = MDMarshaller.unmarshal(ComponentDescription.class, is1, null);
