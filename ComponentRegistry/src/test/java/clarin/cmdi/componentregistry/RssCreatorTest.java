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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    
    
   
   
    ////////////////////////////
    @Test
    public void profileTestToRssItem() {
        
        ProfileDescription pdesc=
                createTestProfileDescription(23, "creatorname", 
                "description", "domainname", "groupname", "href", "name", true, "uid");
        
         
        RssCreator creator = new RssCreator(pdesc);
        RssItem result = creator.toRssItem();
        assertEqualDescriptions(pdesc, result);
       
        
    }
    
    // write test component into file
    private void writeComponentIntoFile(ComponentDescription cdesc, String filename) throws IOException, JAXBException {
        
       
        ByteArrayOutputStream os = new ByteArrayOutputStream();
	MDMarshaller.marshal(cdesc, os);
        
        RegistryTestHelper.writeStreamToFile(os, filename);
       
    }
    
    ///////////////////////////////////////
    // make test component 1 and write it into file
    
    private  ComponentDescription makeTestComponent1() throws IOException, JAXBException {
        
        ComponentDescription cdesc=
                createTestComponentDescription(67, "God", 
                "description1dum", "domainname1dum", "groupname1dum", "href1dum", "name1dum", "uid1dum");
      
        return cdesc;
    }
    
    ///////////////////////////////////////
    // make test component 1 and write it into file
    
    private  ComponentDescription makeTestComponent2() throws IOException, JAXBException {
        
        ComponentDescription cdesc=
                createTestComponentDescription(23, "Allah", 
                "description2dum", "domainname2dum", "groupname2dum", "href2dum", "name2dum", "uid2dum");
      
        return cdesc;
    }
    
    ////////////////////////////////////////
    @Test
    public void testRssChannelAndMarshal() throws IOException, JAXBException {
        
         String dirName="MyTestXmls";
         File testDir = new File(dirName);
         testDir.mkdir();
        
         String path = new File(testDir, dirName).getAbsolutePath();
         
         String comp1="Component1.xml";
         String comp2="Component2.xml";
         String rssOut="rssTest.xml";
                  
        
        
        
        ComponentDescription cdesc1=makeTestComponent1();
        writeComponentIntoFile(cdesc1, path+comp1);
        
        ComponentDescription cdesc2=makeTestComponent2();
        writeComponentIntoFile(cdesc2, path+comp2);
        
        
        
        FileInputStream  is1 = new FileInputStream(path+comp1);
        ComponentDescription desc1 = MDMarshaller.unmarshal(ComponentDescription.class, is1, null) ;
        
        FileInputStream  is2 = new FileInputStream(path+comp2);
        ComponentDescription desc2 = MDMarshaller.unmarshal(ComponentDescription.class, is2, null) ;
        
        
        RssCreator creator1 = new RssCreator(desc1);
        RssItem rssitem1 = creator1.toRssItem();
        assertTrue(rssitem1 !=null);
        assertEqualDescriptions(desc1, rssitem1);
        
        
        RssCreator creator2 = new RssCreator(desc2);
        RssItem rssitem2 = creator2.toRssItem();
        assertTrue(rssitem2 !=null);
        assertEqualDescriptions(desc2, rssitem2);
        
        
        
        final Rss rss = new Rss();
        final RssChannel channel = new RssChannel();
	rss.setChannel(channel);
        channel.getItem().add(rssitem1);
        channel.getItem().add(rssitem2);
        
        ByteArrayOutputStream osrss = new ByteArrayOutputStream();
	MDMarshaller.marshal(rss, osrss);
        
        RegistryTestHelper.writeStreamToFile(osrss, path+rssOut);
        
       
    }
    
    
   
}
