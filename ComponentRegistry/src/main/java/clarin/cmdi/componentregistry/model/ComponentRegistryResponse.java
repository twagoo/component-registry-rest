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
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author jeafer
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({RegisterResponse.class, CommentResponse.class})
public class ComponentRegistryResponse {

    @XmlAttribute(required = true)
    private Boolean registered;
    
    @XmlAttribute(required = true)
    private Boolean isInUserSpace;
    
    @XmlElementWrapper(name = "errors", required = false)
    @XmlElements(
    @XmlElement(name = "error", type = String.class))
    private List<String> errors = new ArrayList<String>();

    public ComponentRegistryResponse() {
    }

    public void addError(String error) {
        getErrors().add(error);
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public void setIsInUserSpace(Boolean isInUserSpace) {
        this.isInUserSpace = isInUserSpace;
    }

    public Boolean isInUserSpace() {
        return isInUserSpace;
    }
}