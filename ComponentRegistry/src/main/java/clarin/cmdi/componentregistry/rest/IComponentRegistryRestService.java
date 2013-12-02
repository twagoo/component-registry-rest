package clarin.cmdi.componentregistry.rest;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.springframework.transaction.annotation.Transactional;

import clarin.cmdi.componentregistry.AllowedAttributetypesXML;
import clarin.cmdi.componentregistry.ComponentRegistry;
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
@Transactional(rollbackFor=Exception.class)
public interface IComponentRegistryRestService {

	public static final String APPLICATION_BASE_URL_PARAM = "eu.clarin.cmdi.componentregistry.serviceRootUrl";
	public static final String DATA_FORM_FIELD = "data";
	public static final String NAME_FORM_FIELD = "name";
	public static final String DESCRIPTION_FORM_FIELD = "description";
	public static final String GROUP_FORM_FIELD = "group";
	public static final String DOMAIN_FORM_FIELD = "domainName";
	public static final String USERSPACE_PARAM = "userspace";
	public static final String GROUPID_PARAM = "groupid";
	public static final String METADATA_EDITOR_PARAM = "mdEditor";
	public static final String NUMBER_OF_RSSITEMS = "limit";

	List<ComponentDescription> getRegisteredComponents(boolean userspace, String groupId)
			throws ComponentRegistryException;

	List<ProfileDescription> getRegisteredProfiles(boolean userspace,
			boolean metadataEditor, String groupId) throws ComponentRegistryException;

	Response getRegisteredComponent(String componentId, boolean userspace)
			throws ComponentRegistryException;

	Response getRegisteredComponentRawType(String componentId, String rawType);

	ComponentRegistry findRegistry(String id,
			RegistryClosure<? extends BaseDescription> clos)
			throws ComponentRegistryException;

	Response getRegisteredProfile(String profileId, boolean userspace)
			throws ComponentRegistryException;

	List<BaseDescription> getComponentUsage(String componentId,
			boolean userspace) throws ComponentRegistryException;

	List<Comment> getCommentsFromProfile(String profileId, boolean userspace)
			throws ComponentRegistryException;

	List<Comment> getCommentsFromComponent(String componentId, boolean userspace)
			throws ComponentRegistryException;

	Comment getSpecifiedCommentFromProfile(String profileId, String commentId,
			boolean userspace) throws ComponentRegistryException;

	Comment getSpecifiedCommentFromComponent(String componentId,
			String commentId, boolean userspace)
			throws ComponentRegistryException;

	/**
	 *
	 * Purely helper method for my front-end (FLEX) which only does post/get
	 * requests. The query param is checked and the "proper" method is called.
	 *
	 * @param profileId
	 * @param method
	 * @return
	 */
	Response manipulateRegisteredProfile(String profileId, String method,
			boolean userspace);

	Response manipulateCommentFromProfile(String profileId, String commentId,
			String method, boolean userspace);

	Response manipulateCommentFromComponent(String componentId,
			String commentId, String method, boolean userspace);

	Response publishRegisteredProfile(String profileId, InputStream input,
			String name, String description, String group, String domainName);

	Response updateRegisteredProfile(String profileId, boolean userspace,
			InputStream input, String name, String description, String group,
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
	Response manipulateRegisteredComponent(String componentId, String method,
			boolean userspace);

	Response publishRegisteredComponent(String componentId, InputStream input,
			String name, String description, String group, String domainName);

	Response updateRegisteredComponent(String componentId, boolean userspace,
			InputStream input, String name, String description, String group,
			String domainName);

	Response deleteRegisteredComponent(String componentId, boolean userspace);

	Response deleteRegisteredProfile(String profileId, boolean userspace);

	Response deleteCommentFromProfile(String profileId, String commentId,
			boolean userspace);

	Response deleteCommentFromComponent(String componentId, String commentId,
			boolean userspace);

	Response getRegisteredProfileRawType(String profileId, String rawType);

	Response registerProfile(InputStream input, String name,
			String description, String group, String domainName,
			boolean userspace);

	Response registerComponent(InputStream input, String name,
			String description, String group, String domainName,
			boolean userspace);

	Response registerCommentInComponent(InputStream input, String componentId,
			boolean userspace) throws ComponentRegistryException;

	Response registerCommentInProfile(InputStream input, String profileId,
			boolean userspace) throws ComponentRegistryException;

	Response pingSession();

	/**
	 * @param componentRegistryFactory the componentRegistryFactory to set
	 */
	void setComponentRegistryFactory(
			ComponentRegistryFactory componentRegistryFactory);

	/**
	 *
	 * @param userspace if "true" then profiles and components from the user's workspace, otherwise -- public
	 * @param limit the number of items to be displayed
	 * @return rss for the components in the database to which we are currently connected
	 * @throws ComponentRegistryException
	 * @throws ParseException
	 */
	Rss getRssComponent(boolean userspace, String limit)
			throws ComponentRegistryException, ParseException;

	/**
	 *
	 * @param userspace if "true" then profiles and components from the user's workspace, otherwise -- public
	 * @param limit the number of items to be displayed
	 * @return rss for the profiles in the database to which we are currently connected
	 * @throws ComponentRegistryException
	 * @throws ParseException
	 */
	Rss getRssProfile(boolean userspace, String limit)
			throws ComponentRegistryException, ParseException;

	/**
	 *
	 * @param profileId the Id of a profile whose comments are to be rss-ed
	 * @param userspace if "true" then profiles and components from the user's workspace, otherwise -- public
	 * @param limit the number of items to be displayed
	 * @return rss of the comments for a chosen profile
	 * @throws ComponentRegistryException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws ParseException
	 */
	Rss getRssOfCommentsFromProfile(String profileId, boolean userspace,
			String limit) throws ComponentRegistryException, IOException,
			JAXBException, ParseException;

	/**
	 *
	 * @param componentId the Id of a component whose comments are to be rss-ed
	 * @param userspace if "true" then profiles and components from the user's workspace, otherwise -- public
	 * @param limit the number of items to be displayed
	 * @return rss of the comments for a chosen component
	 * @throws ComponentRegistryException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws ParseException
	 */
	Rss getRssOfCommentsFromComponent(String componentId, boolean userspace,
			String limit) throws ComponentRegistryException, IOException,
			JAXBException, ParseException;

	AllowedAttributetypesXML getAllowedAttributeTypes()
			throws ComponentRegistryException, IOException, JAXBException,
			ParseException;
	
	void setFileNamesFromListToNull(List<CMDComponentType> listofcomponents);
	
	/**
	 * Get a list of groups the user is a member of
	 * @return
	 */
	List<Group> getGroupsTheCurrentUserIsAMemberOf();

	/**
	 * Get a list of groups the item is available to. For all practical reasons, this will return either 0 or 1 groups
	 * @param itemId ID of component or profile. This is not the DB id but the logical ID which is unique across components and profiles
	 * @return List of groups
	 */
	List<Group> getGroupsTheItemIsAMemberOf(String itemId);
	
	/**
	 * Transfer ownership of an item (group/profile) to a group
	 * @param itemId
	 * @param groupId
	 */
	void transferItemOwnershipToGroup(String itemId, long groupId);
	
	/**
	 * Get any component (public or private) with the specified ID
	 * @param componentId
	 * @return
	 */
	ComponentDescription getComponentDescription(String componentId) throws ComponentRegistryException;

}