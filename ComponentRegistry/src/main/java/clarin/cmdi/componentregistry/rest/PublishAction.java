package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import java.security.Principal;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class PublishAction implements RegisterAction {
    
    private final Principal principal;

    public PublishAction(Principal principal) {
        this.principal = principal;
    }

    @Override
    public int execute(BaseDescription desc, ComponentSpec spec, RegisterResponse response, ComponentRegistry registry) throws UserUnauthorizedException, ItemNotFoundException, AuthenticationRequiredException{
        response.setIsPrivate(false);
        spec.getHeader().setStatus(ComponentStatus.PRODUCTION.toString());
        return registry.publish(desc, spec, principal);
    }

    @Override
    public boolean isPreRegistration() {
        return false;
    }
}
