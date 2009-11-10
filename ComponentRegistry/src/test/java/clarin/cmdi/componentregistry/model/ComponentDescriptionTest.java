package clarin.cmdi.componentregistry.model;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import clarin.cmdi.componentregistry.MDMarshaller;

public class ComponentDescriptionTest {

    @Test
    public void testComponentToXml() throws JAXBException {
        ComponentDescription desc = new ComponentDescription();
        desc.setName("Name");
        desc.setId("myId");
        desc.setCreatorName("myC");
        desc.setDescription("myD");
        desc.setRegistrationDate("myDate");
        desc.setXlink("linkToMyComponent");
        desc.setGroupName("MyGroup");

        Writer writer = new StringWriter();
        MDMarshaller.marshal(desc, writer);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<componentDescription>\n";
        expected += "    <id>myId</id>\n";
        expected += "    <description>myD</description>\n";
        expected += "    <name>Name</name>\n";
        expected += "    <registrationDate>myDate</registrationDate>\n";
        expected += "    <creatorName>myC</creatorName>\n";
        expected += "    <xlink>linkToMyComponent</xlink>\n";
        expected += "    <groupName>MyGroup</groupName>\n";
        expected += "</componentDescription>\n";
        assertEquals(expected, writer.toString());

        ComponentDescription pd = MDMarshaller.unmarshal(ComponentDescription.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertEquals(desc.getId(), pd.getId());
    }

}
