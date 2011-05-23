package clarin.cmdi.componentregistry.model;

import java.io.Serializable;
import java.util.Comparator;

import javax.xml.bind.annotation.XmlRootElement;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.IdSequence;

@XmlRootElement(name = "profileDescription")
public class ProfileDescription extends AbstractDescription implements Serializable{

    private static final long serialVersionUID = 1L;

    public static final Comparator<? super ProfileDescription> COMPARE_ON_NAME = new Comparator<ProfileDescription>() {
        public int compare(ProfileDescription o1, ProfileDescription o2) {
            int result = 0;
            if (o1.getName() != null && o2.getName() != null) {
                result = o1.getName().compareTo(o2.getName());
            } else {
                result = o1.getId().compareTo(o2.getId());
            }
            return result;
        }
    };

    public static ProfileDescription createNewDescription() {
        String id = ComponentRegistry.REGISTRY_ID + "p_" + IdSequence.get();
        ProfileDescription desc = new ProfileDescription();
        desc.setId(id);
        desc.setRegistrationDate(createNewDate());
        return desc;
    }

}
