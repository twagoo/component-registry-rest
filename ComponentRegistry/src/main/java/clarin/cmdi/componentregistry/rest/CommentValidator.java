package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.model.AbstractDescription;
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

    static final String COMMENT_NOT_REGISTERED_ERROR = "referenced comment is not registered or does not have a correct commentId: ";
    static final String PARSE_ERROR = "Error in validation input file. Error is: ";
    static final String COMMENT_NOT_PUBLICLY_REGISTERED_ERROR = "referenced comment cannot be found in the published comments: ";
    static final String COMPONENT_REGISTRY_EXCEPTION_ERROR = "An exception occurred while accessing the component registry: ";
    static final String ILLEGAL_ATTRIBUTE_NAME_ERROR = "Illegal attribute name: ";
    static final Collection<String> ILLEGAL_ATTRIBUTE_NAMES = Collections.unmodifiableCollection(Arrays.asList("ref", "CommentId"));
    private List<String> errorMessages = new ArrayList<String>();
    private Comment com = null;
    private final InputStream input;
    private final AbstractDescription description;

    /*
     * @param input In order to validate the input is consumed. So use @see getCommentSpec to get the parsed CommentSpec.
     * @param description use to validate the comment with the appropriate description (profile or a component)
     */
    public CommentValidator(InputStream input, AbstractDescription description) {
        this.input = input;
        this.description = description;
    }

    @Override
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    @Override
    public boolean validate() {
        try {
            com = MDMarshaller.unmarshal(Comment.class, input, null);
        } catch (JAXBException e) {
            errorMessages.add(PARSE_ERROR + e);
        }
        if (errorMessages.isEmpty()) {
            try {
                validateComment(com);
            } catch (ComponentRegistryException e) {
                errorMessages.add(COMPONENT_REGISTRY_EXCEPTION_ERROR + e);
            }
        }
        return errorMessages.isEmpty();
    }

    /*
     * Method that check wether a comment is valid (good attributes)
     * @param comment, the comment to be validated
     */
    private void validateComment(Comment comment) throws ComponentRegistryException {
        if (comment.getComment() == null | comment.getComment().isEmpty()) {
            errorMessages.add(ILLEGAL_ATTRIBUTE_NAME_ERROR + "comment has to be filled in");
        }
        if (description.isProfile()) {
            if (comment.getProfileDescriptionId() == null | comment.getProfileDescriptionId().isEmpty()) {
                errorMessages.add(ILLEGAL_ATTRIBUTE_NAME_ERROR + "profileId could not be found");
            }
        } else {
            if (comment.getComponentDescriptionId() == null | comment.getComponentDescriptionId().isEmpty()) {
                errorMessages.add(ILLEGAL_ATTRIBUTE_NAME_ERROR + "componentId could not be found");
            }
        }
    }

    public Comment getCommentSpec() {
        return com;
    }
}
