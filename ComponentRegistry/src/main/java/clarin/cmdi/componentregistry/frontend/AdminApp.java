package clarin.cmdi.componentregistry.frontend;

import java.util.Locale;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

public class AdminApp extends WebApplication {

    @Override
    protected void init() {
        super.init();
        getDebugSettings().setAjaxDebugModeEnabled(false);
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));

        mountPage("userSettings", UserSettingsPage.class);
        mountPage("accounts", Accounts.class);
        mountPage("teams", Groups.class);
        mountPage("statistics", StatisticsPage.class);
        mountPage("log", ViewLogPage.class);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return AdminHomePage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return super
                .newSession(request, response)
                .setLocale(Locale.UK);
    }

}
