package clarin.cmdi.componentregistry.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanUtils;

import clarin.cmdi.componentregistry.DatesHelper;
import clarin.cmdi.componentregistry.model.Component;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 * Utilities for working with {@link Component}s
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
public class ComponentUtils {

    public static boolean isProfileId(String componentId) {
	return ("" + componentId).startsWith(ProfileDescription.PROFILE_PREFIX);
    }

    public static boolean isComponentId(String componentId) {
	return ("" + componentId)
		.startsWith(ComponentDescription.COMPONENT_PREFIX);
    }

    public static void copyPropertiesFrom(Component from, Component to) {
	BeanUtils.copyProperties(from, to);
    }

    public static Date getDate(String registrationDate) throws ParseException {
	return DateUtils.parseDate(registrationDate,
		new String[] { DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT
			.getPattern() });
    }

    public static String createPublicHref(String href) {
	String result = href;
	if (href != null) {
	    int index = href.indexOf("?");
	    if (index != -1) { // strip off query params the rest should be the
			       // public href.
		result = href.substring(0, index);
	    }
	}
	return result;
    }

    /**
     * Compares two descriptions by the their value as returned by
     * {@link Component#getName()
     * }
     */
    public static final Comparator<? super Component> COMPARE_ON_NAME = new Comparator<Component>() {
	@Override
	public int compare(Component o1, Component o2) {
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
    /**
     * Compares two descriptions by the their value as returned by
     * {@link Component#getRegistrationDate() () * }
     */
    public static final Comparator<? super Component> COMPARE_ON_DATE = new Comparator<Component>() {
	/**
	 * @returns 1 if o11 is older than o2, returns -1 if o1 is younger than
	 *          o2
	 */
	@Override
	public int compare(Component o1, Component o2) {
	    return o1.getRegistrationDate().compareTo(o2.getRegistrationDate());
	}
    };

    public static ProfileDescription toProfile(Component component) {
	if (component == null)
	    return null;
	ProfileDescription copy = new ProfileDescription();
	BeanUtils.copyProperties(component, copy);
	return copy;
    }

    public static ComponentDescription toComponent(Component component) {
	if (component == null)
	    return null;
	ComponentDescription copy = new ComponentDescription();
	BeanUtils.copyProperties(component, copy);
	return copy;
    }

    public static List<ProfileDescription> toProfiles(
	    List<Component> components) {
	if (components == null)
	    return null;
	List<ProfileDescription> list = new ArrayList<ProfileDescription>();
	for (Component c : components)
	    list.add(toProfile(c));
	return list;
    }

    public static List<ComponentDescription> toComponents(
	    List<Component> components) {
	if (components == null)
	    return null;
	List<ComponentDescription> list = new ArrayList<ComponentDescription>();
	for (Component c : components)
	    list.add(toComponent(c));
	return list;
    }

}
