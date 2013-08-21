package clarin.cmdi.componentregistry.persistence;

import java.util.List;

import org.springframework.dao.DataAccessException;

import clarin.cmdi.componentregistry.model.RegistryUser;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface UserDao {

	List<RegistryUser> getAllUsers() throws DataAccessException;

	/**
	 *
	 * @param principalName User's principal name
	 * @return User, if it exists
	 * @throws DataAccessException
	 */
	RegistryUser getByPrincipalName(String principalName)
			throws DataAccessException;

	/**
	 *
	 * @param id Database record id (key)
	 * @return User, if it exists
	 * @throws DataAccessException
	 */
	RegistryUser getById(Number id) throws DataAccessException;

	/**
	 *
	 * @param user
	 * @return Record id of the inserted user
	 * @throws DataAccessException
	 */
	Number insertUser(RegistryUser user) throws DataAccessException;

	/**
	 * Updates some data stored about the user. At this point only the display name will be updated. ID is taken from 'id' parameter,
	 * not user object.
	 * @param id ID of user to update
	 * @param user object containing new info
	 */
	void updateUser(Number id, RegistryUser user) throws DataAccessException;

}