package clarin.cmdi.componentregistry.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.codec.digest.DigestUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "users")
public class UserMapping {

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class User {

        private String name;
        private String userDir;
        private String principalName;
	private Number id;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setUserDir(String userDir) {
            this.userDir = userDir;
        }

        public String getUserDir() {
            return userDir;
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

    @XmlElement(name = "user", required = true)
    private List<UserMapping.User> users = new ArrayList<UserMapping.User>();

    public void setUsers(List<UserMapping.User> users) {
        this.users = users;
    }

    public void addUsers(UserMapping.User user) {
        users.add(user);

    }

    public List<UserMapping.User> getUsers() {
        return users;
    }

    public User findUser(String principalNameMD5) {
        for (User user : users) {
            if (DigestUtils.md5Hex(user.getPrincipalName()).equals(principalNameMD5)) {
                return user;
            }
        }
        return null;
    }

}
