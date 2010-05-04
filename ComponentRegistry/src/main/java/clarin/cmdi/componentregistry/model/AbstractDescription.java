package clarin.cmdi.componentregistry.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso( { ComponentDescription.class, ProfileDescription.class })
public abstract class AbstractDescription {

    private String id;
    private String description;
    private String name;
    private String registrationDate;
    private String creatorName;
    @XmlElement(namespace = "http://www.w3.org/1999/xlink")
    private String href;

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

    public void setHref(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    @Override
    public String toString() {
        return "Name=" + getName() + ", id=" + getId() + ", creatorName=" + getCreatorName();
    }

    public boolean isProfile() {
        return this instanceof ProfileDescription;
    }

}
