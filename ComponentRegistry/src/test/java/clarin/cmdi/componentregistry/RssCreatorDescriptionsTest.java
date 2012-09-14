
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author olhsha
 */
public class RssCreatorDescriptionsTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    
    

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
    public void testMakeRss() throws JAXBException, UnsupportedEncodingException, IOException{

        
        
        ProfileDescription desc1 = createTestProfileDescription(23, "Joe Unit",
                "description-1", "domainname-1", "groupname-1", "href-1", "titlename-1", true, "uid1", "2001-01-01");

        ProfileDescription desc2 = createTestProfileDescription(23, "Joe Unit",
                "description-2", "domainname-2", "groupname-2", "href-2", "titlename-2", false, "uid-2", "2001-01-02");

        ProfileDescription desc3 = createTestProfileDescription(23, "Terminator",
                "description-3", "domainname-3", "groupname-3", "href-3", "titlename-3", true, "uid-3", "2001-01-03");

        List<ProfileDescription> descriptions = Arrays.asList(desc1, desc2, desc3);

        RssCreatorDescriptions instance = new RssCreatorDescriptions();
        
        
        
        BigDecimal tmp= new BigDecimal("2.0");
        instance.setVersion(tmp);
        
        instance.setCategory(null);
        instance.setCloud(null);
        instance.setCopyright("copyleft");
        instance.setDescription("this is a test rss");
        instance.setDocs("doc");
        instance.setGenerator("generator");
        instance.setImage(null);
        instance.setLanguage("engl");
        instance.setLastBuildDate("today");
        instance.setLink("link");
        instance.setManagingEditor("twan");
        instance.setPubDate("publication date");
        instance.setRating("rating");
        instance.setSkipDays(null);
        instance.setSkipHours(null);
        instance.setTextInput(null);
        instance.setTitle("TITLE");
        instance.setWebMaster("webMaster");
        
        Rss result = instance.makeRss(descriptions);
        
        
        
        List<RssItem> items = result.getChannel().getItem();

        assertEquals(3, result.getChannel().getItem().size());

        // String creatorname, String description, String href, String date, String nametitle, RssItem item
        compareRssVsValues("Joe Unit",
                "description-1", "href-1", "2001-01-01", "titlename-1", items.get(0));

        compareRssVsValues("Joe Unit",
                "description-2", "href-2", "2001-01-02", "titlename-2", items.get(1));

        compareRssVsValues("Terminator",
                "description-3", "href-3", "2001-01-03", "titlename-3", items.get(2));

        
        
        assertEquals(tmp, result.getVersion());
        
        assertEquals(null, result.getChannel().getCategory());
        assertEquals(null, result.getChannel().getCloud());
        assertEquals("copyleft", result.getChannel().getCopyright());
        assertEquals("this is a test rss", result.getChannel().getDescription());
        assertEquals("doc", result.getChannel().getDocs());
        assertEquals("generator", result.getChannel().getGenerator());
        assertEquals(null, result.getChannel().getImage());
        assertEquals("engl", result.getChannel().getLanguage());
        assertEquals("today", result.getChannel().getLastBuildDate());
        assertEquals("link", result.getChannel().getLink());
        assertEquals("twan", result.getChannel().getManagingEditor());
        assertEquals("publication date", result.getChannel().getPubDate());
        assertEquals("rating", result.getChannel().getRating());
        assertEquals(null , result.getChannel().getSkipDays());
        assertEquals(null, result.getChannel().getSkipHours());
        assertEquals(null, result.getChannel().getTextInput());
        assertEquals("TITLE", result.getChannel().getTitle());
        assertEquals("webMaster", result.getChannel().getWebMaster());
        
        //write the Rss chaneel into the file, so you can see  how it looks like in the browser
        String path=RegistryTestHelper.openTestDir("testRss");
        String os = MDMarshaller.marshalToString(result);
        RegistryTestHelper.writeStringToFile(os, path + "testRss1.xml");
    }
    
    
    // the test below shows that if we do not set parameters for the channel or no version for Rss
    // then nothing wrong happen, no null pointer exception, etc.
    @Test
    public void testMakeRssNoChannelSet() throws JAXBException, UnsupportedEncodingException, IOException{

       
        ProfileDescription desc1 = createTestProfileDescription(23, "Joe Unit",
                "description-1", "domainname-1", "groupname-1", "href-1", "titlename-1", true, "uid1", "2001-01-01");

        List<ProfileDescription> descriptions = Arrays.asList(desc1);

        RssCreatorDescriptions instance = new RssCreatorDescriptions();
        Rss result = instance.makeRss(descriptions);
        

        List<RssItem> items = result.getChannel().getItem();

        assertEquals(1, result.getChannel().getItem().size());

        // String creatorname, String description, String href, String date, String nametitle, RssItem item
        compareRssVsValues("Joe Unit",
                "description-1", "href-1", "2001-01-01", "titlename-1", items.get(0));

        
        //write the Rss chaneel into the file, so you can see  how it looks like in the browser
        String path=RegistryTestHelper.openTestDir("testRss");
        String os = MDMarshaller.marshalToString(result);
        RegistryTestHelper.writeStringToFile(os, path + "testRssNoChannelSet.xml");
    }

  
}
// String comp1 = "Component1.xml";
// String path = RegistryTestHelper.openTestDir("MyTestXmls");
// FileInputStream is1 = new FileInputStream(path + comp1);
// ComponentDescription desc1 = MDMarshaller.unmarshal(ComponentDescription.class, is1, null);
