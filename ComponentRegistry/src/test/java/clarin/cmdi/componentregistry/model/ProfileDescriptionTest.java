package clarin.cmdi.componentregistry.model;

import java.io.ByteArrayInputStream;
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
        MDMarshaller.marshal(desc, writer);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<profileDescription>\n";
        expected += "    <id>myId</id>\n";
        expected += "    <description>myD</description>\n";
        expected += "    <name>Name</name>\n";
        expected += "    <registrationDate>myDate</registrationDate>\n";
        expected += "    <creatorName>myC</creatorName>\n";
        expected += "    <xlink>linkToMyProfile</xlink>\n";
        expected += "</profileDescription>\n";
        assertEquals(expected, writer.toString());

        ProfileDescription pd = MDMarshaller.unmarshal(ProfileDescription.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertEquals(desc.getId(), pd.getId());
    }

}
