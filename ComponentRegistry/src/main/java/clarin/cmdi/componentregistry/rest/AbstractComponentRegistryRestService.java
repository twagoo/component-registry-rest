package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.UserCredentials;
import com.sun.jersey.api.core.InjectParam;
import java.security.Principal;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class AbstractComponentRegistryRestService {

    @Context
    protected SecurityContext security;
    
    @InjectParam(value = "componentRegistryFactory")
    protected ComponentRegistryFactory componentRegistryFactory;

    protected ComponentRegistry getBaseRegistry() throws AuthenticationRequiredException {
        Principal userPrincipal = security.getUserPrincipal();
        if (userPrincipal == null) {
            return componentRegistryFactory.getBaseRegistry(null);
        } else {
            UserCredentials userCredentials = this.getUserCredentials(userPrincipal);
            return componentRegistryFactory.getBaseRegistry(userCredentials);
        }
    }

    protected UserCredentials getUserCredentials(Principal userPrincipal) {
        UserCredentials userCredentials = null;
        if (userPrincipal != null) {
            userCredentials = new UserCredentials(userPrincipal);
        }
        return userCredentials;
    }

    /**
     * @param componentRegistryFactory the componentRegistryFactory to set
     */
    public void setComponentRegistryFactory(
            ComponentRegistryFactory componentRegistryFactory) {
        this.componentRegistryFactory = componentRegistryFactory;
    }
}
