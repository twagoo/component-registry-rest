package clarin.cmdi.componentregistry.impl.database;

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
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import static clarin.cmdi.componentregistry.impl.database.ComponentDescriptionDatabase.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AbstractDescriptionDao<T extends AbstractDescription>
	extends SimpleJdbcDaoSupport {

    private final static Logger LOG = LoggerFactory.getLogger(
	    AbstractDescriptionDao.class);

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
     * @param cmdId Profile or component Id (not primary key)
     * @return String value of XML content for profile or component
     */
    public String getContent(String cmdId) {
	String select = "select content from " + TABLE_XML_CONTENT
		+ " join " + getTableName() + " on " + TABLE_XML_CONTENT + "." + COLUMN_ID + " = " + getTableName() + ".content_id"
		+ " where " + getTableName() + "." + getCMDIdColumn() + " = :id";


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
    public Number insertComponent(final AbstractDescription description,
	    final String content) {
	SimpleJdbcInsert insert = new SimpleJdbcInsert(getDataSource()).
		withTableName(TABLE_XML_CONTENT).usingGeneratedKeyColumns(
		COLUMN_ID);
	Number contentId = insert.executeAndReturnKey(Collections.singletonMap(
		"content", (Object) content));

	SimpleJdbcInsert insertDescription = new SimpleJdbcInsert(
		getDataSource()).withTableName(getTableName()).
		usingGeneratedKeyColumns(COLUMN_ID);
	Map<String, Object> params = new HashMap<String, Object>();
	params.put("content_id", contentId);
	params.put("is_public", Boolean.TRUE);
	params.put("is_deleted", Boolean.FALSE);
	params.put(getCMDIdColumn(), description.getId());
	params.put("name", description.getName());
	params.put("description", description.getDescription());
	return insertDescription.executeAndReturnKey(params);
    }

    /**
     * Retrieves description by it's primary key Id
     * @param id Description key
     * @return The description, if it exists; null otherwise
     */
    protected T getById(Number id) {
	return getFirstOrNull(getSelectStatement("where id = :id"), id);
    }

    /**
     *
     * @return All descriptions in the public space
     */
    protected List<T> getPublicDescriptions() {
	return getList(getSelectStatement(" where is_public = TRUE"));
    }

    /**
     *
     * @return All the user's descriptions not in the public space
     */
    protected List<T> getUserspaceDescriptions(Number userId) {
	String select = getSelectStatement().
		append(" join " + ComponentDescriptionDatabase.TABLE_REGISTRY_USER).
		append("     on user_id = " + ComponentDescriptionDatabase.TABLE_REGISTRY_USER + ".id").
		append(" where is_public = FALSE and " + ComponentDescriptionDatabase.TABLE_REGISTRY_USER + ".id = :userId").
		toString();
	return getList(select, userId);
    }

    /*
     * DAO HELPER METHODS (may well be moved to some other place in class hierarchy at a later time)
     */
    private T getFirstOrNull(StringBuilder selectQuery, Object... args) {
	return getFirstOrNull(selectQuery.toString(), args);
    }

    private T getFirstOrNull(String selectQuery, Object... args) {
	List<T> list = getList(selectQuery, args);
	if (list.size() > 0) {
	    return list.get(0);
	} else {
	    return null;
	}
    }

    private List<T> getList(StringBuilder selectQuery, Object... args) {
	return getList(selectQuery.toString(), args);
    }

    private List<T> getList(String selectQuery, Object... args) {
	return getSimpleJdbcTemplate().query(selectQuery, rowMapper, args);
    }

    private StringBuilder getSelectStatement(String... where) {
	StringBuilder sb = new StringBuilder();
	sb.append("select ").append(getDescriptionColumnList()).
		append(" from ").append(getTableName());
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
    private String getDescriptionColumnList() {
	return "name, description, " + getCMDIdColumn();
    }
    private final ParameterizedRowMapper<T> rowMapper = new ParameterizedRowMapper<T>() {

	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
	    try {
		AbstractDescription newDescription = (AbstractDescription) _class.
			newInstance();
		newDescription.setName(rs.getString("name"));
		newDescription.setDescription(rs.getString("description"));
		newDescription.setId(rs.getString(getCMDIdColumn()));
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
