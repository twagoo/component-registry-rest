package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.validation.Validator;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;

/**
 * Implementation of Validator
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
class CommentValidator implements Validator {

    static final String PARSE_ERROR = "Error in validation input file. Error is: ";
    static final String COMMENT_SPECIFICATION_ERROR = "Illegal comment specification: ";
    static final Collection<String> ILLEGAL_ATTRIBUTE_NAMES = Collections.unmodifiableCollection(Arrays.asList("ref", "CommentId"));
    private List<String> errorMessages = new ArrayList<String>();
    private Comment com = null;
    private final InputStream input;
    private final BaseDescription description;
    private final MDMarshaller marshaller;

    /*
     * @param input In order to validate the input is consumed. So use @see getCommentSpec to get the parsed CommentSpec.
     * @param description use to validate the comment with the appropriate description (profile or a component)
     */
    public CommentValidator(InputStream input, BaseDescription description, MDMarshaller marshaller) {
	this.input = input;
	this.description = description;
	this.marshaller = marshaller;
    }

    @Override
    public List<String> getErrorMessages() {
	return errorMessages;
    }

    @Override
    public boolean validate() {
	try {
	    com = marshaller.unmarshal(Comment.class, input, null);
	} catch (JAXBException e) {
	    errorMessages.add(PARSE_ERROR + e);
	}
	if (errorMessages.isEmpty()) {
	    validateComment(com);
	}
	return errorMessages.isEmpty();
    }

    /*
     * Method that check wether a comment is valid (good attributes)
     * @param comment, the comment to be validated
     */
    private void validateComment(Comment comment) {
	if (comment.getComment() == null || comment.getComment().isEmpty()) {
	    errorMessages.add(COMMENT_SPECIFICATION_ERROR + "comment has to be filled in");
	}
	if (description.isProfile()) {
	    if (comment.getComponentRef() == null || comment.getComponentRef().isEmpty()) {
		errorMessages.add(COMMENT_SPECIFICATION_ERROR + "profileId could not be found");
	    }
	} else {
	    if (comment.getComponentRef() == null || comment.getComponentRef().isEmpty()) {
		errorMessages.add(COMMENT_SPECIFICATION_ERROR + "componentId could not be found");
	    }
	}
    }

    public Comment getCommentSpec() {
	return com;
    }
}
