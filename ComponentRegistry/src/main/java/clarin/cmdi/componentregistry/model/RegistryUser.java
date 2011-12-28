package clarin.cmdi.componentregistry.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
public class RegistryUser {

    private String name;
    private String principalName;
    private Number id;

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setPrincipalName(String principalName) {
	this.principalName = principalName;
    }

    public String getPrincipalName() {
	return principalName;
    }

    public Number getId() {
	return id;
    }

    public void setId(Number id) {
	this.id = id;
    }
}
