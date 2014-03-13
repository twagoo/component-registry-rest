package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryResourceResolver;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.schema.cmd.Validator.Message;
import clarin.cmdi.schema.cmd.ValidatorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MDValidator implements Validator {
    
    private final static Logger LOG = LoggerFactory.getLogger(MDValidator.class);
    static final String MISMATCH_ERROR = "Cannot register component as a profile or vica versa.";
    static final String COMPONENT_NOT_REGISTERED_ERROR = "referenced component is not registered or does not have a correct componentId: ";
    static final String PARSE_ERROR = "Error in validation input file: ";
    static final String SCHEMA_ERROR = "Error in reading general component schema: ";
    static final String IO_ERROR = "Error while reading specification or general component schema: ";
    static final String COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR = "referenced component cannot be found in the published components: ";
    static final String COMPONENT_REGISTRY_EXCEPTION_ERROR = "An exception occurred while accessing the component registry: ";
    static final String ILLEGAL_ATTRIBUTE_NAME_ERROR = "Illegal attribute name: ";
    static final String UNKNOWN_VALIDATION_ERROR = "Unknown validation error";
    static final Collection<String> ILLEGAL_ATTRIBUTE_NAMES = Collections.unmodifiableCollection(Arrays.asList("ref", "ComponentId"));
    private List<String> errorMessages = new ArrayList<String>();
    private CMDComponentSpec spec = null;
    private byte[] originalSpecBytes;
    private final InputStream input;
    private final AbstractDescription description;
    private final ComponentRegistry registry;
    private final ComponentRegistry userRegistry;
    private final ComponentRegistry publicRegistry;
    private final MDMarshaller marshaller;

    /**
     *
     * @param input In order to validate the input is consumed. So use
     * @see getCMDComponentSpec to get the parsed CMDComponentSpec.
     * @param desc
     * @param registry (registry you currently used)
     * @param userRegistry can be null, We get user registry as well so we can
     * give nice error messages if needed. Can be the same as
     * @param registry
     */
    public MDValidator(InputStream input, AbstractDescription description, ComponentRegistry registry, ComponentRegistry userRegistry, ComponentRegistry publicRegistry, MDMarshaller marshaller) {
        this.input = input;
        this.description = description;
        this.registry = registry;
        this.userRegistry = userRegistry;
        this.publicRegistry = publicRegistry;
        this.marshaller = marshaller;
    }
    
    @Override
    public List<String> getErrorMessages() {
        return errorMessages;
    }
    
    @Override
    public boolean validate() {
        try {
            clarin.cmdi.schema.cmd.Validator validator = new clarin.cmdi.schema.cmd.Validator(new URL(Configuration.getInstance().getGeneralComponentSchema()));
            validator.setResourceResolver(new ComponentRegistryResourceResolver());
            // We may need to reuse the input stream, so save it to a byte array first
            originalSpecBytes = getBytesFromInputStream();
            StreamSource source = new StreamSource(new ByteArrayInputStream(originalSpecBytes));
            if (!validator.validateProfile(source)) {
                final List<Message> validatorMessages = validator.getMessages();
                if (validatorMessages.size() > 0) {
                    for (Message message : validatorMessages) {
                        errorMessages.add(PARSE_ERROR + message.getText());
                    }
                } else {
                    errorMessages.add(PARSE_ERROR + UNKNOWN_VALIDATION_ERROR);
                }
            } else {
                spec = unmarshalSpec(originalSpecBytes);
                if (spec.isIsProfile() != description.isProfile()) {
                    errorMessages.add(MISMATCH_ERROR);
                }
            }
        } catch (MalformedURLException e) {
            errorMessages.add(SCHEMA_ERROR + e.getMessage());
            LOG.error(SCHEMA_ERROR, e);
        } catch (JAXBException e) {
            errorMessages.add(PARSE_ERROR + e.getMessage());
            LOG.error(PARSE_ERROR, e);
        } catch (ValidatorException e) {
            errorMessages.add(PARSE_ERROR + e.getMessage());
            LOG.error(PARSE_ERROR, e);
        } catch (IOException e) {
            errorMessages.add(IO_ERROR + e.getMessage());
            LOG.error(IO_ERROR, e);
        }
        if (errorMessages.isEmpty()) {
            try {
                validateComponents(spec);
            } catch (ComponentRegistryException e) {
                errorMessages.add(COMPONENT_REGISTRY_EXCEPTION_ERROR + e);
            }
        }
        return errorMessages.isEmpty();
    }
    
    private byte[] getBytesFromInputStream() throws IOException {
        int len;
        byte[] b = new byte[4096];
        final ByteArrayOutputStream bOS = new ByteArrayOutputStream();
        
        while ((len = input.read(b)) > 0) {
            bOS.write(b, 0, len);
        }
        
        return bOS.toByteArray();
    }

    private void validateComponents(CMDComponentSpec componentSpec) throws ComponentRegistryException {
        validateComponents(Collections.singletonList(componentSpec.getCMDComponent()));
    }
    
    private void validateComponents(List<CMDComponentType> cmdComponents) throws ComponentRegistryException {
        for (CMDComponentType cmdComponentType : cmdComponents) {
            validateDescribedComponents(cmdComponentType);
            validateComponents(cmdComponentType.getCMDComponent());//Recursion
        }
    }
    
    private void validateDescribedComponents(CMDComponentType cmdComponentType) throws ComponentRegistryException {
        checkPublicComponents(cmdComponentType);
    }
    
    private void checkPublicComponents(CMDComponentType cmdComponentType) throws ComponentRegistryException {
        if (isDefinedInSeparateFile(cmdComponentType)) {
            String id = cmdComponentType.getComponentId();
            CMDComponentSpec registeredComponent;
            if (registry.isPublic()) { // public registry requires only published components
                registeredComponent = registry.getMDComponent(id);
                if (registeredComponent == null) {
                    String error = cmdComponentType.getComponentId();
                    if (userRegistry != null) {
                        ComponentDescription desc = userRegistry.getComponentDescription(id);
                        if (desc != null) {
                            error = desc.getName() + " (" + cmdComponentType.getComponentId() + ")";
                        }
                    }
                    errorMessages.add(COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR + error);
                }
            } else { //User registry, can link to components from public registry and the user's registry
                registeredComponent = registry.getMDComponent(id);
                if (registeredComponent == null) {
                    registeredComponent = publicRegistry.getMDComponent(id);
                    if (registeredComponent == null) {
                        errorMessages.add(COMPONENT_NOT_REGISTERED_ERROR + cmdComponentType.getComponentId());
                    }
                }
                
            }
        }
    }
    
    private boolean isDefinedInSeparateFile(CMDComponentType cmdComponentType) {
        return cmdComponentType.getName() == null;
    }

    /**
     * Do not call before having called {@link #validate() }!
     *
     * @return the spec unmarshalled during {@link #validate() }. If this has
     * not been called, returns null.
     */
    public CMDComponentSpec getCMDComponentSpec() {
        return spec;
    }

    /**
     * Creates a fresh (re-unmarshalled) copy of the specification this instance
     * has validated. If you are not going to alter this copy, you can re-use
     * and share the copy used during validation by getting it from {@link #getCMDComponentSpec()
     * }.
     * <em>Do not call before having called {@link #validate() }!</em>
     *
     * @return a freshly unmarshalled copy of the spec based on the bytes
     * collected from the input stream passed to {@link #validate() }. If this
     * has not been called, returns null.
     * @throws JAXBException exception occurred while marshalling from the input
     * bytes
     * @see #validate()
     * @see #getCMDComponentSpec()
     */
    public CMDComponentSpec getCopyOfCMDComponentSpec() throws JAXBException {
        // Re-unmarshall original bytes
        return unmarshalSpec(originalSpecBytes);
    }
    
    private CMDComponentSpec unmarshalSpec(byte[] inputBytes) throws JAXBException {
        return marshaller.unmarshal(CMDComponentSpec.class, new ByteArrayInputStream(inputBytes), null);
    }
}
