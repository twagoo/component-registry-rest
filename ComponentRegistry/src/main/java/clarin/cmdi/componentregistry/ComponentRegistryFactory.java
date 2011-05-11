/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clarin.cmdi.componentregistry;

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

    ComponentRegistry getOtherUserComponentRegistry(Principal adminPrincipal, String principalNameMD5);

    ComponentRegistry getPublicRegistry();
}
