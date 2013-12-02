package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import clarin.cmdi.componentregistry.MDMarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.transform.TransformerException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public class CommentResponseTest {

    private MDMarshaller marshaller;

    @Before
    public void setUp() throws TransformerException {
	marshaller = new MDMarshaller();
    }

    /**
     * Test with no validate attribute should return errors
     *
     * @throws Exception
     */
    @Test
    public void testRegisterError() throws Exception {
	CommentResponse resp = new CommentResponse();
	resp.setRegistered(false);
	resp.setIsInUserSpace(true);
	resp.addError("Error 1");
	resp.addError("Error 2, <!-- to be escaped -->");
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	marshaller.marshal(resp, out);
	String expected = "";
	expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	expected += "<commentResponse registered=\"false\" isInUserSpace=\"true\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	expected += "    <errors>\n";
	expected += "        <error>Error 1</error>\n";
	expected += "        <error>Error 2, &lt;!-- to be escaped --&gt;</error>\n";
	expected += "    </errors>\n";
	expected += "</commentResponse>\n";
	assertEquals(expected, out.toString());

	CommentResponse rr = marshaller.unmarshal(CommentResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
	assertFalse(rr.isRegistered());
	assertEquals(2, rr.getErrors().size());
    }

    /**
     * Test successfully processed
     *
     * @throws Exception
     */
    @Test
    public void testRegisterSucces() throws Exception {
	CommentResponse resp = new CommentResponse();
	resp.setRegistered(true);
	resp.setIsInUserSpace(false);
	resp.setComment(getComment());
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	marshaller.marshal(resp, out);
	String expected = "";
	expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	expected += "<commentResponse registered=\"true\" isInUserSpace=\"false\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	expected += "    <errors/>\n";
	expected += "    <comment>\n";
	expected += "        <comments>Name</comments>\n";
	expected += "        <commentDate>myDate</commentDate>\n";
	expected += "        <profileDescriptionId>myD</profileDescriptionId>\n";
	expected += "        <id>myId</id>\n";
	expected += "        <userName>J. Unit</userName>\n";
	expected += "        <canDelete>false</canDelete>\n";
	expected += "    </comment>\n";
	expected += "</commentResponse>\n";
	assertEquals(expected, out.toString());

	CommentResponse rr = marshaller.unmarshal(CommentResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
	assertTrue(rr.isRegistered());
	assertEquals("myId", rr.getComment().getId());
    }

    private Comment getComment() {
	Comment com = new Comment();
	com.setComment("Name");
	com.setId("myId");
	com.setUserId("myU");
	com.setProfileDescriptionId("myD");
	com.setCommentDate("myDate");
	com.setUserName("J. Unit");
	return com;
    }
}
