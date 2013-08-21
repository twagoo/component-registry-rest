package clarin.cmdi.componentregistry.persistence;

import java.util.List;

import clarin.cmdi.componentregistry.model.ComponentDescription;

/**
 * Interface for {@link ComponentDescription} DAOs
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface ComponentDescriptionDao extends AbstractDescriptionDao<ComponentDescription>{

	List<ComponentDescription> getPublicComponentDescriptions();

}