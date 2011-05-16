package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.UserCredentials;
import java.security.Principal;
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
    ComponentRegistryBeanFactory componentRegistryBeanFactory;

    @Override
    public List<ComponentRegistry> getAllUserRegistries() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ComponentRegistry getComponentRegistry(boolean userspace, UserCredentials credentials) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        ComponentRegistryDbImpl componentRegistry = componentRegistryBeanFactory.getNewComponentRegistry();
        componentRegistry.setUserId(userId);
        return componentRegistry;
    }
}
