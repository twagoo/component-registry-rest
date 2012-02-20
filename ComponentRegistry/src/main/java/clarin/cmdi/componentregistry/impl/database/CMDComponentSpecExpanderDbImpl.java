package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.CMDComponentSpecExpander;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CMDComponentSpecExpanderDbImpl extends CMDComponentSpecExpander {

    private ComponentRegistryDbImpl dbImplRegistry;
    
    public CMDComponentSpecExpanderDbImpl(ComponentRegistryDbImpl registry) {
        super(registry);
        dbImplRegistry = registry;
    }

    public static CMDComponentSpec expandComponent(String componentId, ComponentRegistryDbImpl registry) throws ComponentRegistryException {
        CMDComponentSpecExpanderDbImpl expander = new CMDComponentSpecExpanderDbImpl(registry);
        return expander.expandComponent(componentId);
    }

    public static CMDComponentSpec expandProfile(String profileId, ComponentRegistryDbImpl registry) throws ComponentRegistryException {
        CMDComponentSpecExpanderDbImpl expander = new CMDComponentSpecExpanderDbImpl(registry);
        return expander.expandProfile(profileId);
    }

    @Override
    protected CMDComponentSpec getUncachedComponent(String componentId) throws ComponentRegistryException {
        return dbImplRegistry.getUncachedMDComponent(componentId);
    }

    @Override
    protected CMDComponentSpec getUncachedProfile(String profileId) throws ComponentRegistryException {
        return dbImplRegistry.getUncachedMDProfile(profileId);
    }
}
