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
        resp.setProfileDescription(getProfileDescription());
        Writer writer = new StringWriter();
        MDMarshaller.marshal(resp, writer);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<registerResponse registered=\"true\">\n";
        expected += "    <errors/>\n";
        expected += "    <profileDescription>\n";
        expected += "        <id>myId</id>\n";
        expected += "        <description>myD</description>\n";
        expected += "        <name>Name</name>\n";
        expected += "        <registrationDate>myDate</registrationDate>\n";
        expected += "        <creatorName>myC</creatorName>\n";
        expected += "        <xlink>linkToMyProfile</xlink>\n";
        expected += "    </profileDescription>\n";
        expected += "</registerResponse>\n";
        assertEquals(expected, writer.toString());

        RegisterResponse rr = MDMarshaller.unmarshal(RegisterResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertTrue(rr.isRegistered());
        assertEquals("myId", rr.getProfileDescription().getId());
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
