package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;

import clarin.cmdi.componentregistry.Configuration;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.servlet.AbortWithHttpStatusException;

public abstract class SecureAdminWebPage extends WebPage {

    public SecureAdminWebPage(final PageParameters parameters) {
        super(parameters);
        Principal userPrincipal = getUserPrincipal();
        if (!Configuration.getInstance().isAdminUser(userPrincipal)) {
            setResponsePage(new AccessDeniedPage());
        }
        if (userPrincipal == null) {
            throw new AbortWithHttpStatusException(HttpServletResponse.SC_UNAUTHORIZED, false);
        } else {
            add(new MultiLineLabel("message", "Component Registry Admin Page.\nYou are logged in as: " + userPrincipal.getName() + ".\n"));
        }
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
