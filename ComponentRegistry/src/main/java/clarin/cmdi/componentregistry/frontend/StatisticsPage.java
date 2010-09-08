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
import clarin.cmdi.componentregistry.CMDComponentSpecExpander;
import clarin.cmdi.componentregistry.ComponentRegistryImpl;
import clarin.cmdi.componentregistry.components.CMDElementType;
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

    private transient ComponentRegistry registry = ComponentRegistryFactory.getInstance().getPublicRegistry();
    private transient CMDComponentSpecExpander reg = new CMDComponentSpecExpander((ComponentRegistryImpl) registry);
    private int componentnumber = 0;
    private int elementcounter = 0;
    private int conceptlinkcounter = 0;

    public StatisticsPage(final PageParameters pageParameters) throws IOException {
        super(pageParameters);
        addLinks();
        DisplayStatistics();
    }

    private void DisplayStatistics() {
        List<ProfileDescription> profileList = registry.getProfileDescriptions();
        RepeatingView repeating = new RepeatingView("repeating");
        add(repeating);
        add(new Label("profilenumbermessage", "Current number of profiles in the component registry: " + profileList.size()));
        for (ProfileDescription pd : profileList) {
            displayProfileStatistics(pd, repeating);
        }
        List<ComponentDescription> componentList = registry.getComponentDescriptions();
        RepeatingView repeatingcomp = new RepeatingView("repeatingcomp");
        add(repeatingcomp);
        add(new Label("componentnumbermessage", "Current number of components in the component registry: " + componentList.size()));
        for (ComponentDescription cd : componentList) {
            displayComponentStatistics(cd, repeatingcomp);
        }
    }

    private void displayProfileStatistics(ProfileDescription pd, RepeatingView repeatingview) {
        WebMarkupContainer item = new WebMarkupContainer(repeatingview.newChildId());
        repeatingview.add(item);
        item.add(new Label("ID", pd.getId()));
        item.add(new Label("profname", pd.getName()));
        CMDComponentSpec profile = reg.expandProfile(pd.getId(), (ComponentRegistryImpl) registry);
        componentCounter(profile.getCMDComponent());
        item.add(new Label("nrcomp", "" + componentnumber));
        componentnumber = 0;
        item.add(new Label("nrprofelem", "" + elementcounter));
        elementcounter = 0;
        item.add(new Label("nrproflinks", "" + conceptlinkcounter));
        conceptlinkcounter = 0;
    }

    private void displayComponentStatistics(ComponentDescription cd, RepeatingView repeatingview) {
        WebMarkupContainer item = new WebMarkupContainer(repeatingview.newChildId());
        repeatingview.add(item);
        item.add(new Label("ID", cd.getId()));
        item.add(new Label("compname", cd.getName()));
        CMDComponentSpec compspec = reg.expandComponent(cd.getId(), (ComponentRegistryImpl) registry);
        componentCounter(compspec.getCMDComponent());
        item.add(new Label("nrcomp", "" + componentnumber));
        componentnumber = 0;
        item.add(new Label("nrelem", "" + elementcounter));
        elementcounter = 0;
        item.add(new Label("nrcomplinks", "" + conceptlinkcounter));
        conceptlinkcounter = 0;
    }

    private void componentCounter(List<CMDComponentType> components) {
        if (components != null) {
            for (CMDComponentType component : components) {
                componentnumber++;
                List<CMDElementType> elementlist = component.getCMDElement();
                if (elementlist != null) {
                    elementcounter = elementcounter + elementlist.size();
                    for (CMDElementType elem : elementlist) {
                        if (elem.getConceptLink() != null) {
                            conceptlinkcounter++;
                        }
                    }
                }
                componentCounter(component.getCMDComponent());
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
