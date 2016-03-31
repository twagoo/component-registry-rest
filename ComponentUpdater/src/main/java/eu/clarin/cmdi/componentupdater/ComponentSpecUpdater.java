package eu.clarin.cmdi.componentupdater;

import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.schema.cmd.Validator;
import clarin.cmdi.schema.cmd.ValidatorException;
import eu.clarin.cmdi.toolkit.CMDToolkit;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.xml.sax.SAXException;

/**
 * This utility will apply a style sheet to all component specifications in the
 * Component Registry database (versions 1.14.5 and higher) that upgrades these
 * components to CMDI 1.2
 *
 * Usage: - make sure no other application is connected to the database - run
 * the main
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ComponentSpecUpdater {
    
    private static final Logger logger = LoggerFactory.getLogger(ComponentSpecUpdater.class);
    private boolean dryRun = false;
    
    private ClassPathXmlApplicationContext applicationContext;
    private ComponentDao componentDao;
    private TransactionTemplate transactionTemplate;
    private Transformer transformer;
    private final Map<String, String> parameters = new HashMap<>();
    private Validator componentValidator;
    private boolean doValidate = true;
    
    private void init() throws TransformerConfigurationException, SAXException {
        applicationContext = new ClassPathXmlApplicationContext("/spring-config/applicationContext.xml", "/spring-config/container-environment.xml");
        componentDao = applicationContext.getBean(ComponentDao.class);
        transactionTemplate = new TransactionTemplate(applicationContext.getBean(PlatformTransactionManager.class));
        transformer = createTransformer(parameters);
        componentValidator = new Validator(CMDToolkit.class.getResource(CMDToolkit.COMPONENT_SCHEMA));
        //TODO: pass the URL of a derived schema that adds a phase that excludes certain schematron checks we decided to skip for the upgrade
    }
    
    private static Transformer createTransformer(final Map<String, String> transformationParams) throws IllegalArgumentException, TransformerFactoryConfigurationError, TransformerConfigurationException {
        // create transformer
        final TransformerFactory factory = TransformerFactory.newInstance();
        final InputStream xsltStream = CMDToolkit.class.getResourceAsStream(CMDToolkit.XSLT_COMPONENT_UPGRADE);
        Transformer transformer = factory.newTransformer(new StreamSource(xsltStream));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setErrorListener(new ErrorListener() {
            @Override
            public void warning(TransformerException exception) throws TransformerException {
                logger.warn("Warning while transforming: {}", exception.getMessageAndLocation(), exception);
            }
            
            @Override
            public void error(TransformerException exception) throws TransformerException {
                logger.error("Error while transforming: {}", exception.getMessageAndLocation());
                throw exception;
            }
            
            @Override
            public void fatalError(TransformerException exception) throws TransformerException {
                logger.error("Fatal arror while transforming: {}", exception.getMessageAndLocation());
                throw exception;
            }
        });
        for (Entry<String, String> entry : transformationParams.entrySet()) {
            transformer.setParameter(entry.getKey(), entry.getValue());
        }
        return transformer;
    }
    
    private void run() {
        if (dryRun) {
            logger.info("Dry run - no changes will be aplied to the database");
        } else {
            logger.warn("No dry run - changes will be aplied to the database!");
        }
        
        if (!doValidate) {
            logger.warn("Skipping validation!");
        }
        
        final List<BaseDescription> descriptions = componentDao.getAllNonDeletedDescriptions();
        logger.info("Found {} components", descriptions.size());
        
        fixSpecs(descriptions);
    }
    
    private void fixSpecs(final List<BaseDescription> descriptions) {
        final MutableInt success = new MutableInt(0);
        final MutableInt count = new MutableInt(0);

        // read and update all in a single transaction
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus ts) {
                for (final BaseDescription descr : descriptions) {
                    count.increment();
                    final String id = descr.getId();
                    logger.info("Updating {} ({}/{})", id, count, descriptions.size());

                    //set status string
                    final String status = componentDao.isPublic(id) ? "production" : "development";
                    final Map<String, String> params = new HashMap<>(1);
                    params.put("cmd-component-status", status);
                    
                    final String content = componentDao.getContent(false, id);
                    if (content.contains("CMDVersion=\"1.2\"")) {
                        logger.warn("Component {} already appears to be a CMDI 1.2 component. Skipping conversion!", id);
                    } else {
                        try {
                            logger.trace("Transformation input: {}", content);
                            final String newContent = transformXml(content, params);
                            logger.debug("Transformation output: {}", newContent);
                            
                            if (doValidate) {
                                validate(id, newContent);
                            }
                            
                            if (dryRun && logger.isInfoEnabled()) {
                                final int previewLength = Math.min(500, Math.min(content.length(), newContent.length()));
                                logger.info("Transformation result:\n-------\n{}...\n=======>\n{}...\n-------", content.substring(0, previewLength), newContent.substring(0, previewLength));
                            } else {
                                componentDao.updateDescription(descr.getDbId(), descr, newContent);
                            }
                            success.increment();
                        } catch (TransformerException ex) {
                            logger.error("Error while transforming {} in component {}:\n\n{}", ex.getMessageAndLocation(), id, content);
                            throw new RuntimeException("Failed to transform " + id, ex);
                        }
                    }
                }
            }
        });
        logger.info("Successfully transformed {} out of {} components", success.intValue(), descriptions.size());
    }
    
    private String transformXml(String content, Map<String, String> extraParams) throws TransformerException {
        final StringReader sourceReader = new StringReader(content);
        final Source source = new StreamSource(sourceReader);
        final StringWriter resultWriter = new StringWriter();
        final Result result = new StreamResult(resultWriter);

        // set extra params before transform
        for (Entry<String, String> param : extraParams.entrySet()) {
            transformer.setParameter(param.getKey(), param.getValue());
        }
        
        try {
            transformer.transform(source, result);
            return resultWriter.toString();
        } finally {
            //reset parameters
            for (Entry<String, String> param : extraParams.entrySet()) {
                transformer.setParameter(param.getKey(), parameters.get(param.getKey()));
            }
        }
    }
    
    private void validate(final String id, final String newContent) throws RuntimeException {
        logger.debug("Validating content of {}", id);
        try {
            if (!componentValidator.validateProfile(new StreamSource(new StringReader(newContent)))) {
                logger.error("Validation error(s) in {}: '{}'\n\n{}", id, componentValidator.getMessages(), newContent);
                if (!dryRun) {
                    throw new RuntimeException("Validation error");
                }
            }
        } catch (ValidatorException ex) {
            logger.error("Validation error in {}: '{}'\n\n{}", id, ex.getMessage(), newContent);
            throw new RuntimeException("Failed to validate " + id, ex);
        } catch (IOException ex) {
            logger.error("Error reading file", ex);
            throw new RuntimeException("Failed to validate " + id, ex);
        }
        logger.info("New content for {} is valid", id);
    }
    
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
    
    public void setDoValidate(boolean doValidate) {
        this.doValidate = doValidate;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public static void main(String[] args) throws TransformerConfigurationException, SAXException {
        final ComponentSpecUpdater updater = new ComponentSpecUpdater();
        
        final List<String> arguments = Arrays.asList(args);
        updater.setDryRun(arguments.contains("-d"));
        
        final String conversionParamsPropertiesFile = System.getProperty("conversionParamsPropertiesFile");
        if (conversionParamsPropertiesFile != null) {
            applyConversionParams(updater, conversionParamsPropertiesFile);
        }
        
        final String skipValidation = System.getProperty("skipValidation");
        if ("true".equalsIgnoreCase(skipValidation)) {
            updater.setDoValidate(false);
        }
        
        logger.info("Initializing...");
        updater.init();
        
        logger.info("Running fixer...");
        updater.run();
    }
    
    private static void applyConversionParams(final ComponentSpecUpdater componentSpecFixer, final String conversionParamsPropertiesFile) {
        final Map<String, String> params = componentSpecFixer.getParameters();
        try {
            final FileReader fileReader = new FileReader(conversionParamsPropertiesFile);
            final Properties props = new Properties();
            props.load(fileReader);
            for (Object key : props.keySet()) {
                final String keyString = (String) key;
                final String value = props.getProperty(keyString);
                logger.info("Setting stylesheet property '{}' to '{}'", keyString, value);
                params.put(keyString, value);
            }
            
        } catch (IOException ex) {
            logger.error("Could not read conversion parameters from properties file {}", conversionParamsPropertiesFile, ex);
            System.exit(1);
        }
    }
}
