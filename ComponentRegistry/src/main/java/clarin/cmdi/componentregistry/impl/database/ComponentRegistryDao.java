package clarin.cmdi.componentregistry.impl.database;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * Logic and constants shared by the dao's of the DB implementation
 * 
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 * @param <T>
 *            Type the dao maps to
 */
public abstract class ComponentRegistryDao<T> extends
	NamedParameterJdbcDaoSupport implements IComponentRegistryDao<T> {

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
	return getJdbcTemplate().query(selectQuery, getRowMapper(), args);
    }

    /**
     * @return the rowMapper
     */
    protected abstract ParameterizedRowMapper<T> getRowMapper();

    @Override
    @Autowired
    public void setDatasourceProperty(DataSource ds) {
	super.setDataSource(ds);
    }

}
