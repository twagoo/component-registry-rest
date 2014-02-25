package clarin.cmdi.componentregistry.frontend;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringBean;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.components.CMDElementType;
import clarin.cmdi.componentregistry.impl.database.CMDComponentSpecExpanderDbImpl;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryDbImpl;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author paucas
 */
public class StatisticsPage extends SecureAdminWebPage {

    private static final long serialVersionUID = 1L;
    @SpringBean(name = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;

    private static class Statistics {
        private int componentnumber = 0;
        private int elementcounter = 0;
        private int conceptlinkcounter = 0;
    }

    public StatisticsPage(final PageParameters pageParameters) throws IOException, ComponentRegistryException {
        super(pageParameters);
        ComponentRegistry registry = componentRegistryFactory.getPublicRegistry();
        addLinks();
        displayStatistics(registry);
    }

    private void displayStatistics(ComponentRegistry registry) throws ComponentRegistryException {
        List<ProfileDescription> profileList = registry.getProfileDescriptions();
        RepeatingView repeating = new RepeatingView("repeating");
        add(repeating);
        add(new Label("profilenumbermessage", "Current number of profiles in the component registry: " + profileList.size()));
        for (ProfileDescription pd : profileList) {
            displayProfileStatistics(pd, repeating, registry);
        }
        List<ComponentDescription> componentList = registry.getComponentDescriptions();
        RepeatingView repeatingcomp = new RepeatingView("repeatingcomp");
        add(repeatingcomp);
        add(new Label("componentnumbermessage", "Current number of components in the component registry: " + componentList.size()));
        for (ComponentDescription cd : componentList) {
            displayComponentStatistics(cd, repeatingcomp, registry);
        }
    }

    private void displayProfileStatistics(ProfileDescription pd, RepeatingView repeatingview, ComponentRegistry registry) throws ComponentRegistryException {
        WebMarkupContainer item = new WebMarkupContainer(repeatingview.newChildId());
        repeatingview.add(item);
        item.add(new Label("ID", pd.getId()));
        item.add(new Label("profname", pd.getName()));
        CMDComponentSpec profile = CMDComponentSpecExpanderDbImpl.expandProfile(pd.getId(), (ComponentRegistryDbImpl) registry);
        Statistics stats = new Statistics();
        componentCounter(profile, stats);
        item.add(new Label("nrcomp", "" + stats.componentnumber));
        item.add(new Label("nrprofelem", "" + stats.elementcounter));
        item.add(new Label("nrproflinks", "" + stats.conceptlinkcounter));
    }

    private void displayComponentStatistics(ComponentDescription cd, RepeatingView repeatingview,  ComponentRegistry registry) throws ComponentRegistryException {
        WebMarkupContainer item = new WebMarkupContainer(repeatingview.newChildId());
        repeatingview.add(item);
        item.add(new Label("ID", cd.getId()));
        item.add(new Label("compname", cd.getName()));
        CMDComponentSpec compspec = CMDComponentSpecExpanderDbImpl.expandComponent(cd.getId(), (ComponentRegistryDbImpl) registry);
        Statistics stats = new Statistics();
        componentCounter(compspec, stats);
        item.add(new Label("nrcomp", "" + stats.componentnumber));
        item.add(new Label("nrelem", "" + stats.elementcounter));
        item.add(new Label("nrcomplinks", "" + stats.conceptlinkcounter));
    }

    private void componentCounter(CMDComponentSpec components, Statistics stats) {
        componentCounter(Collections.singleton(components.getCMDComponent()), stats);
    }
    
    private void componentCounter(Collection<CMDComponentType> components, Statistics stats) {
        if (components != null) {
            for (CMDComponentType component : components) {
                stats.componentnumber++;
                List<CMDElementType> elementlist = component.getCMDElement();
                if (elementlist != null) {
                    stats.elementcounter += elementlist.size();
                    for (CMDElementType elem : elementlist) {
                        if (elem.getConceptLink() != null) {
                            stats.conceptlinkcounter++;
                        }
                    }
                }
                componentCounter(component.getCMDComponent(), stats);
            }
        }
    }

    @SuppressWarnings("serial")
    private void addLinks() {
        add(new Link("home") {

            @Override
            public void onClick() {
                setResponsePage(AdminHomePage.class);
            }
        });
    }
}
