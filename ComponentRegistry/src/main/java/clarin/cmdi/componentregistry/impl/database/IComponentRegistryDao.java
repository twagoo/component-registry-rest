package clarin.cmdi.componentregistry.impl.database;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 *
 * @param <T>
 */
public interface IComponentRegistryDao<T> {

	public final static String TABLE_COMMENTS = "comments";
	public final static String TABLE_COMPONENT_DESCRIPTION = "component_description";
	public final static String TABLE_PROFILE_DESCRIPTION = "profile_description";
	public final static String TABLE_XML_CONTENT = "xml_content";
	public final static String TABLE_REGISTRY_USER = "registry_user";
	public final static String COLUMN_ID = "id";

	void setDatasourceProperty(DataSource ds);

}