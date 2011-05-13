package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.ComponentDescription;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import static clarin.cmdi.componentregistry.impl.database.ComponentDescriptionDatabase.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentDescriptionDao extends AbstractDescriptionDao {

    @Override
    protected String getTableName() {
        return TABLE_COMPONENT_DESCRIPTION;
    }

    @Override
    protected String getCMDIdColumn() {
        return "component_id";
    }

    public List<ComponentDescription> getPublicComponentDescriptions() {
        String select = "select name, description from " + TABLE_COMPONENT_DESCRIPTION;

        ParameterizedRowMapper<ComponentDescription> rowMapper = new ParameterizedRowMapper<ComponentDescription>() {

            @Override
            public ComponentDescription mapRow(ResultSet rs, int rowNumber) throws SQLException {
                ComponentDescription cd = new ComponentDescription();
                cd.setName(rs.getString("name"));
                cd.setDescription(rs.getString("description"));
                return cd;
            }
        };

        return getSimpleJdbcTemplate().query(select, rowMapper);
    }
}
