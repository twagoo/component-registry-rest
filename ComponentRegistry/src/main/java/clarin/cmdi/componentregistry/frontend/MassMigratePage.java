package clarin.cmdi.componentregistry.frontend;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.impl.database.AbstractDescriptionDao;
import clarin.cmdi.componentregistry.impl.database.ComponentDescriptionDao;
import clarin.cmdi.componentregistry.impl.database.ProfileDescriptionDao;
import clarin.cmdi.componentregistry.impl.database.UserDao;
import clarin.cmdi.componentregistry.impl.filesystem.ComponentRegistryFactoryImpl;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.UserMapping;
import clarin.cmdi.componentregistry.model.UserMapping.User;

/**
 * Page that starts up somekind of migration on the underlying data in the registry. Migrations are usually one off things so do not blindly
 * migrate but take a little time to know what you are doing. Backing up the data before a migrate is probably a good idea as well.
 * 
 */
@SuppressWarnings("serial")
public class MassMigratePage extends SecureAdminWebPage {
    private final static Logger LOG = LoggerFactory.getLogger(MassMigratePage.class);

    private FeedbackPanel feedback;

    @SpringBean(name = "fileRegistryFactory")
    private ComponentRegistryFactoryImpl fileRegistryFactory;
    @SpringBean
    private ComponentDescriptionDao componentDescriptionDao;
    @SpringBean
    private ProfileDescriptionDao profileDescriptionDao;
    @SpringBean
    private UserDao userDao;

    private transient UserMapping userMap;

    public MassMigratePage(final PageParameters pageParameters) {
        super(pageParameters);
        userMap = fileRegistryFactory.getUserMap();
        addLinks();
        feedback = new FeedbackPanel("feedback") {
            protected Component newMessageDisplayComponent(String id, FeedbackMessage message) {
                Serializable serializable = message.getMessage();
                MultiLineLabel label = new MultiLineLabel(id, (serializable == null) ? "" : serializable.toString());
                label.setEscapeModelStrings(getEscapeModelStrings());
                return label;
            }
        };
        feedback.setOutputMarkupPlaceholderTag(true);
        add(feedback);
        addMigrationOptions();
    }

    private void addLinks() {
        add(new Label("linksMessage", "Do some mass migration or go to:"));
        add(new Link("home") {
            @Override
            public void onClick() {
                setResponsePage(AdminHomePage.class);
            }
        });
    }

    private void addMigrationOptions() {
        add(new Label("migrate1Label", "Click here to start the migration of the file storage into the database."));
        add(new IndicatingAjaxLink("migrate1") {
            public void onClick(final AjaxRequestTarget target) {
                if (target != null) {
                    target.addComponent(feedback);
                }
                startMigration();
            }
        });
    }

    private void startMigration() {
        info("Start Migration users...");
        List<User> users = userMap.getUsers();
        for (User user : users) {
            userDao.insertUser(user);
        }
        info("Start Migration descriptions and content...");
        List<ComponentRegistry> registries = new ArrayList<ComponentRegistry>();
        registries.add(fileRegistryFactory.getPublicRegistry());
        registries.addAll(fileRegistryFactory.getAllUserRegistries());
        for (ComponentRegistry registry : registries) {
            migrateDescriptions(registry.getComponentDescriptions(), registry, componentDescriptionDao);
            migrateDescriptions(registry.getProfileDescriptions(), registry, profileDescriptionDao);
        }
        info("End Migration.");
    }

    private void migrateDescriptions(List<? extends AbstractDescription> descs, ComponentRegistry registry, AbstractDescriptionDao descDao) {
        int migrateCount = 0;
        for (AbstractDescription description : descs) {
            try {
                User user = userDao.getByPrincipalName(userMap.findUser(description.getUserId()).getPrincipalName());
                descDao.insertDescription(description, getContent(description, registry), registry.isPublic(), user.getId());
            } catch (Exception e) {
                LOG.error("Error in migration, check the logs!", e);
                info("Error cannot migrate " + description.getId());
            }
            migrateCount++;
        }
        LOG.info(registry.getName() + " migrated: " + migrateCount + " out of " + descs.size() + " descs");
    }

    private String getContent(AbstractDescription description, ComponentRegistry registry) throws UnsupportedEncodingException,
            JAXBException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CMDComponentSpec spec = null;
        if (description.isProfile()) {
            spec = registry.getMDProfile(description.getId());
        } else {
            spec = registry.getMDComponent(description.getId());
        }
        MDMarshaller.marshal(spec, out);
        return out.toString();
    }

}
