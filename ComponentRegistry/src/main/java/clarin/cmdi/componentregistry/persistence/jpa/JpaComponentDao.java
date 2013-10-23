package clarin.cmdi.componentregistry.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import clarin.cmdi.componentregistry.model.Component;
import clarin.cmdi.componentregistry.persistence.ComponentDao;

/**
 * Basic jpa persistence functions. The more complicated rest is performed by {@link ComponentDao} 
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface JpaComponentDao extends JpaRepository<Component, Long>{

    @Query("select c from Component c where c.componentId = ?1")
    Component findByComponentId(String componentId);
    
    @Query("select c from Component c where c.ispublic = true and c.deleted = false order by upper(c.name), c.id")
    List<Component> findPublicItems();

    @Query("select c from Component c where c.ispublic = true and c.deleted = false and c.componentId like ?1 order by upper(c.name) asc")
    List<Component> findPublicItems(String idPrefix);

    @Query("select c from Component c where c.ispublic = false and c.deleted = true and c.dbUserId = ?1 order by upper(c.name), c.id")
    List<Component> findDeletedItemsForUser(Long userId);

    @Query("select c from Component c where c.ispublic = true and c.deleted = true order by upper(c.name), c.id")
    List<Component> findPublicDeletedItems();
    
    @Query("select c from Component c where c.dbUserId = ?1 AND c.deleted = false AND c.ispublic = false AND c.componentId like ?2 AND not exists (select 1 from Ownership o where o.componentId = c.componentId) order by upper(c.name), c.id")
    List<Component> findItemsForUserThatAreNotInGroups(Long userId, String idPrefix);

    @Modifying
    @Query(nativeQuery=true, value="update persistentcomponents set content = ?2 where component_id like ?1")
    void updateContent(String componentId, String content);
    
    @Query(nativeQuery=true, value="select content from persistentcomponents where component_id like ?1 and is_deleted = ?2")
    String findContentByComponentId(String componentId, boolean deleted);

    @Query(nativeQuery=true, value="select content from persistentcomponents where component_id like ?1")
    String findContentByComponentId(String componentId);
}
