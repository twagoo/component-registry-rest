package clarin.cmdi.componentregistry.persistence.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.stereotype.Repository;

import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.model.Component;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.JpaComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;

import javax.sql.DataSource;

/**
 * Base DAO which can be extended to serve {@link ComponentDescription}s and
 * {@link ProfileDescription}s
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */

@Repository
public class ComponentDaoImpl extends NamedParameterJdbcDaoSupport implements
	ComponentDao{

    @Autowired
    private JpaComponentDao jpaComponentDao;
    
    @Autowired
    private UserDao userDao;

    private final static Logger LOG = LoggerFactory
	    .getLogger(ComponentDaoImpl.class);

    /**
     * Copy a set of properties which are allowed to change from "from" to "to" thus making sure that only those
     * properties are updated and not other ones.
     * @param from
     * @param to
     */
    protected void copyPersistentProperties(Component from, Component to) {
	to.setName(from.getName());
	to.setDescription(from.getDescription());
	to.setRegistrationDate(from.getRegistrationDate());
	to.setCreatorName(from.getCreatorName());
	to.setDomainName(from.getDomainName());
	to.setGroupName(from.getGroupName());
	to.setHref(from.getHref());
    }

    protected String getTableName() {
	return "persistentcomponents";
    };

    private boolean compare(String s, Number n) {
	return ("" + s).equals("" + n);
    }

    private String toString(Number n) {
	if (n == null)
	    return null;
	return n.toString();
    }

    private Component castDown(Component c) {
	// JPA will save only BaseComponent instances, not derived classes
	if (c instanceof ComponentDescription
		|| c instanceof ProfileDescription) {
	    Component copy = new Component();
	    ComponentUtils.copyPropertiesFrom(c, copy);
	    return copy;
	}
	return c;
    }

    /**
     * Update {@link Component#getCommentsCount()} (which is a transient property) with the number of comments for that component 
     * @param component
     */
    private void updateCommentsCount(Component component) {
	if (component == null)
	    return;
	int count = getJdbcTemplate().queryForInt(
		"select count(*) from comments where component_id = ?",
		new Object[] { component.getId() });
	component.setCommentsCount(count);
    }

    @Autowired
    public void setNonFinalJdbcTemplate(JdbcTemplate jdbcTemplate) {
	super.setJdbcTemplate(jdbcTemplate);
    }

    /**
     * 
     * @param cmdId
     *            CMD id
     * @return Whether the specified item is in the public space
     */
    @Override
    public boolean isPublic(String cmdId) {
	Component component = jpaComponentDao.findByComponentId(cmdId);
	return component != null && component.isPublic();
    }

    /**
     * 
     * @param cmdId
     *            CMD id
     * @param userId
     *            User db id of workspace owner
     * @return Whether the specified item is in the specified user's workspace
     */
    @Override
    public boolean isInUserSpace(String cmdId, Number userId) {
	Component component = jpaComponentDao.findByComponentId(cmdId);
	boolean b = component != null && !component.isPublic()
		&& compare(component.getUserId(), userId);
	return b;
    }

    /**
     * 
     * @param cmdId
     *            CMD id
     * @param userId
     *            User db id of workspace owner, null for public registry
     * @return Whether the specified item is in the specified workspace (user or
     *         public)
     */
    @Override
    public boolean isInRegistry(String cmdId, Number userId) {
	if (userId == null) {
	    return isPublic(cmdId);
	} else {
	    return isInUserSpace(cmdId, userId);
	}
    }

    /**
     * 
     * @param cmdId
     *            Profile or component Id (not primary key)
     * @return String value of XML content for profile or component
     */
    @Override
    public String getContent(boolean isDeleted, String cmdId)
	    throws DataAccessException {
	Component component = jpaComponentDao.findByComponentId(cmdId);
	if (component == null || component.isDeleted() != isDeleted)
	    return null;
	return component.getContent();
    }

    /**
     * @param description
     *            Description to insert
     * @param content
     *            Content to insert and refer to from description
     * @return Id of newly inserted description
     */
    @Override
    public Number insertDescription(Component description, String content,
	    boolean isPublic, Number userId) throws DataAccessException {

	if (description.getId() != null
		&& jpaComponentDao.findByComponentId(description.getId()) != null) {
	    throw new IllegalArgumentException("Component "
		    + description.getId() + " already in DB");
	}
	Component copy = castDown(description);
	copy.setContent(content);
	copy.setPublic(isPublic);
	copy.setUserId(toString(userId));
	copy = jpaComponentDao.saveAndFlush(copy);
	return copy.getDbId();
    }

    /**
     * Updates a description by database id
     * 
     * @param id
     *            Id (key) of description record
     * @param description
     *            New values for description (leave null to not change)
     * @param content
     *            New content for description (leave null to not change)
     */
    @Override
    public void updateDescription(Number id, Component description,
	    String content) {
	if (description != null) {
	    // Update description
	    Component copy = jpaComponentDao.findByComponentId(description
		    .getId());
	    copyPersistentProperties(description, copy);
	    jpaComponentDao.saveAndFlush(copy);
	}

	if (content != null) {
	    Component copy = jpaComponentDao.findOne(id.longValue());
	    copy.setContent(content);
	    jpaComponentDao.saveAndFlush(copy);
	}
    }

    /**
     * Retrieves description by it's primary key Id
     * 
     * @param id
     *            Description key
     * @return The description, if it exists; null otherwise
     */
    @Override
    public Component getById(Number id) throws DataAccessException {
	Component component = jpaComponentDao.findOne(id.longValue());
	if (component != null && !component.isDeleted()) {
	    updateCommentsCount(component);
	    return component;
	}
	return null;
    }

    /**
     * Get by ComponentId / ProfileId, whether in userspace or public
     * 
     * @param id
     *            Full component id
     * @return The description, if it exists; null otherwise
     */
    @Override
    public Component getByCmdId(String id) throws DataAccessException {
	Component component = jpaComponentDao.findByComponentId(id);
	if (component != null && !component.isDeleted()) {
	    updateCommentsCount(component);
	    return component;
	}
	return null;
    }

    /**
     * Get by ComponentId / ProfileId
     * 
     * @param id
     *            Full component id
     * @param userId
     *            Db id of user for workspace; null for public space
     * @return The description, if it exists; null otherwise
     */
    @Override
    public Component getByCmdId(String id, Number userId)
	    throws DataAccessException {
	Component component = getByCmdId(id);
	if (component != null) {
	    updateCommentsCount(component);
	    if (userId == null) {
		if (!component.isPublic())
		    component = null;
	    } else {
		if (component.isPublic()
			|| !compare(component.getUserId(), userId))
		    component = null;
	    }
	}
	return component;
    }

    /**
     * 
     * @param cmdId
     *            CMD Id of description
     * @return Database id for description record
     */
    @Override
    public Number getDbId(String cmdId) {
	Number id = null;
	Component c = jpaComponentDao.findByComponentId(cmdId);
	if (c!=null)
	    id = c.getDbId();
	return id;
    }

    /**
     * 
     * @return All descriptions in the public space
     */
    @Override
    public List<Component> getPublicDescriptions() throws DataAccessException {
	return jpaComponentDao.findPublicItems();
    }

    /**
     * @return List of deleted descriptions in user space or in public when
     *         userId=null
     * @param userId
     */
    @Override
    public List<Component> getDeletedDescriptions(Number userId) {
	if (userId != null) {
	    return jpaComponentDao.findDeletedItemsForUser(userId.longValue());
	} else {
	    return jpaComponentDao.findPublicDeletedItems();
	}
    }

    /**
     * 
     * @return All the user's descriptions not in the public space
     */
    @Override
    public List<Component> getUserspaceComponents(Number userId)
	    throws DataAccessException {
	return jpaComponentDao.findItemsForUserThatAreNotInGroups(userId.longValue(), ComponentDescription.COMPONENT_PREFIX+"%");
    }

    @Override
    public List<Component> getUserspaceProfiles(Number userId)
	    throws DataAccessException {
	return jpaComponentDao.findItemsForUserThatAreNotInGroups(userId.longValue(), ProfileDescription.PROFILE_PREFIX+"%");
    }

    @Override
    public void setDeleted(Component desc, boolean isDeleted)
	    throws DataAccessException {
	Component copy = jpaComponentDao.findByComponentId(desc.getId());
	copy.setDeleted(isDeleted);
	jpaComponentDao.saveAndFlush(copy);
    }

    @Override
    public void setPublished(Number id, boolean published) {
	Component copy = jpaComponentDao.findOne(id.longValue());
	copy.setPublic(published);
	jpaComponentDao.saveAndFlush(copy);
    }

    /**
     * 
     * @param id
     *            Id of description record
     * @return Principal name of description's owner, if any. Otherwise, null.
     */
    @Override
    public String getOwnerPrincipalName(Number id) {
	Component component = getById(id);
	if (component == null)
	    return null;
	
	long userId = component.getDbUserId();
	RegistryUser user = userDao.findOne(userId);
	if (user == null)
	    return null;

	return user.getPrincipalName();
    }

    @Override
    public List<Component> getPublicComponentDescriptions() {
	return jpaComponentDao.findPublicItems(ComponentDescription.COMPONENT_PREFIX+"%");
    }

    @Override
    public List<Component> getPublicProfileDescriptions() {
	return jpaComponentDao.findPublicItems(ProfileDescription.PROFILE_PREFIX+"%");
    }

}
