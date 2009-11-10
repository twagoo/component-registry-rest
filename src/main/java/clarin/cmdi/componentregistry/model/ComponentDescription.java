package clarin.cmdi.componentregistry.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "componentDescription")
public class ComponentDescription extends AbstractDescription {

    private String groupName;

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

}
