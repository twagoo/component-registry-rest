package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import clarin.cmdi.componentregistry.model.ProfileDescription;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test class for CommentValidator
 * 
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class CommentValidatorTest extends BaseUnitTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MDMarshaller marshaller;

    @Before
    public void setUp() throws TransformerException {
	marshaller = new MDMarshaller();
	ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
    }

    /**
     * Fully successful comment validation
     */
    @Test
    public void testValidateSucces() {
	AbstractDescription description = ProfileDescription
		.createNewDescription();
	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>myCom</comments>\n";
	comContent += "    <commentDate></commentDate>\n";
	comContent += "     <profileDescriptionId>" + description.getId()
		+ "</profileDescriptionId>";
	comContent += "    <userId>0</userId>\n";
	comContent += "    <id>1</id>\n";
	comContent += "</comment>\n";
	InputStream input = new ByteArrayInputStream(comContent.getBytes());
	CommentValidator validator = new CommentValidator(input, description,
		marshaller);
	assertTrue(validator.validate());
    }

    /**
     * Test the validation of comment with componentID
     */
    @Test
    public void testValidateComponent() {
	AbstractDescription desc = ComponentDescription.createNewDescription();
	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>myCom</comments>\n";
	comContent += "    <commentDate></commentDate>\n";
	comContent += "     <componentDescriptionId></componentDescriptionId>";
	comContent += "    <userId>0</userId>\n";
	comContent += "    <id>1</id>\n";
	comContent += "</comment>\n";
	InputStream input = new ByteArrayInputStream(comContent.getBytes());
	CommentValidator validator = new CommentValidator(input, desc,
		marshaller);
	assertFalse(validator.validate());// missing componentId will return an
					  // error
	assertEquals(validator.getErrorMessages().size(), 1);
	assertTrue(validator.getErrorMessages().get(0)
		.startsWith(CommentValidator.COMMENT_SPECIFICATION_ERROR));

	String commentContent = "";
	commentContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	commentContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	commentContent += "    <comments>myCom</comments>\n";
	commentContent += "    <commentDate></commentDate>\n";
	commentContent += "     <componentDescriptionId>" + desc.getId()
		+ "</componentDescriptionId>";
	commentContent += "    <userId>0</userId>\n";
	commentContent += "    <id>1</id>\n";
	commentContent += "</comment>\n";
	input = new ByteArrayInputStream(commentContent.getBytes());
	validator = new CommentValidator(input, desc, marshaller);
	assertTrue(validator.validate()); // componentId valid, validation
					  // complete
    }

    /***
     * Test the validation of comment with profileId
     */
    @Test
    public void testValidateProfile() {
	AbstractDescription desc = ProfileDescription.createNewDescription();
	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>myCom</comments>\n";
	comContent += "    <commentDate></commentDate>\n";
	comContent += "     <profileDescriptionId></profileDescriptionId>";
	comContent += "    <userId>0</userId>\n";
	comContent += "    <id>1</id>\n";
	comContent += "</comment>\n";
	InputStream input = new ByteArrayInputStream(comContent.getBytes());
	CommentValidator validator = new CommentValidator(input, desc,
		marshaller);
	assertFalse(validator.validate());// missing profileId will return an
					  // error
	assertEquals(validator.getErrorMessages().size(), 1);
	assertTrue(validator.getErrorMessages().get(0)
		.startsWith(CommentValidator.COMMENT_SPECIFICATION_ERROR));

	String commentContent = "";
	commentContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	commentContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	commentContent += "    <comments>myCom</comments>\n";
	commentContent += "    <commentDate></commentDate>\n";
	commentContent += "     <profileDescriptionId>" + desc.getId()
		+ "</profileDescriptionId>";
	commentContent += "    <userId>0</userId>\n";
	commentContent += "    <id>1</id>\n";
	commentContent += "</comment>\n";
	input = new ByteArrayInputStream(commentContent.getBytes());
	validator = new CommentValidator(input, desc, marshaller);
	assertTrue(validator.validate()); // profileId valid, validation
					  // complete
    }

    /**
     * Test the validation of comment with content comments
     * 
     * @throws Exception
     */
    @Test
    public void testValidateNoCommentContent() throws Exception {
	AbstractDescription desc = ProfileDescription.createNewDescription();
	String commentContent = "";
	commentContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	commentContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	commentContent += "    <comments></comments>\n";
	commentContent += "    <commentDate></commentDate>\n";
	commentContent += "     <profileDescriptionId>" + desc.getId()
		+ "</profileDescriptionId>";
	commentContent += "    <userId>0</userId>\n";
	commentContent += "    <id>1</id>\n";
	commentContent += "</comment>\n";
	InputStream input = new ByteArrayInputStream(commentContent.getBytes());
	CommentValidator validator = new CommentValidator(input, desc,
		marshaller);
	assertFalse(validator.validate()); // missing content will return an
					   // error
	assertTrue(validator.getErrorMessages().get(0)
		.startsWith(CommentValidator.COMMENT_SPECIFICATION_ERROR));

	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>myCom</comments>\n";
	comContent += "    <commentDate></commentDate>\n";
	comContent += "     <profileDescriptionId>" + desc.getId()
		+ "</profileDescriptionId>";
	comContent += "    <userId>0</userId>\n";
	comContent += "    <id>1</id>\n";
	comContent += "</comment>\n";
	input = new ByteArrayInputStream(comContent.getBytes());
	validator = new CommentValidator(input, desc, marshaller);
	assertTrue(validator.validate());// content comments is fill in,
					 // validation complete

    }
}
