package clarin.cmdi.componentregistry.impl.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
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

import clarin.cmdi.componentregistry.model.AbstractDescription;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.collections.ListUtils;

/**
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AbstractDescriptionDao<T extends AbstractDescription> extends ComponentRegistryDao<T> {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractDescriptionDao.class);
    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private TransactionDefinition txDefinition;

    protected abstract String getTableName();

    protected abstract String getCMDIdColumn();
    /**
     * Class object required to instantiate new description domain objects
     */
    private final Class<T> _class;

    protected AbstractDescriptionDao(Class<T> _class) {
	this._class = _class;
    }

    /**
     * 
     * @param cmdId
     *            CMD id
     * @return Whether the specified item is in the public space
     */
    public boolean isPublic(String cmdId) {
	StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ");
	query.append(getTableName());
	query.append(" WHERE is_public = true AND ").append(getCMDIdColumn()).append(" = ?");
	return (0 < getSimpleJdbcTemplate().queryForInt(query.toString(), cmdId));
    }

    /**
     * 
     * @param cmdId
     *            CMD id
     * @param userId
     *            User db id of workspace owner
     * @return Whether the specified item is in the specified user's workspace
     */
    public boolean isInUserSpace(String cmdId, Number userId) {
	StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ");
	query.append(getTableName());
	query.append(" WHERE is_public = false AND user_id = ? AND ").append(getCMDIdColumn()).append(" = ?");
	return (0 < getSimpleJdbcTemplate().queryForInt(query.toString(), userId, cmdId));
    }

    /**
     * 
     * @param cmdId
     *            CMD id
     * @param userId
     *            User db id of workspace owner, null for public registry
     * @return Whether the specified item is in the specified workspace (user or public)
     */
    public boolean isInRegistry(String cmdId, Number userId) {
	if (userId == null) {
	    return isPublic(cmdId);
	} else {
	    return isInUserSpace(cmdId, userId);
	}
    }

    /**
     * 
     * @param cmdId
     *            Profile or component Id (not primary key)
     * @return String value of XML content for profile or component
     */
    public String getContent(boolean isDeleted, String cmdId) throws DataAccessException {
	String select = "SELECT content FROM " + TABLE_XML_CONTENT + " JOIN " + getTableName() + " ON " + TABLE_XML_CONTENT + "."
		+ COLUMN_ID + " = " + getTableName() + ".content_id" + " WHERE is_deleted = ? AND " + getTableName() + "."
		+ getCMDIdColumn() + " = ?";

	List<String> result = getSimpleJdbcTemplate().query(select, new ParameterizedSingleColumnRowMapper<String>(), isDeleted, cmdId);
	if (result.size() > 0) {
	    return result.get(0);
	} else {
	    return null;
	}
    }

    /**
     * @param description
     *            Description to insert
     * @param content
     *            Content to insert and refer to from description
     * @return Id of newly inserted description
     */
    public Number insertDescription(AbstractDescription description, String content, boolean isPublic, Number userId)
	    throws DataAccessException {

	TransactionStatus transaction = getTransaction();
	try {
	    SimpleJdbcInsert insert = new SimpleJdbcInsert(getDataSource()).withTableName(TABLE_XML_CONTENT).usingGeneratedKeyColumns(
		    COLUMN_ID);
	    Number contentId = insert.executeAndReturnKey(Collections.singletonMap("content", (Object) content));

	    SimpleJdbcInsert insertDescription = new SimpleJdbcInsert(getDataSource()).withTableName(getTableName()).usingGeneratedKeyColumns(COLUMN_ID);
	    Map<String, Object> params = new HashMap<String, Object>();
	    putInsertParameters(params, description, contentId, userId, isPublic);

	    Number id = insertDescription.executeAndReturnKey(params);
	    txManager.commit(transaction);
	    return id;
	} catch (DataAccessException ex) {
	    txManager.rollback(transaction);
	    throw ex;
	}
    }

    private Timestamp extractTimestamp(AbstractDescription description) {
	if (description.getRegistrationDate() != null) {
	    try {
		Date date = AbstractDescription.getDate(description.getRegistrationDate());
		return new Timestamp(date.getTime());
	    } catch (ParseException ex) {
		LOG.warn("Could not convert registration date " + description.getRegistrationDate() + " to date", ex);
	    } catch (IllegalArgumentException ex) {
		LOG.warn("Could not convert registration date " + description.getRegistrationDate() + " to timestamp", ex);
	    }
	}
	return null;
    }

    /**
     * Updates a description by database id
     * 
     * @param id
     *            Id (key) of description record
     * @param description
     *            New values for description (leave null to not change)
     * @param content
     *            New content for description (leave null to not change)
     */
    public void updateDescription(Number id, AbstractDescription description, String content) {
	TransactionStatus transaction = getTransaction();
	try {
	    if (description != null) {
		// Update description
		StringBuilder updateDescription = new StringBuilder();
		updateDescription.append("UPDATE ").append(getTableName());
		appendUpdateColumnsStatement(updateDescription);
		updateDescription.append(" WHERE " + COLUMN_ID + " = ?");
		Collection updateParams = ListUtils.union(getUpdateParameterValues(description), Collections.singletonList(id));
		getSimpleJdbcTemplate().update(updateDescription.toString(), updateParams.toArray());
	    }

	    if (content != null) {
		// Update content
		StringBuilder updateContent = new StringBuilder();
		updateContent.append("UPDATE " + TABLE_XML_CONTENT + " SET content = ? WHERE " + COLUMN_ID + " = ");
		updateContent.append("(SELECT content_id FROM ").append(getTableName()).append(" WHERE " + COLUMN_ID + "= ?)");

		getSimpleJdbcTemplate().update(updateContent.toString(), content, id);
	    }
	    txManager.commit(transaction);
	} catch (DataAccessException ex) {
	    txManager.rollback(transaction);
	    throw ex;
	}
    }

    /**
     * Retrieves description by it's primary key Id
     * 
     * @param id
     *            Description key
     * @return The description, if it exists; null otherwise
     */
    public T getById(Number id) throws DataAccessException {
	return getFirstOrNull(getSelectStatement("WHERE is_deleted = false AND id = ?"), id);
    }

    /**
     * Get by ComponentId / ProfileId, whether in userspace or public
     * 
     * @param id
     *            Full component id
     * @return The description, if it exists; null otherwise
     */
    public T getByCmdId(String id) throws DataAccessException {
	return getFirstOrNull(getSelectStatement("WHERE is_deleted = false AND " + getCMDIdColumn() + " = ?"), id);
    }

    /**
     * Get by ComponentId / ProfileId
     * 
     * @param id
     *            Full component id
     * @param userId
     *            Db id of user for workspace; null for public space
     * @return The description, if it exists; null otherwise
     */
    public T getByCmdId(String id, Number userId) throws DataAccessException {
	StringBuilder query = new StringBuilder("WHERE is_deleted = false AND ").append(getCMDIdColumn()).append(" = ?");
	if (userId == null) {
	    return getFirstOrNull(getSelectStatement(query.append(" AND is_public = true").toString()), id);
	} else {
	    return getFirstOrNull(getSelectStatement(query.append(" AND is_public = false AND user_id = ?").toString()), id, userId);
	}
    }

    /**
     * 
     * @param cmdId
     *            CMD Id of description
     * @return Database id for description record
     */
    public Number getDbId(String cmdId) {
	StringBuilder query = new StringBuilder("SELECT " + COLUMN_ID + " FROM ").append(getTableName());
	query.append(" WHERE ").append(getCMDIdColumn()).append(" = ?");
	return getSimpleJdbcTemplate().queryForInt(query.toString(), cmdId);
    }

    /**
     * 
     * @return All descriptions in the public space
     */
    public List<T> getPublicDescriptions() throws DataAccessException {
	return getList(getSelectStatement(" WHERE is_deleted = false AND is_public = true ").append(getOrderByClause()));
    }

    /**
     * @return List of deleted descriptions in user space or in public when userId=null
     * @param userId
     */
    public List<T> getDeletedDescriptions(Number userId) {
	if (userId != null) {
	    String select = getSelectStatement().append(" WHERE is_deleted = true AND is_public = false AND user_id = ?").append(getOrderByClause()).toString();
	    return getList(select, userId);
	} else {
	    String select = getSelectStatement().append(" WHERE is_deleted = true AND is_public = true").append(getOrderByClause()).toString();
	    return getList(select);
	}
    }

    /**
     * 
     * @return All the user's descriptions not in the public space
     */
    public List<T> getUserspaceDescriptions(Number userId) throws DataAccessException {
	String select = getSelectStatement().append(" WHERE is_deleted = false AND is_public = false AND user_id = ?").append(getOrderByClause()).toString();
	return getList(select, userId);
    }

    public void setDeleted(AbstractDescription desc, boolean isDeleted) throws DataAccessException {
	TransactionStatus transaction = getTransaction();
	Number dbId = getDbId(desc.getId());
	StringBuilder update = new StringBuilder("UPDATE ").append(getTableName());
	update.append(" SET is_deleted = ").append(Boolean.toString(isDeleted)).append(" WHERE " + COLUMN_ID + " = ?");
	getSimpleJdbcTemplate().update(update.toString(), dbId);
	txManager.commit(transaction);
    }

    public void setPublished(Number id, boolean published) {
	TransactionStatus transaction = getTransaction();
	StringBuilder update = new StringBuilder("UPDATE ").append(getTableName());
	update.append(" SET is_public = ? WHERE " + COLUMN_ID + " = ?");
	getSimpleJdbcTemplate().update(update.toString(), published, id);
	txManager.commit(transaction);
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
     * @return the rowMapper
     */
    @Override
    protected ParameterizedRowMapper<T> getRowMapper() {
	return rowMapper;
    }

    /**
     * Inserts parameters int <column, value> parameters map
     * @param params
     * @param description
     * @param contentId
     * @param userId
     * @param isPublic 
     */
    protected void putInsertParameters(Map<String, Object> params, AbstractDescription description, Number contentId, Number userId, boolean isPublic) {
	params.put("content_id", contentId);
	params.put("user_id", userId);
	params.put("is_public", isPublic);
	params.put("is_deleted", Boolean.FALSE);
	params.put(getCMDIdColumn(), description.getId());
	params.put("name", description.getName());
	params.put("description", description.getDescription());
	params.put("creator_name", description.getCreatorName());
	params.put("group_name", description.getGroupName());
	params.put("domain_name", description.getDomainName());
	params.put("href", description.getHref());
	params.put("registration_date", extractTimestamp(description));
    }

    /**
     * Sets values on a new description object from specified ResultSet
     * @param rs ResultSet from database query
     * @param newDescription Newly created description object to be filled
     * @throws SQLException 
     */
    protected void setDescriptionValuesFromResultSet(ResultSet rs, AbstractDescription newDescription) throws SQLException {
	Timestamp registrationDate = rs.getTimestamp("registration_date");
	newDescription.setName(rs.getString("name"));
	newDescription.setDescription(rs.getString("description"));
	newDescription.setId(rs.getString(getCMDIdColumn()));
	newDescription.setRegistrationDate(registrationDate == null ? null : AbstractDescription.createNewDate(registrationDate.getTime()));
	newDescription.setCreatorName(rs.getString("creator_name"));
	newDescription.setDomainName(rs.getString("domain_name"));
	newDescription.setGroupName(rs.getString("group_name"));
	newDescription.setHref(rs.getString("href"));

	Object userId = rs.getObject("user_id");
	if (!rs.wasNull()) {
	    newDescription.setUserId(userId.toString());
	}
    }

    protected void appendUpdateColumnsStatement(StringBuilder updateDescription) {
	updateDescription.append(" SET name = ?, description = ?, registration_date=?, creator_name=?, domain_name=?, group_name=?, href=?");
    }

    protected List getUpdateParameterValues(AbstractDescription description) {
	List updateParams = Arrays.asList(description.getName(), description.getDescription(),
		extractTimestamp(description), description.getCreatorName(), description.getDomainName(),
		description.getGroupName(), description.getHref());
	return updateParams;
    }
    /*
     * DAO HELPER METHODS
     */

    private StringBuilder getSelectStatement(String... where) throws DataAccessException {
	StringBuilder select = new StringBuilder("SELECT ").append(getDescriptionColumnList());
	select.append(" FROM ").append(getTableName());
	if (where.length > 0) {
	    select.append(" ");
	    for (String str : where) {
		select.append(" ").append(str);
	    }
	}
	return select;
    }

    /**
     * 
     * @return List of all description columns to be used in SELECT queries
     */
    protected StringBuilder getDescriptionColumnList() {

	StringBuilder sb = new StringBuilder();
	sb.append(getOrderByColumn());
	sb.append(",description,registration_date,creator_name,domain_name,group_name,href,user_id,");
	sb.append(getCMDIdColumn());
	return sb;
    }

    private String getOrderByColumn() {
	return "name";
    }

    private String getOrderByClause() {
	return " order by upper(" + getOrderByColumn() + "), " + getCMDIdColumn() + " asc ";
    }
    private final ParameterizedRowMapper<T> rowMapper = new ParameterizedRowMapper<T>() {

	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
	    try {
		AbstractDescription newDescription = (AbstractDescription) _class.newInstance();
		setDescriptionValuesFromResultSet(rs, newDescription);
		return (T) newDescription;
	    } catch (InstantiationException ex) {
		LOG.error("Error in row mapping", ex);
	    } catch (IllegalAccessException ex) {
		LOG.error("Error in row mapping", ex);
	    }
	    return null;
	}
    };

    private TransactionStatus getTransaction() {
	return txManager.getTransaction(txDefinition);
    }
}
