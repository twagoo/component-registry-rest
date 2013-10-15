package clarin.cmdi.componentregistry.persistence.impl;

import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.UserDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DaoSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
@Repository
public class UserDaoImpl extends JdbcDaoSupport implements UserDao {

    private final static String SELECT_BASE = "SELECT id, name, principal_name FROM registry_user";

    private RowMapper<RegistryUser> userRowMapper = new RowMapper<RegistryUser>() {

	@Override
	public RegistryUser mapRow(ResultSet rs, int rowNum)
		throws SQLException {
	    RegistryUser user = new RegistryUser();
	    user.setId(rs.getLong("id"));
	    user.setName(rs.getString("name"));
	    user.setPrincipalName(rs.getString("principal_name"));
	    return user;
	}
    };

    public List<RegistryUser> getAllUsers() throws DataAccessException {
	return getJdbcTemplate().query(
		"select * from registry_user order by id", userRowMapper);
    }

    /**
     * 
     * @param principalName
     *            User's principal name
     * @return User, if it exists
     * @throws DataAccessException
     */
    public RegistryUser getByPrincipalName(String principalName)
	    throws DataAccessException {
	List<RegistryUser> list = getJdbcTemplate()
		.query(
			"select * from registry_user where principal_name = ? order by id",
			new Object[] { principalName }, userRowMapper);
	if (list.isEmpty())
	    return null;
	return list.get(0);
    }

    /**
     * 
     * @param id
     *            Database record id (key)
     * @return User, if it exists
     * @throws DataAccessException
     */
    public RegistryUser getById(Number id) throws DataAccessException {
	return (RegistryUser) getJdbcTemplate()
		.queryForObject(
			"select * from registry_user where id = ? order by id",
			new Object[] { id }, userRowMapper);
    }

    /**
     * 
     * @param user
     * @return Record id of the inserted user
     * @throws DataAccessException
     */
    public Number insertUser(RegistryUser user) throws DataAccessException {
	SimpleJdbcInsert insert = new SimpleJdbcInsert(getDataSource())
		.withTableName("registry_user").usingGeneratedKeyColumns(
			"id");

	Map<String, Object> params = new HashMap<String, Object>();
	params.put("name", user.getName());
	params.put("principal_name", user.getPrincipalName());
	return insert.executeAndReturnKey(params);
    }

    /**
     * Updates some data stored about the user. At this point only the display
     * name will be updated. ID is taken from 'id' parameter, not user object.
     * 
     * @param id
     *            ID of user to update
     * @param user
     *            object containing new info
     */
    public void updateUser(Number id, RegistryUser user)
	    throws DataAccessException {
	String updateString = String.format(
		"UPDATE %1$s SET name = ? WHERE %2$s = ?", "registry_user",
		"id");
	getJdbcTemplate().update(updateString, user.getName(), id);
    }

    private final ParameterizedRowMapper<RegistryUser> rowMapper = new ParameterizedRowMapper<RegistryUser>() {

	@Override
	public RegistryUser mapRow(ResultSet rs, int rowNumber)
		throws SQLException {
	    RegistryUser user = new RegistryUser();
	    user.setId(rs.getInt("id"));
	    user.setName(rs.getString("name"));
	    user.setPrincipalName(rs.getString("principal_name"));
	    return user;
	}
    };

    @Resource(name="dataSource")
    public void setDataSourceProperty(DataSource ds) {
	super.setDataSource(ds);
    }
}
