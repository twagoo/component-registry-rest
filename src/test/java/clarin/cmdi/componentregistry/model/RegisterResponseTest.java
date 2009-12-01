package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import clarin.cmdi.componentregistry.MDMarshaller;

public class RegisterResponseTest {

    @Test
    public void testRegisterError() throws JAXBException {
        RegisterResponse resp = new RegisterResponse();
        resp.setRegistered(false);
        resp.addError("Error 1");
        resp.addError("Error 2, <!-- to be escaped -->");
        Writer writer = new StringWriter();
        MDMarshaller.marshal(resp, writer);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<registerResponse registered=\"false\">\n";
        expected += "    <errors>\n";
        expected += "        <error>Error 1</error>\n";
        expected += "        <error>Error 2, &lt;!-- to be escaped --&gt;</error>\n";
        expected += "    </errors>\n";
        expected += "</registerResponse>\n";
        assertEquals(expected, writer.toString());

        RegisterResponse rr = MDMarshaller.unmarshal(RegisterResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertFalse(rr.isRegistered());
        assertEquals(2, rr.getErrors().size());
    }

    @Test
    public void testRegisterSucces() throws JAXBException {
        RegisterResponse resp = new RegisterResponse();
        resp.setRegistered(true);
        resp.setDescription(getProfileDescription());
        Writer writer = new StringWriter();
        MDMarshaller.marshal(resp, writer);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<registerResponse registered=\"true\">\n";
        expected += "    <errors/>\n";
        expected += "    <description xsi:type=\"profileDescription\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
        expected += "        <id>myId</id>\n";
        expected += "        <description>myD</description>\n";
        expected += "        <name>Name</name>\n";
        expected += "        <registrationDate>myDate</registrationDate>\n";
        expected += "        <creatorName>myC</creatorName>\n";
        expected += "        <xlink>linkToMyProfile</xlink>\n";
        expected += "    </description>\n";
        expected += "</registerResponse>\n";
        assertEquals(expected, writer.toString());

        RegisterResponse rr = MDMarshaller.unmarshal(RegisterResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertTrue(rr.isRegistered());
        assertEquals("myId", rr.getDescription().getId());
    }

    @Test
    public void testRegisterSuccesComponent() throws JAXBException {
        RegisterResponse resp = new RegisterResponse();
        resp.setRegistered(true);
        resp.setDescription(getComponentDescription());
        Writer writer = new StringWriter();
        MDMarshaller.marshal(resp, writer);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<registerResponse registered=\"true\">\n";
        expected += "    <errors/>\n";
        expected += "    <description xsi:type=\"componentDescription\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
        expected += "        <id>myId</id>\n";
        expected += "        <description>myD</description>\n";
        expected += "        <name>Name</name>\n";
        expected += "        <registrationDate>myDate</registrationDate>\n";
        expected += "        <creatorName>myC</creatorName>\n";
        expected += "        <xlink>linkToMyProfile</xlink>\n";
        expected += "        <groupName>imdi</groupName>\n";
        expected += "    </description>\n";
        expected += "</registerResponse>\n";
        assertEquals(expected, writer.toString());

        RegisterResponse rr = MDMarshaller.unmarshal(RegisterResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertTrue(rr.isRegistered());
        assertEquals("myId", rr.getDescription().getId());
    }

    private ComponentDescription getComponentDescription() {
        ComponentDescription desc = new ComponentDescription();
        desc.setName("Name");
        desc.setId("myId");
        desc.setGroupName("imdi");
        desc.setCreatorName("myC");
        desc.setDescription("myD");
        desc.setRegistrationDate("myDate");
        desc.setXlink("linkToMyProfile");
        return desc;
    }

    private ProfileDescription getProfileDescription() {
        ProfileDescription desc = new ProfileDescription();
        desc.setName("Name");
        desc.setId("myId");
        desc.setCreatorName("myC");
        desc.setDescription("myD");
        desc.setRegistrationDate("myDate");
        desc.setXlink("linkToMyProfile");
        return desc;
    }
}
