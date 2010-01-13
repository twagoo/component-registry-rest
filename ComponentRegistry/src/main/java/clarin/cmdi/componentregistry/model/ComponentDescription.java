package clarin.cmdi.componentregistry.model;

import javax.xml.bind.annotation.XmlRootElement;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.IdSequence;

@XmlRootElement(name = "componentDescription")
public class ComponentDescription extends AbstractDescription {

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
        return desc;
    }

}
