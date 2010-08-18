/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.IOException;
import java.util.List;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 *
 * @author paucas
 */
public class StatisticsPage extends SecureAdminWebPage {

    private ComponentRegistry registry = ComponentRegistryFactory.getInstance().getPublicRegistry();

    public StatisticsPage(final PageParameters pageParameters) throws IOException {
        super(pageParameters);
        addLinks();
        DisplayStatistics();
    }
    int componentnumber = 0;

    private void DisplayStatistics() {
        List<ProfileDescription> profileList = registry.getProfileDescriptions();       
        RepeatingView repeating = new RepeatingView("repeating");
        add(repeating);
        add(new Label("profilenumbermessage", "Current number of profiles in the component registry: " + profileList.size()));
        for (ProfileDescription pd : profileList) {
            componentsInProfile(pd, repeating);
        }
        List<ComponentDescription> componentList = registry.getComponentDescriptions();
        RepeatingView repeatingcomp = new RepeatingView("repeatingcomp");
        add(repeatingcomp);
        add(new Label("componentnumbermessage", "Current number of components in the component registry: " + componentList.size()));
        for (ComponentDescription cd : componentList) {
            componentsInComponents(cd, repeatingcomp);
        }
    }

    private void componentsInProfile(ProfileDescription pd, RepeatingView repeatingview) {
        WebMarkupContainer item = new WebMarkupContainer(repeatingview.newChildId());
        repeatingview.add(item);
        item.add(new Label("ID", pd.getId()));
        CMDComponentSpec profile = registry.getMDProfile(pd.getId());
        componentCounter(profile.getCMDComponent());
        item.add(new Label("nrcomp", "" + componentnumber));
        componentnumber = 0;
    }

    private void componentsInComponents(ComponentDescription cd, RepeatingView repeatingview) {
        WebMarkupContainer item = new WebMarkupContainer(repeatingview.newChildId());
        repeatingview.add(item);
        item.add(new Label("ID", cd.getId()));
        CMDComponentSpec compspec = registry.getMDComponent(cd.getId());
        componentCounter(compspec.getCMDComponent());
        item.add(new Label("nrcomp", "" + componentnumber));
        componentnumber = 0;
    }

    private void componentCounter(List<CMDComponentType> components) {
        if (components != null) {
            for (CMDComponentType child : components) {
                componentCounter(child.getCMDComponent());
                componentnumber++;
            }
        }
    }

    private void addLinks() {
        add(new Link("home") {

            @Override
            public void onClick() {
                setResponsePage(AdminHomePage.class);
            }
        });
    }
}
