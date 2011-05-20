package clarin.cmdi.componentregistry.impl.database;

import java.text.ParseException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.sql.SQLException;
import java.sql.ResultSet;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AbstractDescriptionDao<T extends AbstractDescription> extends ComponentRegistryDao<T> {

    private final static Logger LOG = LoggerFactory.getLogger(
	    AbstractDescriptionDao.class);
    private final static DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

    protected abstract String getTableName();

    protected abstract String getCMDIdColumn();
    /**
     * Class object required to instantiate new description domain objects
     */
    private final Class<T> _class;

    protected AbstractDescriptionDao(Class<T> _class) {
	this._class = _class;
    }

    public boolean isPublic(String cmdId) {
	String query = "SELECT COUNT(*) FROM " + getTableName() + " WHERE is_public = true AND " + getCMDIdColumn() + " = ?";
	return (0 < getSimpleJdbcTemplate().queryForInt(query, cmdId));
    }

    public boolean isInUserSpace(String cmdId, Number userId) {
	String query = "SELECT COUNT(*) FROM " + getTableName() + " WHERE is_public = false AND user_id = ? AND " + getCMDIdColumn() + " = ?";
	return (0 < getSimpleJdbcTemplate().queryForInt(query, userId, cmdId));
    }

    /**
     * 
     * @param cmdId Profile or component Id (not primary key)
     * @return String value of XML content for profile or component
     */
    public String getContent(String cmdId) throws DataAccessException {
	String select = "SELECT content FROM " + TABLE_XML_CONTENT
		+ " JOIN " + getTableName() + " ON " + TABLE_XML_CONTENT + "." + COLUMN_ID + " = " + getTableName() + ".content_id"
		+ " WHERE is_deleted = false AND " + getTableName() + "." + getCMDIdColumn() + " = ?";

	List<String> result = getSimpleJdbcTemplate().query(select,
		new ParameterizedSingleColumnRowMapper<String>(), cmdId);
	if (result.size() > 0) {
	    return result.get(0);
	} else {
	    return null;
	}
    }

    /**
     * @param description Description to insert
     * @param content Content to insert and refer to from description
     * @return Id of newly inserted description
     */
    public Number insertDescription(AbstractDescription description, String content, boolean isPublic, Number userId) throws DataAccessException {
	SimpleJdbcInsert insert = new SimpleJdbcInsert(getDataSource()).withTableName(TABLE_XML_CONTENT).usingGeneratedKeyColumns(
		COLUMN_ID);
	Number contentId = insert.executeAndReturnKey(Collections.singletonMap(
		"content", (Object) content));

	SimpleJdbcInsert insertDescription = new SimpleJdbcInsert(
		getDataSource()).withTableName(getTableName()).
		usingGeneratedKeyColumns(COLUMN_ID);
	Map<String, Object> params = new HashMap<String, Object>();
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
	return insertDescription.executeAndReturnKey(params);
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
     * @param id Id (key) of description record
     * @param description New values for description (leave null to not change)
     * @param content New content for description (leave null to not change)
     */
    public void updateDescription(Number id, AbstractDescription description, String content) {
	if (description != null) {
	    // Update description
	    StringBuilder updateDescription = new StringBuilder();
	    updateDescription.append("UPDATE ").append(getTableName());
	    updateDescription.append(" SET name = ?, description = ? ,registration_date=?,creator_name=?,domain_name=?,group_name=?,href=?");
	    updateDescription.append(" WHERE " + COLUMN_ID + " = ?");
	    getSimpleJdbcTemplate().update(updateDescription.toString(),
		    description.getName(), description.getDescription(), extractTimestamp(description), description.getCreatorName(),
			description.getDomainName(), description.getGroupName(), description.getHref(),
		    id);
	}

	if (content != null) {
	    // Update content
	    StringBuilder updateContent = new StringBuilder();
	    updateContent.append("UPDATE " + TABLE_XML_CONTENT + " SET content = ? WHERE " + COLUMN_ID + " = ");
	    updateContent.append("(SELECT content_id FROM ").append(getTableName()).
		    append(" WHERE " + COLUMN_ID + "= ?)");

	    getSimpleJdbcTemplate().update(updateContent.toString(),
		    content, id);
	}
    }

    /**
     * Retrieves description by it's primary key Id
     * @param id Description key
     * @return The description, if it exists; null otherwise
     */
    public T getById(Number id) throws DataAccessException {
	return getFirstOrNull(getSelectStatement("WHERE is_deleted = false AND id = ?"), id);
    }

    /**
     * Get by ComponentId / ProfileId, whether in userspace or public
     * @param id Full component id
     * @return The description, if it exists; null otherwise
     */
    public T getByCmdId(String id) throws DataAccessException {
	return getFirstOrNull(getSelectStatement("WHERE is_deleted = false AND " + getCMDIdColumn() + " = ?"), id);
    }

    /**
     * Get by ComponentId / ProfileId
     * @param id Full component id
     * @param userId Db id of user for workspace; null for public space
     * @return The description, if it exists; null otherwise
     */
    public T getByCmdId(String id, Number userId) throws DataAccessException {
	String query = "WHERE is_deleted = false AND " + getCMDIdColumn() + " = ?";
	if (userId == null) {
	    return getFirstOrNull(getSelectStatement(query + " AND is_public = true"), id);
	} else {
	    return getFirstOrNull(getSelectStatement(query + " AND is_public = false AND user_id = ?"), id, userId);
	}
    }

    /**
     *
     * @param cmdId CMD Id of description
     * @return Database id for description record
     */
    public Number getDbId(String cmdId) {
	// Check for is_deleted is important, because an id only has to be unique
	// among non-deleted descriptions
	String query = "SELECT " + COLUMN_ID + " FROM " + getTableName() + " WHERE is_deleted = false AND " + getCMDIdColumn() + " = ?";
	return getSimpleJdbcTemplate().queryForInt(query, cmdId);
    }

    /**
     *
     * @return All descriptions in the public space
     */
    public List<T> getPublicDescriptions() throws DataAccessException {
	return getList(getSelectStatement(" WHERE is_deleted = false AND is_public = true"));
    }

    /**
     *
     * @return All the user's descriptions not in the public space
     */
    public List<T> getUserspaceDescriptions(Number userId) throws DataAccessException {
	String select = getSelectStatement().
		append(" WHERE is_deleted = false AND is_public = false AND user_id = ?").
		toString();
	return getList(select, userId);
    }

    /**
     *
     * @param Full component id
     */
    public void setDeleted(Number id) throws DataAccessException {
	String update = "UPDATE " + getTableName() + " SET is_deleted = true WHERE " + COLUMN_ID + " = ?";
	getSimpleJdbcTemplate().update(update, id);
    }

    public void setPublished(Number id, boolean published) {
	String update = "UPDATE " + getTableName() + " SET is_public = ? WHERE " + COLUMN_ID + " = ?";
	getSimpleJdbcTemplate().update(update, published, id);
    }

    /**
     *
     * @param id Id of description record
     * @return Principal name of description's owner, if any. Otherwise, null.
     */
    public String getOwnerPrincipalName(Number id) {
	String select = "SELECT principal_name FROM " + TABLE_REGISTRY_USER
		+ " JOIN " + getTableName()
		+ " ON user_id = " + TABLE_REGISTRY_USER + ".id "
		+ " WHERE " + getTableName() + ".id = ?";
	List<String> owner = getSimpleJdbcTemplate().query(select, new ParameterizedSingleColumnRowMapper<String>(), id);
	if (owner.isEmpty()) {
	    return null;
	} else {
	    return owner.get(0);
	}
    }

    /*
     * DAO HELPER METHODS
     */
    private StringBuilder getSelectStatement(String... where) throws DataAccessException {
	StringBuilder sb = new StringBuilder();
	sb.append("SELECT ").append(getDescriptionColumnList()).
		append(" FROM ").append(getTableName());
	if (where.length > 0) {
	    sb.append(" ");
	    for (String str : where) {
		sb.append(" ").append(str);
	    }
	}
	return sb;
    }

    /**
     *
     * @return List of all description columns to be used in SELECT queries
     */
    private StringBuilder getDescriptionColumnList() {
	StringBuilder sb = new StringBuilder();
	sb.append("name,description,registration_date,creator_name,domain_name,group_name,href,user_id,");
	sb.append(getCMDIdColumn());
	return sb;
    }

    /**
     * @return the rowMapper
     */
    @Override
    protected ParameterizedRowMapper<T> getRowMapper() {
	return rowMapper;
    }
    private final ParameterizedRowMapper<T> rowMapper = new ParameterizedRowMapper<T>() {

	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
	    try {
		Timestamp registrationDate = rs.getTimestamp("registration_date");

		AbstractDescription newDescription = (AbstractDescription) _class.newInstance();
		newDescription.setName(rs.getString("name"));
		newDescription.setDescription(rs.getString("description"));
		newDescription.setId(rs.getString(getCMDIdColumn()));
		newDescription.setRegistrationDate(registrationDate == null
			? null : ISO_DATE_FORMAT.format(registrationDate));
		newDescription.setCreatorName(rs.getString("creator_name"));
		newDescription.setDomainName(rs.getString("domain_name"));
		newDescription.setGroupName(rs.getString("group_name"));
		newDescription.setHref(rs.getString("href"));

		Object userId = rs.getObject("user_id");
		if (!rs.wasNull()) {
		    newDescription.setUserId(userId.toString());
		}
		return (T) newDescription;
	    } catch (InstantiationException ex) {
		LOG.error("Error in row mapping", ex);
	    } catch (IllegalAccessException ex) {
		LOG.error("Error in row mapping", ex);
	    }
	    return null;
	}
    };
}
