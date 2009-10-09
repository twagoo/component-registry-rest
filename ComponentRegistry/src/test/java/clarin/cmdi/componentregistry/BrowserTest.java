package clarin.cmdi.componentregistry;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class BrowserTest {

    @Test
    public void testApp() throws FileNotFoundException, JAXBException {
        Browser app = new Browser();
        Configuration testConfig = new Configuration();
        testConfig.setRegistryRoot(new File("/Users/patdui/Workspace/Clarin/metadata/toolkit"));//TODO not good work in progress
        testConfig.init();
        String components = app.printComponents(testConfig);
        System.out.println(components);
        //TODO make real test
    }

}
