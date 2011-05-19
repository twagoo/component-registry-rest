package clarin.cmdi.componentregistry.impl;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentSpec.Header;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ComponentRegistryImplBase implements ComponentRegistry{
        private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryImplBase.class);

    @Override
    public List<ComponentDescription> getUsageInComponents(String componentId) {
	List<ComponentDescription> result = new ArrayList<ComponentDescription>();
	List<ComponentDescription> descs = getComponentDescriptions();
	for (ComponentDescription desc : descs) {
	    CMDComponentSpec spec = getMDComponent(desc.getId());
	    if (spec != null && findComponentId(componentId, spec.getCMDComponent())) {
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
	    if (profile != null && findComponentId(componentId, profile.getCMDComponent())) {
		result.add(profileDescription);
	    }
	}
	return result;
    }

    /* HELPER METHODS */

    protected static String stripRegistryId(String id) {
        return StringUtils.removeStart(id, ComponentRegistry.REGISTRY_ID);
    }

    protected static void enrichSpecHeader(CMDComponentSpec spec, AbstractDescription description) {
        Header header = spec.getHeader();
        header.setID(description.getId());
        if (StringUtils.isEmpty(header.getName())) {
            header.setName(description.getName());
        }
        if (StringUtils.isEmpty(header.getDescription())) {
            header.setDescription(description.getDescription());
        }
    }

    protected static boolean findComponentId(String componentId, List<CMDComponentType> componentReferences) {
        for (CMDComponentType cmdComponent : componentReferences) {
            if (componentId.equals(cmdComponent.getComponentId())) {
                return true;
            } else if (findComponentId(componentId, cmdComponent.getCMDComponent())) {
                return true;
            }
        }
        return false;
    }

    protected static void writeXsd(CMDComponentSpec expandedSpec, OutputStream outputStream) {
        MDMarshaller.generateXsd(expandedSpec, outputStream);
    }

    protected static void writeXml(CMDComponentSpec spec, OutputStream outputStream) {
        try {
            MDMarshaller.marshal(spec, outputStream);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error in encoding: ", e);
        } catch (JAXBException e) {
            LOG.error("Cannot marshall spec: " + spec, e);
        }
    }

    /* UNIMPLEMENTED INTERFACE METHODS */

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
