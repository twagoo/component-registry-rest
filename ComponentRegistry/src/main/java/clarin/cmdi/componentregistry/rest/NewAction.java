package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.BaseComponent;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class NewAction implements RegisterAction {

    @Override
    public int execute(BaseComponent desc, CMDComponentSpec spec, RegisterResponse response, ComponentRegistry registry) {
        return registry.register(desc, spec);
    }
}
