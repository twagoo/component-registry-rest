package clarin.cmdi.componentregistry.model;

import clarin.cmdi.componentregistry.DatesHelper;
import clarin.cmdi.componentregistry.MDMarshaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author jeafer
 */
public class CommentTest {

    private MDMarshaller marshaller;

    @Before
    public void setUp() throws TransformerException {
	marshaller = new MDMarshaller();
    }

    @Test
    public void testProfileComment() throws Exception {
	Comment comment1 = new Comment();
	Date testDate = new Date();
	comment1.setCommentDate(testDate);
	comment1.setComment("tester");
	comment1.setId("1");
	comment1.setComponentId("clarin.eu:cr1:p_1297242111880");
	comment1.setUserId(8);
	comment1.setUserName("J. Unit");

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	marshaller.marshal(comment1, out);
	String expected = "";
	expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	expected += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	expected += "    <comments>tester</comments>\n";
	expected += "    <commentDate>"+DatesHelper.formatXmlDateTime(testDate)+"</commentDate>\n";
	expected += "    <componentId>clarin.eu:cr1:p_1297242111880</componentId>\n";
	expected += "    <id>1</id>\n";
	expected += "    <userName>J. Unit</userName>\n";
	expected += "    <canDelete>false</canDelete>\n";
	expected += "</comment>\n";
	assertEquals(expected, out.toString());

	Comment comment = marshaller.unmarshal(Comment.class, new ByteArrayInputStream(expected.getBytes()), null);
	assertEquals(comment1.getId(), comment.getId());
	assertEquals(comment1.getComponentId(), comment.getComponentId());
    }

    @Test
    public void testComponentComment() throws Exception {
	Comment comment1 = new Comment();
	Date testDate = new Date();
	comment1.setCommentDate(testDate);
	comment1.setComment("tester");
	comment1.setId("1");
	comment1.setComponentId("clarin.eu:cr1:c_1297242111880");
	comment1.setUserId(8);
	comment1.setUserName("J. Unit");

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	marshaller.marshal(comment1, out);
	String expected = "";
	expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	expected += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	expected += "    <comments>tester</comments>\n";
	expected += "    <commentDate>"+DatesHelper.formatXmlDateTime(testDate)+"</commentDate>\n";
	expected += "    <componentId>clarin.eu:cr1:c_1297242111880</componentId>\n";
	expected += "    <id>1</id>\n";
	expected += "    <userName>J. Unit</userName>\n";
	expected += "    <canDelete>false</canDelete>\n";
	expected += "</comment>\n";
	assertEquals(expected, out.toString());

	Comment comment = marshaller.unmarshal(Comment.class, new ByteArrayInputStream(expected.getBytes()), null);
	assertEquals(comment1.getId(), comment.getId());
	assertEquals(comment1.getComponentId(), comment.getComponentId());
    }
}
