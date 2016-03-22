package clarin.cmdi.componentregistry.rest;


import javax.ws.rs.Path;

import org.springframework.transaction.annotation.Transactional;


import com.sun.jersey.spi.resource.Singleton;

/**
 *
 * @author twago@mpi.nl
 * @author olsha@mpi.nl
 * @author george.georgovassilis@mpi.nl
 *
 */
@Path("/registry")
@Singleton
@Transactional(rollbackFor = Exception.class)
public interface IComponentRegistryRestService {

    public static final String APPLICATION_URL_BASE_PARAM = "eu.clarin.cmdi.componentregistry.serviceUrlBase";
    public static final String APPLICATION_URL_PATH_PARAM = "eu.clarin.cmdi.componentregistry.serviceUrlPath";
    public static final String APPLICATION_URL_PROTOCOL_HEADER_PARAM = "eu.clarin.cmdi.componentregistry.serviceUrlProtocolHeader";
    public static final String APPLICATION_URL_HOST_HEADER_PARAM = "eu.clarin.cmdi.componentregistry.serviceUrlHostHeader";
    public static final String DATA_FORM_FIELD = "data";
    public static final String NAME_FORM_FIELD = "name";
    public static final String DESCRIPTION_FORM_FIELD = "description";
    public static final String GROUP_FORM_FIELD = "group";
    public static final String DOMAIN_FORM_FIELD = "domainName";
    public static final String REGISTRY_SPACE_PARAM = "registrySpace";
    public static final String USER_SPACE_PARAM = "userspace";
    public static final String GROUPID_PARAM = "groupId";
    public static final String METADATA_EDITOR_PARAM = "mdEditor";
    public static final String NUMBER_OF_RSSITEMS = "limit";

    public static final String REGISTRY_SPACE_PUBLISHED = "published";
    public static final String REGISTRY_SPACE_PRIVATE = "private";
    public static final String REGISTRY_SPACE_GROUP = "group";

}
