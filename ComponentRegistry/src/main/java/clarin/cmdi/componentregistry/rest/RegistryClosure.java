package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.model.BaseComponent;

public interface RegistryClosure<T extends BaseComponent> {
    
    T getDescription(ComponentRegistry registry, String id) throws ComponentRegistryException;


}
