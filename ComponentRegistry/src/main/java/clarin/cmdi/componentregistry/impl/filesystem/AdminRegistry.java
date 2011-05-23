package clarin.cmdi.componentregistry.impl.filesystem;

import java.io.IOException;
import java.security.Principal;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.frontend.CMDItemInfo;
import clarin.cmdi.componentregistry.frontend.SubmitFailedException;
import clarin.cmdi.componentregistry.impl.database.ComponentDescriptionDao;
import clarin.cmdi.componentregistry.impl.database.ProfileDescriptionDao;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class AdminRegistry {
    private final static Logger LOG = LoggerFactory.getLogger(AdminRegistry.class);

    private ComponentRegistryFactory componentRegistryFactory;
    private ProfileDescriptionDao profileDescriptionDao;
    private ComponentDescriptionDao componentDescriptionDao;

    public void setComponentRegistryFactory(ComponentRegistryFactory componentRegistryFactory) {
	this.componentRegistryFactory = componentRegistryFactory;
    }

    public void setProfileDescriptionDao(ProfileDescriptionDao profileDescriptionDao) {
	this.profileDescriptionDao = profileDescriptionDao;
    }

    public void setComponentDescriptionDao(ComponentDescriptionDao componentDescriptionDao) {
	this.componentDescriptionDao = componentDescriptionDao;
    }

    public void submitFile(CMDItemInfo info, Principal userPrincipal) throws SubmitFailedException {
	try {
	    AbstractDescription originalDescription = info.getDataNode().getDescription();
	    AbstractDescription description = null;
	    CMDComponentSpec spec = null;
	    if (originalDescription.isProfile()) {
		description = MDMarshaller.unmarshal(ProfileDescription.class, IOUtils.toInputStream(info.getDescription(), "UTF-8"), null);
	    } else {
		description = MDMarshaller.unmarshal(ComponentDescription.class, IOUtils.toInputStream(info.getDescription(), "UTF-8"),
			null);
	    }
	    spec = MDMarshaller.unmarshal(CMDComponentSpec.class, IOUtils.toInputStream(info.getContent(), "UTF-8"), null);
	    checkId(originalDescription.getId(), description.getId());

	    int result = getRegistry(userPrincipal, originalDescription, info).update(description, spec, userPrincipal, info.isForceUpdate());
	    if (result < 0) {
		throw new SubmitFailedException("Problem occured while registering, please check the tomcat logs for errors.");
	    }
	} catch (JAXBException e) {
	    throw new SubmitFailedException(e);
	} catch (IOException e) {
	    throw new SubmitFailedException(e);
	}
    }

    private void checkId(String id, String id2) throws SubmitFailedException {
	if (id == null || id2 == null || !id.equals(id2)) {
	    throw new SubmitFailedException("Id's do not match up, you cannot edit id's: id1=" + id + ", id2=" + id2);
	}
    }

    public void undelete(CMDItemInfo info) throws SubmitFailedException {
	AbstractDescription desc = info.getDataNode().getDescription();
	try {
	    if (desc.isProfile()) {
		profileDescriptionDao.setDeleted(desc, false);
	    } else {
		componentDescriptionDao.setDeleted(desc, false);
	    }
	} catch (DataAccessException e) {
	    throw new SubmitFailedException("Undelete failed", e);
	}
    }

    public void delete(CMDItemInfo info, Principal userPrincipal) throws SubmitFailedException {
	String id = info.getName();
	AbstractDescription desc = info.getDataNode().getDescription();
	try {
	    deleteFromRegistry(userPrincipal, desc, info);
	    LOG.info("Deleted item: " + id);
	} catch (IOException e) {
	    throw new SubmitFailedException(e);
	} catch (UserUnauthorizedException e) {
	    throw new SubmitFailedException(e);
	} catch (DeleteFailedException e) {
	    throw new SubmitFailedException(e);
	} catch (ComponentRegistryException e) {
	    throw new SubmitFailedException(e);
	}

    }

    private void deleteFromRegistry(Principal userPrincipal, AbstractDescription desc, CMDItemInfo info) throws IOException,
	    UserUnauthorizedException, ComponentRegistryException {
	ComponentRegistry registry = getRegistry(userPrincipal, desc, info);
	LOG.info("Deleting item: " + desc);
	if (desc.isProfile()) {
	    registry.deleteMDProfile(desc.getId(), userPrincipal);
	} else {
	    registry.deleteMDComponent(desc.getId(), userPrincipal, info.isForceUpdate());
	}
    }

    private ComponentRegistry getRegistry(Principal userPrincipal, AbstractDescription desc, CMDItemInfo info) {
	ComponentRegistry registry = componentRegistryFactory.getPublicRegistry();
	if (info.isInUserWorkSpace()) {
	    registry = componentRegistryFactory.getOtherUserComponentRegistry(userPrincipal, desc.getUserId());
	}
	return registry;
    }

}
