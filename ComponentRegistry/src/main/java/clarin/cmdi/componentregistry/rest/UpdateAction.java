package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class UpdateAction implements RegisterAction {

    @Override
    public int execute(AbstractDescription desc, CMDComponentSpec spec, RegisterResponse response, ComponentRegistry registry) {
        return registry.update(desc, spec);
    }

}
