package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.model.UserMapping.User;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of ComponentRegistryFactory that uses the
 * ComponentRegistryDbImpl implementation of ComponentRegistry for accessing the
 * registry
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryFactoryDbImpl implements ComponentRegistryFactory {

    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private UserDao userDao;

    @Override
    public List<ComponentRegistry> getAllUserRegistries() {
	//TODO: this probably could use some caching
	List<User> users = userDao.getAllUsers();
	List<ComponentRegistry> registries = new ArrayList<ComponentRegistry>();
	for (User user : users) {
	    ComponentRegistryDbImpl registry = componentRegistryBeanFactory.
		    getNewComponentRegistry();
	    registry.setUserId(user.getId());
	    registries.add(registry);
	}
	return registries;
    }

    @Override
    public ComponentRegistry getComponentRegistry(boolean userspace, UserCredentials credentials) {
	ComponentRegistry result = null;
	if (userspace) {
	    if (credentials != null && !ANONYMOUS_USER.equals(credentials.
		    getPrincipalName())) {
		String principalName = credentials.getPrincipalName();
		User user = getOrCreateUser(principalName, credentials.
			getDisplayName());
		result = getComponentRegistryForUser(user.getId());
	    } else {
		throw new IllegalArgumentException("No user credentials available cannot load userspace.");
	    }
	} else {
	    result = getPublicRegistry();
	}
	return result;
    }

    @Override
    public ComponentRegistry getOtherUserComponentRegistry(Principal adminPrincipal, String principalNameMD5) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ComponentRegistry getPublicRegistry() {
	return getComponentRegistryForUser(null);
    }

    private ComponentRegistry getComponentRegistryForUser(Number userId) {
	ComponentRegistryDbImpl componentRegistry = componentRegistryBeanFactory.
		getNewComponentRegistry();
	componentRegistry.setUserId(userId);
	return componentRegistry;
    }

    private User getOrCreateUser(String principalName, String displayName) {
	// Try getting it from db
	User user = userDao.getByPrincipalName(principalName);
	if (user == null) {
	    // Create the new user
	    user = new User();
	    user.setPrincipalName(principalName);
	    user.setName(displayName);
	    userDao.insertUser(user);
	    // Retrieve from db
	    user = userDao.getByPrincipalName(principalName);
	}
	return user;
    }
}
