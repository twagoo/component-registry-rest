package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class ProfileClosure implements RegistryClosure<ProfileDescription> {

    @Override
    public ProfileDescription getDescription(ComponentRegistry registry, String id) {
        return registry.getProfileDescription(id);
    }

}
