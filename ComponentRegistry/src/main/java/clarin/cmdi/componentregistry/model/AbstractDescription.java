package clarin.cmdi.componentregistry.model;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

@XmlRootElement(name = "description")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso( { ComponentDescription.class, ProfileDescription.class })
public abstract class AbstractDescription {

    private String id;
    private String description;
    private String name;
    private String registrationDate;
    private String creatorName;
    private String userId;
    private String domainName;
    @XmlElement(namespace = "http://www.w3.org/1999/xlink")
    private String href;
    private String groupName;

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

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * MD5 string representation of the user id. Storing the hash because the userId can be the email address which we don't want to make
     * public.
     */
    public String getUserId() {
        return userId;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return "Name=" + getName() + ", id=" + getId() + ", creatorName=" + getCreatorName() + ", userId=" + getUserId();
    }

    public boolean isProfile() {
        return this instanceof ProfileDescription;
    }

    public String getType() {
        return isProfile() ? "profile" : "component";
    }

    public static String createNewDate() {
        return createNewDate(new Date().getTime());
    }
    
    public static String createNewDate(long time) {
        return DateFormatUtils.formatUTC(time, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
    }

    /**
     * Helper method.
     * @param userId normal string which will be checked to see if it matches the md5 hash of the stored userId
     */
    public boolean isThisTheOwner(String userId) {
        String userHash = DigestUtils.md5Hex(userId);
        return userHash.equals(getUserId());
    }

    public static Date getDate(String registrationDate) throws ParseException {
        return DateUtils.parseDate(registrationDate, new String[] { DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern() });
    }

    public static String createPublicHref(String href) {
        String result = href;
        if (href != null) {
            int index = href.indexOf("?");
            if (index != -1) { //strip off query params the rest should be the public href.
                result = href.substring(0, index);
            }
        }
        return result;
    }

    public static final Comparator<? super AbstractDescription> COMPARE_ON_NAME = new Comparator<AbstractDescription>() {
        public int compare(AbstractDescription o1, AbstractDescription o2) {
            int result = 0;
            if (o1.getName() != null && o2.getName() != null) {
                result = o1.getName().compareToIgnoreCase(o2.getName());
            }
            if (o1.getId() != null && result == 0) {
                result = o1.getId().compareTo(o2.getId());
            }
            return result;
        }
    };
}
