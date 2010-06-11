package clarin.cmdi.componentregistry.frontend;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

public class AdminApp extends WebApplication {

    @Override
    protected void init() {
        super.init();
        getDebugSettings().setAjaxDebugModeEnabled(false);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return AdminHomePage.class;
    }

}
