package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.Comment;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 *
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
public class CommentsDao extends ComponentRegistryDao<Comment> {

    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private TransactionDefinition txDefinition;
    private final static Logger LOG = LoggerFactory.getLogger(CommentsDao.class);
    private final static String SELECT_BASE = "SELECT " + COLUMN_ID + ", comments, comment_date, user_id, "
            + "component_description_id, profile_description_id, user_name FROM " + TABLE_COMMENTS;

    protected String getTableName() {
        return TABLE_COMMENTS;
    }

    protected String getCMDIdColumn() {
        return COLUMN_ID;
    }

    /**
     * Get the list of all the comments available in the database
     * The distinction between profile or component is not treated in this method
     * @return list of Comments 
     */
    public List<Comment> getAllComments() throws DataAccessException {
        return getList(SELECT_BASE);
    }

    /**
     * Look for a specified Comment through its id
     * @param id Database record id (key)
     * @return Comment, if it exists
     * @throws DataAccessException
     */
    public Comment getById(Number id) throws DataAccessException {
        return getFirstOrNull(SELECT_BASE + " WHERE " + COLUMN_ID + " = ?", id);
    }

    /**
     * Retrieve the comments related to a certain profile
     * @param id Database record id (fkey)
     * @return list of Comments
     */
    public List<Comment> getCommentsFromProfile(String profileId) throws DataAccessException {
        return getList((SELECT_BASE + " WHERE profile_description_id = ?").concat(getOrderByDate()), profileId);
    }

    /**
     * Retrieve a specific comment related to a profile
     * @param id Database record id (key)
     * @return Comment
     * @throws DataAccessException 
     */
    public Comment getSpecifiedCommentFromProfile(String commentId) throws DataAccessException {
        return getFirstOrNull(SELECT_BASE + " WHERE " + COLUMN_ID + " = ?", Integer.parseInt(commentId));
    }

    /**
     * Retrieve the comments related to a certain component
     * @param id Database record id (fkey)
     * @return list of Comments
     * @throws DataAccessException 
     */
    public List<Comment> getCommentsFromComponent(String componentId) throws DataAccessException {
        return getList((SELECT_BASE + " WHERE component_description_id = ?").concat(getOrderByDate()), componentId);
    }

    /**
     * Retrieve a specific comment related to a component
     * @param id Database record id (key)
     * @return Comment
     * @throws DataAccessException 
     */
    public Comment getSpecifiedCommentFromComponent(String commentId) throws DataAccessException {
        return getFirstOrNull(SELECT_BASE + " WHERE " + COLUMN_ID + " =  ?", Integer.parseInt(commentId));
    }

    /**
     * 
     * @param id
     *            Id of description record
     * @return Principal name of description's owner, if any. Otherwise, null.
     */
    public String getOwnerPrincipalName(Number id) {
        StringBuilder select = new StringBuilder("SELECT principal_name FROM " + TABLE_REGISTRY_USER);
        select.append(" JOIN ").append(getTableName());
        select.append(" ON user_id = " + TABLE_REGISTRY_USER + ".id ");
        select.append(" WHERE ").append(getTableName()).append(".id = ?");
        List<String> owner = getSimpleJdbcTemplate().query(select.toString(), new ParameterizedSingleColumnRowMapper<String>(), id);
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
     *              the comment to be inserted
     * @param content
     *              
     * @param userId
     *              the id of the user
     * @return Record id of the inserted comment
     * @throws DataAccessException
     */
    public Number insertComment(Comment comment, Number userId) throws DataAccessException {
        TransactionStatus transaction = getTransaction();
        try {
            SimpleJdbcInsert insertComment = new SimpleJdbcInsert(getDataSource()).withTableName(getTableName()).usingGeneratedKeyColumns(COLUMN_ID);
            Map<String, Object> params = new HashMap<String, Object>();
            putInsertComment(params, comment, userId);

            Number id = insertComment.executeAndReturnKey(params);
            txManager.commit(transaction);
            return id;
        } catch (DataAccessException ex) {
            txManager.rollback(transaction);
            throw ex;
        }
    }

    /**
     * 
     * @param params
     * @param comment
     * @param contentId
     * @param userId
     * @throws DataAccessException 
     */
    protected void putInsertComment(Map<String, Object> params, Comment comment, Number userId) throws DataAccessException {
        params.put("comments", comment.getComment());
        params.put("comment_date", extractTimestamp(comment));
        params.put("component_description_id", comment.getComponentDescriptionId());
        params.put("profile_description_id", comment.getProfileDescriptionId());
        params.put("user_id", userId);
	params.put("user_name", comment.getUserName());
    }

    @Override
    protected ParameterizedRowMapper<Comment> getRowMapper() {
        return rowMapper;
    }
    private final ParameterizedRowMapper<Comment> rowMapper = new ParameterizedRowMapper<Comment>() {

        @Override
        public Comment mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Comment comment = new Comment();
            Timestamp commentDate = rs.getTimestamp("comment_date");
            comment.setId(rs.getString(COLUMN_ID));
            comment.setComment(rs.getString("comments"));
            comment.setComponentDescriptionId(rs.getString("component_description_id"));
            comment.setProfileDescriptionId(rs.getString("profile_description_id"));
            comment.setUserId(rs.getString("user_id"));
	    comment.setUserName(rs.getString("user_name"));
            comment.setCommentDate(commentDate == null ? null : Comment.createNewDate(commentDate.getTime()));
            return comment;
        }
    };

    /**
     * Method that will delete a comment from the database based on its id
     * @param com
     *           comment to be deleted
     * @throws DataAccessException 
     */
    public void deleteComment(Comment com) throws DataAccessException {
        TransactionStatus transaction = getTransaction();
        Number dbId = Integer.parseInt(com.getId());
        StringBuilder delete = new StringBuilder("DELETE FROM ").append(getTableName());
        delete.append(" WHERE " + COLUMN_ID + " = ?");
        getSimpleJdbcTemplate().update(delete.toString(), dbId);
        txManager.commit(transaction);
    }

    private Timestamp extractTimestamp(Comment comment) {
        if (comment.getCommentDate() != null) {
            try {
                Date date = Comment.getDate(comment.getCommentDate());
                return new Timestamp(date.getTime());
            } catch (ParseException ex) {
                LOG.warn("Could not convert registration date " + comment.getCommentDate() + " to date", ex);
            } catch (IllegalArgumentException ex) {
                LOG.warn("Could not convert registration date " + comment.getCommentDate() + " to timestamp", ex);
            }
        }
        return null;
    }

    private TransactionStatus getTransaction() {
        return txManager.getTransaction(txDefinition);
    }

    /**
     * Method use for tests that will select comments based on the content
     * @param aComment
     * @return list of Comments
     * @throws DataAccessException 
     */
    public Comment getByComment(String aComment) throws DataAccessException {
        return getFirstOrNull(SELECT_BASE + " WHERE comments = ?", aComment);
    }
    
    /**
     * Sort the returned comments per date from the most recent to the oldest
     * @return 
     */
        private String getOrderByDate() {
	return " order by comment_date asc ";
    }
}
