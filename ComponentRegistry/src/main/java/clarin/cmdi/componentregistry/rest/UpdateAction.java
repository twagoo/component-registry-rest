package clarin.cmdi.componentregistry.rest;

//import java.security.Principal;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class UpdateAction implements RegisterAction {
    
//    private final Principal principal;
//
//    public UpdateAction(Principal principal) {
//        this.principal = principal;
//    }
    
    @Override
    public int execute(BaseDescription desc, CMDComponentSpec spec, RegisterResponse response, ComponentRegistry registry)  throws UserUnauthorizedException, ItemNotFoundException, AuthenticationRequiredException{
        return registry.update(desc, spec, false);
    }

}
