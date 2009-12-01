package clarin.cmdi.componentregistry;

import java.util.List;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public interface ComponentRegistry {

    List<MDComponent> getMDComponents();

    List<ComponentDescription> getComponentDescriptions();

    List<MDComponent> getMDProfiles();

    List<ProfileDescription> getProfileDescriptions();

    CMDComponentSpec getMDProfile(String id);

    CMDComponentSpec getMDComponent(String id);

    List<MDProfile> searchMDProfiles(String searchPattern);

    /**
     * 
     * @return -1 if profile could not be registered
     */
    int registerMDProfile(ProfileDescription description, CMDComponentSpec spec);
    //What about some Credentials or metadata also add in the parameters?

    int registerMDComponent(ComponentDescription description, CMDComponentSpec spec);

    String getMDProfileAsXml(String profileId);
    String getMDProfileAsXsd(String profileId);

    String getMDComponentAsXml(String componentId);
    String getMDComponentAsXsd(String componentId);

    //Remove from registry?
    //getElements?
    //List<ConceptLinks> getConceptLinks(String componentId, String xpath); We only need xpath I think it contains the componentId's
}
