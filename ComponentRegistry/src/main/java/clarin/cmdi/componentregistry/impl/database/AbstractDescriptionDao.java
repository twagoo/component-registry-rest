package clarin.cmdi.componentregistry.impl.database;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import static clarin.cmdi.componentregistry.impl.database.ComponentDescriptionDatabase.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AbstractDescriptionDao extends SimpleJdbcDaoSupport {

    protected abstract String getTableName();

    protected abstract String getCMDIdColumn();

    /**
     * 
     * @param cmdId Profile or component Id (not primary key)
     * @return String value of XML content for profile or component
     */
    public String getContent(String cmdId) {
        String select = "select content from " + TABLE_XML_CONTENT
                + " join " + getTableName() + " on " + TABLE_XML_CONTENT + "." + COLUMN_ID + " = " + getTableName() + ".content_id"
                + " where " + getTableName() + "." + getCMDIdColumn() + " = :id";
        return getSimpleJdbcTemplate().queryForObject(select, String.class, cmdId);
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
        params.put(getCMDIdColumn(), "clarin_" + contentId);
        params.put("name", description.getName());
        params.put("description", description.getDescription());
        return insertDescription.executeAndReturnKey(params);
    }
}
