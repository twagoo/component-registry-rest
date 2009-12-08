package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import clarin.cmdi.componentregistry.MDMarshaller;

public class ProfileDescriptionTest {

    @Test
    public void testProfileToXml() throws JAXBException, UnsupportedEncodingException {
        ProfileDescription desc = new ProfileDescription();
        desc.setName("Name");
        desc.setId("myId");
        desc.setCreatorName("myC");
        desc.setDescription("myD");
        desc.setRegistrationDate("myDate");
        desc.setXlink("linkToMyProfile");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(desc, out);
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
        assertEquals(expected, out.toString());

        ProfileDescription pd = MDMarshaller.unmarshal(ProfileDescription.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertEquals(desc.getId(), pd.getId());
    }

}
