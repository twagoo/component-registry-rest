package clarin.cmdi.componentregistry;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import clarin.cmdi.componentregistry.Browser;

public class BrowserTest {

    @Test
    public void testApp() throws FileNotFoundException, JAXBException {
        Browser app = new Browser();
        String components = app.printComponents();
        System.out.println(components);

        assertTrue("TODO Make real tests", false);
    }

}
