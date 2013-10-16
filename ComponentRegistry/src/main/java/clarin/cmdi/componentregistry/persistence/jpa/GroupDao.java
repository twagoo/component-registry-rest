package clarin.cmdi.componentregistry.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import clarin.cmdi.componentregistry.model.Group;

public interface GroupDao extends JpaRepository<Group, Long>{

    @Query("select g from Group g where g.name=?1")
    Group findGroupByName(String name);
    
    @Query("select g from Group g where g.ownerId=?1 order by g.name")
    List<Group> findGroupOwnedByUser(long userId);
    
}
