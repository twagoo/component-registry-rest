package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.RegistryUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
@Repository
public class UserDao extends ComponentRegistryDao<RegistryUser> implements IUserDAO{

	private final static String SELECT_BASE = "SELECT " + COLUMN_ID
			+ ", name, principal_name FROM " + TABLE_REGISTRY_USER;

	public List<RegistryUser> getAllUsers() throws DataAccessException {
		return getList(SELECT_BASE);
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
		return getFirstOrNull(SELECT_BASE + " WHERE principal_name = ?",
				principalName);
	}

	/**
	 * 
	 * @param id
	 *            Database record id (key)
	 * @return User, if it exists
	 * @throws DataAccessException
	 */
	public RegistryUser getById(Number id) throws DataAccessException {
		return getFirstOrNull(SELECT_BASE + " WHERE " + COLUMN_ID + " = ?", id);
	}

	/**
	 * 
	 * @param user
	 * @return Record id of the inserted user
	 * @throws DataAccessException
	 */
	public Number insertUser(RegistryUser user) throws DataAccessException {
		SimpleJdbcInsert insert = new SimpleJdbcInsert(getDataSource())
				.withTableName(TABLE_REGISTRY_USER).usingGeneratedKeyColumns(
						COLUMN_ID);

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
				"UPDATE %1$s SET name = ? WHERE %2$s = ?", TABLE_REGISTRY_USER,
				COLUMN_ID);
		getJdbcTemplate().update(updateString, user.getName(), id);
	}

	@Override
	protected ParameterizedRowMapper<RegistryUser> getRowMapper() {
		return rowMapper;
	}

	private final ParameterizedRowMapper<RegistryUser> rowMapper = new ParameterizedRowMapper<RegistryUser>() {

		@Override
		public RegistryUser mapRow(ResultSet rs, int rowNumber)
				throws SQLException {
			RegistryUser user = new RegistryUser();
			user.setId(rs.getInt(COLUMN_ID));
			user.setName(rs.getString("name"));
			user.setPrincipalName(rs.getString("principal_name"));
			return user;
		}
	};
}
