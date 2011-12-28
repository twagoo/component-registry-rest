package clarin.cmdi.componentregistry.impl.database;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.model.RegistryUser;

/**
 * Implementation of ComponentRegistryFactory that uses the
 * ComponentRegistryDbImpl implementation of ComponentRegistry for accessing the
 * registry
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryFactoryDbImpl implements ComponentRegistryFactory {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryFactoryDbImpl.class);
    @Autowired
    private Configuration configuration;
    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private UserDao userDao;
    private ComponentRegistry publicComponentRegistry = null;

    @Override
    public List<ComponentRegistry> getAllUserRegistries() {
	// TODO: this probably could use some caching
	try {
	    List<RegistryUser> users = userDao.getAllUsers();
	    List<ComponentRegistry> registries = new ArrayList<ComponentRegistry>();
	    for (RegistryUser user : users) {
		registries.add(getNewComponentRegistryForUser(user.getId()));
	    }
	    return registries;
	} catch (DataAccessException ex) {
	    LOG.error("Could not retrieve users", ex);
	    throw ex;
	}
    }

    @Override
    public ComponentRegistry getComponentRegistry(boolean userspace, UserCredentials credentials) {
	ComponentRegistry result = null;
	if (userspace) {
	    RegistryUser user = getOrCreateUser(credentials);
	    if (user != null) {
		try {
		    result = getNewComponentRegistryForUser(user.getId());
		} catch (DataAccessException ex) {
		    LOG.error("Could not retrieve or create user", ex);
		    throw ex;
		}
	    } else {
		throw new IllegalArgumentException("No user credentials available cannot load userspace.");
	    }
	} else {
	    result = getPublicRegistry();
	}
	return result;
    }

    @Override
    public ComponentRegistry getOtherUserComponentRegistry(Principal adminPrincipal, String userId) {
	try {
	    RegistryUser user = userDao.getById(Integer.parseInt(userId));
	    ComponentRegistry result = null;
	    if (user != null) {
		if (configuration.isAdminUser(adminPrincipal)) {
		    result = getNewComponentRegistryForUser(user.getId());
		} else {
		    LOG.info(adminPrincipal.getName() + " not found in list of " + configuration.getAdminUsersArray().length);
		    throw new IllegalArgumentException("User " + adminPrincipal.getName() + " is not admin user cannot load userspace.");
		}
	    }
	    return result;
	} catch (DataAccessException ex) {
	    LOG.error("Could not retrieve user by id", ex);
	    throw ex;
	}
    }

    @Override
    public ComponentRegistry getPublicRegistry() {
	if (publicComponentRegistry == null) {
	    publicComponentRegistry = getNewComponentRegistryForUser(null);
	}
	return publicComponentRegistry;
    }

    private ComponentRegistry getNewComponentRegistryForUser(Number userId) {
	ComponentRegistryDbImpl componentRegistry = componentRegistryBeanFactory.getNewComponentRegistry();
	componentRegistry.setUserId(userId);
	return componentRegistry;
    }

    @Override
    public RegistryUser getOrCreateUser(UserCredentials credentials) {
	if (credentials != null && !ANONYMOUS_USER.equals(credentials.getPrincipalName())) {
	    String principalName = credentials.getPrincipalName();
	    return getOrCreateUser(principalName, credentials.getDisplayName());
	}
	return null;
    }

    private synchronized RegistryUser getOrCreateUser(String principalName, String displayName) {
	// Try getting it from db
	RegistryUser user = userDao.getByPrincipalName(principalName);
	if (user == null) {
	    // Create the new user
	    user = new RegistryUser();
	    user.setPrincipalName(principalName);
	    user.setName(displayName);
	    userDao.insertUser(user);
	    // Retrieve from db
	    user = userDao.getByPrincipalName(principalName);
	}
	return user;
    }
}
