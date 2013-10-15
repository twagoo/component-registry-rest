package clarin.cmdi.componentregistry.persistence.impl;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.persistence.CommentsDao;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Serves comments for profiles and components
 * 
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
@Repository
public class CommentsDaoImpl extends JdbcDaoSupport implements CommentsDao {

    private final static Logger LOG = LoggerFactory
	    .getLogger(CommentsDaoImpl.class);

    private final static String SELECT_BASE = "SELECT id, component_id"
	    + ", comments, comment_date, user_id, "
	    + "component_id, user_name FROM comments";

    protected String getTableName() {
	return TABLE_COMMENTS;
    }

    protected String getCMDIdColumn() {
	return COLUMN_ID;
    }

    /**
     * Get the list of all the comments available in the database The
     * distinction between profile or component is not treated in this method
     * 
     * used only in tests
     * 
     * @return list of Comments
     */
    @Override
    public List<Comment> getAllComments() throws DataAccessException {
	return getJdbcTemplate().query("select * from comments order by id",
		rowMapper);
    }

    /**
     * Look for a specified Comment through its id
     * 
     * @param id
     *            Database record id (key)
     * @return Comment, if it exists
     * @throws DataAccessException
     */
    @Override
    public Comment getById(Number id) throws DataAccessException {
	return getJdbcTemplate().queryForObject(
		SELECT_BASE + " WHERE " + COLUMN_ID + " = ?",
		new Object[] { id }, rowMapper);
    }

    /**
     * Retrieve the comments related to a certain profile
     * 
     * @param id
     *            Database record id (fkey)
     * @return list of Comments
     */
    @Override
    public List<Comment> getCommentsFromProfile(String profileId)
	    throws DataAccessException {
	return getJdbcTemplate().query(
		SELECT_BASE + " WHERE component_id = ?"
			+ getOrderByDate(), new Object[] { profileId },
		rowMapper);
    }

    /**
     * Retrieve a specific comment related to a profile
     * 
     * @param id
     *            Database record id (key)
     * @return Comment
     * @throws DataAccessException
     */
    public Comment getSpecifiedCommentFromProfile(String commentId)
	    throws DataAccessException {
	return getJdbcTemplate().queryForObject(
		SELECT_BASE + " WHERE " + COLUMN_ID + " = ?",
		new Object[] { commentId }, rowMapper);
    }

    /**
     * Retrieve the comments related to a certain component
     * 
     * @param id
     *            Database record id (fkey)
     * @return list of Comments
     * @throws DataAccessException
     */
    public List<Comment> getCommentsFromComponent(String componentId)
	    throws DataAccessException {
	return getJdbcTemplate()
		.query(
			(SELECT_BASE + " WHERE component_id = ?")
				.concat(getOrderByDate()),
			new Object[] { componentId }, rowMapper);
    }

    /**
     * Retrieve a specific comment related to a component
     * 
     * @param id
     *            Database record id (key)
     * @return Comment
     * @throws DataAccessException
     */
    public Comment getSpecifiedCommentFromComponent(String commentId)
	    throws DataAccessException {
	return getJdbcTemplate().queryForObject(SELECT_BASE + " WHERE " + COLUMN_ID + " =  ?",
		new Object[] {commentId}, rowMapper);
    }

    /**
     * 
     * @param id
     *            Id of description record
     * @return Principal name of description's owner, if any. Otherwise, null.
     */
    public String getOwnerPrincipalName(Number id) {
	StringBuilder select = new StringBuilder("SELECT principal_name FROM "
		+ TABLE_REGISTRY_USER);
	select.append(" JOIN ").append(getTableName());
	select.append(" ON user_id = " + TABLE_REGISTRY_USER + ".id ");
	select.append(" WHERE ").append(getTableName()).append(".id = ?");
	List<String> owner = getJdbcTemplate().query(select.toString(),
		new ParameterizedSingleColumnRowMapper<String>(), id);
	if (owner.isEmpty()) {
	    return null;
	}
	{
	    return owner.get(0);
	}
    }

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
    public Number insertComment(Comment comment, Number userId)
	    throws DataAccessException {
	SimpleJdbcInsert insertComment = new SimpleJdbcInsert(getDataSource())
		.withTableName(getTableName()).usingGeneratedKeyColumns(
			COLUMN_ID);
	Map<String, Object> params = new HashMap<String, Object>();
	putInsertComment(params, comment, userId);

	Number id = insertComment.executeAndReturnKey(params);
	return id;
    }

    /**
     * 
     * @param params
     * @param comment
     * @param contentId
     * @param userId
     * @throws DataAccessException
     */
    protected void putInsertComment(Map<String, Object> params,
	    Comment comment, Number userId) throws DataAccessException {
	params.put("comments", comment.getComment());
	params.put("comment_date", extractTimestamp(comment));
	params.put("component_id",
		comment.getComponentId());
	params.put("user_id", userId);
	params.put("user_name", comment.getUserName());
    }

    private final ParameterizedRowMapper<Comment> rowMapper = new ParameterizedRowMapper<Comment>() {

	@Override
	public Comment mapRow(ResultSet rs, int rowNumber) throws SQLException {
	    Comment comment = new Comment();
	    Timestamp commentDate = rs.getTimestamp("comment_date");
	    comment.setId(rs.getString(COLUMN_ID));
	    comment.setComment(rs.getString("comments"));
	    comment.setComponentId(rs
		    .getString("component_id"));
	    comment.setUserId(rs.getString("user_id"));
	    comment.setUserName(rs.getString("user_name"));
	    comment.setCommentDate(commentDate == null ? null : Comment
		    .createNewDate(commentDate.getTime()));
	    return comment;
	}
    };

    /**
     * Method that will delete a comment from the database based on its id
     * 
     * @param com
     *            comment to be deleted
     * @throws DataAccessException
     */
    public void deleteComment(Comment com) throws DataAccessException {
	Number dbId = Integer.parseInt(com.getId());
	StringBuilder delete = new StringBuilder("DELETE FROM ")
		.append(getTableName());
	delete.append(" WHERE " + COLUMN_ID + " = ?");
	getJdbcTemplate().update(delete.toString(), dbId);
    }

    private Timestamp extractTimestamp(Comment comment) {
	if (comment.getCommentDate() != null) {
	    try {
		Date date = Comment.getDate(comment.getCommentDate());
		return new Timestamp(date.getTime());
	    } catch (ParseException ex) {
		LOG.warn(
			"Could not convert registration date "
				+ comment.getCommentDate() + " to date", ex);
	    } catch (IllegalArgumentException ex) {
		LOG.warn(
			"Could not convert registration date "
				+ comment.getCommentDate() + " to timestamp",
			ex);
	    }
	}
	return null;
    }

    /**
     * Method use for tests that will select comments based on the content
     * 
     * @param aComment
     * @return list of Comments
     * @throws DataAccessException
     */
    public Comment getByComment(String aComment) throws DataAccessException {
	
	List<Comment> list = getJdbcTemplate().query(SELECT_BASE + " WHERE comments = ?", new Object[] {aComment}, rowMapper);
	if (list.isEmpty())
	    return null;
	return list.get(0);
    }

    /**
     * Sort the returned comments per date from the most recent to the oldest
     * 
     * @return
     */
    private String getOrderByDate() {
	return " order by comment_date asc, id asc";
    }

    @Override
    @Resource(name="dataSource")
    public void setDatasourceProperty(DataSource ds) {
	super.setDataSource(ds);
    }
}
