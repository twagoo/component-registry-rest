package clarin.cmdi.componentregistry.persistence.jpa;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.RegistryUser;

/**
 * Interface for DAOs that handle comments
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface CommentsDao extends JpaRepository<Comment, Long>{

	/**
	 * Get the list of all the comments available in the database The
	 * distinction between profile or component is not treated in this method
	 * 
	 * @return list of Comments
	 */
    	@Query("select c from Comment c order by c.id")
	List<Comment> getAllComments();

	/**
	 * Retrieve the comments related to a certain component
	 * 
	 * @param id
	 *            Database record id (fkey)
	 * @return list of Comments
	 * @throws DataAccessException
	 */
    	@Query("select c from Comment c where c.componentId = ?1 order by c.commentDate, c.id")
	List<Comment> getCommentsFromComponent(String componentId)
			throws DataAccessException;

	/**
	 * 
	 * @param id
	 *            Id of description record
	 * @return Principal name of description's owner, if any. Otherwise, null.
	 */
    	@Query("select user from RegistryUser user, Comment comment, Component component where comment.id = ?1 and comment.componentId = component.componentId and component.dbUserId = user.id")
	RegistryUser getOwnerOfComment(long id);

	/**
	 * Method use for tests that will select comments based on the content
	 * 
	 * @param aComment
	 * @return list of Comments
	 * @throws DataAccessException
	 */
    	@Query("select c from Comment c where c.comments = ?1")
	Comment getByComment(String aComment) throws DataAccessException;

}