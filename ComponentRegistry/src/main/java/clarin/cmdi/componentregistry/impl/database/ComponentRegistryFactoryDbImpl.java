package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.UserCredentials;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryFactoryDbImpl implements ComponentRegistryFactory {

    @Autowired
    private Configuration configuration;
    
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
