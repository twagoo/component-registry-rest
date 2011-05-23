package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.model.AbstractDescription;

public interface RegistryClosure<T extends AbstractDescription> {
    
    T getDescription(ComponentRegistry registry, String id) throws ComponentRegistryException;


}
