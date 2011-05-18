package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class CMDComponentSpecExpander {

    private final static Logger LOG = LoggerFactory.getLogger(CMDComponentSpecExpander.class);
    protected final ComponentRegistry registry;

    public CMDComponentSpecExpander(ComponentRegistry registry) {
	this.registry = registry;
    }

    private void expandNestedComponent(List<CMDComponentType> cmdComponents) {
	List<CMDComponentType> expanded = new ArrayList<CMDComponentType>();
	for (CMDComponentType cmdComponentType : cmdComponents) {
	    String componentId = cmdComponentType.getComponentId();
	    if (componentId != null) {
		// Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
		CMDComponentSpec spec = getUncachedComponent(componentId);
		CMDComponentType nested = getComponentTypeOfAComponent(spec);
		expandNestedComponent(nested.getCMDComponent());
		overwriteAttributes(cmdComponentType, nested);
		expanded.add(nested);
	    } else {
		expandNestedComponent(cmdComponentType.getCMDComponent());
		expanded.add(cmdComponentType);//no attributes overwritten
	    }
	}
	cmdComponents.clear();
	cmdComponents.addAll(expanded);
    }

    private CMDComponentType getComponentTypeOfAComponent(CMDComponentSpec result) {
	List<CMDComponentType> cmdComponents = result.getCMDComponent();
	if (cmdComponents.size() != 1) {
	    LOG.error("Internal error: CMDComponentSpec which is not a profile can only have one "
		    + "CMDComponentType (which is the description of the component itself).");
	}
	CMDComponentType cmdComponentType = cmdComponents.get(0);
	return cmdComponentType;
    }

    /**
     * Copying the cardinality specified in the referenceDeclaration over the values in the actual component.
     */
    private void overwriteAttributes(CMDComponentType referenceDeclaration, CMDComponentType nested) {
	if (!referenceDeclaration.getCardinalityMax().isEmpty()) {
	    List<String> cardinalityMax = nested.getCardinalityMax();
	    cardinalityMax.clear();
	    cardinalityMax.addAll(referenceDeclaration.getCardinalityMax());
	}
	if (!referenceDeclaration.getCardinalityMin().isEmpty()) {
	    List<String> cardinalityMin = nested.getCardinalityMin();
	    cardinalityMin.clear();
	    cardinalityMin.addAll(referenceDeclaration.getCardinalityMin());
	}
	nested.setComponentId(referenceDeclaration.getComponentId()); // Setting componentId for better xsd generation.
    }

    protected CMDComponentSpec expandComponent(String componentId) {
	// Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
	CMDComponentSpec result = getUncachedComponent(componentId);//registry.getUncachedComponent(componentId);
	CMDComponentType cmdComponentType = getComponentTypeOfAComponent(result);
	expandNestedComponent(cmdComponentType.getCMDComponent());
	return result;
    }

    protected CMDComponentSpec expandProfile(String profileId) {
	// Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
	CMDComponentSpec result = getUncachedProfile(profileId);//registry.getUncachedProfile(profileId);
	List<CMDComponentType> cmdComponents = result.getCMDComponent();
	expandNestedComponent(cmdComponents);
	return result;
    }

    /**
     * Get uncached component from "this" registry and possibly from public registry. Note: "this" registry can be a user registry.
     * @param componentId
     */
    protected abstract CMDComponentSpec getUncachedComponent(String componentId);

    protected abstract CMDComponentSpec getUncachedProfile(String profileId);
}
