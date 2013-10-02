package clarin.cmdi.componentregistry.persistence.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import clarin.cmdi.componentregistry.persistence.ComponentRegistryDao;

/**
 * Logic and constants shared by the dao's of the DB implementation
 * 
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 * @param <T>
 *            Type the dao maps to
 */
public abstract class ComponentRegistryDaoImpl<T> extends
	NamedParameterJdbcDaoSupport implements ComponentRegistryDao<T> {

    public ComponentRegistryDaoImpl() {
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
