package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.UserCredentials;
import java.security.Principal;
import java.util.List;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryFactoryDbImpl implements ComponentRegistryFactory {

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
