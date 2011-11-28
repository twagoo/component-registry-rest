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
import clarin.cmdi.componentregistry.model.CommentMapping.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ComponentRegistryImplBase implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryImplBase.class);

    @Override
    public List<ComponentDescription> getUsageInComponents(String componentId) throws ComponentRegistryException {
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
    public List<ProfileDescription> getUsageInProfiles(String componentId) throws ComponentRegistryException {
        List<ProfileDescription> result = new ArrayList<ProfileDescription>();
        for (ProfileDescription profileDescription : getProfileDescriptions()) {
            CMDComponentSpec profile = getMDProfile(profileDescription.getId());
            if (profile != null && findComponentId(componentId, profile.getCMDComponent())) {
                result.add(profileDescription);
            }
        }
        return result;
    }

//    public List<Comment> getUsageInComments(String componentId) throws ComponentRegistryException {
//        List<Comment> result = new ArrayList<Comment>();
//        for (Comment comment : getComments()) {
//            CMDComponentSpec myComment = getMDComment(comment.getId());
//            if (myComment != null && findComponentId(componentId, myComment.getCMDComponent())) {
//            result.add(comment);
//            System.out.println("get unsage in comments : " + myComment + "\n");
//            }
//        }
//        
//        return result;
//    }

    /**
     * 
     * @return List of profile descriptions ordered by name ascending, only the ones marked for showing in metadata editor
     * @throws ComponentRegistryException
     */
    @Override
    public List<ProfileDescription> getProfileDescriptionsForMetadaEditor() throws ComponentRegistryException {
        // TODO: Below can also be done by accepting and passing a parameter in the ProfileDescriptionDao, should have better performance

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

    protected void checkStillUsed(String componentId) throws DeleteFailedException, ComponentRegistryException {
        List<ProfileDescription> profiles = getUsageInProfiles(componentId);
        List<ComponentDescription> components = getUsageInComponents(componentId);
        if (!profiles.isEmpty() || !components.isEmpty()) {
            throw new DeleteFailedException(createStillInUseMessage(profiles, components));
        }
    }

    private String createStillInUseMessage(List<ProfileDescription> profiles, List<ComponentDescription> components) {
        StringBuilder result = new StringBuilder();
        if (!profiles.isEmpty()) {
            result.append("Still used by the following profiles: \n");
            for (ProfileDescription profileDescription : profiles) {
                result.append(" - ").append(profileDescription.getName()).append("\n");
            }
        }
        if (!components.isEmpty()) {
            result.append("Still used by the following components: \n");
            for (ComponentDescription componentDescription : components) {
                result.append(" - ").append(componentDescription.getName()).append("\n");
            }
        }
        result.append("Try to change above mentioned references first.");
        return result.toString();
    }
}
