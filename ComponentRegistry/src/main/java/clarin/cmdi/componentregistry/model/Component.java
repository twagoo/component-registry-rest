package clarin.cmdi.componentregistry.model;

import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.util.RFCDateAdapter;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * The BaseComponent (formally AbstractDescription) models profiles and
 * components alike by containing <strong>all</strong> their persistent
 * attributes. It is meant to serve as a base for XML generation and JPA
 * persistence. Extending classes are not allowed to model any persistent
 * attributes.
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
@XmlRootElement(name = "description")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ ComponentDescription.class, ProfileDescription.class })
@Entity
@Table(name = "persistentcomponents")
public class Component implements Serializable {

    
    @Id
    @SequenceGenerator( name = "profile_description_id_seq", sequenceName = "profile_description_id_seq", allocationSize = 1, initialValue = 1 )
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "profile_description_id_seq" )
    @Column(name = "id")
    @XmlTransient
    private Long dbId;

    @XmlElement(name = "id")
    @Column(name = "component_id", nullable = false, unique = true)
    private String componentId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "registration_date")
    @XmlJavaTypeAdapter(RFCDateAdapter.class)
    private Date registrationDate;

    @Column(name = "creator_name")
    private String creatorName;

    @XmlTransient
    @Column(name = "user_id", columnDefinition = "integer")
    private long dbUserId;

    @Transient
    private String userId;

    @Column(name = "domain_name")
    private String domainName;

    @XmlElement(namespace = "http://www.w3.org/1999/xlink")
    private String href;

    @Column(name = "group_name")
    private String groupName;

    @Transient
    private int commentsCount;

    @Column(name = "show_in_editor", nullable = false)
    @XmlTransient
    private boolean shownInEditor = true;

    @XmlTransient
    @Column(name = "content", nullable = false)
    private String content = "";

    @XmlTransient
    @Column(name = "is_public", nullable = false)
    private boolean ispublic;

    @XmlTransient
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    public long getDbUserId() {
        return dbUserId;
    }

    public void setDbUserId(long dbUserId) {
        this.dbUserId = dbUserId;
        setUserId(""+dbUserId);
    }

    public String getContent() {
	return content;
    }

    public void setContent(String content) {
	this.content = content;
    }

    public boolean isDeleted() {
	return deleted;
    }

    public void setDeleted(boolean deleted) {
	this.deleted = deleted;
    }

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
     * @param showInEditor
     *            new value of showInEditor
     */
    public void setShowInEditor(boolean showInEditor) {
	this.shownInEditor = showInEditor;
    }

    public void setId(String id) {
	if (id != null && !ComponentUtils.isComponentId(id)
		&& !ComponentUtils.isProfileId(id))
	    throw new IllegalArgumentException(
		    "ID doesn't follow the naming schema for components or profiles "
			    + id);
	this.componentId = id;
    }

    public String getId() {
	return componentId;
    }

    public Long getDbId() {
	return dbId;
    }

    public void setDbId(Long dbId) {
	this.dbId = dbId;
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

    public void setRegistrationDate(Date registrationDate) {
	this.registrationDate = registrationDate;
    }

    public Date getRegistrationDate() {
	return registrationDate;
    }

    public void setCreatorName(String creatorName) {
	this.creatorName = creatorName;
    }

    public String getCreatorName() {
	return creatorName;
    }

    public void setUserId(String userId) {
	try {
	    this.dbUserId = Long.parseLong(userId);
	} catch (Exception e) {
	    this.dbUserId = 0;
	}
	this.userId = userId;
    }

    /**
     * MD5 string representation of the user id. Storing the hash because the
     * userId can be the email address which we don't want to make public.
     */
    public String getUserId() {
	if (userId == null)
	    return ""+dbUserId;
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
     * @param commentsCount
     *            the number of comments posted on this component
     */
    public void setCommentsCount(int commentsCount) {
	this.commentsCount = commentsCount;
    }

    @Override
    public String toString() {
	return "Name=" + getName() + ", id=" + getId() + ", creatorName="
		+ getCreatorName() + ", userId=" + getUserId();
    }

    public boolean isProfile() {
	return this instanceof ProfileDescription;
    }

    public String getType() {
	return isProfile() ? "profile" : "component";
    }

    /**
     * Helper method.
     * 
     * @param userId
     *            normal string which will be checked to see if it matches the
     *            md5 hash of the stored userId
     */
    public boolean isThisTheOwner(String userId) {
	String userHash = DigestUtils.md5Hex(userId);
	return userHash.equals(getUserId());
    }

    public boolean isPublic() {
	return ispublic;
    }

    public void setPublic(boolean ispublic) {
	this.ispublic = ispublic;
    }

}
