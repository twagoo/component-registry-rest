package clarin.cmdi.componentregistry;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.components.CMDComponentType;


/**
 * Hello world!
 * 
 */
public class Browser {

    public String printComponents() throws FileNotFoundException, JAXBException {
        MDRegistry registry = new MDRegistryImpl();
        StringBuilder result = new StringBuilder();
        List<CMDComponentType> cmdComponents = registry.getCMDComponents();
        result.append("There are " + cmdComponents.size() + " components registered\n");
        for (CMDComponentType cmdComponentType : cmdComponents) {
            result.append("component : " + cmdComponentType.getName() + "\n");
        }
        result.append("\n");
        return result.toString();
    }

}
