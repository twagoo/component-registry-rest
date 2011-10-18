package clarin.cmdi.componentregistry.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.AttributeListType.Attribute;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class MDValidator implements Validator {

    static final String MISMATCH_ERROR = "Cannot register component as a profile or vica versa.";
    static final String COMPONENT_NOT_REGISTERED_ERROR = "referenced component is not registered or does not have a correct componentId: ";
    static final String PARSE_ERROR = "Error in validation input file. Error is: ";
    static final String COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR = "referenced component cannot be found in the published components: ";
    static final String COMPONENT_REGISTRY_EXCEPTION_ERROR = "An exception occurred while accessing the component registry: ";
    static final String ILLEGAL_ATTRIBUTE_NAME_ERROR = "Illegal attribute name: ";
    static final Collection<String> ILLEGAL_ATTRIBUTE_NAMES = Collections.unmodifiableCollection(Arrays.asList("ref", "ComponentId"));
    private List<String> errorMessages = new ArrayList<String>();
    private CMDComponentSpec spec = null;
    private final InputStream input;
    private final AbstractDescription description;
    private final ComponentRegistry registry;
    private final ComponentRegistry userRegistry;
    private final ComponentRegistry publicRegistry;

    /**
     * 
     * @param input In order to validate the input is consumed. So use @see getCMDComponentSpec to get the parsed CMDComponentSpec.
     * @param desc
     * @param registry (registry you currently used) 
     * @param userRegistry can be null, We get user registry as well so we can give nice error messages if needed. Can be the same as @param registry
     */
    public MDValidator(InputStream input, AbstractDescription description, ComponentRegistry registry, ComponentRegistry userRegistry, ComponentRegistry publicRegistry) {
	this.input = input;
	this.description = description;
	this.registry = registry;
	this.userRegistry = userRegistry;
	this.publicRegistry = publicRegistry;
    }

    @Override
    public List<String> getErrorMessages() {
	return errorMessages;
    }

    @Override
    public boolean validate() {
	try {
	    spec = MDMarshaller.unmarshal(CMDComponentSpec.class, input, MDMarshaller.getCMDComponentSchema());
	    if (spec.isIsProfile() != description.isProfile()) {
		errorMessages.add(MISMATCH_ERROR);
	    }
	} catch (JAXBException e) {
	    errorMessages.add(PARSE_ERROR + e);
	}
	if (errorMessages.isEmpty()) {
	    try {
		validateComponents(spec.getCMDComponent());
	    } catch (ComponentRegistryException e) {
		errorMessages.add(COMPONENT_REGISTRY_EXCEPTION_ERROR + e);
	    }
	}
	return errorMessages.isEmpty();
    }

    private void validateComponents(List<CMDComponentType> cmdComponents) throws ComponentRegistryException {
	for (CMDComponentType cmdComponentType : cmdComponents) {
	    validateDescribedComponents(cmdComponentType);
	    validateComponents(cmdComponentType.getCMDComponent());//Recursion
	}
    }

    private void validateDescribedComponents(CMDComponentType cmdComponentType) throws ComponentRegistryException {
	checkPublicComponents(cmdComponentType);
	checkLegalAttributeNames(cmdComponentType);
    }

    private void checkPublicComponents(CMDComponentType cmdComponentType) throws ComponentRegistryException {
	if (isDefinedInSeparateFile(cmdComponentType)) {
	    String id = cmdComponentType.getComponentId();
	    CMDComponentSpec registeredComponent = null;
	    if (registry.isPublic()) { // public registry requires only published components
		registeredComponent = registry.getMDComponent(id);
		if (registeredComponent == null) {
		    String error = cmdComponentType.getComponentId();
		    if (userRegistry != null) {
			ComponentDescription desc = userRegistry.getComponentDescription(id);
			if (desc != null) {
			    error = desc.getName() + " (" + cmdComponentType.getComponentId() + ")";
			}
		    }
		    errorMessages.add(COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR + error);
		}
	    } else { //User registry, can link to components from public registry and the user's registry
		registeredComponent = registry.getMDComponent(id);
		if (registeredComponent == null) {
		    registeredComponent = publicRegistry.getMDComponent(id);
		    if (registeredComponent == null) {
			errorMessages.add(COMPONENT_NOT_REGISTERED_ERROR + cmdComponentType.getComponentId());
		    }
		}

	    }
	}
    }

    private void checkLegalAttributeNames(CMDComponentType cmdComponentType) {
	if (cmdComponentType.getAttributeList() != null) {
	    for (Attribute attribute : cmdComponentType.getAttributeList().getAttribute()) {
		if (ILLEGAL_ATTRIBUTE_NAMES.contains(attribute.getName())) {
		    errorMessages.add(ILLEGAL_ATTRIBUTE_NAME_ERROR + attribute.getName() + " (" + cmdComponentType.getComponentId() + ")");
		}
	    }
	}
    }

    private boolean isDefinedInSeparateFile(CMDComponentType cmdComponentType) {
	return cmdComponentType.getName() == null;
    }

    public CMDComponentSpec getCMDComponentSpec() {
	return spec;
    }
}
