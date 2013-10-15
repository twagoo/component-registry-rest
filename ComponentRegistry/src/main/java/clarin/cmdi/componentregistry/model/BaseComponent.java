package clarin.cmdi.componentregistry.model;

import clarin.cmdi.componentregistry.DatesHelper;
import clarin.cmdi.componentregistry.impl.ComponentUtils;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 * The BaseComponent (formally AbstractDescription) models profiles and components alike by containing <strong>all</strong> their persistent attributes.
 * It is meant to serve as a base for XML generation and JPA persistence. Extending classes are not allowed to model any persistent attributes.
 * @author george.georgovassilis@mpi.nl
 *
 */
@XmlRootElement(name = "description")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ComponentDescription.class, ProfileDescription.class})
public class BaseComponent {

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
    private int commentsCount;
    @XmlTransient
    private boolean shownInEditor = true;

    /**
     * Whether this profile should be shown in metadata editor (e.g. Arbil)
     *
     * @return the value of showInEditor
     */
    public boolean isShowInEditor() {
	return shownInEditor;
    }

    /**
     * Gets whether this profile should be shown in metadata editor (e.g. Arbil)
     *
     * @param showInEditor new value of showInEditor
     */
    public void setShowInEditor(boolean showInEditor) {
	this.shownInEditor = showInEditor;
    }

    public void setId(String id) {
	if (id!=null && !ComponentUtils.isComponentId(id) && !ComponentUtils.isProfileId(id))
	    throw new IllegalArgumentException("ID doesn't follow the naming schema for components or profiles "+id);
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
     * MD5 string representation of the user id. Storing the hash because the
     * userId can be the email address which we don't want to make public.
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

    /**
     * @return the number of comments posted on this component
     */
    public int getCommentsCount() {
        return commentsCount;
    }

    /**
     * @param commentsCount the number of comments posted on this component
     */
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
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
     *
     * @param userId normal string which will be checked to see if it matches
     * the md5 hash of the stored userId
     */
    public boolean isThisTheOwner(String userId) {
        String userHash = DigestUtils.md5Hex(userId);
        return userHash.equals(getUserId());
    }

}
