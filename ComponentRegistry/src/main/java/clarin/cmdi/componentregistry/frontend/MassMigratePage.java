package clarin.cmdi.componentregistry.frontend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
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

    private FeedbackPanel feedback;

    public MassMigratePage(final PageParameters pageParameters) {
        super(pageParameters);
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

    //Two migrate commands: 
    // 1) Put userId hashes in descriptions
    // 2) Move descriptions from CMD to description.xml
    private void addMigrationOptions() {
        add(new Label("migrate1Label", "Click here to start the migration of usernames into the descriptions."));
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
        info("Start Migration...");
        UserMapping userMap = ComponentRegistryFactory.getInstance().getUserMap();
        for (User user : userMap.getUsers()) {
            String userId = DigestUtils.md5Hex(user.getPrincipalName());
            ComponentRegistry registry = ComponentRegistryFactory.getInstance().getOtherUserComponentRegistry(getUserPrincipal(), userId);
            updateDescriptions(registry, userId, userMap);
        }
        ComponentRegistry registry = ComponentRegistryFactory.getInstance().getPublicRegistry();
        updateDescriptions(registry, null, userMap); // public has different username for every description.
        info("End Migration.");
    }

    private void updateDescriptions(ComponentRegistry registry, String userIdMD5, UserMapping userMap) {
        List<AbstractDescription> descs = new ArrayList<AbstractDescription>();
        descs.addAll(registry.getComponentDescriptions());
        descs.addAll(registry.getProfileDescriptions());
        for (AbstractDescription desc : descs) {
            setUserId(desc, userIdMD5, userMap, registry);
            setDescription(desc, registry);
        }
    }

    private void setUserId(AbstractDescription desc, String userIdMD5, UserMapping userMap, ComponentRegistry registry) {
        if (desc.getUserId() == null) {
            String userId = null;
            if (userIdMD5 == null) { //Lookup for every description, if not passed as parameter.
                for (User user : userMap.getUsers()) {
                    if (user.getName().equals(desc.getCreatorName())) {
                        userId = DigestUtils.md5Hex(user.getPrincipalName());
                        break;
                    }
                }
            } else {
                userId = userIdMD5;
            }
            if (userId != null) {
                desc.setUserId(userId);
                try {
                    registry.update(desc, getUserPrincipal(), null);
                    info("Updated: " + desc);
                } catch (Exception e) {
                    error("Error during update of:" + desc + "\nException" + e);
                }
            } else {
                error("Cannot get userId for: " + desc);
            }
        }
    }

    private void setDescription(AbstractDescription desc, ComponentRegistry registry) {
        CMDComponentSpec spec = getSpec(desc, registry);
        String specDescription = spec.getHeader().getDescription();
        if (specDescription != null && !specDescription.equals(desc.getDescription())) {
            info("override description: " + desc.getDescription() + " \nwith: " + specDescription);
            desc.setDescription(specDescription);
            try {
                registry.update(desc, getUserPrincipal(), null);
                info("Updated: " + desc);
            } catch (Exception e) {
                error("Error during update of:" + desc + "\nException" + e);
            }
        }
    }

    private CMDComponentSpec getSpec(AbstractDescription desc, ComponentRegistry registry) {
        if (desc.isProfile()) {
            return registry.getMDProfile(desc.getId());
        } else {
            return registry.getMDComponent(desc.getId());
        }
    }

}
