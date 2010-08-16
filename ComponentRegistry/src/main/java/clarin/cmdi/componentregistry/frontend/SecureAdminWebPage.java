package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;

import clarin.cmdi.componentregistry.Configuration;

public abstract class SecureAdminWebPage extends WebPage {

    public SecureAdminWebPage(final PageParameters parameters) {
        super(parameters);
        Principal userPrincipal = getUserPrincipal();
        if (!Configuration.getInstance().isAdminUser(userPrincipal)) {
            setResponsePage(new AccessDeniedPage());
        }
        add(new MultiLineLabel("message", "Component Registry Admin Page.\nYou are logged in as: " + userPrincipal.getName()
                + ".\nRegistry is located in: " + Configuration.getInstance().getRegistryRoot()));
    }

    protected Principal getUserPrincipal() {
        return getWebRequestCycle().getWebRequest().getHttpServletRequest().getUserPrincipal();
    }

}
