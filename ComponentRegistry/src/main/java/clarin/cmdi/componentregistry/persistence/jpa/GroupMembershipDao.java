package clarin.cmdi.componentregistry.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import clarin.cmdi.componentregistry.model.GroupMembership;

public interface GroupMembershipDao extends JpaRepository<GroupMembership, String>{

    @Query("select gm from GroupMembership gm where gm.userId = ?1")
    List<GroupMembership> findGroupsTheUserIsAmemberOf(long userId);

    @Query("select gm from GroupMembership gm where gm.userId = ?1 and gm.groupId = ?2")
    GroupMembership findMembership(long userId, long groupId);
//
//    @Query("delete gm from GroupMembership gm where gm.userId = ?1 and gm.groupId = ?2")
//    int deleteMembership(long userId, long groupId);

    @Query("select gm from GroupMembership gm where gm.groupId = ?1")
    public List<GroupMembership> findForGroup(long groupId);
}
