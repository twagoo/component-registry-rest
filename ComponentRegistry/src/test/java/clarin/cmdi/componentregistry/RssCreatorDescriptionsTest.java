
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
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
            String description, String domainname, String groupname, 
            String name, String date, String href) {

        desc.setCommentsCount(commentcount);
        desc.setCreatorName(creatorname);
        desc.setDescription(description);
        desc.setDomainName(domainname);
        desc.setGroupName(groupname);
        desc.setName(name);
        desc.setRegistrationDate(date);
        desc.setHref(href);

    }

    private ProfileDescription createTestProfileDescription(int commentcount, String creatorname,
            String description, String domainname, String groupname, 
            String name, boolean editorFlag, String date, String href) {

        ProfileDescription pdesc = ProfileDescription.createNewDescription();

        createTestDescription(pdesc, commentcount, creatorname, description, domainname, groupname, name, date, href);

        pdesc.setShowInEditor(editorFlag);

        return pdesc;


    }

    private ComponentDescription createTestComponentDescription(int commentcount, String creatorname,
            String description, String domainname, String groupname, 
            String name, String date, String href) {

        ComponentDescription cdesc = ComponentDescription.createNewDescription();

        createTestDescription(cdesc, commentcount, creatorname, description, domainname, groupname, name, date, href);

        return cdesc;


    }

    //////////////////////////////////////
    private void compareRssVsValues(String description, String href, String date, String title, RssItem item) {
       
        assertEquals(description, item.getDescription());
        assertEquals(href, item.getGuid().getValue()  );
        assertEquals(date, item.getPubDate());
        assertEquals(title, item.getTitle());
    }

    /**
     * Creates 3 profile descriptions, makes them into a list, and makes a 3-item Rss out of this list
     * Checks if the values of the fields in each item are as expected
     */
    @Test
    public void testMakeRss() throws JAXBException, UnsupportedEncodingException, IOException, ParseException{

        String href="http";
        
        ProfileDescription desc1 = createTestProfileDescription(23, "Creator 1",
                "description-1", "domainname-1", "groupname-1", "name-1", true, "2001-01-01", href);

        ProfileDescription desc2 = createTestProfileDescription(23, "Creator 2",
                "description-2", "domainname-2", "groupname-2", "name-2", false, "2001-01-02", href);

        ProfileDescription desc3 = createTestProfileDescription(23, "Creator 3",
                "description-3", "domainname-3", "groupname-3", "name-3", true, "2001-01-03", href);

        List<ProfileDescription> descriptions = Arrays.asList(desc1, desc2, desc3);

        RssCreatorDescriptions instance = new RssCreatorDescriptions();
        
        
        
        
        instance.setVersion(2.0);
        
        instance.setLink(href);
        instance.setCategory(null);
        instance.setCloud(null);
        instance.setCopyright("copyleft");
        instance.setDescription("this is a test rss");
        instance.setDocs("doc");
        instance.setGenerator("generator");
        instance.setImage(null);
        instance.setLanguage("engl");
        instance.setLastBuildDate("today");
        instance.setManagingEditor("twan");
        instance.setPubDate("publication date");
        instance.setRating("rating");
        instance.setSkipDays(null);
        instance.setSkipHours(null);
        instance.setTextInput(null);
        instance.setTitle("TITLE");
        instance.setWebMaster("webMaster");
        
        Rss result = instance.makeRss(descriptions);
        assertEquals(Double.toString(result.getVersion()), "2.0");
        
        
        List<RssItem> items = result.getChannel().getItem();

        assertEquals(3, result.getChannel().getItem().size());

        // String creatorname, String description, String href, String date, String nametitle, RssItem item
        compareRssVsValues("description-1", href, "2001-01-01", 
                   instance.makeDescriptionTitle("name-1", "Creator-1", "groupname-1","domainname-1"), items.get(0));

        compareRssVsValues("description-2", href, "2001-01-02", 
                instance.makeDescriptionTitle("name-2", "Creator-2", "groupname-2","domainname-2"), items.get(1));

        compareRssVsValues("description-3", href, "2001-01-03", 
                instance.makeDescriptionTitle("name-3", "Creator-3", "groupname-3","domainname-3"), items.get(2));

        
        
        assertEquals("2.0", Double.toString(result.getVersion()));
        
        assertEquals(null, result.getChannel().getCategory());
        assertEquals(null, result.getChannel().getCloud());
        assertEquals("copyleft", result.getChannel().getCopyright());
        assertEquals("this is a test rss", result.getChannel().getDescription());
        assertEquals("doc", result.getChannel().getDocs());
        assertEquals("generator", result.getChannel().getGenerator());
        assertEquals(null, result.getChannel().getImage());
        assertEquals("engl", result.getChannel().getLanguage());
        assertEquals("today", result.getChannel().getLastBuildDate());
        assertEquals(href, result.getChannel().getLink());
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
    
   
}