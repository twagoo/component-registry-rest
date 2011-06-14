package clarin.cmdi.componentregistry.rest;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;


import clarin.cmdi.componentregistry.impl.database.UserDao;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.UserMapping.User;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.multipart.impl.FormDataMultiPartDispatchProvider;
import com.sun.jersey.spi.container.servlet.WebComponent;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

public abstract class ComponentRegistryRestServiceTestCase extends JerseyTest {
    //CommandLine test e.g.:  curl -i -H "Accept:application/json" -X GET  http://localhost:8080/ComponentRegistry/rest/registry/profiles

    protected final static GenericType<List<ProfileDescription>> PROFILE_LIST_GENERICTYPE = new GenericType<List<ProfileDescription>>() {
    };
    protected final static GenericType<List<ComponentDescription>> COMPONENT_LIST_GENERICTYPE = new GenericType<List<ComponentDescription>>() {
    };

    @Autowired
    private UserDao userDao;

    
    protected abstract String getApplicationContextFile();

    @Override
    protected AppDescriptor configure() {
        WebAppDescriptor.Builder builder = new WebAppDescriptor.Builder()
                .contextParam("contextConfigLocation", getApplicationContextFile())
                .servletClass(SpringServlet.class)
                .initParam(WebComponent.RESOURCE_CONFIG_CLASS,ClassNamesResourceConfig.class.getName())
                .initParam(ClassNamesResourceConfig.PROPERTY_CLASSNAMES,FormDataMultiPartDispatchProvider.class.getName() + ";" + ComponentRegistryRestService.class.getName())
                .addFilter(DummySecurityFilter.class, "DummySecurityFilter")
                .requestListenerClass(RequestContextListener.class)
                .contextListenerClass(ContextLoaderListener.class) 
                ;
        return builder.build();
    }

    protected WebResource getResource() {
        return resource();
    }

    protected Builder getAuthenticatedResource(String path) {
        return getAuthenticatedResource(getResource().path(path));
    }

    protected Builder getAuthenticatedResource(WebResource resource) {
        return resource.header(HttpHeaders.AUTHORIZATION, "Basic "
                + new String(Base64.encode(DummyPrincipal.DUMMY_PRINCIPAL.getName() + ":dummy")));
    }
    
    protected void createUserRecord() {
	User user = new User();
	user.setName("Database test user");
	user.setPrincipalName(DummyPrincipal.DUMMY_PRINCIPAL.getName());
	userDao.insertUser(user);
    }
    
    protected UserDao getUserDao() {
	return userDao;
    }

}
