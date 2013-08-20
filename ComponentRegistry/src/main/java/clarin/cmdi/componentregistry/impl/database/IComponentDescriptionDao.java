package clarin.cmdi.componentregistry.impl.database;

import java.util.List;

import clarin.cmdi.componentregistry.model.ComponentDescription;

/**
 * Interface for {@link ComponentDescription} DAOs
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface IComponentDescriptionDao extends IAbstractDescriptionDao<ComponentDescription>{

	List<ComponentDescription> getPublicComponentDescriptions();

}