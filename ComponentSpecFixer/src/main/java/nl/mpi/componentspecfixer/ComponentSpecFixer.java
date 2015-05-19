/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.componentspecfixer;

import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentSpecFixer {

    private final static Logger logger = LoggerFactory.getLogger(ComponentSpecFixer.class);

    private ClassPathXmlApplicationContext applicationContext;
    private ComponentDao componentDao;
    private TransactionTemplate transactionTemplate;

    private void init() {
        applicationContext = new ClassPathXmlApplicationContext("/spring-config/applicationContext.xml", "/spring-config/container-environment.xml");
        componentDao = applicationContext.getBean(ComponentDao.class);
        transactionTemplate = new TransactionTemplate(applicationContext.getBean(PlatformTransactionManager.class));
    }

    private void run() {
        final List<BaseDescription> descriptions = componentDao.getAllNonDeletedDescriptions();
        logger.info("Found {} components", descriptions.size());

        fixSpecs(descriptions);
    }

    private void fixSpecs(List<BaseDescription> descriptions) {
        for (final BaseDescription descr : descriptions) {
            final String id = descr.getId();

            transactionTemplate.execute(new TransactionCallback() {

                @Override
                public Object doInTransaction(TransactionStatus ts) {
                    logger.info("Updating {}", id);

                    final String content = componentDao.getContent(false, id);
                    //TODO: update content
                    final String newContent = content;
                    componentDao.updateDescription(descr.getDbId(), descr, newContent);

                    return null;
                }
            });

        }
    }

    public static void main(String[] args) {
        final ComponentSpecFixer componentSpecFixer = new ComponentSpecFixer();

        logger.info("Initializing...");
        componentSpecFixer.init();

        logger.info("Running fixer...");
        componentSpecFixer.run();
    }
}
