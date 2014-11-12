package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;

import clarin.cmdi.componentregistry.Configuration;
import org.apache.wicket.markup.html.link.Link;

public abstract class SecureAdminWebPage extends WebPage {

    public SecureAdminWebPage(final PageParameters parameters) {
        super(parameters);
        Principal userPrincipal = getUserPrincipal();
        if (!Configuration.getInstance().isAdminUser(userPrincipal)) {
            setResponsePage(new AccessDeniedPage());
        }
        add(new MultiLineLabel("message", "Component Registry Admin Page.\nYou are logged in as: " + userPrincipal.getName() + ".\n"));
    }

    protected final Principal getUserPrincipal() {
	return getWebRequestCycle().getWebRequest().getHttpServletRequest().getUserPrincipal();
    }

    @SuppressWarnings(value = "serial")
    protected void addLinks() {
        add(new Link("home") {
            @Override
            public void onClick() {
                setResponsePage(AdminHomePage.class);
            }
        });
    }

}
