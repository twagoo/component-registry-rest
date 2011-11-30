package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.model.Comment;
import java.security.Principal;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class PublishAction implements RegisterAction {
    
    private final Principal principal;

    public PublishAction(Principal principal) {
        this.principal = principal;
    }

    @Override
    public int execute(AbstractDescription desc, CMDComponentSpec spec, RegisterResponse response, ComponentRegistry registry) {
        response.setIsInUserSpace(false);
        return registry.publish(desc, spec, principal);
    }

    @Override
    public int executeComment(Comment com, Comment spec, RegisterResponse response, ComponentRegistry registry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
