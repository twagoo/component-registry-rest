package clarin.cmdi.componentregistry.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import clarin.cmdi.componentregistry.model.Ownership;

public interface OwnershipDao extends CrudRepository<Ownership, String>{

    @Query("select o from Ownership o where o.userId=?1 and o.componentId=?2")
    Ownership findOwnershipByUserAndComponent(long userId, String componentId);

    @Query("select o from Ownership o where o.groupId=?1 and o.componentId=?2")
    Ownership findOwnershipByGroupAndComponent(long groupId, String componentId);

    @Query("select o from Ownership o where o.groupId=?1")
    List<Ownership> findOwnershipByGroup(long groupId);
    
    @Query("select o from Ownership o where o.componentId=?1")
    List<Ownership> findOwnershipByComponentId(String componentId);

}
