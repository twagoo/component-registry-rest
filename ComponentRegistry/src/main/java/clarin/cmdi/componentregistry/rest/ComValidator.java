/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.AttributeListType.Attribute;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;

/**
 *
 * @author jeafer
 */
class ComValidator implements Validator {

    static final String MISMATCH_ERROR = "Cannot register component as a profile or vica versa.";
    static final String COMMENT_NOT_REGISTERED_ERROR = "referenced comment is not registered or does not have a correct commentId: ";
    static final String PARSE_ERROR = "Error in validation input file. Error is: ";
    static final String COMMENT_NOT_PUBLICLY_REGISTERED_ERROR = "referenced comment cannot be found in the published comments: ";
    static final String COMPONENT_REGISTRY_EXCEPTION_ERROR = "An exception occurred while accessing the component registry: ";
    static final String ILLEGAL_ATTRIBUTE_NAME_ERROR = "Illegal attribute name: ";
    static final Collection<String> ILLEGAL_ATTRIBUTE_NAMES = Collections.unmodifiableCollection(Arrays.asList("ref", "CommentId"));
    private List<String> errorMessages = new ArrayList<String>();
    private Comment spec = null;
    private final InputStream input;
    private final Comment comment;
    private final ComponentRegistry registry;
    private final ComponentRegistry userRegistry;
    private final AbstractDescription descriptionId;

    /**
     * 
     * @param input In order to validate the input is consumed. So use @see getCMDComponentSpec to get the parsed CMDComponentSpec.
     * @param desc
     * @param registry (registry you currently used) 
     * @param userRegistry can be null, We get user registry as well so we can give nice error messages if needed. Can be the same as @param registry
     */
    public ComValidator(InputStream input, Comment comment, ComponentRegistry registry, ComponentRegistry userRegistry, AbstractDescription descriptionId) {
        this.input = input;
        this.comment = comment;
        this.registry = registry;
        this.userRegistry = userRegistry;
        this.descriptionId = descriptionId;
    }

    @Override
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    @Override
    public boolean validate() {
        try {
            spec = MDMarshaller.unmarshal(Comment.class, input, null);
        } catch (JAXBException e) {
            errorMessages.add(PARSE_ERROR + e);
        }
        if (errorMessages.isEmpty()) {
            try {
                validateComment(spec.getComments());
            } catch (ComponentRegistryException e) {
                errorMessages.add(COMPONENT_REGISTRY_EXCEPTION_ERROR + e);
            }
        }
        return errorMessages.isEmpty();
    }

    private void validateComment(List<Comment> comment) throws ComponentRegistryException {
        for (Comment commentType : comment) {
            validateDescribedComments(commentType);
            validateComment(commentType.getComments());//Recursion
        }
    }

    private void validateDescribedComments(Comment comment) throws ComponentRegistryException {
        checkPublicComments(comment);
        checkLegalAttributes(comment);
    }

    private void checkPublicComments(Comment comment) throws ComponentRegistryException {
        if (isDefinedInSeparateFile(comment)) {
            String id = comment.getId();
            String profileId = comment.getProfileDescriptionId();
            String componentId = comment.getComponentDescriptionId();
            Comment registeredComment = null;
            if (descriptionId.isProfile()) {
                if (descriptionId != null) {
                    registeredComment = registry.getSpecifiedCommentInProfile(profileId);
                    if (registeredComment == null) {
                        String error = comment.getId();
                        errorMessages.add(COMMENT_NOT_PUBLICLY_REGISTERED_ERROR + error);
                    }
                }
            } else { //User registry, can link to components from public registry and the user's registry
                if (descriptionId != null) {
                    registeredComment = registry.getSpecifiedCommentInComponent(componentId);
                    if (registeredComment == null) {
                        errorMessages.add(COMMENT_NOT_REGISTERED_ERROR + comment.getId());
                    }

                }
            }
        }
    }

    private boolean isDefinedInSeparateFile(Comment comment) {
        return comment.getComment() == null;
    }

    private void checkLegalAttributes(Comment comment) {
        if (comment.getComment() == null) {
            errorMessages.add(ILLEGAL_ATTRIBUTE_NAME_ERROR + "comment has to be filled in");
        }
        if (descriptionId.isProfile()) {
            if (comment.getProfileDescriptionId() == null) {
                errorMessages.add(ILLEGAL_ATTRIBUTE_NAME_ERROR + "profileId could not be found");
            }
        } else {
            if (comment.getComponentDescriptionId() == null) {
                errorMessages.add(ILLEGAL_ATTRIBUTE_NAME_ERROR + "componentId could not be found");
            }
        }
    }
    
        public Comment getCommentSpec() {
	return spec;
    }
}
