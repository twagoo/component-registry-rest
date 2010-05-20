package clarin.cmdi.componentregistry.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "users")
public class UserMapping {

    public static class User {

        private String name;
        private String userDir;

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

    public User findUser(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

}
