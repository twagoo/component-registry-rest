package clarin.cmdi.componentregistry.model;

import java.util.Comparator;

import javax.xml.bind.annotation.XmlRootElement;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.IdSequence;

@XmlRootElement(name = "componentDescription")
public class ComponentDescription extends AbstractDescription {

    public static final Comparator<? super ComponentDescription> COMPARE_ON_GROUP_AND_NAME = new Comparator<ComponentDescription>() {
        public int compare(ComponentDescription o1, ComponentDescription o2) {
            int result=0;
            if (o1.getGroupName() != null && o2.getGroupName() != null)
                result = o1.getGroupName().compareTo(o2.getGroupName());
            if (result == 0) { 
                if (o1.getName() != null && o2.getName() != null) {
                    result = o1.getName().compareTo(o2.getName());
                } else {
                    result = o1.getId().compareTo(o2.getId());
                }
            }
            return result;
        }
    };
    
    private String groupName;

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public static ComponentDescription createNewDescription() {
        String id = ComponentRegistry.REGISTRY_ID+"c_" + IdSequence.get();
        ComponentDescription desc = new ComponentDescription();
        desc.setId(id);
        desc.setRegistrationDate(createNewDate());
        return desc;
    }

}
