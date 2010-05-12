package clarin.cmdi.componentregistry;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;

public class CMDComponentSpecExpander {

    private final ComponentRegistryImpl registry;
    private final static Logger LOG = LoggerFactory.getLogger(CMDComponentSpecExpander.class);

    public CMDComponentSpecExpander(ComponentRegistryImpl registry) {
        this.registry = registry;
    }

    public static CMDComponentSpec expandProfile(String profileId, ComponentRegistryImpl registry) {
        CMDComponentSpecExpander expander = new CMDComponentSpecExpander(registry);
        return expander.expandProfile(profileId);
    }

    public static CMDComponentSpec expandComponent(String componentId, ComponentRegistryImpl registry) {
        CMDComponentSpecExpander expander = new CMDComponentSpecExpander(registry);
        return expander.expandComponent(componentId);
    }

    private CMDComponentSpec expandComponent(String componentId) {
        // Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
        CMDComponentSpec result = registry.getUncachedComponent(componentId);
        CMDComponentType cmdComponentType = getComponentTypeOfAComponent(result);
        expandNestedComponent(cmdComponentType.getCMDComponent());
        return result;
    }

    private CMDComponentSpec expandProfile(String profileId) {
        // Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
        CMDComponentSpec result = registry.getUncachedProfile(profileId);
        List<CMDComponentType> cmdComponents = result.getCMDComponent();
        expandNestedComponent(cmdComponents);
        return result;
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

    /**
     * Get uncached component from "this" registry and possibly from public registry. Note: "this" registry can be an user registry.
     * @param componentId
     */
    private CMDComponentSpec getUncachedComponent(String componentId) {
        CMDComponentSpec result = registry.getUncachedComponent(componentId); //TODO PD fix this uncached stuff or put it in interface or make sure getComponent return a mutable object.
        if (result == null && !registry.isPublic()) {
            result = ((ComponentRegistryImpl) ComponentRegistryFactory.getInstance().getPublicRegistry()).getUncachedComponent(componentId);
        }
        return result;
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
    }

}
