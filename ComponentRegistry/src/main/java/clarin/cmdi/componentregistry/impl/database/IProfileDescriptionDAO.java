package clarin.cmdi.componentregistry.impl.database;

import java.util.List;

import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface IProfileDescriptionDAO extends IAbstractDescriptionDao<ProfileDescription>{

	List<ProfileDescription> getPublicProfileDescriptions();

}