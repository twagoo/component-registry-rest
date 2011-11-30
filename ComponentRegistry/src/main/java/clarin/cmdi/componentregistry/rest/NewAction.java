package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class NewAction implements RegisterAction {

    @Override
    public int execute(AbstractDescription desc, CMDComponentSpec spec, RegisterResponse response, ComponentRegistry registry) {
        return registry.register(desc, spec);
    }

    @Override
    public int executeComment(Comment com, Comment spec, RegisterResponse response, ComponentRegistry registry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
