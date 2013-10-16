package clarin.cmdi.componentregistry.impl.database;

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
import clarin.cmdi.componentregistry.ComponentStatus;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.OwnerUser;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.frontend.CMDItemInfo;
import clarin.cmdi.componentregistry.frontend.SubmitFailedException;
import clarin.cmdi.componentregistry.model.Component;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
public class AdminRegistry {

    private final static Logger LOG = LoggerFactory
	    .getLogger(AdminRegistry.class);
    private ComponentRegistryFactory componentRegistryFactory;
    private ComponentDao componentDao;
    private MDMarshaller marshaller;

    public void setComponentRegistryFactory(
	    ComponentRegistryFactory componentRegistryFactory) {
	this.componentRegistryFactory = componentRegistryFactory;
    }

    public void setComponentDao(ComponentDao componentDao) {
	this.componentDao = componentDao;
    }

    public void setMarshaller(MDMarshaller marshaller) {
	this.marshaller = marshaller;
    }

    public void submitFile(CMDItemInfo info, Principal userPrincipal)
	    throws SubmitFailedException {
	try {
	    Component originalDescription = info.getDataNode()
		    .getDescription();
	    Component description = null;
	    CMDComponentSpec spec = null;
	    if (originalDescription.isProfile()) {
		description = marshaller.unmarshal(ProfileDescription.class,
			IOUtils.toInputStream(info.getDescription(), "UTF-8"),
			null);
	    } else {
		description = marshaller.unmarshal(ComponentDescription.class,
			IOUtils.toInputStream(info.getDescription(), "UTF-8"),
			null);
	    }
	    spec = marshaller.unmarshal(CMDComponentSpec.class,
		    IOUtils.toInputStream(info.getContent(), "UTF-8"), null);
	    checkId(originalDescription.getId(), description.getId());

	    int result = getRegistry(userPrincipal, originalDescription, info)
		    .update(description, spec, userPrincipal,
			    info.isForceUpdate());
	    if (result < 0) {
		throw new SubmitFailedException(
			"Problem occured while registering, please check the tomcat logs for errors.");
	    }
	} catch (JAXBException e) {
	    throw new SubmitFailedException(e);
	} catch (IOException e) {
	    throw new SubmitFailedException(e);
	}
    }

    private void checkId(String id, String id2) throws SubmitFailedException {
	if (id == null || id2 == null || !id.equals(id2)) {
	    throw new SubmitFailedException(
		    "Id's do not match up, you cannot edit id's: id1=" + id
			    + ", id2=" + id2);
	}
    }

    public void undelete(CMDItemInfo info) throws SubmitFailedException {
	Component desc = info.getDataNode().getDescription();
	try {
	    componentDao.setDeleted(desc, false);
	} catch (DataAccessException e) {
	    throw new SubmitFailedException("Undelete failed", e);
	}
    }

    public void delete(CMDItemInfo info, Principal userPrincipal)
	    throws SubmitFailedException {
	String id = info.getName();
	Component desc = info.getDataNode().getDescription();
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

    private void deleteFromRegistry(Principal userPrincipal,
	    Component desc, CMDItemInfo info) throws IOException,
	    UserUnauthorizedException, ComponentRegistryException {
	ComponentRegistry registry = getRegistry(userPrincipal, desc, info);
	LOG.info("Deleting item: " + desc);
	if (desc.isProfile()) {
	    registry.deleteMDProfile(desc.getId(), userPrincipal);
	} else {
	    registry.deleteMDComponent(desc.getId(), userPrincipal,
		    info.isForceUpdate());
	}
    }

    private ComponentRegistry getRegistry(Principal userPrincipal,
	    Component desc, CMDItemInfo info) {
	ComponentRegistry registry = componentRegistryFactory
		.getPublicRegistry();
	// TODO: More generic check
	if (info.getStatus() == ComponentStatus.PRIVATE /*
							 * || info.getStatus()
							 * == ComponentStatus.
							 * DEVELOPMENT
							 */) {
	    registry = componentRegistryFactory.getOtherUserComponentRegistry(
		    userPrincipal, info.getStatus(),
		    new OwnerUser(Integer.parseInt(desc.getUserId())));
	}
	return registry;
    }
}
