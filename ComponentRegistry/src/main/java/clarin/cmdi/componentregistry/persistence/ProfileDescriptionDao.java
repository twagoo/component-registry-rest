package clarin.cmdi.componentregistry.persistence;

import java.util.List;

import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 *
 */
public interface ProfileDescriptionDao extends AbstractDescriptionDao<ProfileDescription>{

	List<ProfileDescription> getPublicProfileDescriptions();

}