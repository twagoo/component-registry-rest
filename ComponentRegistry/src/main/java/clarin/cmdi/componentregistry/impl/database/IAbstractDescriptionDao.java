package clarin.cmdi.componentregistry.impl.database;

import java.util.List;

import org.springframework.dao.DataAccessException;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 * Interface for a DAO that serves descriptions. Serves as a base for interfaces and implementations that want to serve
 * {@link ProfileDescription}s and {@link ComponentDescription}s
 * @author george.georgovassilis@mpi.nl
 *
 * @param <T>
 */
public interface IAbstractDescriptionDao<T extends AbstractDescription> {

	/**
	 * 
	 * @param cmdId
	 *            CMD id
	 * @return Whether the specified item is in the public space
	 */
	boolean isPublic(String cmdId);

	/**
	 * 
	 * @param cmdId
	 *            CMD id
	 * @param userId
	 *            User db id of workspace owner
	 * @return Whether the specified item is in the specified user's workspace
	 */
	boolean isInUserSpace(String cmdId, Number userId);

	/**
	 * 
	 * @param cmdId
	 *            CMD id
	 * @param userId
	 *            User db id of workspace owner, null for public registry
	 * @return Whether the specified item is in the specified workspace (user or
	 *         public)
	 */
	boolean isInRegistry(String cmdId, Number userId);

	/**
	 * 
	 * @param cmdId
	 *            Profile or component Id (not primary key)
	 * @return String value of XML content for profile or component
	 */
	String getContent(boolean isDeleted, String cmdId)
			throws DataAccessException;

	/**
	 * @param description
	 *            Description to insert
	 * @param content
	 *            Content to insert and refer to from description
	 * @return Id of newly inserted description
	 */
	Number insertDescription(AbstractDescription description, String content,
			boolean isPublic, Number userId) throws DataAccessException;

	/**
	 * Updates a description by database id
	 * 
	 * @param id
	 *            Id (key) of description record
	 * @param description
	 *            New values for description (leave null to not change)
	 * @param content
	 *            New content for description (leave null to not change)
	 */
	void updateDescription(Number id, AbstractDescription description,
			String content);

	/**
	 * Retrieves description by it's primary key Id
	 * 
	 * @param id
	 *            Description key
	 * @return The description, if it exists; null otherwise
	 */
	T getById(Number id) throws DataAccessException;

	/**
	 * Get by ComponentId / ProfileId, whether in userspace or public
	 * 
	 * @param id
	 *            Full component id
	 * @return The description, if it exists; null otherwise
	 */
	T getByCmdId(String id) throws DataAccessException;

	/**
	 * Get by ComponentId / ProfileId
	 * 
	 * @param id
	 *            Full component id
	 * @param userId
	 *            Db id of user for workspace; null for public space
	 * @return The description, if it exists; null otherwise
	 */
	T getByCmdId(String id, Number userId) throws DataAccessException;

	/**
	 * 
	 * @param cmdId
	 *            CMD Id of description
	 * @return Database id for description record
	 */
	Number getDbId(String cmdId);

	/**
	 * 
	 * @return All descriptions in the public space
	 */
	List<T> getPublicDescriptions() throws DataAccessException;

	/**
	 * @return List of deleted descriptions in user space or in public when
	 *         userId=null
	 * @param userId
	 */
	List<T> getDeletedDescriptions(Number userId);

	/**
	 * 
	 * @return All the user's descriptions not in the public space
	 */
	List<T> getUserspaceDescriptions(Number userId) throws DataAccessException;

	void setDeleted(AbstractDescription desc, boolean isDeleted)
			throws DataAccessException;

	void setPublished(Number id, boolean published);

	/**
	 * 
	 * @param id
	 *            Id of description record
	 * @return Principal name of description's owner, if any. Otherwise, null.
	 */
	String getOwnerPrincipalName(Number id);

}