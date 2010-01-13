package clarin.cmdi.componentregistry.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registerResponse")
public class RegisterResponse {

    @XmlAttribute(required = true)
    private Boolean isProfile;

    @XmlAttribute(required = true)
    private Boolean registered;

    @XmlElementWrapper(name = "errors", required = false)
    @XmlElements(@XmlElement(name = "error", type = String.class))
    private List<String> errors = new ArrayList<String>();

    @XmlElement
    private AbstractDescription description;


    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        getErrors().add(error);
    }

    public void setDescription(AbstractDescription description) {
        this.description = description;
    }

    public AbstractDescription getDescription() {
        return description;
    }

    public boolean isProfile() {
        return isProfile;
    }

    public void setIsProfile(boolean isProfile) {
        this.isProfile = isProfile;
    }
    
}
