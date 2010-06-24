package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class AccessDeniedPage extends WebPage {

    private static final long serialVersionUID = 1L;

    public AccessDeniedPage() {
        Principal userPrincipal = getWebRequestCycle().getWebRequest().getHttpServletRequest().getUserPrincipal();
        String userName = "not logged in";
        if (userPrincipal != null && userPrincipal.getName() != null) {
            userName = userPrincipal.getName();
        }
        add(new Label("userName", userName));
    }

    @Override
    protected void configureResponse() {
        super.configureResponse();
        getWebRequestCycle().getWebResponse().getHttpServletResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public boolean isErrorPage() {
        return true;
    }
}
