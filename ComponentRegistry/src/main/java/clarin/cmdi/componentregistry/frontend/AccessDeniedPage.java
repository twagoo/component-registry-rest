package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.http.WebResponse;

public class AccessDeniedPage extends WebPage {

    private static final long serialVersionUID = 1L;

    public AccessDeniedPage() {
        final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
        final Principal userPrincipal = request.getUserPrincipal();
        String userName = "not logged in";
        if (userPrincipal != null && userPrincipal.getName() != null) {
            userName = userPrincipal.getName();
        }
        add(new Label("userName", userName));
    }

    @Override
    protected void configureResponse(WebResponse response) {
        super.configureResponse(response);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
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
