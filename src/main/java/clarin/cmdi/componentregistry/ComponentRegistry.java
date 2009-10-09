package clarin.cmdi.componentregistry;

import java.util.List;


public interface ComponentRegistry {

    List<MDComponent> getMDComponents();
    
    List<MDProfile> getMDProfiles();

    String getMDProfile(String id);

    List<MDProfile> searchMDProfiles(String searchPattern);

    /**
     * 
     * @param The xml string of the profile. 
     * @return boolean
     */
    boolean registerMDProfile(String profile);
    //What about some Credentials or metadata also add in the parameters?

    //Remove from registry?
    //getElements?
    //List<ConceptLinks> getConceptLinks(String componentId, String xpath); We only need xpath I think it contains the componentId's
}
