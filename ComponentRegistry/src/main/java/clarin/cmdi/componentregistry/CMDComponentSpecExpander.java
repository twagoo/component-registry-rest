package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
    
    public void expandNestedComponent(List<CMDComponentType> cmdComponents, String id) throws ComponentRegistryException {
	expandNestedComponent(cmdComponents, new HashSet<String>(Collections.singleton(id)));
    }
    
    private void expandNestedComponent(List<CMDComponentType> cmdComponents, Collection<String> path) throws ComponentRegistryException {
	List<CMDComponentType> expanded = new ArrayList<CMDComponentType>();
	for (CMDComponentType cmdComponentType : cmdComponents) {
	    String componentId = cmdComponentType.getComponentId();
	    if (componentId != null) {
		if (LOG.isDebugEnabled()) {
		    LOG.debug("[Level {}] Expanding {}", path.size(), componentId);
		}
		if (path.contains(componentId)) {
		    throw new ComponentRegistryException("Detected recursion in component specification: " + path.toString() + " already contains " + componentId);
		} else {
		    Collection<String> newPath = new HashSet<String>(path);
		    newPath.add(componentId);
		    // Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
		    CMDComponentSpec spec = getUncachedComponent(componentId);
		    if (spec != null) {
			CMDComponentType nested = spec.getCMDComponent();
			expandNestedComponent(nested.getCMDComponent(), newPath);
			overwriteAttributes(cmdComponentType, nested);
			expanded.add(nested);
		    } else {
			// Spec could not be resolved
			LOG.warn("Could not resolve referenced component with id " + componentId);
			// Add spec itself, without futher expanding
			expanded.add(cmdComponentType);
		    }
		}
	    } else {
		// No id = embedded component
		expandNestedComponent(cmdComponentType.getCMDComponent(), path);
		expanded.add(cmdComponentType);//no attributes overwritten
	    }
	}
	cmdComponents.clear();
	cmdComponents.addAll(expanded);
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
    
    protected CMDComponentSpec expandComponent(String componentId) throws ComponentRegistryException {
	// Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
	CMDComponentSpec result = getUncachedComponent(componentId);//registry.getUncachedComponent(componentId);
	CMDComponentType cmdComponentType = result.getCMDComponent();
	expandNestedComponent(cmdComponentType.getCMDComponent(), componentId);
	return result;
    }
    
    protected CMDComponentSpec expandProfile(String profileId) throws ComponentRegistryException {
	// Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
	CMDComponentSpec result = getUncachedProfile(profileId);//registry.getUncachedProfile(profileId);
	CMDComponentType cmdComponent = result.getCMDComponent();
	expandNestedComponent(Collections.singletonList(cmdComponent), profileId);
	return result;
    }

//    protected CMDComponentSpec expandComment(String commentId) throws ComponentRegistryException {
//        CMDComponentSpec result = getUncachedComment(commentId);
//        List<CMDComponentType> cmdComponents = result.getCMDComponent();
//        expandNestedComponent(cmdComponents);
//        return result;
//    }
    /**
     * Get uncached component from "this" registry and possibly from public registry. Note: "this" registry can be a user registry.
     *
     * @param componentId
     */
    protected abstract CMDComponentSpec getUncachedComponent(String componentId) throws ComponentRegistryException;
    
    protected abstract CMDComponentSpec getUncachedProfile(String profileId) throws ComponentRegistryException;
//    protected abstract CMDComponentSpec getUncachedComment(String commentId) throws ComponentRegistryException;
}
