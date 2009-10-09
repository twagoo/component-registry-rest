package clarin.cmdi.componentregistry;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBException;

public class Browser {

    public String printComponents(Configuration configuration) throws FileNotFoundException, JAXBException {
        ComponentRegistry registry = new ComponentRegistryImpl(configuration);
        StringBuilder result = new StringBuilder();
        List<MDComponent> mdComponents = registry.getMDComponents();
        result.append("Registry is located in: "+configuration.getRegistryRoot()+"\n");
        result.append("There are " + mdComponents.size() + " components registered\n");
        for (MDComponent component : mdComponents) {
            result.append("component : " + component.getCmdComponentType().getName() + "\n");
        }
        result.append("\n");
        return result.toString();
    }


}
