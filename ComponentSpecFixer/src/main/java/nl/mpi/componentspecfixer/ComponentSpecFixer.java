/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.componentspecfixer;

import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentSpecFixer {

    private static final Logger logger = LoggerFactory.getLogger(ComponentSpecFixer.class);
    private static final String XSLT_RESOURCE = "/xslt/collapse-component-spec.xsl";
    private static final boolean DRY_RUN = false;

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
        final InputStream xsltStream = getClass().getResourceAsStream(XSLT_RESOURCE);
        transformer = factory.newTransformer(new StreamSource(xsltStream));
    }

    private void run() {
        final List<BaseDescription> descriptions = componentDao.getAllNonDeletedDescriptions();
        logger.info("Found {} components", descriptions.size());

        fixSpecs(descriptions);
    }

    private void fixSpecs(List<BaseDescription> descriptions) {
        for (final BaseDescription descr : descriptions) {
            final String id = descr.getId();

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                @Override
                protected void doInTransactionWithoutResult(TransactionStatus ts) {
                    logger.debug("Updating {}", id);

                    final String content = componentDao.getContent(false, id);
                    try {
                        final String newContent = fixXml(content);
                        if (content.length() / newContent.length() > 1) {
                            logger.info("Reduced {} from {} to {} characters", id, content.length(), newContent.length());

                            if (!DRY_RUN) {
                                componentDao.updateDescription(descr.getDbId(), descr, newContent);
                            }
                        } else {
                            logger.info("No significant reduction for {}", id);
                        }

                    } catch (TransformerException ex) {
                        throw new RuntimeException("Failed to transform " + id, ex);
                    }
                }
            });

        }
    }

    private String fixXml(String content) throws TransformerException {
        final StringReader sourceReader = new StringReader(content);
        final Source source = new StreamSource(sourceReader);
        final StringWriter resultWriter = new StringWriter();
        final Result result = new StreamResult(resultWriter);
        transformer.transform(source, result);
        return resultWriter.toString();
    }

    public static void main(String[] args) throws TransformerConfigurationException {
        final ComponentSpecFixer componentSpecFixer = new ComponentSpecFixer();

        logger.info("Initializing...");
        componentSpecFixer.init();

        logger.info("Running fixer...");
        componentSpecFixer.run();
    }
}
