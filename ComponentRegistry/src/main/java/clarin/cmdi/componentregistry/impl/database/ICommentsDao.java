package clarin.cmdi.componentregistry.impl.database;

import java.util.List;

import org.springframework.dao.DataAccessException;

import clarin.cmdi.componentregistry.model.Comment;

/**
 * Interface for DAOs that handle comments
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface ICommentsDao extends IComponentRegistryDao<Comment>{

	/**
	 * Get the list of all the comments available in the database The
	 * distinction between profile or component is not treated in this method
	 * 
	 * @return list of Comments
	 */
	List<Comment> getAllComments() throws DataAccessException;

	/**
	 * Look for a specified Comment through its id
	 * 
	 * @param id
	 *            Database record id (key)
	 * @return Comment, if it exists
	 * @throws DataAccessException
	 */
	Comment getById(Number id) throws DataAccessException;

	/**
	 * Retrieve the comments related to a certain profile
	 * 
	 * @param id
	 *            Database record id (fkey)
	 * @return list of Comments
	 */
	List<Comment> getCommentsFromProfile(String profileId)
			throws DataAccessException;

	/**
	 * Retrieve a specific comment related to a profile
	 * 
	 * @param id
	 *            Database record id (key)
	 * @return Comment
	 * @throws DataAccessException
	 */
	Comment getSpecifiedCommentFromProfile(String commentId)
			throws DataAccessException;

	/**
	 * Retrieve the comments related to a certain component
	 * 
	 * @param id
	 *            Database record id (fkey)
	 * @return list of Comments
	 * @throws DataAccessException
	 */
	List<Comment> getCommentsFromComponent(String componentId)
			throws DataAccessException;

	/**
	 * Retrieve a specific comment related to a component
	 * 
	 * @param id
	 *            Database record id (key)
	 * @return Comment
	 * @throws DataAccessException
	 */
	Comment getSpecifiedCommentFromComponent(String commentId)
			throws DataAccessException;

	/**
	 * 
	 * @param id
	 *            Id of description record
	 * @return Principal name of description's owner, if any. Otherwise, null.
	 */
	String getOwnerPrincipalName(Number id);

	/**
	 * 
	 * @param comment
	 *            the comment to be inserted
	 * @param content
	 * 
	 * @param userId
	 *            the id of the user
	 * @return Record id of the inserted comment
	 * @throws DataAccessException
	 */
	Number insertComment(Comment comment, Number userId)
			throws DataAccessException;

	/**
	 * Method that will delete a comment from the database based on its id
	 * 
	 * @param com
	 *            comment to be deleted
	 * @throws DataAccessException
	 */
	void deleteComment(Comment com) throws DataAccessException;

	/**
	 * Method use for tests that will select comments based on the content
	 * 
	 * @param aComment
	 * @return list of Comments
	 * @throws DataAccessException
	 */
	Comment getByComment(String aComment) throws DataAccessException;

}