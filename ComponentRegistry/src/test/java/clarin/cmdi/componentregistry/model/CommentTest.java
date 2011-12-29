package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;

import clarin.cmdi.componentregistry.MDMarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Test;

/**
 *
 * @author jeafer
 */
public class CommentTest {

    @Test
    public void testProfileComment() throws Exception {
        Comment comment1 = new Comment();
        comment1.setCommentDate("myDate");
        comment1.setComment("tester");
        comment1.setId("1");
        comment1.setProfileDescriptionId("clarin.eu:cr1:p_1297242111880");
        comment1.setUserId("8");
	comment1.setUserName("J. Unit");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(comment1, out);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        expected += "    <comments>tester</comments>\n";
        expected += "    <commentDate>myDate</commentDate>\n";
        expected += "    <profileDescriptionId>clarin.eu:cr1:p_1297242111880</profileDescriptionId>\n";
        expected += "    <id>1</id>\n";
        expected += "    <userName>J. Unit</userName>\n";
        expected += "</comment>\n";
        assertEquals(expected, out.toString());

        Comment comment = MDMarshaller.unmarshal(Comment.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertEquals(comment1.getId(), comment.getId());
	assertEquals(comment1.getProfileDescriptionId(), comment.getProfileDescriptionId());
    }
    
    @Test
    public void testComponentComment() throws Exception {
        Comment comment1 = new Comment();
        comment1.setCommentDate("myDate");
        comment1.setComment("tester");
        comment1.setId("1");
        comment1.setComponentDescriptionId("clarin.eu:cr1:c_1297242111880");
        comment1.setUserId("8");
	comment1.setUserName("J. Unit");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MDMarshaller.marshal(comment1, out);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        expected += "    <comments>tester</comments>\n";
        expected += "    <commentDate>myDate</commentDate>\n";
        expected += "    <componentDescriptionId>clarin.eu:cr1:c_1297242111880</componentDescriptionId>\n";
        expected += "    <id>1</id>\n";
        expected += "    <userName>J. Unit</userName>\n";
        expected += "</comment>\n";
        assertEquals(expected, out.toString());

        Comment comment = MDMarshaller.unmarshal(Comment.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertEquals(comment1.getId(), comment.getId());
	assertEquals(comment1.getComponentDescriptionId(), comment.getComponentDescriptionId());
    }
}
