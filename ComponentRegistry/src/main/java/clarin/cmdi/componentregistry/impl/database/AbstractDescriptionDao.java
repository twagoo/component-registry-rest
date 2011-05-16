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
public abstract class AbstractDescriptionDao<T extends AbstractDescription> extends SimpleJdbcDaoSupport {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractDescriptionDao.class);

    protected abstract String getTableName();
    protected abstract String getCMDIdColumn();

    /**
     * Class object required to instantiate new description domain objects
     */
    private Class<T> _class;

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


        List<String> result = getSimpleJdbcTemplate().query(select, new ParameterizedSingleColumnRowMapper<String>(), cmdId);
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
    public Number insertComponent(final AbstractDescription description, final String content) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(getDataSource()).withTableName(TABLE_XML_CONTENT).usingGeneratedKeyColumns(COLUMN_ID);
        Number contentId = insert.executeAndReturnKey(Collections.singletonMap("content", (Object) content));

        SimpleJdbcInsert insertDescription = new SimpleJdbcInsert(getDataSource()).withTableName(getTableName()).usingGeneratedKeyColumns(COLUMN_ID);
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
     *
     * @return All descriptions in the public space
     */
    protected List<T> getPublicDescriptions() {
        String select = "select name, description, " + getCMDIdColumn() + " from " + getTableName() + " where is_public = TRUE";
        return getSimpleJdbcTemplate().query(select, new AbstractDescriptionRowMapper());
    }

    private class AbstractDescriptionRowMapper<T> implements ParameterizedRowMapper<T> {

        @Override
        public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
            try {
                AbstractDescription newDescription = (AbstractDescription) _class.newInstance();
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
