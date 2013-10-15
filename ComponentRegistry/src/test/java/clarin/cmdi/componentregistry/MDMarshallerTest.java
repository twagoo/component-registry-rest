package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.BaseComponent;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import java.io.ByteArrayInputStream;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MDMarshallerTest {

    private static Logger logger = LoggerFactory.getLogger(MDMarshallerTest.class);
    
    @Test
    public void testMarshalToString() throws Exception {
	MDMarshaller instance = new MDMarshaller();
	BaseComponent desc = ComponentDescription.createNewDescription();
	desc.setName("Test \u00CA test");
	String test = instance.marshalToString(desc);
	logger.info(test);
	Assert.assertTrue(test.contains("Test \u00CA test"));
	BaseComponent result = instance.unmarshal(ComponentDescription.class, new ByteArrayInputStream(test.getBytes("UTF-8")), null);
	logger.info(result.getName());
	Assert.assertEquals(desc.getName(), result.getName());
    }
}
