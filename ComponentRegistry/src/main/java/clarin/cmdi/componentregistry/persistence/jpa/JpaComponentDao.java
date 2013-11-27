package clarin.cmdi.componentregistry.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;

/**
 * Basic jpa persistence functions. The more complicated rest is performed by {@link ComponentDao} 
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface JpaComponentDao extends JpaRepository<BaseDescription, Long>{

    @Query("select c from BaseDescription c where c.componentId = ?1")
    BaseDescription findByComponentId(String componentId);
    
    @Query("select c from BaseDescription c where c.ispublic = true and c.deleted = false order by upper(c.name), c.id")
    List<BaseDescription> findPublicItems();

    @Query("select c from BaseDescription c where c.ispublic = true and c.deleted = false and c.componentId like ?1 order by upper(c.name) asc")
    List<BaseDescription> findPublicItems(String idPrefix);

    @Query("select c from BaseDescription c where c.ispublic = false and c.deleted = true and c.dbUserId = ?1 order by upper(c.name), c.id")
    List<BaseDescription> findDeletedItemsForUser(Long userId);

    @Query("select c from BaseDescription c where c.ispublic = true and c.deleted = true order by upper(c.name), c.id")
    List<BaseDescription> findPublicDeletedItems();
    
    @Query("select c from BaseDescription c where c.dbUserId = ?1 AND c.deleted = false AND c.ispublic = false AND c.componentId like ?2 AND not exists (select 1 from Ownership o where o.componentId = c.componentId) order by upper(c.name), c.id")
    List<BaseDescription> findItemsForUserThatAreNotInGroups(Long userId, String idPrefix);

    @Modifying
    @Query(nativeQuery=true, value="update basedescription set content = ?2 where component_id like ?1")
    void updateContent(String componentId, String content);
    
    @Query(nativeQuery=true, value="select content from basedescription where component_id like ?1 and is_deleted = ?2")
    String findContentByComponentId(String componentId, boolean deleted);

    @Query(nativeQuery=true, value="select content from basedescription where component_id like ?1")
    String findContentByComponentId(String componentId);
    
    @Query("select c.componentId from BaseDescription c where c.deleted = false and c.componentId like ?1 order by c.id asc")
    List<String> findNonDeletedItemIds(String componentIdPrefix);

}
