package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.RegistryUser;
import java.security.Principal;
import java.util.List;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ComponentRegistryFactory {

    public static final String ANONYMOUS_USER = "anonymous"; //Default shibboleth fallback.

    List<ComponentRegistry> getAllUserRegistries();

    ComponentRegistry getComponentRegistry(boolean userspace, UserCredentials credentials);

    ComponentRegistry getOtherUserComponentRegistry(Principal adminPrincipal, String userId);

    ComponentRegistry getPublicRegistry();

    RegistryUser getOrCreateUser(UserCredentials credentials);
}
