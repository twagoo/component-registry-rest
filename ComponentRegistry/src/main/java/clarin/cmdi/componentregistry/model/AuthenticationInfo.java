package clarin.cmdi.componentregistry.model;

import clarin.cmdi.componentregistry.UserCredentials;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@XmlRootElement
public class AuthenticationInfo {

    @XmlElement
    private boolean authenticated;
    @XmlElement
    private String username;
    @XmlElement
    private String displayName;

    public AuthenticationInfo() {
    }

    public AuthenticationInfo(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public AuthenticationInfo(UserCredentials userInfo) {
        this.authenticated = (userInfo != null);
        if (userInfo != null) {
            this.username = userInfo.getPrincipalName();
            this.displayName = userInfo.getDisplayName();
        }
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

}
