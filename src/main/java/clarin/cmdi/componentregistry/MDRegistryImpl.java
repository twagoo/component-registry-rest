package clarin.cmdi.componentregistry;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;


public class MDRegistryImpl implements MDRegistry {

    private File componentDir = new File("/Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi");

    public List<CMDComponentType> getCMDComponents() {
        File[] files = componentDir.listFiles((FileFilter) new WildcardFileFilter("component*.xml"));
        List<CMDComponentType> result = new ArrayList<CMDComponentType>();
        for (int i = 0; i < files.length; i++) {
            CMDComponentSpec spec;
            try {
                spec = MDMarshaller.unmarshal(CMDComponentSpec.class, new FileInputStream(files[i]));
                List<CMDComponentType> cmdComponents = spec.getCMDComponent();
                if (cmdComponents.size() != 1) {
                    throw new RuntimeException("a component can consist of only one CMDComponent.");
                }
                result.add(cmdComponents.get(0));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
