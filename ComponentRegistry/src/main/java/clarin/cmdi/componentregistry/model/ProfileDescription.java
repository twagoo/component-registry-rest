package clarin.cmdi.componentregistry.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.IdSequence;

@XmlRootElement(name = "profileDescription")
public class ProfileDescription extends AbstractDescription implements Serializable{

    private static final long serialVersionUID = 1L;

    public static ProfileDescription createNewDescription() {
        String id = ComponentRegistry.REGISTRY_ID + "p_" + IdSequence.get();
        ProfileDescription desc = new ProfileDescription();
        desc.setId(id);
        desc.setRegistrationDate(createNewDate());
        return desc;
    }

}
