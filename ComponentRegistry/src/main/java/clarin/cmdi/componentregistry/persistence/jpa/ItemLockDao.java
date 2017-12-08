package clarin.cmdi.componentregistry.persistence.jpa;

import clarin.cmdi.componentregistry.model.ItemLock;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


/**
 * 
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface ItemLockDao extends JpaRepository<ItemLock, Long>{

    	@Query("select l from ItemLock l where l.itemId = ?1")
	ItemLock getLockForItem(Integer itemId)
			throws DataAccessException;

}
