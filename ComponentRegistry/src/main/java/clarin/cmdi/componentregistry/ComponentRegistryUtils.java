package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentSpec.Header;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public final class ComponentRegistryUtils {
    private ComponentRegistryUtils(){}

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryUtils.class);

    public static String stripRegistryId(String id) {
        return StringUtils.removeStart(id, ComponentRegistry.REGISTRY_ID);
    }

    public static void enrichSpecHeader(CMDComponentSpec spec, AbstractDescription description) {
        Header header = spec.getHeader();
        header.setID(description.getId());
        if (StringUtils.isEmpty(header.getName())) {
            header.setName(description.getName());
        }
        if (StringUtils.isEmpty(header.getDescription())) {
            header.setDescription(description.getDescription());
        }
    }

    public static boolean findComponentId(String componentId, List<CMDComponentType> componentReferences) {
        for (CMDComponentType cmdComponent : componentReferences) {
            if (componentId.equals(cmdComponent.getComponentId())) {
                return true;
            } else if (findComponentId(componentId, cmdComponent.getCMDComponent())) {
                return true;
            }
        }
        return false;
    }

    public static void writeXsd(CMDComponentSpec expandedSpec, OutputStream outputStream) {
        MDMarshaller.generateXsd(expandedSpec, outputStream);
    }

    public static void writeXml(CMDComponentSpec spec, OutputStream outputStream) {
        try {
            MDMarshaller.marshal(spec, outputStream);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error in encoding: ", e);
        } catch (JAXBException e) {
            LOG.error("Cannot marshall spec: " + spec, e);
        }
    }
}
