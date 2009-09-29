package clarin.cmdi.componentregistry;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBException;

/**
 * Hello world!
 * 
 */
public class Browser {

    public String printComponents() throws FileNotFoundException, JAXBException {
        ComponentRegistry registry = new ComponentRegistryImpl();
        StringBuilder result = new StringBuilder();
        List<MDComponent> mdComponents = registry.getMDComponents();
        result.append("There are " + mdComponents.size() + " components registered\n");
        for (MDComponent component : mdComponents) {
            result.append("component : " + component.getCmdComponentType().getName() + "\n");
        }
        result.append("\n");
        return result.toString();
    }

}
