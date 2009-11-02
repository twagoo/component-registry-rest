package clarin.cmdi.componentregistry.model;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public abstract class AbstractDescription {

    @XmlAttribute
    private String id;
    @XmlAttribute
    private String description;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String registrationDate;
    @XmlAttribute
    private String creatorName;
    @XmlAttribute
    private String xlink;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setXlink(String xlink) {
        this.xlink = xlink;
    }

    public String getXlink() {
        return xlink;
    }

    @Override
    public String toString() {
        return "Name=" + getName() + ", id=" + getId();
    }

}
