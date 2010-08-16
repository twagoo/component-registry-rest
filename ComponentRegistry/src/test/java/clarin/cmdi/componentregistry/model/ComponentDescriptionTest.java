package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import clarin.cmdi.componentregistry.MDMarshaller;

public class ComponentDescriptionTest {

    @Test
    public void testComponentToXml() throws JAXBException, UnsupportedEncodingException {
        ComponentDescription desc = new ComponentDescription();
        desc.setName("Name");
        desc.setId("myId");
        desc.setCreatorName("myC");
        desc.setUserId("user1");
        desc.setDescription("myD");
        desc.setRegistrationDate("myDate");
        desc.setHref("linkToMyComponent");
        desc.setGroupName("MyGroup");
        desc.setDomainName("Linguistics");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(desc, out);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<componentDescription xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        expected += "    <id>myId</id>\n";
        expected += "    <description>myD</description>\n";
        expected += "    <name>Name</name>\n";
        expected += "    <registrationDate>myDate</registrationDate>\n";
        expected += "    <creatorName>myC</creatorName>\n";
        expected += "    <userId>user1</userId>\n";
        expected += "    <domainName>Linguistics</domainName>\n";
        expected += "    <ns2:href>linkToMyComponent</ns2:href>\n";
        expected += "    <groupName>MyGroup</groupName>\n";
        expected += "</componentDescription>\n";
        assertEquals(expected, out.toString());

        ComponentDescription pd = MDMarshaller.unmarshal(ComponentDescription.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertEquals(desc.getId(), pd.getId());
    }

}
