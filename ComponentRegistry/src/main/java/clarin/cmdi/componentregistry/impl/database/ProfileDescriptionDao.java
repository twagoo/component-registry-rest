package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.util.List;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ProfileDescriptionDao extends SimpleJdbcDaoSupport {

    List<ProfileDescription> getPublicProfileDescriptions() {
        String select = "select name, description from component_description";

        ParameterizedRowMapper<ProfileDescription> rowMapper = new ParameterizedBeanPropertyRowMapper<ProfileDescription>();

        return getSimpleJdbcTemplate().query(select, rowMapper);
    }
}
