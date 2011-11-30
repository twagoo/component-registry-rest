package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.model.Comment;
import java.security.Principal;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class UpdateAction implements RegisterAction {
    
    private final Principal principal;

    public UpdateAction(Principal principal) {
        this.principal = principal;
    }
    
    @Override
    public int execute(AbstractDescription desc, CMDComponentSpec spec, RegisterResponse response, ComponentRegistry registry) {
        return registry.update(desc, spec, principal, false);
    }

    @Override
    public int executeComment(Comment com, Comment spec, RegisterResponse response, ComponentRegistry registry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
