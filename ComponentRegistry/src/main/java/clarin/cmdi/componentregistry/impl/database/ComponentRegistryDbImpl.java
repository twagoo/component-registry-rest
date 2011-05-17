package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryUtils;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

/**
 * Implementation of ComponentRegistry that uses Database Acces Objects for
 * accessing the registry (ergo: a database implementation)
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryDbImpl implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryDbImpl.class);
    private Number userId;
    @Autowired
    private ProfileDescriptionDao profileDescriptionDao;
    @Autowired
    private ComponentDescriptionDao componentDescriptionDao;
    @Autowired
    Configuration configuration;

    /**
     * Default constructor, makes this a (spring) bean. No user is set, so
     * public registry by default. Use setUser() to make it a user registry.
     * @see setUser
     */
    public ComponentRegistryDbImpl() {
    }

    /**
     * Creates a new ComponentRegistry (either public or not) for the provided user
     * @param userId User id of the user to create registry for. Pass null for public
     */
    public ComponentRegistryDbImpl(Number userId) {
	this.userId = userId;
    }

    @Override
    public List<ProfileDescription> getProfileDescriptions() {
	try {
	    if (isPublic()) {
		return profileDescriptionDao.getPublicProfileDescriptions();
	    } else {
		return profileDescriptionDao.getUserspaceDescriptions(getUserId());
	    }
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to get profile descriptions", ex);
	    throw ex;
	}
    }

    @Override
    public ProfileDescription getProfileDescription(String id) {
	return profileDescriptionDao.getByCmdId(id);
    }

    @Override
    public List<ComponentDescription> getComponentDescriptions() {
	try {
	    if (isPublic()) {
		return componentDescriptionDao.getPublicComponentDescriptions();
	    } else {
		return componentDescriptionDao.getUserspaceDescriptions(getUserId());
	    }
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to get component descriptions", ex);
	    throw ex;
	}
    }

    @Override
    public ComponentDescription getComponentDescription(String id) {
	return componentDescriptionDao.getByCmdId(id);
    }

    @Override
    public CMDComponentSpec getMDProfile(String id) {
	return getMDComponent(id, profileDescriptionDao);
    }

    @Override
    public CMDComponentSpec getMDComponent(String id) {
	return getMDComponent(id, componentDescriptionDao);
    }

    private CMDComponentSpec getMDComponent(String id, AbstractDescriptionDao dao) {
	String xml = dao.getContent(id);
	if (xml != null) {
	    try {
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		return MDMarshaller.unmarshal(CMDComponentSpec.class, is, MDMarshaller.
			getCMDComponentSchema());
	    } catch (JAXBException ex) {
		LOG.error(null, ex);
	    }
	}
	return null;
    }

    @Override
    public int register(AbstractDescription description, CMDComponentSpec spec) {
	ComponentRegistryUtils.enrichSpecHeader(spec, description);
	try {
	    OutputStream os = new ByteArrayOutputStream();
	    MDMarshaller.marshal(spec, os);
	    String xml = os.toString();
	    if (description.isProfile()) {
		profileDescriptionDao.insertDescription(description, xml, isPublic(), userId);
	    } else {
		componentDescriptionDao.insertDescription(description, xml, isPublic(), userId);
	    }
	    return 0;
	} catch (JAXBException ex) {
	    LOG.error(null, ex);
	} catch (UnsupportedEncodingException ex) {
	    LOG.error(null, ex);
	} catch (DataAccessException ex) {
	    LOG.error("Database error while registering component", ex);
	}
	return -1;
    }

    @Override
    public int update(AbstractDescription description, CMDComponentSpec spec) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int publish(AbstractDescription desc, CMDComponentSpec spec, Principal principal) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getMDProfileAsXml(String profileId, OutputStream output) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getMDProfileAsXsd(String profileId, OutputStream outputStream) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getMDComponentAsXml(String componentId, OutputStream output) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getMDComponentAsXsd(String componentId, OutputStream outputStream) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteMDProfile(String profileId, Principal principal) throws IOException, UserUnauthorizedException, DeleteFailedException {
	ProfileDescription desc = getProfileDescription(profileId);
	if (desc != null) {
	    checkAuthorisation(desc, principal);
	    checkAge(desc, principal);
	    profileDescriptionDao.setDeleted(profileId);
	}
    }

    private void checkAuthorisation(AbstractDescription desc, Principal principal) throws UserUnauthorizedException {
	if (!desc.isThisTheOwner(principal.getName()) && !configuration.
		isAdminUser(principal)) {
	    throw new UserUnauthorizedException("Unauthorized operation user '" + principal.
		    getName()
		    + "' is not the creator (nor an administrator) of the " + (desc.
		    isProfile() ? "profile" : "component") + "(" + desc
		    + ").");
	}
    }

    private void checkAge(AbstractDescription desc, Principal principal) throws DeleteFailedException {
	if (isPublic() && !configuration.isAdminUser(principal)) {
	    try {
		Date regDate = AbstractDescription.getDate(desc.
			getRegistrationDate());
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
		if (regDate.before(calendar.getTime())) { //More then month old
		    throw new DeleteFailedException(
			    "The "
			    + (desc.isProfile() ? "Profile" : "Component")
			    + " is more then a month old and cannot be deleted anymore. It might have been used to create metadata, deleting it would invalidate that metadata.");
		}
	    } catch (ParseException e) {
		LOG.error("Cannot parse date of " + desc + " Error:" + e);
	    }
	}
    }

    @Override
    public void deleteMDComponent(String componentId, Principal principal, boolean forceDelete) throws IOException, UserUnauthorizedException, DeleteFailedException {
	ComponentDescription desc = componentDescriptionDao.getByCmdId(componentId);
	if (desc != null) {
	    checkAuthorisation(desc, principal);
	    checkAge(desc, principal);

	    // TODO : check still used!
//            if (!forceDelete) {
//                checkStillUsed(componentId);
//            }
	    componentDescriptionDao.setDeleted(componentId);
	}
    }

    @Override
    public List<ComponentDescription> getUsageInComponents(String componentId) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ProfileDescription> getUsageInProfiles(String componentId) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isPublic() {
	return null == userId;
    }

    public void setPublic() {
	this.userId = null;
    }

    /**
     * @return The user, or null if this is the public registry.
     */
    public Number getUserId() {
	return userId;
    }

    /**
     * @param User for which this should be the registry. Pass null for
     * the public registry
     */
    public void setUserId(Number user) {
	this.userId = user;
    }
}
