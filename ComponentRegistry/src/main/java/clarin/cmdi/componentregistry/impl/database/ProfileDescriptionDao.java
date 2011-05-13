package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.util.List;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import static clarin.cmdi.componentregistry.impl.database.ComponentDescriptionDatabase.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ProfileDescriptionDao extends AbstractDescriptionDao {

    @Override
    protected String getTableName() {
        return TABLE_PROFILE_DESCRIPTION;
    }

    @Override
    protected String getCMDIdColumn() {
        return "profile_id";
    }

    List<ProfileDescription> getPublicProfileDescriptions() {
        String select = "select name, description from profile_description";

        ParameterizedRowMapper<ProfileDescription> rowMapper = new ParameterizedBeanPropertyRowMapper<ProfileDescription>();

        return getSimpleJdbcTemplate().query(select, rowMapper);
    }
}
