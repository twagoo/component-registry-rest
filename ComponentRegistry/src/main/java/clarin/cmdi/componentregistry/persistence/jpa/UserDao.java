package clarin.cmdi.componentregistry.persistence.jpa;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import clarin.cmdi.componentregistry.model.RegistryUser;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface UserDao extends JpaRepository<RegistryUser, Long>{

    	@Query("select u from RegistryUser u order by id")
	List<RegistryUser> getAllUsers() throws DataAccessException;

	/**
	 *
	 * @param principalName User's principal name
	 * @return User, if it exists
	 * @throws DataAccessException
	 */
    	@Query("select u from RegistryUser u where u.principalName = ?1")
	RegistryUser getByPrincipalName(String principalName)
			throws DataAccessException;
        
        @Query("select u from RegistryUser u where u.id = ?1")
	RegistryUser getPrincipalNameById(Number id) throws DataAccessException;

}