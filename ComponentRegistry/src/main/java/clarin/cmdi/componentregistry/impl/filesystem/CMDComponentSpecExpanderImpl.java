package clarin.cmdi.componentregistry.impl.filesystem;

import clarin.cmdi.componentregistry.CMDComponentSpecExpander;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;

public class CMDComponentSpecExpanderImpl extends CMDComponentSpecExpander {
    private ComponentRegistryImpl fsImplRegistry;

    public CMDComponentSpecExpanderImpl(ComponentRegistryImpl registry) {
	super(registry);
	fsImplRegistry = registry;
    }

    public static CMDComponentSpec expandComponent(String componentId, ComponentRegistryImpl registry) {
	CMDComponentSpecExpanderImpl expander = new CMDComponentSpecExpanderImpl(registry);
	return expander.expandComponent(componentId);
    }

    public static CMDComponentSpec expandProfile(String profileId, ComponentRegistryImpl registry) {
	CMDComponentSpecExpanderImpl expander = new CMDComponentSpecExpanderImpl(registry);
	return expander.expandProfile(profileId);
    }

    /**
     * Get uncached component from "this" registry and possibly from public registry. Note: "this" registry can be an user registry.
     * @param componentId
     */
    @Override
    protected CMDComponentSpec getUncachedComponent(String componentId) {
	CMDComponentSpec result = fsImplRegistry.getUncachedComponent(componentId); //TODO PD fix this uncached stuff or put it in interface or make sure getComponent return a mutable object.
	if (result == null && !registry.isPublic()) {
	    result = ((ComponentRegistryImpl) ComponentRegistryFactoryImpl.
		    getInstance().getPublicRegistry()).getUncachedComponent(componentId);
	}
	return result;
    }

    @Override
    protected CMDComponentSpec getUncachedProfile(String profileId) {
	CMDComponentSpec result = fsImplRegistry.getUncachedProfile(profileId); //TODO PD fix this uncached stuff or put it in interface or make sure getComponent return a mutable object.
	if (result == null && !registry.isPublic()) {
	    result = ((ComponentRegistryImpl) ComponentRegistryFactoryImpl.
		    getInstance().getPublicRegistry()).getUncachedProfile(profileId);
	}
	return result;
    }
}
