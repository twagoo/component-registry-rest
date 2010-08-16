package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.model.UserMapping.User;

public class UserMappingTest {

    @Test
    public void testUserMapping() throws Exception {
        UserMapping users = new UserMapping();
        User user = new UserMapping.User();
        user.setName("name");
        user.setUserDir("dir");
        user.setPrincipalName("a@b.com");
        users.addUsers(user);
        User user2 = new UserMapping.User();
        user2.setName("name2");
        user2.setUserDir("dir2");
        user2.setPrincipalName("a2@b.com");
        users.addUsers(user2);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(users, out);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<users xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        expected += "    <user>\n";
        expected += "        <name>name</name>\n";
        expected += "        <userDir>dir</userDir>\n";
        expected += "        <principalName>a@b.com</principalName>\n";
        expected += "    </user>\n";
        expected += "    <user>\n";
        expected += "        <name>name2</name>\n";
        expected += "        <userDir>dir2</userDir>\n";
        expected += "        <principalName>a2@b.com</principalName>\n";
        expected += "    </user>\n";
        expected += "</users>\n";
        assertEquals(expected, out.toString());

        UserMapping mapping = MDMarshaller.unmarshal(UserMapping.class, IOUtils.toInputStream(expected), null);
        assertEquals(2, mapping.getUsers().size());
    }

}
