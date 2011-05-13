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
        return getPublicDescriptions(ComponentDescription.class);
    }
}
