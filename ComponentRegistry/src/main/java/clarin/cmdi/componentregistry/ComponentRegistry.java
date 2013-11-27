package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;

/**
 * @author twagoo@mpi.nl
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface ComponentRegistry {

    // Attention! REGISTRY_ID here and the client's Config.REGISTRY_ID must be the same 
    // If you change REGISTRY_ID here, then change the client's  Config.REGISTRY_ID
    public static final String REGISTRY_ID = "clarin.eu:cr1:";
    public static final String PUBLIC_NAME = "Public Registry";

    /**
     * @return List of component descriptions ordered by name ascending
     * @throws ComponentRegistryException
     */
    List<ComponentDescription> getComponentDescriptions() throws ComponentRegistryException;

    /**
     * @param principalName
     * @param groupId
     * @return List of component descriptions ordered by name ascending
     * @throws ComponentRegistryException
     */
    List<ComponentDescription> getComponentDescriptionsInGroup(String principalName, String groupId) throws ComponentRegistryException;

    ComponentDescription getComponentDescription(String id) throws ComponentRegistryException;

    /**
     *
     * @return List of profile descriptions ordered by name ascending
     * @throws ComponentRegistryException
     */
    List<ProfileDescription> getProfileDescriptions() throws ComponentRegistryException;

    /**
    * @param groupId
    * @return List of profile descriptions ordered by name ascending
    * @throws ComponentRegistryException
    */
   List<ProfileDescription> getProfileDescriptionsInGroup(String groupId) throws ComponentRegistryException;

   /**
     *
     * @return List of profile descriptions ordered by name ascending, only the ones marked for showing in metadata editor
     * @throws ComponentRegistryException
     */
    List<ProfileDescription> getProfileDescriptionsForMetadaEditor() throws ComponentRegistryException;

    /**
    * @param groupId
    * @return List of profile descriptions ordered by name ascending, only the ones marked for showing in metadata editor
    * @throws ComponentRegistryException
    */
   List<ProfileDescription> getProfileDescriptionsForMetadaEditor(String groupId) throws ComponentRegistryException;

   ProfileDescription getProfileDescription(String id) throws ComponentRegistryException;

    CMDComponentSpec getMDProfile(String id) throws ComponentRegistryException;

    CMDComponentSpec getMDComponent(String id) throws ComponentRegistryException;

    /**
     *
     * @return -1 if profile could not be registered
     */
    int register(BaseDescription desc, CMDComponentSpec spec);

    /**
     *
     * @param comment
     * @return -1 if comment could not be registered
     */
    int registerComment(Comment comment, String userId) throws ComponentRegistryException;

    /**
     *
     * @return -1 if component could not be updated
     */
    int update(BaseDescription description, CMDComponentSpec spec, Principal principal, boolean forceUpdate);

    /**
     *
     * @return -1 if component could not be published. Published means move from
     * current (private) workspace to public workspace.
     */
    int publish(BaseDescription desc, CMDComponentSpec spec, Principal principal);

    void getMDProfileAsXml(String profileId, OutputStream output) throws ComponentRegistryException;

    void getMDProfileAsXsd(String profileId, OutputStream outputStream) throws ComponentRegistryException;

    void getMDComponentAsXml(String componentId, OutputStream output) throws ComponentRegistryException;

    void getMDComponentAsXsd(String componentId, OutputStream outputStream) throws ComponentRegistryException;

    /**
     *
     * @param profileId
     * @param principal
     * @throws IOException
     * @throws UserUnauthorizedException
     * thrown when principal does not match creator of profile
     * @throws DeleteFailedException
     */
    void deleteMDProfile(String profileId, Principal principal) throws IOException, UserUnauthorizedException, ComponentRegistryException, DeleteFailedException;

    /**
     *
     * @param componentId
     * @param principal
     * @param forceDelete
     * ignores the fact that the component is still in use by other
     * components and just deletes.
     * @throws IOException
     * @throws UserUnauthorizedException
     * thrown when principal does not match creator of component
     * @throws DeleteFailedException
     */
    void deleteMDComponent(String componentId, Principal principal, boolean forceDelete) throws IOException, ComponentRegistryException, UserUnauthorizedException,
	    DeleteFailedException;

    /**
     *
     * @param componentId
     * @return List of ComponentDescriptions of Components that use the given
     * Component.
     */
    List<ComponentDescription> getUsageInComponents(String componentId) throws ComponentRegistryException;

    /**
     *
     * @param componentId
     * @return List of ProfileDescriptions of Profiles that use the given
     * Component.
     */
    List<ProfileDescription> getUsageInProfiles(String componentId) throws ComponentRegistryException;

    /**
     * Return true if this registry is the public registry as opposed to a
     * registry used for the user privately.
     * */
    boolean isPublic();

    ComponentStatus getStatus();

    Owner getOwner();

    /**
     * @return {@link ComponentRegistry.PUBLIC_NAME} or name of the user's workspace
     */
    String getName();

    /**
     *
     * @return List of profile descriptions ordered by name ascending
     */
    List<ProfileDescription> getDeletedProfileDescriptions();

    /**
     *
     * @return List of component descriptions ordered by name ascending
     */
    List<ComponentDescription> getDeletedComponentDescriptions();

    /**
     *
     * @param profileId
     * @return List of comments fro a specific profile
     * @throws ComponentRegistryException
     */
    List<Comment> getCommentsInProfile(String profileId, Principal principal) throws ComponentRegistryException;

    /**
     *
     * @param profileId Id of profile to retrieve comment from
     * @param commentId Id of comment to retrieve
     * @return a specific comment from a profile
     * @throws ComponentRegistryException
     */
    Comment getSpecifiedCommentInProfile(String profileId, String commentId, Principal principal) throws ComponentRegistryException;

    /**
     *
     * @param componentId
     * @return List of comments from a specific component
     * @throws ComponentRegistryException
     */
    List<Comment> getCommentsInComponent(String componentId, Principal principal) throws ComponentRegistryException;

    /**
     *
     * @param componentId Id of component to retrieve comment from
     * @param commentId Id of comment to retrieve
     * @return a specific comment from a component
     * @throws ComponentRegistryException
     */
    Comment getSpecifiedCommentInComponent(String componentId, String commentId, Principal principal) throws ComponentRegistryException;

    /**
     *
     * @param commentId Id of comment to delete
     * @param principal User principal
     * @throws IOException
     * @throws ComponentRegistryException When comment cannot be successfully retrieved from registry
     * @throws UserUnauthorizedException
     * @throws DeleteFailedException
     */
    public void deleteComment(String commentId, Principal principal) throws IOException, ComponentRegistryException, UserUnauthorizedException,
	    DeleteFailedException;

    /**
     *
     * @return a component specification expander for this registry
     */
    public CMDComponentSpecExpander getExpander();
    
    /**
     * Get a list of ids ({@link BaseDescription#getId()}) of all non-deleted profiles
     * @return
     */
    List<String> getAllNonDeletedProfileIds();

    /**
     * Get a list of ids ({@link BaseDescription#getId()}) of all non-deleted components
     * @return
     */
    List<String> getAllNonDeletedComponentIds();
}
