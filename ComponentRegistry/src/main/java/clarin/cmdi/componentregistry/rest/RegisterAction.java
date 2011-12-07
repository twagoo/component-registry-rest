package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public interface RegisterAction {

    int execute(AbstractDescription desc, CMDComponentSpec spec, RegisterResponse response, ComponentRegistry registry);

    int executeComment(Comment com, RegisterResponse response, ComponentRegistry registry, String userId);

}
