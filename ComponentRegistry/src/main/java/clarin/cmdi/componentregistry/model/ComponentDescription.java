package clarin.cmdi.componentregistry.model;

import java.io.Serializable;
import java.util.Comparator;

import javax.xml.bind.annotation.XmlRootElement;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.IdSequence;

@XmlRootElement(name = "componentDescription")
public class ComponentDescription extends AbstractDescription implements Serializable{

    private static final long serialVersionUID = 1L;
    
    public static final Comparator<? super ComponentDescription> COMPARE_ON_GROUP_AND_NAME = new Comparator<ComponentDescription>() {
        public int compare(ComponentDescription o1, ComponentDescription o2) {
            int result = 0;
            if (o1.getGroupName() != null && o2.getGroupName() != null)
                result = o1.getGroupName().compareToIgnoreCase(o2.getGroupName());
            if (result == 0) {
                if (o1.getName() != null && o2.getName() != null) {
                    result = o1.getName().compareToIgnoreCase(o2.getName());
                } else {
                    result = o1.getId().compareTo(o2.getId());
                }
            }
            return result;
        }
    };

    public static ComponentDescription createNewDescription() {
        String id = ComponentRegistry.REGISTRY_ID + "c_" + IdSequence.get();
        ComponentDescription desc = new ComponentDescription();
        desc.setId(id);
        desc.setRegistrationDate(createNewDate());
        return desc;
    }

}
