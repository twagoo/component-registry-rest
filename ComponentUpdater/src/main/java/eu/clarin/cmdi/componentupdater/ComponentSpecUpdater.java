package eu.clarin.cmdi.componentupdater;

import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import eu.clarin.cmdi.toolkit.CMDToolkit;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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

    private void init() throws TransformerConfigurationException {
        applicationContext = new ClassPathXmlApplicationContext("/spring-config/applicationContext.xml", "/spring-config/container-environment.xml");
        componentDao = applicationContext.getBean(ComponentDao.class);
        transactionTemplate = new TransactionTemplate(applicationContext.getBean(PlatformTransactionManager.class));

        // create transformer
        final TransformerFactory factory = TransformerFactory.newInstance();
        final InputStream xsltStream = CMDToolkit.class.getResourceAsStream(CMDToolkit.XSLT_COMPONENT_UPGRADE);
        transformer = factory.newTransformer(new StreamSource(xsltStream));
    }

    private void run() {
        if (dryRun) {
            logger.info("Dry run - no changes will be aplied to the database");
        } else {
            logger.warn("No dry run - changes will be aplied to the database!");
        }
        final List<BaseDescription> descriptions = componentDao.getAllNonDeletedDescriptions();
        logger.info("Found {} components", descriptions.size());

        fixSpecs(descriptions);
    }

    private void fixSpecs(final List<BaseDescription> descriptions) {
        final MutableInt count = new MutableInt(0);

        // read and update all in a single transaction
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus ts) {
                for (final BaseDescription descr : descriptions) {
                    final String id = descr.getId();
                    logger.debug("Updating {}", id);

                    final String content = componentDao.getContent(false, id);
                    try {
                        logger.trace("Transformation input: {}", content);
                        final String newContent = transformXml(content);
                        logger.debug("Transformation output: {}", newContent);
                        if (dryRun && logger.isInfoEnabled()) {
                            final int previewLength = Math.min(500, Math.min(content.length(), newContent.length()));
                            logger.info("Transformation result:\n-------\n{}...\n=======>\n{}...\n-------", content.substring(0, previewLength), newContent.substring(0, previewLength));
                        } else {
                            componentDao.updateDescription(descr.getDbId(), descr, newContent);
                        }
                        count.increment();
                    } catch (TransformerException ex) {
                        logger.error("Error while transforming {} in component {}:\n\n{}", ex.getMessageAndLocation(), id, content);
                        throw new RuntimeException("Failed to transform " + id, ex);
                    }
                }
            }
        });

        logger.info("Successfully transformed {} components", count.intValue());
    }

    private String transformXml(String content) throws TransformerException {
        final StringReader sourceReader = new StringReader(content);
        final Source source = new StreamSource(sourceReader);
        final StringWriter resultWriter = new StringWriter();
        final Result result = new StreamResult(resultWriter);
        transformer.transform(source, result);
        return resultWriter.toString();
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public static void main(String[] args) throws TransformerConfigurationException {
        final ComponentSpecUpdater componentSpecFixer = new ComponentSpecUpdater();

        final List<String> arguments = Arrays.asList(args);
        componentSpecFixer.setDryRun(arguments.contains("-d"));

        logger.info("Initializing...");
        componentSpecFixer.init();

        logger.info("Running fixer...");
        componentSpecFixer.run();
    }
}
