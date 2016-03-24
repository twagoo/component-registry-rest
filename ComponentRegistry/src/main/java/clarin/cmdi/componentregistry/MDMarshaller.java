package clarin.cmdi.componentregistry;

import static clarin.cmdi.componentregistry.CmdVersion.CANONICAL_CMD_VERSION;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import com.google.common.collect.Maps;
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
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final Map<CmdVersion, Templates> componentToSchemaTemplatesMap;
    private final ComponentRegistryResourceResolver resourceResolver;
    
    @Autowired
    private ComponentSpecConverter specConverter;
    
    public MDMarshaller(Map<CmdVersion, String> stylesheetLocations) throws TransformerException {
        resourceResolver = new ComponentRegistryResourceResolver();
        
        final TransformerFactory transformerFactory = TransformerFactory.newInstance(net.sf.saxon.TransformerFactoryImpl.class.getName(), null);
        transformerFactory.setURIResolver(resourceResolver);

        // create templates on basis of stylesheet URIs
        componentToSchemaTemplatesMap = Maps.newEnumMap(CmdVersion.class);
        for (Entry<CmdVersion, String> versionStylesheet : stylesheetLocations.entrySet()) {
            final String stylesheetUri = versionStylesheet.getValue();
            if (stylesheetUri == null) {
                // probably missing context parameter
                LOG.warn("Missing URI for {}", versionStylesheet.getKey());
            } else {
                // create templates
                final Templates templates = transformerFactory.newTemplates(resourceResolver.resolve(stylesheetUri, null));
                // add to map
                componentToSchemaTemplatesMap.put(versionStylesheet.getKey(), templates);
            }
        }
        
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
    
    public void generateXsd(ComponentSpec spec, CmdVersion cmdVersion, OutputStream outputStream) throws JAXBException, TransformerException {
        try {
            final Templates templates = componentToSchemaTemplatesMap.get(cmdVersion);
            if (templates == null) {
                LOG.error("No transformation templates to create a schema for {}", cmdVersion);
                throw new UnsupportedOperationException("Don't know how to make a schema for " + cmdVersion.toString());
            } else {
                final Transformer transformer = templates.newTransformer();
                transformer.setParameter(CMDToolkit.XSLT_PARAM_COMP2SCHEMA_TOOL_KITLOCATION, Configuration.getInstance().getToolkitLocation());

                // create spec XML
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                marshal(spec, out);

                // check if conversion is necessary
                final byte[] xmlBytes;
                if (cmdVersion != CANONICAL_CMD_VERSION) {
                    //convert to target version before transforming
                    final byte[] canonicalOut = out.toByteArray();
                    final ByteArrayOutputStream convertedOut = new ByteArrayOutputStream();
                    specConverter.convertComponentSpec(CANONICAL_CMD_VERSION, cmdVersion, new ByteArrayInputStream(canonicalOut), new OutputStreamWriter(convertedOut));

                    //use converted XML as input for transformation
                    xmlBytes = convertedOut.toByteArray();
                } else {
                    //use 'canonical' XML as input for transformation
                    xmlBytes = out.toByteArray();
                }

                // transform to XSD
                ByteArrayInputStream input = new ByteArrayInputStream(xmlBytes);
                transformer.transform(new StreamSource(input), new StreamResult(outputStream));
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error in encoding: ", e);
            throw new RuntimeException();
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
