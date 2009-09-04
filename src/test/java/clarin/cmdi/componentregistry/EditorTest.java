package clarin.cmdi.componentregistry;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import clarin.cmdi.componentregistry.Editor;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;


public class EditorTest {
    
    @Test
    public void testCreateProfile() throws FileNotFoundException, JAXBException {
        CMDComponentSpec locComponent = MDMarshaller.unmarshal(CMDComponentSpec.class, new FileInputStream(new File(
                "/Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi/component-location.xml")));
        CMDComponentSpec writtenResourceComponent = MDMarshaller.unmarshal(CMDComponentSpec.class, new FileInputStream(new File(
                "/Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi/component-keys.xml")));

        Editor editor = new Editor();
        String result = editor.createProfile(locComponent.getCMDComponent().get(0), writtenResourceComponent.getCMDComponent().get(0));
        System.out.println(result);

        assertTrue("TODO Make real tests", false);
    }

}
