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


public class ComponentRegistryImpl implements ComponentRegistry {

    private File componentDir = new File("/Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi");

    public List<MDComponent> getMDComponents() {
        File[] files = componentDir.listFiles((FileFilter) new WildcardFileFilter("component*.xml"));
        List<MDComponent> result = new ArrayList<MDComponent>();
        for (int i = 0; i < files.length; i++) {
            CMDComponentSpec spec;
            try {
                spec = MDMarshaller.unmarshal(CMDComponentSpec.class, new FileInputStream(files[i]));
                List<CMDComponentType> cmdComponents = spec.getCMDComponent();
                if (cmdComponents.size() != 1) {
                    throw new RuntimeException("a component can consist of only one CMDComponent.");
                }
                result.add(new MDComponent(cmdComponents.get(0)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String getMDProfile(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<MDProfile> getMDProfiles() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean registerMDProfile(String profile) {
        // TODO Auto-generated method stub
        return false;
    }

    public List<MDProfile> searchMDProfiles(String searchPattern) {
        // TODO Auto-generated method stub
        return null;
    }

}
