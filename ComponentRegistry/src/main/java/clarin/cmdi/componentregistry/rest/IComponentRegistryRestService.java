package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.UserUnauthorizedException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.springframework.transaction.annotation.Transactional;

import clarin.cmdi.componentregistry.AllowedAttributetypesXML;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rss.Rss;

import com.sun.jersey.spi.resource.Singleton;

/**
 *
 * @author twago@mpi.nl
 * @author olsha@mpi.nl
 * @author george.georgovassilis@mpi.nl
 *
 */
@Path("/registry")
@Singleton
@Transactional(rollbackFor = Exception.class)
public interface IComponentRegistryRestService {

    public static final String APPLICATION_BASE_URL_PARAM = "eu.clarin.cmdi.componentregistry.serviceRootUrl";
    public static final String DATA_FORM_FIELD = "data";
    public static final String NAME_FORM_FIELD = "name";
    public static final String DESCRIPTION_FORM_FIELD = "description";
    public static final String GROUP_FORM_FIELD = "group";
    public static final String DOMAIN_FORM_FIELD = "domainName";
    public static final String REGISTRY_SPACE_PARAM = "registrySpace";
    public static final String GROUPID_PARAM = "groupid";
    public static final String METADATA_EDITOR_PARAM = "mdEditor";
    public static final String NUMBER_OF_RSSITEMS = "limit";

    List<ComponentDescription> getRegisteredComponents(String registrySpace, String groupId)
            throws ComponentRegistryException, IOException, UserUnauthorizedException;

    List<ProfileDescription> getRegisteredProfiles(String registrySpace, boolean metadataEditor, String groupId) throws ComponentRegistryException, IOException, UserUnauthorizedException;

    Response getRegisteredComponent(String componentId) throws IOException;

    Response getRegisteredProfile(String profileId) throws IOException;

    Response getRegisteredComponentRawType(String componentId, String rawType) throws ComponentRegistryException;

//	ComponentRegistry findRegistry(String id,
//			RegistryClosure<? extends BaseDescription> clos)
//			throws ComponentRegistryException;
//
//	
    List<BaseDescription> getComponentUsage(String componentId) throws ComponentRegistryException, IOException;

    List<Comment> getCommentsFromProfile(String profileId) throws ComponentRegistryException, IOException;

    List<Comment> getCommentsFromComponent(String componentId) throws ComponentRegistryException, IOException;

    Comment getSpecifiedCommentFromProfile(String profileId, String commentId) throws ComponentRegistryException, IOException;

    Comment getSpecifiedCommentFromComponent(String componentId, String commentId) throws ComponentRegistryException, IOException;

    /**
     *
     * Purely helper method for my front-end (FLEX) which only does post/get
     * requests. The query param is checked and the "proper" method is called.
     *
     * @param profileId
     * @param method
     * @return
     */
    Response manipulateRegisteredProfile(String profileId, String method);

    Response manipulateCommentFromProfile(String profileId, String commentId,
            String method);

    Response manipulateCommentFromComponent(String componentId,
            String commentId, String method);

    Response publishRegisteredProfile(String profileId, InputStream input,
            String name, String description, String group, String domainName);

    Response updateRegisteredProfile(String profileId,
            InputStream input, String name, String description, String groupId,
            String domainName);

    /**
     *
     * Purely helper method for my front-end (FLEX) which van only do post/get
     * requests. The query param is checked and the "proper" method is called.
     *
     * @param componentId
     * @param method
     * @return
     */
    Response manipulateRegisteredComponent(String componentId, String method);

    Response publishRegisteredComponent(String componentId, InputStream input,
            String name, String description, String groupId, String domainName);

    Response updateRegisteredComponent(String componentId,
            InputStream input, String name, String description, String groupId,
            String domainName);

    Response deleteRegisteredComponent(String componentId);

    Response deleteRegisteredProfile(String profileId);

    Response deleteCommentFromProfile(String profileId, String commentId);

    Response deleteCommentFromComponent(String componentId, String commentId);

    Response getRegisteredProfileRawType(String profileId, String rawType) throws ComponentRegistryException;

    Response registerProfile(InputStream input, String name,
            String description, String groupId, String domainName);

    Response registerComponent(InputStream input, String name,
            String description, String groupId, String domainName);

    Response registerCommentInComponent(InputStream input, String componentId) throws ComponentRegistryException;

    Response registerCommentInProfile(InputStream input, String profileId) throws ComponentRegistryException;

    Response pingSession();

    /**
     * @param componentRegistryFactory the componentRegistryFactory to set
     */
    void setComponentRegistryFactory(ComponentRegistryFactory componentRegistryFactory);

    Rss getRssComponent(String groupId, String registrySpace, String limit)
            throws ComponentRegistryException, ParseException, IOException;

    Rss getRssProfile(String groupId, String registrySpace, String limit)
            throws ComponentRegistryException, ParseException, IOException;

    Rss getRssOfCommentsFromProfile(String profileId, String limit) throws ComponentRegistryException,
            JAXBException, ParseException, IOException;

    Rss getRssOfCommentsFromComponent(String componentId,
            String limit) throws ComponentRegistryException, IOException,
            JAXBException, ParseException;

    AllowedAttributetypesXML getAllowedAttributeTypes()
            throws ComponentRegistryException, IOException, JAXBException,
            ParseException;

    void setFileNamesFromListToNull(List<CMDComponentType> listofcomponents);

  

    /**
     * Get any component (public or private) with the specified ID
     *
     * @param componentId
     * @return
     */
    BaseDescription getBaseDescription(String componentId) throws ComponentRegistryException, IOException;

    // Group Service 
    
     /**
     * Get a list of groups the user is a member of
     *
     * @return
     */
    List<Group> getGroupsTheCurrentUserIsAMemberOf();

   
    
     /**
     * Get a list of groups the item is available to. For all practical reasons,
     * this will return either 0 or 1 groups
     *
     * @param itemId ID of component or profile. This is not the DB id but the
     * logical ID which is unique across components and profiles
     * @return List of groups
     */
    List<Group> getGroupsTheItemIsAMemberOf(String itemId);

    /**
     * Transfer ownership of an item (group/profile) to a group
     *
     * @param itemId
     * @param groupId
     */
    Response transferItemOwnershipToGroup(String itemId, long groupId) throws IOException;
    
    // (added by Olha)
    
    Response createNewGroup(String groupName) throws IOException;

    List<Group> getGroupsOwnedByUser(String pricipalName) throws IOException;

    Response listGroupNames() throws IOException;

    Response isOwner(String groupName) throws IOException;

    Response makeGroupMember(String groupName, String principalName) throws IOException;
    
    //Response removeGroupMember(String groupName, String principalName) throws IOException;

    Response listProfiles(String groupId) throws IOException;

    Response listComponents(String groupId) throws IOException;

    Response getGroupNameById(String groupId) throws IOException;

    Response getGroupIdByName(String groupName) throws IOException;
}