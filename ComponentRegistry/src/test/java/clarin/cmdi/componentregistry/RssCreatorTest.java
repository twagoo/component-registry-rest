/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import clarin.cmdi.componentregistry.rss.RssItem;
import java.io.IOException;
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
    
    public RssCreatorTest() {
    }
    
    // ????????????????
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of toRssItem method, of class RssCreator.
     */
    @Test
    public void nullTestToRssItem() {
        
        RssCreator creator = new RssCreator(null);
        RssItem result = creator.toRssItem();
        assertEquals(null, result);
       
    }
    
    
    
    private ProfileDescription createTestProfileDescription(int commentcount, String creatorname, 
    String description, String domainname, String groupname, String href, 
    String name, boolean editorflag, String uid){
        
        ProfileDescription pdesc = ProfileDescription.createNewDescription();
        
        pdesc.setCommentsCount(commentcount);
        pdesc.setCreatorName(creatorname);
        pdesc.setDescription(description);
        pdesc.setDomainName(domainname);
        pdesc.setGroupName(groupname);
        pdesc.setHref(href);
        pdesc.setName(name);
        pdesc.setShowInEditor(editorflag);
        pdesc.setUserId(uid);
        
        return pdesc;
        
        
    }
    
    private ComponentDescription createTestComponentDescription(int commentcount, String creatorname, 
    String description, String domainname, String groupname, String href, 
    String name, String uid){
        
        ComponentDescription cdesc = ComponentDescription.createNewDescription();
        
        cdesc.setCommentsCount(commentcount);
        cdesc.setCreatorName(creatorname);
        cdesc.setDescription(description);
        cdesc.setDomainName(domainname);
        cdesc.setGroupName(groupname);
        cdesc.setHref(href);
        cdesc.setName(name);
        cdesc.setUserId(uid);
        
        return cdesc;
        
        
    }
    
    private void assertEqualDescriptions(AbstractDescription desc, RssItem item){
        assertEquals(desc.getCreatorName(), item.getAuthor());
        assertEquals(desc.getDescription(), item.getDescription());
        assertEquals(desc.getHref(), item.getLink());
        assertEquals(desc.getRegistrationDate(), item.getPubDate());
        assertEquals(desc.getName(), item.getTitle());
    }
    
    @Test
    public void profileTestToRssItem() {
        
        ProfileDescription pdesc=
                createTestProfileDescription(23, "creatorname", 
                "description", "domainname", "groupname", "href", "name", true, "uid");
        
        RssCreator creator = new RssCreator(pdesc);
        RssItem result = creator.toRssItem();
        assertEqualDescriptions(pdesc, result);
       
    }
    
    @Test
    public void componentTestToRssItem() {
        
        ComponentDescription cdesc=
                createTestComponentDescription(67, "creatorname", 
                "description", "domainname", "groupname", "href", "name", "uid");
        
        RssCreator creator = new RssCreator(cdesc);
        RssItem result = creator.toRssItem();
        assertEqualDescriptions(cdesc, result);
       
    }
    
    
    @Test
      public  void fileLargeProfileTestToRssItem() throws IOException, JAXBException {
          
          String largeprofilestring = RegistryTestHelper.getLargeProfileContent(); // reading from the file
          CMDComponentSpec compspec=RegistryTestHelper.getComponentFromString(largeprofilestring); // calling unmarchaller
          
          
      // do not know how to get AbstractDescription from this file    
          
      }
      
}
