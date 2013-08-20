package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.ComponentDescription;

import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * Serves components
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
@Repository
public class ComponentDescriptionDao extends
	AbstractDescriptionDao<ComponentDescription> implements
	IComponentDescriptionDao {

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

    @Override
    protected String getCommentsForeignKeyColumn() {
	return "component_description_id";
    }

    public List<ComponentDescription> getPublicComponentDescriptions() {
	return getPublicDescriptions();
    }
}
