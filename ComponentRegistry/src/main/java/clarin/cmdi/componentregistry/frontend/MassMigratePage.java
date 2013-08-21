package clarin.cmdi.componentregistry.frontend;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page that starts up somekind of migration on the underlying data in the registry. Migrations are usually one off things so do not blindly
 * migrate but take a little time to know what you are doing. Backing up the data before a migrate is probably a good idea as well.
 * 
 */
@SuppressWarnings("serial")
public class MassMigratePage extends SecureAdminWebPage {

    private final static Logger LOG = LoggerFactory.getLogger(MassMigratePage.class);
    private FeedbackPanel feedback;
//    @SpringBean
//    private ComponentDescriptionDaoImpl componentDescriptionDao;
//    @SpringBean
//    private ProfileDescriptionDaoImpl profileDescriptionDao;
//    @SpringBean
//    private UserDaoImpl userDao;

    public MassMigratePage(final PageParameters pageParameters) {
	super(pageParameters);
	addLinks();
	feedback = new FeedbackPanel("feedback") {

	    @Override
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
	add(new Label("migrate1Label", "No migration implemented at the moment..."));
	add(new IndicatingAjaxLink("migrate1") {

	    @Override
	    public void onClick(final AjaxRequestTarget target) {
		if (target != null) {
		    target.addComponent(feedback);
		}
		startMigration();
	    }
	});
    }

    private void startMigration() {
	info("Nothing to migrate...");
    }
}
