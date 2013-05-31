package clarin.cmdi.componentregistry.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.IdSequence;

@XmlRootElement(name = "profileDescription")
public class ProfileDescription extends AbstractDescription implements Serializable {

    // Attention! PROFILE_PREFIX here and the client's Config.PROFILE_PREFIX must be the same 
    // If you change PROFILE_PREFIX here, then the client's  Config.PROFILE_PREFIX
    public static final String PROFILE_PREFIX = ComponentRegistry.REGISTRY_ID + "p_";
    
    private static final long serialVersionUID = 1L;
    private boolean showInEditor = true;

    public static ProfileDescription createNewDescription() {
	String id = PROFILE_PREFIX + IdSequence.get();
	ProfileDescription desc = new ProfileDescription();
	desc.setId(id);
	desc.setRegistrationDate(createNewDate());
	return desc;
    }

    /**
     * Whether this profile should be shown in metadata editor (e.g. Arbil)
     *
     * @return the value of showInEditor
     */
    public boolean isShowInEditor() {
	return showInEditor;
    }

    /**
     * Gets whether this profile should be shown in metadata editor (e.g. Arbil)
     *
     * @param showInEditor new value of showInEditor
     */
    public void setShowInEditor(boolean showInEditor) {
	this.showInEditor = showInEditor;
    }
}
