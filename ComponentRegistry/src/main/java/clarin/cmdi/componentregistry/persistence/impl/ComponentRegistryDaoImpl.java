package clarin.cmdi.componentregistry.persistence.impl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import clarin.cmdi.componentregistry.model.BaseComponent;
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
public abstract class ComponentRegistryDaoImpl extends
	NamedParameterJdbcDaoSupport implements ComponentRegistryDao {

    public ComponentRegistryDaoImpl() {
    }

    protected BaseComponent getFirstOrNull(StringBuilder selectQuery, Object... args) {
	return getFirstOrNull(selectQuery.toString(), args);
    }

    protected BaseComponent getFirstOrNull(String selectQuery, Object... args) {
	List<BaseComponent> list = getList(selectQuery, args);
	if (list.size() > 0) {
	    return list.get(0);
	} else {
	    return null;
	}
    }

    protected List<BaseComponent> getList(StringBuilder selectQuery, Object... args) {
	return getList(selectQuery.toString(), args);
    }

    protected List<BaseComponent> getList(String selectQuery, Object... args) {
	return getJdbcTemplate().query(selectQuery, getRowMapper(), args);
    }

    /**
     * @return the rowMapper
     */
    protected abstract ParameterizedRowMapper<BaseComponent> getRowMapper();

    @Autowired
    public void setNonFinalJdbcTemplate(JdbcTemplate jdbcTemplate) {
	super.setJdbcTemplate(jdbcTemplate);
    }
    
    @Override
    public void setDatasourceProperty(DataSource ds) {
	super.setDataSource(ds);
    }
    
}
