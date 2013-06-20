package clarin.cmdi.componentregistry.impl.database;

import java.util.List;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 * Logic and constants shared by the dao's of the DB implementation
 *
 * TODO: For future extensions we may want to use {@link org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport}
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @param <T> Type the dao maps to
 */
public abstract class ComponentRegistryDao<T> extends SimpleJdbcDaoSupport {

    public final static String TABLE_COMMENTS = "comments";
    public final static String TABLE_COMPONENT_DESCRIPTION = "component_description";
    public final static String TABLE_PROFILE_DESCRIPTION = "profile_description";
    public final static String TABLE_XML_CONTENT = "xml_content";
    public final static String TABLE_REGISTRY_USER = "registry_user";
    public final static String COLUMN_ID = "id";

    public ComponentRegistryDao() {
    }

    protected T getFirstOrNull(StringBuilder selectQuery, Object... args) {
	return getFirstOrNull(selectQuery.toString(), args);
    }

    protected T getFirstOrNull(String selectQuery, Object... args) {
	List<T> list = getList(selectQuery, args);
	if (list.size() > 0) {
	    return list.get(0);
	} else {
	    return null;
	}
    }

    protected List<T> getList(StringBuilder selectQuery, Object... args) {
	return getList(selectQuery.toString(), args);
    }

    protected List<T> getList(String selectQuery, Object... args) {
	return getSimpleJdbcTemplate().query(selectQuery, getRowMapper(), args);
    }

    /**
     * @return the rowMapper
     */
    protected abstract ParameterizedRowMapper<T> getRowMapper();
}
