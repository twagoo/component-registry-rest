package clarin.cmdi.componentregistry.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import clarin.cmdi.componentregistry.model.GroupMembership;

public interface GroupMembershipDao extends CrudRepository<GroupMembership, String>{

    @Query("select gm from GroupMembership gm where gm.userId = ?1")
    List<GroupMembership> findGroupsTheUserIsAmemberOf(long userId);

    @Query("select gm from GroupMembership gm where gm.userId = ?1 and gm.groupId = ?2")
    GroupMembership findMembership(long userId, long groupId);

}
