package clarin.cmdi.componentregistry.model;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import clarin.cmdi.componentregistry.MDMarshaller;


public class ProfileDescriptionTest {
    
    @Test
    public void testProfileToXml() throws JAXBException {
        ProfileDescription desc = new ProfileDescription();
        desc.setName("Name");
        desc.setId("myId");
        desc.setCreatorName("myC");
        desc.setDescription("myD");
        desc.setRegistrationDate("myDate");
        desc.setXlink("linkToMyProfile");

        Writer writer = new StringWriter();
        MDMarshaller.marshall(desc, writer);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"; 
        expected += "<profile xlink=\"linkToMyProfile\" registrationDate=\"myDate\" name=\"Name\" id=\"myId\" description=\"myD\" creatorName=\"myC\"/>\n"; 
        assertEquals(expected, writer.toString());
    }

}
