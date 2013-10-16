package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.Component;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public interface RegisterAction {

    int execute(Component desc, CMDComponentSpec spec, RegisterResponse response, ComponentRegistry registry);

}
