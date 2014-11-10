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

    public Owner getRegistryOwner();

    public void setRegistryOwner(Owner registryOwner);

    public RegistrySpace getRegistrySpace();

    public void setRegistrySpace(RegistrySpace registrySpace);

    public Number getGroupId();

    public void setGroupId(Number groupId);

    public String getGroupName(Number groupId) throws ItemNotFoundException;

    public Number getBaseDescriptionOwnerId(String cmdId);

    public List<Number> getItemGroups(String cmdId);

    /**
     * @return List of component descriptions ordered by name ascending
     * @throws ComponentRegistryException
     */
    List<ComponentDescription> getComponentDescriptions() throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException;

    ComponentDescription getComponentDescriptionAccessControlled(String id) throws ComponentRegistryException, UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

    /**
     *
     * @return List of profile descriptions ordered by name ascending
     * @throws ComponentRegistryException
     */
    List<ProfileDescription> getProfileDescriptions() throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException;

    /**
     *
     * @return List of profile descriptions ordered by name ascending, only the
     * ones marked for showing in metadata editor
     * @throws ComponentRegistryException
     */
    List<ProfileDescription> getProfileDescriptionsForMetadaEditor() throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException;

    /**
     * @param groupId
     * @return List of profile descriptions ordered by name ascending, only the
     * ones marked for showing in metadata editor
     * @throws ComponentRegistryException
     */
    List<ProfileDescription> getProfileDescriptionsForMetadaEditor(Number groupId) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException;

    ProfileDescription getProfileDescriptionAccessControlled(String id) throws ComponentRegistryException, UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

    CMDComponentSpec getMDProfileAccessControled(String id) throws ComponentRegistryException, UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

    CMDComponentSpec getMDComponentAccessControlled(String id) throws ComponentRegistryException, UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

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
    int registerComment(Comment comment, String userId) throws ComponentRegistryException, ItemNotFoundException, UserUnauthorizedException, AuthenticationRequiredException;

    /**
     *
     * @return -1 if component could not be updated
     */
    int update(BaseDescription description, CMDComponentSpec spec, boolean forceUpdate) throws UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

    /**
     *
     * @return -1 if component could not be published. Published means move from
     * current (private) workspace to public workspace.
     */
    int publish(BaseDescription desc, CMDComponentSpec spec, Principal principal) throws UserUnauthorizedException,  AuthenticationRequiredException, ItemNotFoundException;

    void getMDProfileAsXml(String profileId, OutputStream output) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException;

    void getMDProfileAsXsd(String profileId, OutputStream outputStream) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException;

    void getMDComponentAsXml(String componentId, OutputStream output) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException;

    void getMDComponentAsXsd(String componentId, OutputStream outputStream) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException;

    /**
     *
     * @param profileIds
     * @throws IOException
     * @throws UserUnauthorizedException thrown when principal does not match
     * creator of profile
     * @throws DeleteFailedException
     */
    void deleteMDProfile(String profileId) throws IOException, UserUnauthorizedException, AuthenticationRequiredException, ComponentRegistryException, DeleteFailedException, ItemNotFoundException;

    /**
     *
     * @param componentId
     * @param principal
     * @param forceDelete ignores the fact that the component is still in use by
     * other components and just deletes.
     * @throws IOException
     * @throws UserUnauthorizedException thrown when principal does not match
     * creator of component
     * @throws DeleteFailedException
     */
    void deleteMDComponent(String componentId, boolean forceDelete) throws IOException, ComponentRegistryException, UserUnauthorizedException, AuthenticationRequiredException,
            DeleteFailedException, ItemNotFoundException;

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
     * @return {@link ComponentRegistry.PUBLIC_NAME} or name of the user's
     * workspace
     */
    String getName();

    /**
     *
     * @return List of profile descriptions ordered by name ascending
     */
    List<ProfileDescription> getDeletedProfileDescriptions() throws ComponentRegistryException;

    /**
     *
     * @return List of component descriptions ordered by name ascending
     */
    List<ComponentDescription> getDeletedComponentDescriptions() throws ComponentRegistryException;

    /**
     *
     * @param profileId
     * @return List of comments fro a specific profile
     * @throws ComponentRegistryException
     */
    List<Comment> getCommentsInProfile(String profileId) throws ComponentRegistryException, UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

    /**
     *
     * @param profileId Id of profile to retrieve comment from
     * @param commentId Id of comment to retrieve
     * @return a specific comment from a profile
     * @throws ComponentRegistryException
     */
    Comment getSpecifiedCommentInProfile(String profileId, String commentId) throws ComponentRegistryException, UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

    /**
     *
     * @param componentId
     * @return List of comments from a specific component
     * @throws ComponentRegistryException
     */
    List<Comment> getCommentsInComponent(String componentId) throws ComponentRegistryException, UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

    /**
     *
     * @param componentId Id of component to retrieve comment from
     * @param commentId Id of comment to retrieve
     * @return a specific comment from a component
     * @throws ComponentRegistryException
     */
    Comment getSpecifiedCommentInComponent(String componentId, String commentId) throws ComponentRegistryException, UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

    /**
     *
     * @param commentId Id of comment to delete
     * @param principal User principal
     * @throws IOException
     * @throws ComponentRegistryException When comment cannot be successfully
     * retrieved from registry
     * @throws UserUnauthorizedException
     * @throws DeleteFailedException
     */
    public void deleteComment(String commentId) throws IOException, UserUnauthorizedException, AuthenticationRequiredException,
            DeleteFailedException, ItemNotFoundException;

    /**
     *
     * @return a component specification expander for this registry
     */
    public CMDComponentSpecExpander getExpander();

    /**
     * Get a list of ids ({@link BaseDescription#getId()}) of all non-deleted
     * profiles
     *
     * @return
     */
    List<String> getAllNonDeletedProfileIds();

    /**
     * Get a list of ids ({@link BaseDescription#getId()}) of all non-deleted
     * components
     *
     * @return
     */
    List<String> getAllNonDeletedComponentIds();

    Boolean isItemPublic(String id) throws ItemNotFoundException;

    Number makeGroupMember(String principalName, String groupName) throws UserUnauthorizedException, ItemNotFoundException;

    //long removeGroupMember(String principalName, String groupName) throws  UserUnauthorizedException, ItemNotFoundException;
}
