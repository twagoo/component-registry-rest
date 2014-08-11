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
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentSpec.Header;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ComponentRegistryImplBase implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryImplBase.class);

    protected abstract MDMarshaller getMarshaller();

    

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
        List<String> descriptionsCollectionIds = getAllNonDeletedProfileIds();
        // Filter out ones that do should not be shown for metadata editor
        ArrayList<ProfileDescription> descriptions = new ArrayList<ProfileDescription>();
        for (String id : descriptionsCollectionIds) {
            try {
                ProfileDescription profile = getProfileDescriptionAccessControlled(id);
                if (profile.isShowInEditor()) {
                    descriptions.add(profile);
                }
            } catch (UserUnauthorizedException e) {
            } catch (ItemNotFoundException e) {
            }

        }
        // Return filtered list
        return descriptions;
    }

    /* HELPER METHODS */
    protected static String stripRegistryId(String id) {
        return StringUtils.removeStart(id, ComponentRegistry.REGISTRY_ID);
    }

    protected static void enrichSpecHeader(CMDComponentSpec spec, BaseDescription description) {
        Header header = spec.getHeader();
        header.setID(description.getId());
        if (StringUtils.isEmpty(header.getName())) {
            header.setName(description.getName());
        }
        if (StringUtils.isEmpty(header.getDescription())) {
            header.setDescription(description.getDescription());
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

}
