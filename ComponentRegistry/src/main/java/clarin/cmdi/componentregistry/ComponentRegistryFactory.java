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

    /**
     * Gets the specified registry
     *
     * @param status status of the registry
     * @param owner owner of the registry. Passing null will assume the authenticated user as a {@link OwnerUser}
     * @param credentials credentials that authenticate user, can be left null if requested registry has a public status
     * @return
     */
    ComponentRegistry getComponentRegistry(ComponentStatus status, Owner owner, UserCredentials credentials) throws UserUnauthorizedException;

    ComponentRegistry getOtherUserComponentRegistry(Principal adminPrincipal, ComponentStatus status, Owner owner);

    ComponentRegistry getPublicRegistry();

    RegistryUser getOrCreateUser(UserCredentials credentials);
}
