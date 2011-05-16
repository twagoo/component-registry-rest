package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentSpec.Header;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public final class ComponentRegistryUtils {
    private ComponentRegistryUtils(){}

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
}
