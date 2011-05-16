package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.ComponentDescription;
import java.util.List;
import static clarin.cmdi.componentregistry.impl.database.ComponentDescriptionDatabase.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentDescriptionDao extends AbstractDescriptionDao<ComponentDescription> {

    public ComponentDescriptionDao() {
        super(ComponentDescription.class);
    }

    @Override
    protected String getTableName() {
        return TABLE_COMPONENT_DESCRIPTION;
    }

    @Override
    protected String getCMDIdColumn() {
        return "component_id";
    }

    public List<ComponentDescription> getPublicComponentDescriptions() {
        return getPublicDescriptions();
    }
}
