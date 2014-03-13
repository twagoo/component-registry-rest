package clarin.cmdi.componentregistry.impl;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.components.CMDComponentSpec.Header;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ComponentRegistryImplBase implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryImplBase.class);

    protected abstract MDMarshaller getMarshaller();

    @Override
    public List<ComponentDescription> getUsageInComponents(String componentId) throws ComponentRegistryException {
        LOG.debug("Checking usage of component {} in components", componentId);
        List<ComponentDescription> result = new ArrayList<ComponentDescription>();
	List<ComponentDescription> descs = getComponentDescriptions();
	for (ComponentDescription desc : descs) {
	    CMDComponentSpec spec = getMDComponent(desc.getId());
	    if (spec != null && hasComponentId(componentId, spec.getCMDComponent())) {
                LOG.debug("Component {} used in component {}", componentId, spec.getHeader().getID());
		result.add(desc);
            }
        }
        return result;
    }

    @Override
    public List<ProfileDescription> getUsageInProfiles(String componentId) throws ComponentRegistryException {
        LOG.debug("Checking usage of component {} in profiles", componentId);
        List<ProfileDescription> result = new ArrayList<ProfileDescription>();
	for (ProfileDescription profileDescription : getProfileDescriptions()) {
	    CMDComponentSpec profile = getMDProfile(profileDescription.getId());
	    if (profile != null && hasComponentId(componentId, profile.getCMDComponent())) {
                LOG.debug("Component {} used in profile {}", componentId, profile.getHeader().getID());
		result.add(profileDescription);
            }
        }
        return result;
    }

    /**
     *
     * @return List of profile descriptions ordered by name ascending, only the
     * ones marked for showing in metadata editor
     * @throws ComponentRegistryException
     */
    @Override
    public List<ProfileDescription> getProfileDescriptionsForMetadaEditor() throws ComponentRegistryException {
	// TODO: Below can also be done by accepting and passing a parameter in the ProfileDescriptionDaoImpl, should have better performance

        // Get all profile descriptions
	List<ProfileDescription> descriptionsCollection = getProfileDescriptions();
        // Filter out ones that do should not be shown for metadata editor
	ArrayList<ProfileDescription> descriptions = new ArrayList<ProfileDescription>(descriptionsCollection.size());
	for (ProfileDescription profile : descriptionsCollection) {
	    if (((ProfileDescription) profile).isShowInEditor()) {
		descriptions.add((ProfileDescription) profile);
            }
        }
        // Return filtered list
        return descriptions;
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
            if (hasComponentId(componentId, cmdComponent)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasComponentId(String componentId, CMDComponentType cmdComponent) {
        if (componentId.equals(cmdComponent.getComponentId())) {
            return true;
        } else if (findComponentId(componentId, cmdComponent.getCMDComponent())) {
            return true;
        } else {
            return false;
        }
    }

    protected void writeXsd(CMDComponentSpec expandedSpec, OutputStream outputStream) {
        getMarshaller().generateXsd(expandedSpec, outputStream);
    }

    protected void writeXml(CMDComponentSpec spec, OutputStream outputStream) {
        try {
            getMarshaller().marshal(spec, outputStream);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error in encoding: ", e);
        } catch (JAXBException e) {
            LOG.error("Cannot marshall spec: " + spec, e);
        }
    }

    protected void checkStillUsed(String componentId) throws DeleteFailedException, ComponentRegistryException {
	for (ProfileDescription profileDescription : getProfileDescriptions()) {
	    CMDComponentSpec spec = getMDProfile(profileDescription.getId());
	    if (spec != null && hasComponentId(componentId, spec.getCMDComponent())) {
                LOG.warn("Cannot delete component {}, still used in profile {} and possibly other profiles and/or components", componentId, spec.getHeader().getID());
                // Profile match - throw
                throw new DeleteFailedException("Component is still in use by other components or profiles. Request component usage for details.");
            }
        }

        LOG.debug("Component {} is not used in any profiles", componentId);

	for (ComponentDescription desc : getComponentDescriptions()) {
	    CMDComponentSpec spec = getMDComponent(desc.getId());
	    if (spec != null && hasComponentId(componentId, spec.getCMDComponent())) {
                LOG.warn("Cannot delete component {}, still used in component {} and possibly other components", componentId, spec.getHeader().getID());
                // Component match -> throw
                throw new DeleteFailedException("Component is still in use by one or more other components. Request component usage for details.");
            }
        }
    }
}
