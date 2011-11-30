/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author jeafer
 */
public class CommentsDao extends ComponentRegistryDao<Comment> {

    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private TransactionDefinition txDefinition;
    private final static Logger LOG = LoggerFactory.getLogger(CommentsDao.class);
    private final static String SELECT_BASE = "SELECT " + COLUMN_ID + ", comments, comment_date, user_id, "
            + "component_description_id, profile_description_id FROM " + TABLE_COMMENTS;

    protected String getTableName() {
        return TABLE_COMMENTS;
    }

    public List<Comment> getAllComments() throws DataAccessException {
        return getList(SELECT_BASE);
    }

    /**
     *
     * @param id Database record id (key)
     * @return Comment, if it exists
     * @throws DataAccessException
     */
    public Comment getById(String id) throws DataAccessException {
        return getFirstOrNull(SELECT_BASE + " WHERE " + COLUMN_ID + " = ?", id);
    }

    /**
     * @param id Database record id (key)
     * Method to retrieve comments from profiles
     * @return  Comments, comments_date, user_id and id
     */
    public List<Comment> getCommentsFromProfile(String profileId) throws DataAccessException {
        return getList(SELECT_BASE + " WHERE profile_description_id = ?", profileId);
    }

    public Comment getSpecifiedCommentFromProfile(String commentId) throws DataAccessException {
        //String select = SELECT_BASE + " WHERE " + COLUMN_ID + " = ?";
        return getFirstOrNull(SELECT_BASE + " WHERE " + COLUMN_ID + " = ?", Integer.parseInt(commentId));
    }

    /**
     * @param id Database record id (key)
     * Method to retrieve comments from components
     * @return  Comments, comments_date, user_id and id
     */
    public Comment getSpecifiedCommentFromComponent(String commentId) throws DataAccessException {
        //String select = SELECT_BASE + " WHERE " + COLUMN_ID + " =  ?";
        return getFirstOrNull(SELECT_BASE + " WHERE " + COLUMN_ID + " =  ?", Integer.parseInt(commentId));
    }

    public List<Comment> getCommentsFromComponent(String componentId) throws DataAccessException {
        return getList(SELECT_BASE + " WHERE component_description_id = ?", componentId);
    }

    public Comment getByComment(String aComment) throws DataAccessException {
        return getFirstOrNull(SELECT_BASE + " WHERE comments = ?", aComment);
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
	} else {
	    return owner.get(0);
	}
    }
    /**
     *
     * @param comment
     * @return Record id of the inserted comment
     * @throws DataAccessException
     */
    public Number insertComment(Comment comment) throws DataAccessException {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(getDataSource()).withTableName(TABLE_COMMENTS).usingGeneratedKeyColumns(
                COLUMN_ID);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("comments", comment.getComment());
        params.put("comment_date", extractTimestamp(comment));
        params.put("component_description_id", comment.getComponentDescriptionId());
        params.put("profile_description_id", comment.getProfileDescriptionId());
        params.put("user_id", comment.getUserId());
        return insert.executeAndReturnKey(params);
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
            comment.setCommentDate(commentDate == null ? null : Comment.createNewDate(commentDate.getTime()));
            return comment;
        }
    };
//    protected String getTableName() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    
//    protected String getCMDIdColumn() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    String getContent(boolean b, String id) {
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    boolean isPublic(String cmdId) {
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    boolean isInUserSpace(String cmdId, Number userId) {
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    public void deleteComment(Comment com, boolean isDeleted) throws DataAccessException {
        TransactionStatus transaction = getTransaction();
        Number dbId = getDbId(Integer.parseInt(com.getId()));
        StringBuilder delete = new StringBuilder("DELETE ").append(getTableName());
        delete.append(" WHERE " + COLUMN_ID + " = ?");
        getSimpleJdbcTemplate().update(delete.toString(), dbId);
        txManager.commit(transaction);
    }

    public Number getDbId(Number cmdId) {
        StringBuilder query = new StringBuilder("SELECT " + COLUMN_ID + " FROM ").append(getTableName());
        query.append(" WHERE ").append(getDbId(cmdId)).append(" = ?");
        return getSimpleJdbcTemplate().queryForInt(query.toString(), cmdId);
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
}
