package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;

import clarin.cmdi.componentregistry.Configuration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class SecureAdminWebPage extends WebPage {

    public SecureAdminWebPage(final PageParameters parameters) {
        super(parameters);
        Principal userPrincipal = getUserPrincipal();
        if (!Configuration.getInstance().isAdminUser(userPrincipal)) {
            setResponsePage(new AccessDeniedPage());
        }
        if (userPrincipal == null) {
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            add(new MultiLineLabel("message", "Component Registry Admin Page.\nYou are logged in as: " + userPrincipal.getName() + ".\n"));
        }
    }

    protected final Principal getUserPrincipal() {
        return getHttpServletRequest().getUserPrincipal();
    }

    protected HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) getRequest().getContainerRequest();
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
