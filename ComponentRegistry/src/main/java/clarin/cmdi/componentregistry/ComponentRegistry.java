package clarin.cmdi.componentregistry;

import java.util.List;

import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public interface ComponentRegistry {

    List<MDComponent> getMDComponents();

    List<String> getComponentDescriptions();

    List<MDComponent> getMDProfiles();

    List<String> getProfileDescriptions();

    String getMDProfile(String id);

    String getMDComponent(String id);

    List<MDProfile> searchMDProfiles(String searchPattern);

    /**
     * 
     * @return -1 if profile could not be registered
     */
    int registerMDProfile(ProfileDescription description, String profileContent);
    //What about some Credentials or metadata also add in the parameters?

    int registerMDComponent(ComponentDescription description, String componentContent);

    //Remove from registry?
    //getElements?
    //List<ConceptLinks> getConceptLinks(String componentId, String xpath); We only need xpath I think it contains the componentId's
}
