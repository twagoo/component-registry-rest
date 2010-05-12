package clarin.cmdi.componentregistry;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public interface ComponentRegistry {

    public static final String REGISTRY_ID = "clarin.eu:cr1:";

    List<ComponentDescription> getComponentDescriptions();

    ComponentDescription getComponentDescription(String id);

    List<ProfileDescription> getProfileDescriptions();

    ProfileDescription getProfileDescription(String id);

    CMDComponentSpec getMDProfile(String id);

    CMDComponentSpec getMDComponent(String id);

    /**
     * 
     * @return -1 if profile could not be registered
     */
    int registerMDProfile(ProfileDescription description, CMDComponentSpec spec);

    int registerMDComponent(ComponentDescription description, CMDComponentSpec spec);

    String getMDProfileAsXml(String profileId);

    String getMDProfileAsXsd(String profileId);

    String getMDComponentAsXml(String componentId);

    String getMDComponentAsXsd(String componentId);

    /**
     * 
     * @param profileId
     * @param principal
     * @throws IOException
     * @throws UserUnauthorizedException thrown when principal does not match creator of profile
     */
    void deleteMDProfile(String profileId, Principal principal) throws IOException, UserUnauthorizedException;

    /**
     * 
     * @param componentId
     * @param principal
     * @throws IOException
     * @throws UserUnauthorizedException thrown when principal does not match creator of component
     */
    void deleteMDComponent(String componentId, Principal principal) throws IOException, UserUnauthorizedException;

    void update(AbstractDescription desc, Principal principal, CMDComponentSpec spec) throws IOException, JAXBException,
    UserUnauthorizedException;

    /**
     * 
     * @param componentId
     * @return List of ComponentDescriptions of Components that use the given Component.
     */
    List<ComponentDescription> getUsageInComponents(String componentId) ;

    /**
     * 
     * @param componentId
     * @return List of ProfileDescriptions of Profiles that use the given Component.
     */
    List<ProfileDescription> getUsageInProfiles(String componentId) ;

    /**
     * Return true if this registry is the public registry as opposed to a registry used for the user privately.
     **/
    boolean isPublic();
        
    
    //List<ConceptLinks> getConceptLinks(String componentId, String xpath); We only need xpath I think it contains the componentId's
}
