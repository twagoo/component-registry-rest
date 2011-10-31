package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import clarin.cmdi.componentregistry.MDMarshaller;

public class ProfileDescriptionTest {

    @Test
    public void testProfileToXml() throws JAXBException, UnsupportedEncodingException {
        ProfileDescription desc = new ProfileDescription();
        desc.setName("Name");
        desc.setId("myId");
        desc.setCreatorName("myC");
        desc.setUserId(DigestUtils.md5Hex("user1"));
        desc.setDescription("myD");
        desc.setRegistrationDate("myDate");
        desc.setDomainName("Linguistics");
        desc.setHref("linkToMyProfile");
        desc.setGroupName("MyGroup");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(desc, out);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<profileDescription xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        expected += "    <id>myId</id>\n";
        expected += "    <description>myD</description>\n";
        expected += "    <name>Name</name>\n";
        expected += "    <registrationDate>myDate</registrationDate>\n";
        expected += "    <creatorName>myC</creatorName>\n";
        expected += "    <userId>"+DigestUtils.md5Hex("user1")+"</userId>\n";
        expected += "    <domainName>Linguistics</domainName>\n";
        expected += "    <ns2:href>linkToMyProfile</ns2:href>\n";
        expected += "    <groupName>MyGroup</groupName>\n";
        expected += "    <showInEditor>true</showInEditor>\n";
	expected += "</profileDescription>\n";
        assertEquals(expected, out.toString());

        ProfileDescription pd = MDMarshaller.unmarshal(ProfileDescription.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertEquals(desc.getId(), pd.getId());
        assertTrue(desc.isThisTheOwner("user1"));
        assertFalse(desc.isThisTheOwner("aap"));
    }

}
