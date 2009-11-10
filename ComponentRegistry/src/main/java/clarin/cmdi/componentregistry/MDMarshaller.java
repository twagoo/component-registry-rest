package clarin.cmdi.componentregistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class MDMarshaller {
    private final static Logger LOG = LoggerFactory.getLogger(MDMarshaller.class);
    private static Schema generalComponentSchema;

    private MDMarshaller() {
    }

    /**
     * Helper method that logs errors and returns null if unmarshal failed
     */
    public static <T> T unmarshal(Class<T> docClass, File file) {
        T result = null;
        try {
            result = unmarshal(docClass, new FileInputStream(file), null);
        } catch (JAXBException e) {
            LOG.error("Cannot unmarshal xml file: " + file, e);
        } catch (IOException e) {
            LOG.error("Cannot retrieve profile description from file: " + file, e);
        }
        return result;
    }

    /**
     * 
     * @param docClass
     * @param inputStream
     * @param schema to validate against, can be null for no validation.
     * @return
     * @throws JAXBException
     */
    public static <T> T unmarshal(Class<T> docClass, InputStream inputStream, Schema schema) throws JAXBException {
        String packageName = docClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();
        if (schema != null) {
            u.setSchema(schema);
        }
        Object unmarshal = u.unmarshal(inputStream);
        T doc = (T) unmarshal;
        return doc;
    }

    public static <T> void marshal(T marshallableObject, Writer writer) throws JAXBException {
        String packageName = marshallableObject.getClass().getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(marshallableObject, writer);
    }

    public static Schema getCMDComponentSchema() {
        if (generalComponentSchema == null) {
            try {
                generalComponentSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(
                        new URL("http://www.clarin.eu/cmd/general-component-schema.xsd"));
            } catch (MalformedURLException e) {
                LOG.error("Cannot instantiate schema", e);
            } catch (SAXException e) {
                LOG.error("Cannot instantiate schema", e);
            }
        }
        return generalComponentSchema;
    }

}
