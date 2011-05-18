package clarin.cmdi.componentregistry.impl;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryUtils;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ComponentRegistryImplBase implements ComponentRegistry{

    @Override
    public List<ComponentDescription> getUsageInComponents(String componentId) {
	List<ComponentDescription> result = new ArrayList<ComponentDescription>();
	List<ComponentDescription> descs = getComponentDescriptions();
	for (ComponentDescription desc : descs) {
	    CMDComponentSpec spec = getMDComponent(desc.getId());
	    if (spec != null && ComponentRegistryUtils.findComponentId(componentId, spec.getCMDComponent())) {
		result.add(desc);
	    }
	}
	return result;
    }

    @Override
    public List<ProfileDescription> getUsageInProfiles(String componentId) {
	List<ProfileDescription> result = new ArrayList<ProfileDescription>();
	for (ProfileDescription profileDescription : getProfileDescriptions()) {
	    CMDComponentSpec profile = getMDProfile(profileDescription.getId());
	    if (profile != null && ComponentRegistryUtils.findComponentId(componentId, profile.getCMDComponent())) {
		result.add(profileDescription);
	    }
	}
	return result;
    }

    @Override
    public abstract List<ComponentDescription> getComponentDescriptions();

    @Override
    public abstract ComponentDescription getComponentDescription(String id);

    @Override
    public abstract List<ProfileDescription> getProfileDescriptions();

    @Override
    public abstract ProfileDescription getProfileDescription(String id);

    @Override
    public abstract CMDComponentSpec getMDProfile(String id);

    @Override
    public abstract CMDComponentSpec getMDComponent(String id);

    @Override
    public abstract int register(AbstractDescription desc, CMDComponentSpec spec);

    @Override
    public abstract  int update(AbstractDescription description, CMDComponentSpec spec);

    @Override
    public abstract  int publish(AbstractDescription desc, CMDComponentSpec spec, Principal principal);

    @Override
    public abstract  void getMDProfileAsXml(String profileId, OutputStream output);

    @Override
    public abstract  void getMDProfileAsXsd(String profileId, OutputStream outputStream);

    @Override
    public abstract  void getMDComponentAsXml(String componentId, OutputStream output);

    @Override
    public abstract  void getMDComponentAsXsd(String componentId, OutputStream outputStream);

    @Override
    public abstract  void deleteMDProfile(String profileId, Principal principal) throws IOException, UserUnauthorizedException, DeleteFailedException;

    @Override
    public abstract  void deleteMDComponent(String componentId, Principal principal, boolean forceDelete) throws IOException, UserUnauthorizedException, DeleteFailedException;

    @Override
    public abstract  boolean isPublic();

}
