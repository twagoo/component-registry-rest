package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.ComponentSpec;
import eu.clarin.cmdi.toolkit.CMDToolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.XMLConstants;
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

public class MDMarshaller implements Serializable {

    private final static Logger LOG = LoggerFactory.getLogger(MDMarshaller.class);
    /**
     * I define W3C_XML_SCHEMA_NS_URI here cannot get it from
     *
     * @see XMLConstants there is a conflict between stax-api and java5.
     */
    private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
    private Schema generalComponentSchema;
    private final Templates componentToSchemaTemplates;
    private final ComponentRegistryResourceResolver resourceResolver;

    public MDMarshaller() throws TransformerException {
        resourceResolver = new ComponentRegistryResourceResolver();
        final TransformerFactory transformerFactory = TransformerFactory.newInstance(net.sf.saxon.TransformerFactoryImpl.class.getName(), null);
        transformerFactory.setURIResolver(resourceResolver);
        componentToSchemaTemplates = transformerFactory.newTemplates(resourceResolver.resolve(Configuration.getInstance().getComponent2SchemaXsl(), null));
    }

    /**
     *
     * @param docClass
     * @param inputStream
     * @param schema to validate against, can be null for no validation.
     * @return
     * @throws JAXBException
     */
    public <T> T unmarshal(Class<T> docClass, InputStream inputStream, Schema schema) throws JAXBException {
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
     * Will wrap the Outputstream in a OutputStreamWriter with encoding set to
     * UTF-8. This to make sure profiles are stored correctly.
     */
    public <T> void marshal(T marshallableObject, OutputStream out) throws JAXBException, UnsupportedEncodingException {
        final String packageName = marshallableObject.getClass().getPackage().getName();
        final JAXBContext jc = JAXBContext.newInstance(packageName);

        final Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        if (ComponentSpec.class.equals(marshallableObject.getClass())) {
            m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, Configuration.getInstance().getGeneralComponentSchema());
        }

        final Writer writer = new OutputStreamWriter(out, "UTF-8");
        m.marshal(marshallableObject, writer);
    }

    public synchronized Schema getComponentSchema() {
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

    public void generateXsd(ComponentSpec spec, OutputStream outputStream) {
        try {
            Transformer transformer = componentToSchemaTemplates.newTransformer();
            transformer.setParameter(CMDToolkit.XSLT_PARAM_COMP2SCHEMA_TOOL_KITLOCATION, Configuration.getInstance().getToolkitLocation());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            marshal(spec, out);
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

    private String getSpecId(ComponentSpec spec) {
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
    public <T> String marshalToString(T marshallableObject) {
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
