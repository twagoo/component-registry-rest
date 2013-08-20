package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ComponentStatus;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.Owner;
import clarin.cmdi.componentregistry.OwnerUser;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.model.RegistryUser;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

/**
 * Implementation of ComponentRegistryFactory that uses the
 * ComponentRegistryDbImpl implementation of ComponentRegistry for accessing the
 * registry
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class ComponentRegistryFactoryDbImpl implements ComponentRegistryFactory {

    private final static Logger LOG = LoggerFactory
	    .getLogger(ComponentRegistryFactoryDbImpl.class);
    @Autowired
    private Configuration configuration;
    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private IUserDAO userDao;
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
    public ComponentRegistry getComponentRegistry(ComponentStatus status,
	    Owner owner, UserCredentials credentials)
	    throws UserUnauthorizedException {
	switch (status) {
	case PRIVATE:
	    return getPrivateRegistry(owner, credentials);
	case PUBLISHED:
	    return getPublicRegistry();
	default:
	    // TODO: Add support for other status types
	    throw new UnsupportedOperationException(
		    "Unsupported component status" + status);
	}
    }

    private ComponentRegistry getPrivateRegistry(Owner owner,
	    UserCredentials credentials) throws IllegalArgumentException,
	    DataAccessException, UserUnauthorizedException {
	if (owner == null || owner instanceof OwnerUser) {
	    RegistryUser user = getOrCreateUser(credentials);
	    if (user != null) {
		if (owner != null && !user.getId().equals(owner.getId())) {
		    throw new UserUnauthorizedException(
			    "User cannot access other user's private registry");
		}

		try {
		    return getNewComponentRegistryForUser(user.getId());
		} catch (DataAccessException ex) {
		    LOG.error("Could not retrieve or create user", ex);
		    throw ex;
		}
	    } else {
		throw new UserUnauthorizedException(
			"No user credentials available cannot load userspace.");
	    }
	} else {
	    // TODO: Support group owners
	    throw new UnsupportedOperationException(
		    "Group owners not supported");
	}
    }

    @Override
    public ComponentRegistry getOtherUserComponentRegistry(
	    Principal adminPrincipal, ComponentStatus status, Owner owner) {
	try {
	    RegistryUser user;
	    if (owner instanceof OwnerUser) {
		user = userDao.getById(owner.getId());
	    } else {
		// TODO: Implement for groups
		throw new UnsupportedOperationException(
			"Groups not implemented yet");
	    }
	    ComponentRegistry result = null;
	    if (user != null) {
		if (configuration.isAdminUser(adminPrincipal)) {
		    result = getNewComponentRegistryForUser(user.getId());
		} else {
		    LOG.info("{} not found in list of {}",
			    adminPrincipal.getName(),
			    configuration.getAdminUsersArray().length);
		    throw new IllegalArgumentException("User "
			    + adminPrincipal.getName()
			    + " is not admin user cannot load userspace.");
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
	ComponentRegistryDbImpl componentRegistry = componentRegistryBeanFactory
		.getNewComponentRegistry();
	if (userId != null) {
	    // Null means public registry
	    // TODO: Make this more explicit
	    componentRegistry.setStatus(ComponentStatus.PRIVATE, new OwnerUser(
		    userId));
	}
	return componentRegistry;
    }

    @Override
    public RegistryUser getOrCreateUser(UserCredentials credentials) {
	if (credentials != null
		&& !ANONYMOUS_USER.equals(credentials.getPrincipalName())) {
	    String principalName = credentials.getPrincipalName();
	    return getOrCreateUser(principalName, credentials.getDisplayName());
	}
	return null;
    }

    private synchronized RegistryUser getOrCreateUser(String principalName,
	    String displayName) {
	// Try getting it from db
	RegistryUser user = userDao.getByPrincipalName(principalName);
	if (user == null) {
	    LOG.info(
		    "Request to create user with principal name {} and display name {}",
		    new Object[] { principalName, displayName });
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
