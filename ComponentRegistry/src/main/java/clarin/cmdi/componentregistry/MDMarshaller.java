package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class MDMarshaller {

    private final static Logger LOG = LoggerFactory.getLogger(MDMarshaller.class);
    /**
     * I define W3C_XML_SCHEMA_NS_URI here cannot get it from
     *
     * @see XMLConstants there is a conflict between stax-api and java5.
     */
    private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
    private static Schema generalComponentSchema;

    private MDMarshaller() {
    }

    /**
     * Helper method that logs errors and returns null if unmarshal failed
     */
    public static <T> T unmarshal(Class<T> docClass, File file, Schema schema) {
	T result = null;
	try {
	    result = unmarshal(docClass, new FileInputStream(file), schema);
	} catch (JAXBException e) {
	    LOG.error("Cannot unmarshal xml file: " + file, e);
	} catch (IOException e) {
	    LOG.error("Cannot retrieve content from file: " + file, e);
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

    /**
     * Will wrap the Outputstream in a OutputStreamWriter with encoding set to UTF-8. This to make sure profiles are stored correctly.
     */
    public static <T> void marshal(T marshallableObject, OutputStream out) throws JAXBException, UnsupportedEncodingException {
	String packageName = marshallableObject.getClass().getPackage().getName();
	JAXBContext jc = JAXBContext.newInstance(packageName);

	Marshaller m = jc.createMarshaller();
	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	String schemaLocation = Configuration.getInstance().getSchemaLocation(marshallableObject.getClass().getName());
	if (schemaLocation != null) {
	    m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
	}
	Writer writer = new OutputStreamWriter(out, "UTF-8");
	m.marshal(marshallableObject, writer);
    }

    public static synchronized Schema getCMDComponentSchema() {
	if (generalComponentSchema == null) {
	    try {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setResourceResolver(new ComponentRegistryResourceResolver());
		generalComponentSchema = schemaFactory.newSchema(new URL(Configuration.getInstance().getGeneralComponentSchema()));
	    } catch (MalformedURLException e) {
		LOG.error("Cannot instantiate schema", e);
	    } catch (SAXException e) {
		LOG.error("Cannot instantiate schema", e);
	    }
	}
	return generalComponentSchema;
    }

    public static void generateXsd(CMDComponentSpec spec, OutputStream outputStream) {
	Templates componentToSchemaTemplates;
	try {
	    System.setProperty("javax.xml.transform.TransformerFactory", net.sf.saxon.TransformerFactoryImpl.class.getName());
	    componentToSchemaTemplates = TransformerFactory.newInstance().newTemplates(
		    new StreamSource(Configuration.getInstance().getComponent2SchemaXsl()));
	} catch (TransformerConfigurationException e) {
	    LOG.error("Cannot create Template", e);
	    return;
	}
	try {
	    Transformer transformer = componentToSchemaTemplates.newTransformer();
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    MDMarshaller.marshal(spec, out);
	    ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
	    transformer.transform(new StreamSource(input), new StreamResult(outputStream));
	} catch (TransformerConfigurationException e) {
	    LOG.error("Cannot create Transformer", e);
	} catch (TransformerException e) {
	    LOG.error("Cannot transform xml file: " + getSpecId(spec), e);
	} catch (UnsupportedEncodingException e) {
	    LOG.error("Error in encoding: ", e);
	} catch (JAXBException e) {
	    LOG.error("Cannot marshall spec: " + getSpecId(spec), e);
	}
    }

    private static String getSpecId(CMDComponentSpec spec) {
	String result = "";
	if (spec != null && spec.getHeader() != null) {
	    result = spec.getHeader().getID();
	}
	return result;
    }

    /**
     * @param marshallableObject
     * @return the xml representation of the marshallableObject
     * @throws jaxb exceptions are wrapped in RuntimeExceptions
     */
    public static <T> String marshalToString(T marshallableObject) {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try {
	    marshal(marshallableObject, out);
	    return out.toString("UTF-8");
	} catch (UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	} catch (JAXBException e) {
	    throw new RuntimeException(e);
	}
    }
}
