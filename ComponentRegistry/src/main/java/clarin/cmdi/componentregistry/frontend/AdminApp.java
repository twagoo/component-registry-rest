package clarin.cmdi.componentregistry.frontend;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

public class AdminApp extends WebApplication {

    @Override
    protected void init() {
        super.init();
        getDebugSettings().setAjaxDebugModeEnabled(false);
        addComponentInstantiationListener(new SpringComponentInjector(this));
	
	mountBookmarkablePage("userSettings", UserSettingsPage.class);
        mountBookmarkablePage("accounts", Accounts.class);
        mountBookmarkablePage("groups", Groups.class);
        mountBookmarkablePage("statistics", StatisticsPage.class);
        mountBookmarkablePage("log", ViewLogPage.class);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return AdminHomePage.class;
    }

}
