package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import clarin.cmdi.componentregistry.MDMarshaller;

public class RegisterResponseTest {

    @Test
    public void testRegisterError() throws Exception {
        RegisterResponse resp = new RegisterResponse();
        resp.setRegistered(false);
        resp.setIsProfile(true);
        resp.addError("Error 1");
        resp.addError("Error 2, <!-- to be escaped -->");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(resp, out);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<registerResponse registered=\"false\" isProfile=\"true\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        expected += "    <errors>\n";
        expected += "        <error>Error 1</error>\n";
        expected += "        <error>Error 2, &lt;!-- to be escaped --&gt;</error>\n";
        expected += "    </errors>\n";
        expected += "</registerResponse>\n";
        assertEquals(expected, out.toString());

        RegisterResponse rr = MDMarshaller.unmarshal(RegisterResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertFalse(rr.isRegistered());
        assertTrue(rr.isProfile());
        assertEquals(2, rr.getErrors().size());
    }

    @Test
    public void testRegisterSucces() throws Exception {
        RegisterResponse resp = new RegisterResponse();
        resp.setRegistered(true);
        resp.setIsProfile(true);
        resp.setIsInUserSpace(false);
        resp.setDescription(getProfileDescription());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(resp, out);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<registerResponse registered=\"true\" isProfile=\"true\" isInUserSpace=\"false\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        expected += "    <errors/>\n";
        expected += "    <description xsi:type=\"profileDescription\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
        expected += "        <id>myId</id>\n";
        expected += "        <description>myD</description>\n";
        expected += "        <name>Name</name>\n";
        expected += "        <registrationDate>myDate</registrationDate>\n";
        expected += "        <creatorName>myC</creatorName>\n";
        expected += "        <ns2:href>linkToMyProfile</ns2:href>\n";
        expected += "    </description>\n";
        expected += "</registerResponse>\n";
        assertEquals(expected, out.toString());

        RegisterResponse rr = MDMarshaller.unmarshal(RegisterResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertTrue(rr.isRegistered());
        assertTrue(rr.isProfile());
        assertEquals("myId", rr.getDescription().getId());
    }

    @Test
    public void testRegisterSuccesComponent() throws Exception {
        RegisterResponse resp = new RegisterResponse();
        resp.setRegistered(true);
        resp.setIsProfile(false);
        resp.setIsInUserSpace(true);
        resp.setDescription(getComponentDescription());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(resp, out);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<registerResponse registered=\"true\" isProfile=\"false\" isInUserSpace=\"true\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        expected += "    <errors/>\n";
        expected += "    <description xsi:type=\"componentDescription\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
        expected += "        <id>myId</id>\n";
        expected += "        <description>myD</description>\n";
        expected += "        <name>Name</name>\n";
        expected += "        <registrationDate>myDate</registrationDate>\n";
        expected += "        <creatorName>myC</creatorName>\n";
        expected += "        <ns2:href>linkToMyProfile</ns2:href>\n";
        expected += "        <groupName>imdi</groupName>\n";
        expected += "    </description>\n";
        expected += "</registerResponse>\n";
        assertEquals(expected, out.toString());

        RegisterResponse rr = MDMarshaller.unmarshal(RegisterResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertTrue(rr.isRegistered());
        assertFalse(rr.isProfile());
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
        desc.setHref("linkToMyProfile");
        return desc;
    }

    private ProfileDescription getProfileDescription() {
        ProfileDescription desc = new ProfileDescription();
        desc.setName("Name");
        desc.setId("myId");
        desc.setCreatorName("myC");
        desc.setDescription("myD");
        desc.setRegistrationDate("myDate");
        desc.setHref("linkToMyProfile");
        return desc;
    }
}
